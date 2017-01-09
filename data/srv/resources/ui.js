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
};

ui.flashClass = function(element,cssClass)
{
	if (element == undefined)
	{
		throw new Error('No element to flash');
	}
	
	if (cssClass != undefined)
	{
		element.classList.add(cssClass);
		setTimeout( () => {
			element.classList.remove(cssClass);
		}, 50 );
	}
};

ui.getKindUrlIcon = function(kindUrl, expanded)
{
	return ui.getKindIcon(ui._kinds[kindUrl], expanded);
};

ui.getKindIcon = function(kind, expanded)
{
	var urlPres = kind && kind.presentation;
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
	return '/resources/images/red-ball.png';
};

ui.isKindListable = function(kindUrl)
{
	if (kindUrl !== undefined && ui._kinds[kindUrl] != undefined)
	{
		return ui._kinds[kindUrl].listable || false;
	}
	return false;
};

ui._setupAce = function()
{
    editor = ace.edit("editor");
    editor.setTheme("ace/theme/chrome");
    editor.setShowPrintMargin(false);
    editor.$blockScrolling = Infinity;
};

ui._fetchKind = function(url)
{
	return http.getJson(url).then( kind => {
		ui._kinds[url] = kind;
	});
};

ui.refreshCache = function() {
	return http.getJson(http.home + '/entity/kind').then(
		list => {
			var promises = [];
			for (var child of list.childResources)
			{
				promises.push( ui._fetchKind(child.url) );
			}
			return Promise.all(promises);
		}
	);
};
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

Object.defineProperty(ui, 'allKindUrls', {
	get: function()
	{
		return Array.from(Object.keys(ui._kinds));
	}
});

ui.getKind = function(kindUrl)
{
	return ui._kinds[kindUrl];
};

ui.kindDisplayName = function(kind)
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

ui._visitUntitled = function(pseudoUrl)
{
	fileTabs.switchTo(pseudoUrl);
	
	var kind = ui._kinds[fileTabs.activeFile.kindUrl];
	
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
				console.error('Fetching prototype: ' + error);
				return {visitSuccess: 'server-error'};
			}
		);
	}

};

