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
			var ignore = el.offsetWidth;
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
	document.getElementById('editor').classList.remove('gone');
	document.getElementById('create-page').classList.add('gone');
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
		result.sort((a,b) => a.displayName.localeCompare(b.displayName));
		return result;
	}
});

ui._visitAbout = function(pseudoUrl)
{
	switch(pseudoUrl)
	{
	case 'about:create':
	{
		document.getElementById('editor').classList.add('gone');
		document.getElementById('create-page').classList.remove('gone');
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
				var displayName = kind.kindId;
				if (kind.presentation != undefined && kind.presentation.displayName != undefined)
				{
					displayName = kind.presentation.displayName;
				}
				
				div.classList.add('create-kind');
				
				icon.classList.add('create-kind-icon');
				icon.style['background-image'] = "url('" + iconUrl + "')";
				
				textSpan.textContent = displayName;
				textSpan.classList.add('create-kind-name');
				
				div.append(icon);
				div.append(textSpan);
				panel.append(div);
			}
		}
		return undefined;
	}
	default:
		return undefined;
	}
};

ui.visit = function(url)
{
	if (typeof url !== 'string')
	{
		throw new Error('url must be string, was ' + url);
	}

	if (url.startsWith('about:'))
	{
		return ui._visitAbout(url);
	}

	return http.getJson(url).then( data => {
		if (data.dataAction != undefined)
		{
			var success = fileTabs.openIfNotAlready(url);
			fileTabs.switchTo(url);
			
			return http.getString(data.dataAction.mimeType, data.dataAction.url).then( text => {
				ui.setEditorText(text);
				return {
					visitSuccess: success,
					visitInfo: data
				};
			});
		}
		else
		{
			return {
				visitSuccess: 'failed',
				visitInfo: data
			};
		}
	} );
}

ui._visitScratch = function(text)
{
	fileTabs.switchTo(undefined);
	ui.setEditorText(text);
}

ui.visitInfo = function(url)
{
	return http.getString('application/json', url)
		.then( text => ui._visitScratch(text) )
		.catch( error => ui._visitScratch(error) );
}

function clickReload(event)
{
	http.post(http.home + '/system/reload', undefined, undefined).then( () => {
		ui._visitScratch('Server has reloaded configuration');
	}).catch( error => {
		ui._visitScratch(error);
	});
}

function clickShutdown(event)
{
	http.post(http.home + '/system/terminate', undefined, undefined).then( () => {
		ui._visitScratch('Terminated');
	}).catch( error => {
		ui._visitScratch(error);
	});;
}

window.onload = function()
{
	ui._setupAce();
	
	document.getElementById('btn-reload').onclick = clickReload;
	document.getElementById('btn-shutdown').onclick = clickShutdown;

	fileTabs.createCreateTab();

	ui.refreshCache().then( () => resourceTree.initialize() );
}
