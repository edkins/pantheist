'use strict';

var editor = undefined;
var ui = ui || {};
ui.tab = undefined;
ui._kinds = {};

ui.removeChildren = function(element)
{
	while (element.firstChild)
	{
    	element.removeChild(element.firstChild);
	}
}

ui.flashClass = function(element,cssClass)
{
	if (cssClass !== undefined && !cssClass.startsWith('flash-'))
	{
		throw new Error('Flash classes start with flash- so we can keep track of them');
	}
	if (element == undefined)
	{
		throw new Error('No element to flash');
	}

	var oldClasses = Array.from( element.classList );
	for (var oldClass of oldClasses)
	{
		if (oldClass.startsWith('flash-'))
		{
			element.classList.remove(oldClass);
		}
	}
	
	if (cssClass !== undefined)
	{

		if (oldClasses.indexOf(cssClass) !== -1)
		{
			// Dirty css/js hack
			// Triggering reflow to make sure that animation gets restarted when we add the class back
			var ignore = element.offsetWidth;
		}

		element.classList.add(cssClass);
	}
}

ui.getKindIcon = function(kindUrl, expanded)
{
	var result = undefined;
	if (kindUrl !== undefined && ui._kinds[kindUrl] != undefined)
	{
		var urlPres = ui._kinds[kindUrl].presentation;
		if (urlPres !== undefined)
		{
			if (expanded && urlPres.openIconUrl != undefined)
			{
				return urlPres.openIconUrl;
			}
			if (urlPres.iconUrl != undefined)
			{
				return urlPres.iconUrl;
			}
		}
	}
	
	return '/resources/images/red-ball.png';
};

Object.defineProperty(ui, 'rootKindUrl', {
	get: function()
	{
		return http.home + '/kind/pantheist-root';
	}
});

ui.setEditorText = function(text)
{
	editor.setValue(''+text);
	editor.selection.clearSelection();
	document.getElementById('editor').classList.remove('hidden');
	document.getElementById('create-page').classList.add('hidden');
}

ui._setupAce = function()
{
    editor = ace.edit("editor");
    editor.setTheme("ace/theme/chrome");
    editor.setShowPrintMargin(false);
    editor.$blockScrolling = Infinity;
}

ui._absorbKindInfo = function(kindInfo)
{
	ui._kinds = {};
	for (var kind of kindInfo.childResources)
	{
		ui._kinds[kind.url] = kind;
	}
};

ui.refreshCache = function() {
	return http.getJson(http.home + '/kind').then( kindInfo => {
			ui._absorbKindInfo(kindInfo);
		});
};

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
/*
function constructCreateUrl(t)
{
	var url = http.home;
	
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
				url += '/' + encodeURIComponent(item.dataset.name);
				break;
			case 'var':
				if (item.value === '')
				{
					flashMsg('Values must be nonempty');
					return undefined;
				}
				url += '/' + encodeURIComponent(item.value);
				break;
			}
		} 
	}
	return url;
}
*/

Object.defineProperty(ui, '_sortedKindList', {
	get: function()
	{
		var result = [];
		for (var kindUrl in ui._kinds)
		{
			result.push(ui._kinds[kindUrl]);
		}
		result.sort((a,b) => ui._kindDisplayName(a).localeCompare(ui._kindDisplayName(b)));
		return result;
	}
});

ui._kindDisplayName = function(kind)
{
	if (kind.presentation != undefined && kind.presentation.displayName != undefined)
	{
		return kind.presentation.displayName;
	}
	else
	{
		return kind.kindId;
	}
};

ui._visitCreate = function(pseudoUrl)
{
	document.getElementById('editor').classList.add('hidden');
	document.getElementById('create-page').classList.remove('hidden');
	fileTabs.switchTo(pseudoUrl);
	var panel = document.getElementById('create-page-list');
	ui.removeChildren(panel);
	for (var kind of ui._sortedKindList)
	{
		if (kind.createAction != undefined)
		{
			var div = document.createElement('div');
			var icon = document.createElement('span');
			var textSpan = document.createElement('span');
			var iconUrl = ui.getKindIcon(kind.url, true);
			var displayName = ui._kindDisplayName(kind);
			
			div.classList.add('create-kind');
			div.dataset.kindUrl = kind.url;
			div.onclick = ui._onclickCreateItem;
			
			icon.classList.add('create-kind-icon');
			icon.style['background-image'] = "url('" + iconUrl + "')";
			
			textSpan.textContent = displayName;
			textSpan.classList.add('create-kind-name');
			
			div.append(icon);
			div.append(textSpan);
			panel.append(div);
		}
	}
	return {visitSuccess: 'success'};
};

ui._visitUntitled = function(pseudoUrl)
{
	fileTabs.switchTo(pseudoUrl);
	
	var kind = ui._kinds[fileTabs.activeKindUrl];
	
	if (kind === undefined || kind.createAction === undefined || kind.createAction.prototypeUrl === undefined)
	{
		ui.setEditorText('');
		return {visitSuccess: 'success'};
	}
	else
	{
		http.getString('text/plain', kind.createAction.prototypeUrl).then(
			text => {
				ui.setEditorText(text);
				return {visitSuccess: 'success'};
			},
			error => {
				console.log('Fetching prototype: ' + error);
				return {visitSuccess: 'server-error'};
			}
		);
	}

};

