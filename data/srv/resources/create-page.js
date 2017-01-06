'use strict';

var createPage = {};

createPage.showCreatePage = function()
{
	document.getElementById('create-page').classList.remove('hidden');
	var panel = document.getElementById('create-page-list');
	ui.removeChildren(panel);
	
	var itemList = [];
	
	for (var kindUrl of ui.allKindUrls)
	{
		var kind = ui.getKind(kindUrl);
		if (kind.createAction != undefined)
		{
			var background = document.createElement('div');
			var button = document.createElement('div');
			var icon = document.createElement('span');
			var textSpan = document.createElement('span');
			var iconUrl = ui.getKindIcon(kind, true);
			var displayName = ui.kindDisplayName(kind);
			
			background.classList.add('create-kind-background');
			
			button.classList.add('create-kind');
			button.dataset.kindUrl = kindUrl;
			button.onclick = createPage._onclickCreateItem;
			
			icon.classList.add('create-kind-icon');
			icon.style['background-image'] = "url('" + iconUrl + "')";
			
			textSpan.textContent = displayName;
			textSpan.classList.add('create-kind-name');
			
			button.append(icon);
			button.append(textSpan);
			background.append(button);
			
			itemList.push([displayName.toLowerCase(), background]);
		}
	}

	itemList.sort();
	for (var pair of itemList)
	{
		panel.append(pair[1]);
	}
};

createPage.hideCreatePage = function()
{
	document.getElementById('create-page').classList.add('hidden');
};

createPage._onclickCreateItem = function(event)
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
	
	var dataUrl = undefined;
	if (!uri.templateHasParameters(kind.createAction.urlTemplate))
	{
		dataUrl = kind.createAction.urlTemplate;
	}
	
	ui.openNew(kindUrl, dataUrl, button);
	ui.flashClass(button, 'flash-activate');
};
