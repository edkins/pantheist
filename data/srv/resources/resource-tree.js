'use strict';

var resourceTree = {};
resourceTree.expandedNodes = {};

resourceTree.invertExpansion = function(url)
{
	if (url in resourceTree.expandedNodes)
	{
		delete resourceTree.expandedNodes[url];
	}
	else
	{
		resourceTree.expandedNodes[url] = '';
	}
	resourceTree._listThings();
};

resourceTree._createTreeItem = function(kindUrl,expanded,url,name)
{
	var item = document.createElement('div');
	var icon = document.createElement('span');
	
	var iconUrl = ui.getKindIcon(kindUrl,expanded);
	
	icon.style['background-image'] = "url('" + iconUrl + "')";
	icon.classList.add('tree-item-icon');
	
	item.append(icon);
	
	item.classList.add('tree-item');
	item.append(name);
	item.dataset.url = url;
	item.onclick = resourceTree._onclickTreeItem;

	return item;
};

resourceTree._createUl = function(parentUrl)
{
	var ul = document.createElement('ul');
	return resourceTree._createListElements(parentUrl).then(
		elements => {
			for (var element of elements)
			{
				ul.append(element);
			}
			return ul;
		}
	);
};

resourceTree._createTreeNode = function(kindUrl,url)
{
	var name = decodeURIComponent(uri.lastSegment(url));
	
	var li = document.createElement('li');
	
	var expanded = (url in resourceTree.expandedNodes);
	
	var item = resourceTree._createTreeItem(kindUrl,expanded,url,name);
	li.append(item);

	li.classList.add('tree-node');
	if (expanded)
	{
		return resourceTree._createUl(url).then( cul => {
			li.append(cul);
			return li;
		} );
	}
	
	return Promise.resolve(li);
};

resourceTree._createListElements = function(parentUrl)
{
	return http.getJson(parentUrl).then(
		data => {
			if (data.childResources.length === 0)
			{
				var li = document.createElement('li');
				li.append('empty');
				li.classList.add('tree-node');
				li.classList.add('notice');
				return [li];
			}
		
			var elements = [];
			return resourceTree._processList(0, data.childResources, child =>
				{
					if (child.suggestHiding)
					{
						return resourceTree._createListElements(child.url).then(
							els => {
								for (var el of els)
								{
									elements.push(el);
								}
							}
						);
					}
					else
					{
						return resourceTree._createTreeNode(child.kindUrl, child.url).then(
							li => elements.push(li)
						);
					}
				}
			).then( x => elements );
		}
	).catch(
		error => {
			var li = document.createElement('li');
			li.append(''+error);
			li.classList.add('tree-node');
			li.classList.add('error');
			return [li];
		}
	);
};

resourceTree._listThings = function()
{
	var rootItem = resourceTree._createTreeItem(ui.rootKindUrl,true,http.home,'root'); 
	return resourceTree._createUl(http.home).then(
		ul => {
			var panel = document.getElementById('resource-list');
			ui.removeChildren(panel);
			panel.append(rootItem);
			panel.append(ul);
		}
	);
};

resourceTree._processList = function(i,array,fn)
{
	if (i >= array.length)
	{
		return Promise.resolve(undefined);
	}
	else
	{
		return Promise.resolve(fn(array[i])).then( x => resourceTree._processList(i+1,array,fn) );
	}
};

resourceTree.initialize = function()
{
	return resourceTree._listThings();
};

resourceTree._flashUrl = function(url,cssClass)
{
	var treeItems = document.getElementById('resource-list').getElementsByTagName('div');
	for (var i = 0; i < treeItems.length; i++)
	{
		if (treeItems.item(i).dataset.url === url)
		{
			ui.flashClass(treeItems.item(i), cssClass);
		}
	}
}

resourceTree._onclickTreeItem = function(event)
{
	var url = event.currentTarget.dataset.url;
	
	if (url == undefined)
	{
		console.error('No url defined for this element');
		ui.flashClass(event.currentTarget, 'flash-client-error');
		return;
	}
	
	if (event.ctrlKey)
	{
		ui.visitInfo(url);
	}
	else
	{
		//resourceTree._flashUrl(url,'flash-loading');
		ui.visit(url).then( result => {
			if (result.visitSuccess === 'success')
			{
				resourceTree._flashUrl(url,'flash-activate');
			}
			else if (result.visitSuccess === 'already')
			{
				resourceTree._flashUrl(url,'flash-neutral');
			}
			else if (result.visitSuccess === 'server-error')
			{
				resourceTree._flashUrl(url,'flash-server-error');
			}
			else if (result.visitSuccess === 'no-data-action' && result.visitInfo != undefined && result.visitInfo.childResources != undefined)
			{
				resourceTree.invertExpansion(url);
				resourceTree._flashUrl(url,'flash-expand');
			}
			else if (result.visitSuccess === 'client-error' || true)
			{
				resourceTree._flashUrl(url,'flash-client-error');
			}
		} ).catch( error => {
			console.error('Unexpected error: ' + error); 
			resourceTree._flashUrl(url,'flash-client-error');
		} );
	}
};
