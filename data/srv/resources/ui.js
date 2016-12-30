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
	var rootItem = createTreeItem(http.home,'root'); 
	return createUl(t,http.home).then(
		ul => {
			var panel = document.getElementById('resource-list');
			removeChildren(panel);
			panel.append(rootItem);
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

function createTreeItem(url,name)
{
	var item = document.createElement('div');
	item.classList.add('tree-item');
	item.append(name);
	item.dataset.url = url;
	item.onclick = clickTreeItem;
	return item;
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
				
				var item = createTreeItem(child.url,name);
				li.append(item);

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

function setEditorText(text)
{
	editor.setValue(text);
	editor.selection.clearSelection();
}

function showDataForInfoPane(t)
{
	if (t.tab === 'btn-info')
	{
		if (t.data === undefined)
		{
			setEditorText('')
		}
		else
		{
			setEditorText(JSON.stringify(t.data, null, '    '))
		}
	}
}

function setupAce()
{
    editor = ace.edit("editor");
    editor.setTheme("ace/theme/chrome");
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
	
	var panel = document.getElementById('create-additional');
	removeChildren(panel);
	
	if (t.data.createAction.additionalStructure != undefined)
	{
		for (var additional of t.data.createAction.additionalStructure)
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

function hideCreateForm()
{
	document.getElementById('create-form').classList.add('hidden');
}

function hideSendButton()
{
	document.getElementById('btn-send').classList.add('hidden');
	document.getElementById('send-response').textContent = '';
}

function showSendButton()
{
	document.getElementById('btn-send').classList.remove('hidden');
}

function setEditorMode(basicType)
{
	switch(basicType)
	{
	case 'text':
	case 'unknown':
	    editor.getSession().setMode('ace/mode/text');
		break;
	case 'json':
	    editor.getSession().setMode('ace/mode/json');
		break;
	case 'java':
	    editor.getSession().setMode('ace/mode/java');
		break;
	default:
		console.log('Unrecognized basic type: ' + basicType);
	    editor.getSession().setMode('ace/mode/text');
		break;
	}
}

function refreshCurrentPane(t)
{
	switch(t.tab)
	{
	case 'btn-info':
		hideSendButton();
		hideCreateForm();
		setEditorMode('json');
		break;
	case 'btn-data':
		if (t.data !== undefined && t.data.dataAction != undefined)
		{
			if (t.data.dataAction.canPut)
			{
				showSendButton();
			}
			else
			{
				hideSendButton();
			}
			setEditorMode(t.data.dataAction.basicType);
		}
		hideCreateForm();
		break;
	case 'btn-create':
		showSendButton();
		if (t.data !== undefined && t.data.createAction != undefined)
		{
			showCreateForm(t);
    		setEditorMode(t.data.createAction.basicType);
		}
		else
		{
			hideCreateForm();
			setEditorMode('unknown');
		}

		break;
	default:
		hideSendButton();
		hideCreateForm();
	}
}

function constructCreateUrl(t)
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

function showAvailableActionsAsTabs(t)
{
	if (t.data !== undefined && t.data.createAction != undefined)
	{
		document.getElementById('btn-create').classList.remove('hidden');
	}
	else
	{
		document.getElementById('btn-create').classList.add('hidden');
	}

	if (t.data !== undefined && t.data.dataAction != undefined)
	{
		document.getElementById('btn-data').classList.remove('hidden');
	}
	else
	{
		document.getElementById('btn-data').classList.add('hidden');
	}
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

function clickTreeItem(event)
{
	var url = event.target.dataset.url;

	if (document.getElementById('address-bar').value === url)
	{
		invertExpansion(url);
	}
	else
	{
		document.getElementById('address-bar').value = url;
	}

	Transaction.fetch().then( t => {
		showAvailableActionsAsTabs(t);
	
		if (t.tab === undefined)
		{
			ui.tab = 'btn-info';
			t.tab = 'btn-info';
			highlightActiveTab('btn-info');
		}
		
		refreshCurrentPane(t);
		showDataForInfoPane(t);
		listThings(t).then( () => {
			highlightLocationInResourceList(t.url);
		});
	});
}

function clickReload(event)
{
	http.post(http.home + '/system/reload', undefined, undefined);
}

function clickShutdown(event)
{
	var t = new Transaction();
	http.post(http.home + '/system/terminate', undefined, undefined).then( () => {
		flashMsg('Shutdown');
	});
}

function clickInfo(event)
{
	ui.tab = 'btn-info';
	highlightActiveTab('btn-info');
	
	if (document.getElementById('address-bar').value === '')
	{
		document.getElementById('address-bar').value = http.home;
	}

	Transaction.fetch().then( t => {
		showAvailableActionsAsTabs(t);
		highlightLocationInResourceList(t.url);
		showDataForInfoPane(t);
		listThings(t);
	});
}

function clickCreate(event)
{
	ui.tab = 'btn-create';
	highlightActiveTab('btn-create');

	Transaction.fetch().then( t => {
		setEditorText('');
		refreshCurrentPane(t);
	});
}

function clickData(event)
{
	ui.tab = 'btn-data';
	highlightActiveTab('btn-data');

	Transaction.fetch().then( t => {
		refreshCurrentPane(t);
		if (t.data !== undefined && t.data.dataAction != undefined)
		{
			http.getString(t.data.dataAction.mimeType, t.url + '/data').then(
				text => {
					setEditorText(text);
				}
			).catch(
				error => {
					flashMsg(error);
				}
			);
		}
	});
}

function clickSend(event)
{
	Transaction.fetch().then( t => {
		var text = editor.getValue();
		
		switch(t.tab)
		{
		case 'btn-data':
			if (t.data === undefined || t.data.dataAction == undefined)
			{
				flashMsg('Unknown data type to send');
				return;
			}
		
			return http.putString(t.url + '/data', t.data.dataAction.mimeType, text)
				.then( x => flashMsg('OK') )
				.catch (error => flashMsg(error) );
			break;
		case 'btn-create':
			if (t.data === undefined || t.data.createAction == undefined)
			{
				flashMsg('Unknown data type to send');
				return;
			}
		
			constructCreateUrl(t)
				.then( sendUrl => {
					return http.putString(sendUrl, t.data.createAction.mimeType, text)
						.then( x => flashMsg('OK') );
				} )
				.catch (error => flashMsg(error) );
			break;
		}
	} );
}

function changeAddressBar(event)
{
	if (ui.tab === 'btn-info')
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
	document.getElementById('btn-info').onclick = clickInfo;
	document.getElementById('btn-data').onclick = clickData;
	document.getElementById('btn-create').onclick = clickCreate;
	document.getElementById('btn-send').onclick = clickSend;

	document.getElementById('address-bar').value = http.home;

	Transaction.fetch().then( t => {
		refreshCurrentPane(t);
		listThings(t);
	});
}
