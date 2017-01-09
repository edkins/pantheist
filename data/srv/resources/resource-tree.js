'use strict';

var resourceTree = {};
resourceTree._info = {};

/*

Structure:

  <li class="tree-node">
  	<div class="tree-item" data-url="...">
  		<span class="tree-item-icon" style="background-image:url(...)"></span>
  		Name of the thing
  	</div>
  	
  	<!-- only if expanded -->
    <ul>
    	<li ... />
    	<li ... />
    	<li ... />
    </ul>
  </li>


The exception is the root node, which instead of being contained in <li/> is contained
in the <div class="panel-right-div" id="resource-list" /> that you see in the html.
*/

// Recursively delete all the dom elements and associated info
// Note this means when you collapse a node it will forget any nested
// expansion info.
// This is arguably useful as it lets you tidy up your view of
// the tree easily.
resourceTree._unexpand = function(parent)
{
	if (parent == undefined || !parent.expanded || parent.ulElement == undefined)
	{
		throw new Error('_unexpand: parent not in expanded state');
	}
	
	for (var childResource of parent.data.childResources)
	{
		var child = resourceTree._info[childResource.url];
		if (child.expanded)
		{
			resourceTree._unexpand(child);
		}
		
		// Don't call parent.ulElement.removeChild(child)
		// instead we'll remove the entire ul
		
		delete resourceTree._info[childResource.url];
	}
	
	parent.liElement.removeChild(parent.ulElement);
	parent.ulElement = undefined;
	parent.expanded = false;
};

resourceTree._expand = function(parent)
{
	if (parent == undefined)
	{
		throw new Error('_expand: url is not known');
	}
	if (parent.data.childResources == undefined)
	{
		throw new Error('_expand: leaf node can\'t be expanded');
	}

	if (parent.expanded)
	{
		throw new Error('_expand: already expanded');
	}

	if (parent.ulElement != undefined)
	{
		throw new Error('_expand: not expecting to have ulElement for unexpanded node');
	}
		
	parent.ulElement = document.createElement('ul');
	parent.liElement.append(parent.ulElement);
	parent.expanded = true;

	for (var resource of parent.data.childResources)
	{
		var child = resourceTree._info[resource.url];
		if (child === undefined)
		{
			var name = decodeURIComponent(uri.lastSegment(resource.url));
			var li = resourceTree._createTreeLi(resource.kindUrl, false, resource.url, name);
			child = {
				liElement: li,
				ulElement: undefined,
				expanded: false,
				kindUrl: resource.kindUrl
			};
			
			resourceTree._info[resource.url] = child;
		}
		
		parent.ulElement.append(child.liElement);
	}
};

resourceTree._createTreeItem = function(kindUrl,expanded,url,name)
{
	var item = document.createElement('div');
	var icon = document.createElement('span');
	
	var iconUrl = ui.getKindUrlIcon(kindUrl,expanded);
	
	icon.style['background-image'] = "url('" + iconUrl + "')";
	icon.classList.add('tree-item-icon');
	
	item.append(icon);
	
	item.classList.add('tree-item');
	item.append(name);
	item.dataset.url = url;
	item.onclick = resourceTree._onclickTreeItem;

	return item;
};

resourceTree._createTreeLi = function(kindUrl,expanded,url,name)
{
	var li = document.createElement('li');
	
	var item = resourceTree._createTreeItem(kindUrl,expanded,url,name);
	li.append(item);

	li.classList.add('tree-node');
	
	return li;
};

resourceTree.initialize = function()
{
	var rootElement = document.getElementById('resource-list');
	ui.removeChildren(rootElement);
	resourceTree._info = {};
	
	var rootUrl = http.home;
	var rootKind = http.home + '/entity/kind/pantheist-root';
	
	rootElement.append(resourceTree._createTreeItem(rootKind, false, rootUrl, 'root'));
	
	var rootItem = {
		liElement: rootElement,
		ulElement: undefined,
		expanded: false,
		kindUrl: rootKind
	};
	
	resourceTree._info[rootUrl] = rootItem;
};

resourceTree._onclickTreeItem = function(event)
{
	var url = event.currentTarget.dataset.url;
	var elementToFlash = event.currentTarget;
	
	if (url == undefined)
	{
		console.error('No url defined for this element');
		ui.flashClass(elementToFlash, 'flash-client-error');
		return;
	}
	
	var item = resourceTree._info[url];
	
	if (item == undefined)
	{
		console.error('No information for url ' + url);
		ui.flashClass(elementToFlash, 'flash-client-error');
		return;
	}
	
	if (item.expanded && !event.ctrlKey)
	{
		resourceTree._unexpand(item);
		return;
	}
	
	if (ui.isKindListable(item.kindUrl))
	{
		http.getJson( url ).then (
			data => {
				item.data = data;
				if (event.ctrlKey)
				{
					ui.visitScratch(JSON.stringify(data, null, '    '));
				}
				else
				{
					resourceTree._expand(item);
				}
			},
			
			error => {
				console.error('Fetching info: ' + error);
				ui.flashClass(event.currentTarget, 'flash-server-error');
			}
		);
	}
	else
	{
		return ui.visit(url,
			item.kindUrl,
			elementToFlash,
			true);
	}
};
