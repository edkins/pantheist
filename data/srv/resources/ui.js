'use strict';

var editor = undefined;

function removeChildren(element)
{
	while (element.firstChild)
	{
    	element.removeChild(element.firstChild);
	}
}

function invertExpansion(url)
{
	if (url in ui.expandedNodes)
	{
		delete ui.expandedNodes[url];
	}
	else
	{
		ui.expandedNodes[url] = '';
	}
}

function listThings(t)
{
	return createUl(t,http.home).then(
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

function createUl(t,parentUrl)
{
	var ul = document.createElement('ul');
	return t.getJson(parentUrl).then(
		data => {
			if (data.childResources.length === 0)
			{
				var li = document.createElement('li');
				li.append('empty');
				li.classList.add('tree-node');
				li.classList.add('notice');
				ul.append(li);
				return ul;
			}
		
			return processList(0, data.childResources, child => {
				var name = http.lastSegment(child.url);
				
				var li = document.createElement('li');
				ul.append(li);
				
				var item = document.createElement('div');
				li.append(item);
				item.classList.add('tree-item');
				item.append(name);
				item.dataset.url = child.url;
				item.onclick = clickTreeItem;

				li.classList.add('tree-node');
				if (child.url in t.expandedNodes)
				{
					li.classList.add('expanded');
					return createUl(t,child.url).then( cul => li.append(cul) );
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

function showData(t)
{
	if (t.data === undefined)
	{
		setEditorText('text', 'Error:'+t.error)
	}
	else
	{
		setEditorText('json', JSON.stringify(t.data, null, '    '))
	}
}

function setupAce()
{
    editor = ace.edit("editor");
    editor.setTheme("ace/theme/chrome");
    editor.getSession().setMode("ace/mode/json");
    editor.setShowPrintMargin(false);
    editor.$blockScrolling = Infinity;
}

function highlightLocationInResourceList(url)
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
}

function highlightActiveTab(buttonId)
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

function showCreateForm(t)
{
	document.getElementById('create-form').classList.remove('hidden');
	document.getElementById('create-label-1').textContent = http.lastSegment(t.url);
	
	if (t.data !== undefined)
	{
		var panel = document.getElementById('create-additional');
		removeChildren(panel);
		if ('additionalStructure' in t.data)
		{
			for (var additional of t.data.additionalStructure)
			{
				if (additional.literal)
				{
					var span = document.createElement('span');
					span.append(' ' + additional.name + ' ');
					span.dataset.segtype = 'literal';
					span.dataset.name = additional.name;
					panel.append(span);
				}
				else
				{
					var inp = document.createElement('input');
					inp.type = 'text';
					inp.dataset.segtype = 'var';
					panel.append(inp);
				}
			}
		}
	}
}

function hideCreateForm()
{
	document.getElementById('create-form').classList.add('hidden');
}

function refreshCurrentPane(t)
{
	switch(t.tab)
	{
	case 'btn-get':
		document.getElementById('btn-send').classList.add('hidden');
		hideCreateForm();
		showData(t);

		break;
	case 'btn-create':
		document.getElementById('btn-send').classList.remove('hidden');
		showCreateForm(t);

		break;
	default:
		document.getElementById('btn-send').classList.add('hidden');
		hideCreateForm();
	}
}

function clickTreeItem(event)
{
	var url = event.target.dataset.url;

	document.getElementById('address-bar').value = url;
	invertExpansion(url);

	Transaction.fetch().then( t => {
		if (t.tab === undefined)
		{
			ui.tab = 'btn-get';
			t.tab = 'btn-get';
			highlightActiveTab('btn-get');
		}
		
		refreshCurrentPane(t);
		listThings(t).then( () => {
			highlightLocationInResourceList(t.url);
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
	ui.tab = 'btn-get';
	highlightActiveTab('btn-get');

	Transaction.fetch().then( t => {
		highlightLocationInResourceList(t.url);
		refreshCurrentPane(t);
	});
}

function clickCreate(event)
{
	ui.tab = 'btn-create';
	highlightActiveTab('btn-create');

	Transaction.fetch().then( t => {
		setEditorText('json','');
		refreshCurrentPane(t);
	});
}

function constructSendUrl(t)
{
	var url = t.url;
	
	var panel = document.getElementById('create-form');
	var items = panel.getElementsByTagName('*');
	for (var i = 0; i < items.length; i++)
	{
		var item = items.item(i);
		if ('segtype' in item.dataset)
		{
			switch(item.dataset.segtype)
			{
			case 'literal':
				url += '/' + item.dataset.name;
				break;
			case 'var':
				if (item.value === '')
				{
					return Promise.reject('Values must be nonempty');
				}
				url += '/' + item.value;
				break;
			}
		} 
	}
	return Promise.resolve(url);
}

function flashMsg(msg)
{
	var el = document.getElementById('send-response');
	
	el.textContent = '' + msg;
	
	// Dirty css/js hack
	// Restart css animation by removing class, triggering reflow, then adding class back again
	el.classList.remove('msg-flash');
	var ignore = el.offsetWidth;
	el.classList.add('msg-flash');
}

function clickSend(event)
{
	var t = new Transaction();
	var text = editor.getValue();
	
	constructSendUrl(t)
		.then( sendUrl => {
			return http.putString(sendUrl, 'application/json', text)
				.then( x => flashMsg('OK') );
		} )
		.catch (error => flashMsg(error) );
}

function changeAddressBar(event)
{
	if (ui.tab === 'btn-get')
	{
		ui.tab = undefined;
		highlightActiveTab(undefined);
	}
}

window.onload = function()
{
	setupAce();
	
	document.getElementById('address-bar').oninput = changeAddressBar;
	document.getElementById('btn-reload').onclick = clickReload;
	document.getElementById('btn-shutdown').onclick = clickShutdown;
	document.getElementById('btn-get').onclick = clickGet;
	document.getElementById('btn-create').onclick = clickCreate;
	document.getElementById('btn-send').onclick = clickSend;

	document.getElementById('address-bar').value = http.home;

	Transaction.fetch().then( t => {
		refreshCurrentPane(t);
		listThings(t);
	});
}
