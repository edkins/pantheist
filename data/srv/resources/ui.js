'use strict';

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

function setEditorText(mode,text)
{
	editor.setValue(text);
    editor.getSession().setMode('ace/mode/' + mode);
	editor.selection.clearSelection();
}

function showResource(url)
{
	return http.getJson(url).then(
		text => setEditorText('json', JSON.stringify(text, null, '    ')),
		error => setEditorText('text', 'Error:'+error)
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

function setActiveLocation(url)
{
	var panel = document.getElementById('resource-list');
	var items = panel.getElementsByTagName('div');
	for (var i = 0; i < items.length; i++)
	{
		var item = items.item(i);
		var wasActive = item.classList.contains('active');
		if (item.dataset.url === url && !wasActive)
		{
			item.classList.add('active');
		}
		if (item.dataset.url !== url && wasActive)
		{
			item.classList.remove('active');
		}
	}
	
	document.getElementById('address-bar').value = url;
}

function setActiveActionButton(buttonId)
{
	var panel = document.getElementById('tab-bar');
	var items = panel.getElementsByTagName('span');
	for (var i = 0; i < items.length; i++)
	{
		items.item(i).classList.remove('active');
	}
	if (buttonId !== undefined)
	{
		document.getElementById(buttonId).classList.add('active');
	}
}

function clickTreeItem(event)
{
	var url = event.target.dataset.url;
	
	showResource(url).then( () => {
		invertExpansion(url);
		listThings().then( () => {
			setActiveLocation(url);
			setActiveActionButton('btn-get');
		});
	});
}

function clickReload(event)
{
	http.post(http.home + 'system/reload', undefined, undefined);
}

function clickShutdown(event)
{
	http.post(http.home + 'system/terminate', undefined, undefined);
}

function clickGet(event)
{
	var url = document.getElementById('address-bar').value;
	showResource(url).then( () => {
		setActiveLocation(url);
		setActiveActionButton('btn-get');
	});
}

function clickPut(event)
{
	setEditorText('json','');
	setActiveActionButton('btn-put');
}

function changeAddressBar(event)
{
	setActiveActionButton(undefined);
}

window.onload = function()
{
	setupAce();
	listThings();
	
	document.getElementById('address-bar').value = http.home;
	
	document.getElementById('address-bar').oninput = changeAddressBar;
	document.getElementById('btn-reload').onclick = clickReload;
	document.getElementById('btn-shutdown').onclick = clickShutdown;
	document.getElementById('btn-get').onclick = clickGet;
	document.getElementById('btn-put').onclick = clickPut;
}
