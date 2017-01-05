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

fileTabs.has = function(url)
{
	return fileTabs._find(url) != undefined;
}

Object.defineProperty(fileTabs, 'activeFile', {
	get: function()
	{
		var file = fileTabs._find(fileTabs._activeUrl);
		return file;
	}
});

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

fileTabs.open = function(url, kindUrl, dataUrl, mimeType, elementToFlash)
{
	if (url == undefined || kindUrl == undefined)
	{
		// do nothing: bad url
		console.error('opening invalid values: ' + url + ' ' + kindUrl);
		ui.flashClass(elementToFlash, 'client-error');
		return;
	}

	var file = fileTabs._find(url);
	if (file != undefined)
	{
		// do nothing: already open.
		console.error('already open: ' + url);
		ui.flashClass(elementToFlash, 'client-error');
		return;
	}

	var newFile = {
		url: url,
		domElement: document.createElement('li'),
		kindUrl: kindUrl,
		dataUrl: dataUrl,
		mimeType: mimeType,
		method: 'put'
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
};

fileTabs._nextUntitledPseudoUrl = function()
{
	for (var i = 0;; i++)
	{
		var name = 'about:untitled/' + i;
		if (fileTabs._find(name) == undefined)
		{
			return name;
		}
	}
};

fileTabs.openNew = function(kindUrl, displayName, dataUrl, mimeType, method)
{
	var newFile = {
		url: fileTabs._nextUntitledPseudoUrl(),
		domElement: document.createElement('li'),
		kindUrl: kindUrl,
		dataUrl: dataUrl,
		mimeType: mimeType,
		method: method
	};
	
	newFile.domElement.dataset.url = newFile.url;
	newFile.domElement.textContent = displayName;
	newFile.domElement.onclick = fileTabs._onclickTab;
	newFile.domElement.classList.add('untitled');
	
	fileTabs._panel.append(newFile.domElement);
	fileTabs._openFiles.push(newFile);
	
	return newFile.url;
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
	var url = event.currentTarget.dataset.url;
	if (event.ctrlKey)
	{
		var file = fileTabs._find(url);
		if (file === undefined)
		{
			file = {url: url};
		}
		else
		{
			ui.visitScratch(JSON.stringify(file, null, '    '));
		}
	}
	else
	{
		ui.switchTo(url);
	}
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

fileTabs._mustKeep = function(file)
{
	return file.url === 'about:create';
};

fileTabs.closeAll = function(url)
{
	var remainingFiles = [];
	for (var file of fileTabs._openFiles)
	{
		if (fileTabs._mustKeep(file))
		{
			remainingFiles.push(file);
		}
		else
		{
			fileTabs._panel.removeChild(file.domElement);
			if (file.url === fileTabs._activeUrl)
			{
				fileTabs._activeUrl = undefined;
			}
		}
	}
	fileTabs._openFiles = remainingFiles;
};
