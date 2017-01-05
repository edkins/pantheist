'use strict';

var editSessions = {};

editSessions._sessions = {};
editSessions._currentUrl = undefined;

editSessions.has = function(url)
{
	return editSessions._sessions[url] != undefined;
};

editSessions.switchTo = function(url)
{
	if (editSessions.has(url))
	{
		editSessions._currentUrl = url;
		editor.setSession(editSessions._sessions[url].session);
		document.getElementById('editor').classList.remove('hidden');
	}
	else
	{
		editSessions._currentUrl = undefined;
		document.getElementById('editor').classList.add('hidden');
	}
};

editSessions.create = function(url,text)
{
	if (typeof url !== 'string')
	{
		throw new Error('create session: url must be string, was ' + url);
	}
	if (typeof text !== 'string')
	{
		throw new Error('create session: text must be string, was ' + text);
	}
	if (editSessions.has(url))
	{
		throw new Error('create session: url already has a session: ' + url);
	}

	editSessions._sessions[url] = {
		session: new ace.EditSession(text)
	};
};

editSessions.textForUrl = function(url)
{
	if (editSessions.has(url))
	{
		return editSessions._sessions[url].session.getValue();
	}
	else
	{
		return undefined;
	}
};

editSessions.scratch = function(text)
{
	if (typeof text !== 'string')
	{
		throw new Error('scratch text must be string');
	}

	editSessions._currentUrl = undefined;
	editor.setSession(new ace.EditSession(text));
	document.getElementById('editor').classList.remove('hidden');
};

/*
ui.setEditorText = function(text)
{
	editor.setValue(''+text);
	editor.selection.clearSelection();
	document.getElementById('editor').classList.remove('hidden');
	document.getElementById('create-page').classList.add('hidden');
}

ui.editorVisible = function()
{
	return document.getElementById('editor').classList.contains('hidden');
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
*/