ui._visitAbout = function(pseudoUrl)
{
	if (pseudoUrl === 'about:create')
	{
		return ui._visitCreate(pseudoUrl);
	}
	else if (pseudoUrl.startsWith('about:untitled/'))
	{
		return ui._visitUntitled(pseudoUrl);
	}
	else
	{
		console.error('visiting invalid value: ' + pseudoUrl);
		return {visitSuccess: 'client-error'};
	}
};

ui.visit = function(url)
{
	if (typeof url !== 'string')
	{
		console.error('visiting invalid value: ' + url);
		return {
			visitSuccess: 'client-error'
		}
	}

	if (url.startsWith('about:'))
	{
		return ui._visitAbout(url);
	}

	return http.getJson(url).then( data => {
		if (data.dataAction != undefined)
		{
			var success = fileTabs.openIfNotAlready(url, data.kindUrl, data.dataAction.url);
			fileTabs.switchTo(url);
			
			return http.getString(data.dataAction.mimeType, data.dataAction.url).then( text => {
				ui.setEditorText(text);
				return {
					visitSuccess: success,
					visitInfo: data
				};
			}).catch(error => {
				console.error('Data fetch: ' + error);
				return {
					visitSuccess: 'server-error',
					visitInfo: error
				};
			});
		}
		else
		{
			return {
				visitSuccess: 'no-data-action',
				visitInfo: data
			};
		}
	}, error => {
		console.error('Info fetch: ' + error);
		return {
			visitSuccess: 'server-error',
			visitInfo: error
		};
	} );
};

ui._visitScratch = function(text)
{
	fileTabs.switchTo(undefined);
	ui.setEditorText(text);
};

ui.visitInfo = function(url)
{
	return http.getString('application/json', url)
		.then( text => ui._visitScratch(text) )
		.catch( error => ui._visitScratch(error) );
};

ui._onclickReload = function(event)
{
	var button = document.getElementById('btn-reload');
	http.post(http.home + '/system/reload', undefined, undefined).then( () => {
		ui.flashClass(button, 'flash-activate');
		ui._visitScratch('Server has reloaded configuration');
	}).catch( error => {
		console.error(''+error);
		ui.flashClass(button, 'flash-server-error');
	});
};

ui._onclickShutdown = function(event)
{
	var button = document.getElementById('btn-shutdown');
	http.post(http.home + '/system/terminate', undefined, undefined).then( () => {
		ui.flashClass(button, 'flash-activate');
		ui._visitScratch('Terminated');
	}).catch( error => {
		console.error(''+error);
		ui.flashClass(button, 'flash-server-error');
	});
};

ui._onclickSave = function(event)
{
	var button = document.getElementById('btn-save');
	var url = fileTabs.activeDataUrl;
	
	if (url == undefined)
	{
		console.error('data url is undefined');
		ui.flashClass(button, 'flash-client-error');
		return;
	}
	
	var kindUrl = fileTabs.activeKindUrl;
	if (kindUrl == undefined)
	{
		console.error('kindUrl is undefined');
		ui.flashClass(button, 'flash-client-error');
		return;
	}

	var kind = ui._kinds[kindUrl];
	if (kind == undefined)
	{
		console.error('unknown kind: ' + kindUrl);
		ui.flashClass(button, 'flash-client-error');
		return;
	}
	
	if (kind.dataAction == undefined)
	{
		console.error('no data action for kind: ' + kindUrl);
		ui.flashClass(saveButton, 'flash-client-error');
		return;
	}
	
	if (!kind.dataAction.canPut)
	{
		console.error('read-only kind: ' + kindUrl);
		ui.flashClass(saveButton, 'flash-client-error');
		return;
	}

	if (document.getElementById('editor').classList.contains('hidden'))
	{
		console.error('editor is hidden');
		ui.flashClass(saveButton, 'flash-client-error');
		return;
	}

	http.putString(url, kind.dataAction.mimeType, editor.getValue()).then(
		ok => {
			ui.flashClass(saveButton, 'flash-activate');
		},
		error => {
			console.error('sending data: ' + error);
			ui.flashClass(saveButton, 'flash-server-error');
		}
	);
};

ui._onclickCreateItem = function(event)
{
	var button = event.currentTarget;
	var kindUrl = event.currentTarget.dataset.kindUrl;
	var kind = ui._kinds[kindUrl];
	if (kind == undefined)
	{
		console.error('unknown kind: ' + kindUrl);
		ui.flashClass(button, 'flash-client-error');
		return;
	}

	if (kind.createAction == undefined)
	{
		console.error('no create action for kind: ' + kindUrl);
		ui.flashClass(button, 'flash-client-error');
		return;
	}
	
	fileTabs.openNew(kindUrl, ui._kindDisplayName(kind));
	ui.flashClass(button, 'flash-activate');
};

window.onload = function()
{
	ui._setupAce();
	
	document.getElementById('btn-reload').onclick = ui._onclickReload;
	document.getElementById('btn-shutdown').onclick = ui._onclickShutdown;
	document.getElementById('btn-save').onclick = ui._onclickSave;
	document.getElementById('btn-close-all').onclick = fileTabs.onclickCloseAll;

	fileTabs.createCreateTab();

	ui.refreshCache().then( () => resourceTree.initialize() );
};
