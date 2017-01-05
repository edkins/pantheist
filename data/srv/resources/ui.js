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
				console.log('Fetching prototype: ' + error);
				return {visitSuccess: 'server-error'};
			}
		);
	}

};

ui.openNew = function(kindUrl, dataUrl, elementToFlash)
{
	var kind = ui._kinds[kindUrl];
	var displayName = ui._kindDisplayName(kind);
	var mimeType = kind.createAction.mimeType;
	var method = kind.createAction.method;
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
				ui.flashClass(elementToFlash, 'flash-activate');
			},
			error => {
				console.log('Fetching prototype: ' + error);
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

ui.switchTo = function(url)
{
	fileTabs.switchTo(url);
	if (url === 'about:create')
	{
		editSessions.switchTo(undefined);
		createPage.showCreatePage();
	}
	else
	{
		createPage.hideCreatePage();
		editSessions.switchTo(url);
	}
};

ui.visit = function(url, kindUrl, dataUrl, mimeType, elementToFlash, flashOnSuccess)
{
	if (typeof url !== 'string' || typeof kindUrl !== 'string' || typeof dataUrl !== 'string' || typeof mimeType !== 'string')
	{
		console.error('visiting invalid value: ' + url + ' ' + kindUrl + ' ' + dataUrl + ' ' + mimeType);
		ui.flashClass(elementToFlash, 'flash-client-error');
		return undefined;
	}
	
	if (fileTabs.has(url) !== editSessions.has(url))
	{
		console.error('fileTabs and editSessions disagree on whether url exists: ' + url + ' ' + fileTabs.has(url) + ' ' + editSessions.has(url));
		ui.flashClass(elementToFlash, 'flash-client-error');
		return undefined;
	}

	if (fileTabs.has(url))
	{
		// Already loaded, just need to switch to it.
		fileTabs.switchTo(url);
		editSessions.switchTo(url);
		
		if (flashOnSuccess)
		{
			ui.flashClass(elementToFlash, 'flash-neutral');
		}
		return undefined;
	}

	return http.getString(mimeType, dataUrl).then(
		text => {
			var displayName = uri.lastSegment(url);
			fileTabs.open(url, kindUrl, dataUrl, mimeType)
			editSessions.create(url, text);

			fileTabs.switchTo(url);
			editSessions.switchTo(url);
			
			if (flashOnSuccess)
			{
				ui.flashClass(elementToFlash, 'flash-activate');
			}
			return undefined;
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
	
	var url = fileTabs.activeFile.dataUrl;
	var mimeType = fileTabs.activeFile.mimeType;
	var method = fileTabs.activeFile.method;
	var text = editSessions.textForUrl(fileTabs.activeFile.url);
	
	if (text == undefined)
	{
		console.error('text is undefined');
		ui.flashClass(button, 'flash-client-error');
		return;
	}
	
	if (url == undefined)
	{
		console.error('data url is undefined');
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
		return http.post(url, mimeType, text).then(
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
}

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