ui.openNew = function(kindUrl, dataUrl, elementToFlash)
{
	var kind = ui._kinds[kindUrl];
	var displayName = ui.kindDisplayName(kind);
	var mimeType = kind.createAction.mimeType;
	var method = kind.createAction.method;
	var schemaUrl = kind.createAction.jsonSchema;
	var schemaHintUrl = kind.presentation && kind.presentation.schemaHint;
	var pseudoUrl = fileTabs.openNew(kindUrl, displayName, dataUrl, mimeType, method)
	
	if (kind === undefined || kind.createAction === undefined || kind.createAction.prototypeUrl === undefined)
	{
		editSessions.create(pseudoUrl,'');
		ui.flashClass(elementToFlash, 'flash-activate');
		return undefined;
	}
	else
	{
		return http.getString('text/plain', kind.createAction.prototypeUrl).then(
			text => {
				editSessions.create(pseudoUrl,text);
				if (schemaUrl != undefined && false)  // hidden this for now because it's difficult to get right
				{
					return jsonForm.create(pseudoUrl, schemaUrl, schemaHintUrl, elementToFlash, true);
				}
				else
				{
					ui.flashClass(elementToFlash, 'flash-activate');
				}
			},
			error => {
				console.error('Fetching prototype: ' + error);
				ui.flashClass(elementToFlash, 'flash-server-error');
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

ui.loadIfNotAlready = function(url)
{
	if (fileTabs.has(url))
	{
		return Promise.resolve(undefined);
	}
};

ui._viewSwitch = function(view)
{
	var panel = document.getElementById('view-switcher');
	if (view === undefined)
	{
		panel.classList.add('hidden');
	}
	else if (view === 'form')
	{
		document.getElementById('btn-view-text').classList.remove('active');
		document.getElementById('btn-view-form').classList.add('active');
		panel.classList.remove('hidden');
	}
	else if (view === 'text')
	{
		document.getElementById('btn-view-form').classList.remove('active');
		document.getElementById('btn-view-text').classList.add('active');
		panel.classList.remove('hidden');
	}
	else
	{
		throw new Error('Unknown view: ' + view);
	}
};

ui.switchTo = function(url)
{
	fileTabs.switchTo(url);
	if (url === 'about:create')
	{
		ui._viewSwitch(undefined);
		editSessions.switchTo(undefined);
		jsonForm.switchTo(undefined);
		createPage.showCreatePage();
	}
	else if (editSessions.has(url) && !jsonForm.hasActive(url))
	{
		if (jsonForm.has(url))
		{
			ui._viewSwitch('text');
		}
		else
		{
			ui._viewSwitch(undefined);
		}
		createPage.hideCreatePage();
		jsonForm.switchTo(undefined);
		editSessions.switchTo(url);
	}
	else if (jsonForm.has(url))
	{
		ui._viewSwitch('form');
		createPage.hideCreatePage();
		editSessions.switchTo(undefined);
		jsonForm.switchTo(url);
	}
	else
	{
		throw new Error('No component has url: ' + url);
	}
};

ui.visit = function(url, kindUrl, elementToFlash, flashOnSuccess)
{
	if (typeof url !== 'string' || typeof kindUrl !== 'string')
	{
		console.error('visiting invalid value: ' + url + ' ' + kindUrl);
		ui.flashClass(elementToFlash, 'flash-client-error');
		return undefined;
	}

	if (fileTabs.has(url))
	{
		// Already loaded, just need to switch to it.
		ui.switchTo(url);
		
		if (flashOnSuccess)
		{
			ui.flashClass(elementToFlash, 'flash-neutral');
		}
		return undefined;
	}

	if (editSessions.has(url))
	{
		console.error('editSessions has url when it shouldn\'t: ' + url);
		ui.flashClass(elementToFlash, 'flash-client-error');
		return undefined;
	}

	if (jsonForm.has(url))
	{
		console.error('jsonForm has url when it shouldn\'t: ' + url);
		ui.flashClass(elementToFlash, 'flash-client-error');
		return undefined;
	}
	
	var kind = ui._kinds[kindUrl];
	
	if (kind === undefined)
	{
		console.error('unknown kind: ' + kindUrl);
		ui.flashClass(elementToFlash, 'flash-client-error');
		return undefined;
	}
	
	var schemaUrl = undefined;
	var mimeType = undefined;
	if (kind.createAction != undefined)
	{
		schemaUrl = kind.createAction.jsonSchema;
		mimeType = kind.createAction.mimeType;
	}
	if (kind.computed != undefined)
	{
		if (kind.computed.mimeType != undefined)
		{
			mimeType = kind.computed.mimeType;
		}
	}
	
	if (mimeType == undefined)
	{
		console.error('unknown mime type to fetch');
		ui.flashClass(elementToFlash, 'flash-client-error');
		return undefined;
	}
	
	var schemaHintUrl = kind.presentation && kind.presentation.schemaHint;

	return http.getString(mimeType, url).then(
		text => {
			var displayName = uri.lastSegment(url);
			fileTabs.open(url, kindUrl, mimeType);

			editSessions.create(url, text);
			if (schemaUrl != undefined && false)   // hidden this for now because it's difficult to get right
			{
				return jsonForm.create(url, schemaUrl, schemaHintUrl, elementToFlash, flashOnSuccess)
					.then( () => {
						ui.switchTo(url);
					} );
			}
			else
			{
				ui.switchTo(url);
				if (flashOnSuccess)
				{
					ui.flashClass(elementToFlash, 'flash-activate');
				}
			}
		},
		
		error => {
			console.error('fetching data: ' + error);
			ui.flashClass(elementToFlash, 'flash-server-error');
			return undefined;
		}
	);
};

ui.visitScratch = function(text)
{
	fileTabs.switchTo(undefined);
	editSessions.scratch(text);
};

ui._onclickReload = function(event)
{
	var button = document.getElementById('btn-reload');
	http.post(http.home + '/system/reload', undefined, undefined).then( () => {
		ui.flashClass(button, 'flash-activate');
		ui.visitScratch('Server has reloaded configuration');
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
		ui.visitScratch('Terminated');
	}).catch( error => {
		console.error(''+error);
		ui.flashClass(button, 'flash-server-error');
	});
};

ui.save = function()
{
	var button = document.getElementById('btn-save');
	if (fileTabs.activeFile === undefined)
	{
		console.error('no active file');
		ui.flashClass(button, 'flash-client-error');
		return;
	}
	
	var url = fileTabs.activeFile.url;
	var mimeType = fileTabs.activeFile.mimeType;
	var method = fileTabs.activeFile.method;
	var text = editSessions.textForUrl(fileTabs.activeFile.url);
	
	if (text == undefined)
	{
		console.error('text is undefined');
		ui.flashClass(button, 'flash-client-error');
		return;
	}
	
	if (mimeType == undefined)
	{
		console.error('mime type is undefined');
		ui.flashClass(button, 'flash-client-error');
		return;
	}

	if (method == undefined)
	{
		console.error('method is undefined');
		ui.flashClass(button, 'flash-client-error');
		return;
	}

	switch(method)
	{
	case 'put':
		return http.putString(url, mimeType, text).then(
			ok => {
				ui.flashClass(button, 'flash-activate');
			},
			error => {
				console.error('putting data: ' + error);
				ui.flashClass(button, 'flash-server-error');
			} );
	case 'post':
		if (fileTabs.activeFile.createUrl == undefined)
		{
			console.error('create url is undefined');
			ui.flashClass(button, 'flash-client-error');
			return;
		}
	
		return http.post(fileTabs.activeFile.createUrl, mimeType, text).then(
			url => {
				ui.flashClass(button, 'flash-activate');
			},
			error => {
				console.error('posting data: ' + error);
				ui.flashClass(button, 'flash-server-error');
			} );
	default:
		console.error('unrecognized method: ' + method);
		ui.flashClass(button, 'flash-client-error');
		return;
	}
};

ui._onclickSave = function(event)
{
	ui.save();
};

ui._onclickCloseAll = function(event)
{
	fileTabs.closeAll();
	editSessions.closeAll();
	jsonForm.closeAll();
	ui._switchView(undefined);
};

ui._onclickViewForm = function(event)
{
	editSessions.switchTo(undefined);
	jsonForm.switchTo(fileTabs.activeUrl);
	ui._viewSwitch('form');
};

ui._onclickViewText = function(event)
{
	jsonForm.deactivate();
	editSessions.switchTo(fileTabs.activeUrl);
	ui._viewSwitch('text');
};

window.onload = function()
{
	ui._setupAce();
	
	document.getElementById('btn-reload').onclick = ui._onclickReload;
	document.getElementById('btn-shutdown').onclick = ui._onclickShutdown;
	document.getElementById('btn-save').onclick = ui._onclickSave;
	document.getElementById('btn-close-all').onclick = ui._onclickCloseAll;
	document.getElementById('btn-view-form').onclick = ui._onclickViewForm;
	document.getElementById('btn-view-text').onclick = ui._onclickViewText;

	fileTabs.createCreateTab();

	ui.refreshCache().then( () => resourceTree.initialize() );
};
