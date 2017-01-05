'use strict';

var jsonForm = {};

jsonForm._sessions = {};
jsonForm._schemas = {};
jsonForm._currentUrl = undefined;

jsonForm.has = function(url)
{
	return jsonForm._sessions[url] != undefined;
};

jsonForm.hasActive = function(url)
{
	return jsonForm.has(url) &&
		jsonForm._sessions[url].active;
};

jsonForm.closeAll = function()
{
	jsonForm._sessions = {};
	jsonForm._schemas = {};
	jsonForm._currentUrl = undefined;
	document.getElementById('json-form').classList.add('hidden');
};

jsonForm.deactivate = function()
{
	jsonForm._sessions[jsonForm._currentUrl].active = false;
	jsonForm._currentUrl = undefined;
	document.getElementById('json-form').classList.add('hidden');
};

jsonForm.create = function(url, schemaUrl, elementToFlash, flashOnSuccess)
{
	if (typeof url !== 'string')
	{
		console.error('url should be a string, was: ' + url);
		ui.flashClass(elementToFlash, 'flash-client-error');
		return Promise.resolve(undefined);
	}
	if (jsonForm.has(url))
	{
		console.error('jsonForm already has url: ' + url);
		ui.flashClass(elementToFlash, 'flash-client-error');
		return Promise.resolve(undefined);
	}
	if (typeof schemaUrl !== 'string')
	{
		console.error('schemaUrl should be a string, was: ' + schemaUrl);
		ui.flashClass(elementToFlash, 'flash-client-error');
		return Promise.resolve(undefined);
	}
	
	if (schemaUrl in jsonForm._schemas)
	{
		jsonForm._sessions[url] = {
			schemaUrl: schemaUrl,
			value: {},
			active: true
		};
		if (flashOnSuccess)
		{
			ui.flashClass(elementToFlash, 'flash-activate');
		}
		return Promise.resolve(undefined);
	}
	else
	{
		return http.getString('application/schema+json', schemaUrl).then(
			schemaText => {
				var schema = JSON.parse(schemaText);
				jsonForm._schemas[schemaUrl] = {
					schema: schema,
					layout: jsonForm._generateLayout(schema)
				};
				jsonForm._sessions[url] = {
					schemaUrl: schemaUrl,
					value: {},
					active: true
				};
				if (flashOnSuccess)
				{
					ui.flashClass(elementToFlash, 'flash-activate');
				}
			},
			error => {
				console.error('Fetching json schema: ' + error);
				ui.flashClass(elementToFlash, 'flash-server-error');
			}
		);
	}
};

jsonForm._persist = function()
{
	// todo
};

jsonForm.switchTo = function(url)
{
	if (url === undefined)
	{
		document.getElementById('json-form').classList.add('hidden');
		jsonForm._currentUrl = undefined;
		return;
	}
	
	if (!jsonForm.has(url))
	{
		throw new Error('jsonForm unknown url: ' + url);
	}
	
	document.getElementById('json-form').classList.remove('hidden');
	
	var schemaUrl = jsonForm._sessions[url].schemaUrl;
	
	if (jsonForm._schemas[schemaUrl] == undefined)
	{
		throw new Error('jsonForm unknown schema: ' + schemaUrl);
	}
	
	jsonForm._sessions[url].active = true;
	jsonForm._replayLayout( jsonForm._schemas[schemaUrl].layout );
	jsonForm._currentUrl = url;
};

jsonForm._generateLayout = function(schema)
{
	var layout = [];
	
	layout.push( {
		type: 'h2',
		text: schema.title || uri.lastSegment(schema.id)
	});
	
	if (schema.properties != undefined)
	{
		for (var key in schema.properties)
		{
			layout.push( {
				type: 'h4',
				text: key
			} );
			layout.push( {
				type: 'input'
			} );
		}
	}
	return layout;
};

jsonForm._replayLayout = function(layout)
{
	if (layout == undefined)
	{
		throw new Error('layout is undefined');
	}
	var panel = document.getElementById('json-form');
	ui.removeChildren(panel);
	for (var item of layout)
	{
		var element;
		switch(item.type)
		{
		case 'h2':
		case 'h3':
		case 'h4':
		case 'p':
			element = document.createElement(item.type);
			element.textContent = item.text;
			break;
		case 'input':
			element = document.createElement('input');
			element.type = 'text';
			break;
		default:
			throw new Error('Unrecognized form element type: ' + item.type);
		}
		
		panel.append(element);
	}
};
