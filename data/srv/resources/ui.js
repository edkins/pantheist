var editor = undefined;

var expandedNodes = {};

function withoutTrailingSlash(url)
{
	if (url.endsWith('/'))
	{
		return url.substring(0,url.length-1);
	}
	else
	{
		return url;
	}
}

function lastSegment(url)
{
	url = withoutTrailingSlash(url);
	var i = url.lastIndexOf('/');
	if (i == -1)
	{
		return url;
	}
	else
	{
		return url.substring(i + 1);
	}
}

function removeChildren(element)
{
	while (element.firstChild)
	{
    	element.removeChild(element.firstChild);
	}
}

function invertExpansion(url)
{
	if (url in expandedNodes)
	{
		delete expandedNodes[url];
	}
	else
	{
		expandedNodes[url] = '';
	}
}

function clickTreeItem(event)
{
	var url = event.target.dataset.url;
	
	showResource(url).then( () => {
		invertExpansion(url);
		listThings();
	});
}

function listThings()
{
	var url = http.home;
	
	return createUl(url).then(
		ul => {
			var panel = document.getElementById('resource-list');
			removeChildren(panel);
			panel.append(ul);
		}
	);
}

function processList(i,array,fn)
{
	if (i >= array.length)
	{
		return Promise.resolve(undefined);
	}
	else
	{
		return Promise.resolve(fn(array[i])).then( x => processList(i+1,array,fn) );
	}
}

function createUl(parentUrl)
{
	var ul = document.createElement('ul');
	return http.getJson(parentUrl).then(
		text => {
			if (text.childResources.length === 0)
			{
				var li = document.createElement('li');
				li.append('empty');
				li.classList.add('tree-node');
				li.classList.add('notice');
				ul.append(li);
				return ul;
			}
		
			return processList(0, text.childResources, child => {
				var name = lastSegment(child.url);
				
				var li = document.createElement('li');
				ul.append(li);
				
				var item = document.createElement('div');
				li.append(item);
				item.classList.add('tree-item');
				item.append(name);
				item.dataset.url = child.url;
				item.onclick = clickTreeItem;

				li.classList.add('tree-node');
				if (child.url in expandedNodes)
				{
					li.classList.add('expanded');
					return createUl(child.url).then( cul => li.append(cul) );
				}
				else
				{
					li.classList.add('collapsed');
				}
			}).then( x => ul );
		}
	).catch(
		error => {
			var li = document.createElement('li');
			li.append(''+error);
			li.classList.add('tree-node');
			li.classList.add('error');
			ul.append(li);
			return ul;
		}
	);
}

function showResource(url)
{
	return http.getJson(url).then(
		text => editor.setValue(JSON.stringify(text, null, '    ')),
		error => editor.setValue('Error:'+error)
	);
}

function setupAce()
{
    editor = ace.edit("editor");
    editor.setTheme("ace/theme/chrome");
    editor.getSession().setMode("ace/mode/json");
    editor.setShowPrintMargin(false);
    editor.$blockScrolling = Infinity;
}

window.onload = function()
{
	setupAce();
	listThings();
}
