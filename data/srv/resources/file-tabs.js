'use strict';

var fileTabs = {};

fileTabs._openFiles = [];
fileTabs._activeUrl = undefined;

fileTabs._find = function(url)
{
	if (url == undefined)
	{
		return undefined;
	}
	for (var file of fileTabs._openFiles)
	{
		if (file.url === url)
		{
			return file;
		}
	}
	return undefined;
}

Object.defineProperty(fileTabs, '_panel', {
	get: function()
	{
		return document.getElementById('open-file-list');
	}
});

fileTabs.hasUrlOpen = function(url)
{
	return fileTabs._find(url) !== undefined;
};

fileTabs.openIfNotAlready = function(url)
{
	if (url == undefined)
	{
		// do nothing: bad url
		return 'failed';
	}

	var file = fileTabs._find(url);
	if (file != undefined)
	{
		// do nothing: already open.
		return 'already';
	}

	var newFile = {
		url: url,
		domElement: document.createElement('li')
	};
	
	newFile.domElement.textContent = uri.lastSegment(url);
	newFile.domElement.dataset.url = url;
	newFile.domElement.onclick = fileTabs._onclickTab;
	
	fileTabs._panel.append(newFile.domElement);
	fileTabs._openFiles.push(newFile);
	
	if (fileTabs._activeUrl === url)
	{
		// corner case. If the active url was incorrectly set to a tab which
		// isn't open, we don't want to think that we've already switched to it.
		fileTabs._activeUrl = undefined;
	}
	
	return 'success';
};

fileTabs.switchTo = function(url)
{
	if (fileTabs._activeUrl === url)
	{
		// do nothing: already active.
		return;
	}

	var activeFile = fileTabs._find(fileTabs._activeUrl);
	if (activeFile !== undefined)
	{
		activeFile.domElement.classList.remove('active');
	}
	
	var file = fileTabs._find(url);
	if (file == undefined)
	{
		// url is not open so switch to undefined and do not highlight anything
		fileTabs._activeUrl = undefined;
		return;
	}

	file.domElement.classList.add('active');
	fileTabs._activeUrl = url;
}

fileTabs._onclickTab = function(event)
{
	ui.visit(event.target.dataset.url);
};

fileTabs.createCreateTab = function()
{
	var url = 'about:create';
	var newFile = {
		url: url,
		domElement: document.createElement('li')
	};
	
	newFile.domElement.textContent = 'Create...';
	newFile.domElement.dataset.url = url;
	newFile.domElement.onclick = fileTabs._onclickTab;

	fileTabs._panel.append(newFile.domElement);
	fileTabs._openFiles.push(newFile);
}
