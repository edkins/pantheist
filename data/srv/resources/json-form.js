'use strict';

var jsonForm = {};

jsonForm._sessions = {};
jsonForm._schemas = {};
jsonForm._schemaHints = {};
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

jsonForm._loadSchema = function(schemaUrl)
{
	if (schemaUrl in jsonForm._schemas)
	{
		return Promise.resolve(undefined);
	}
	else
	{
		return http.getString('application/schema+json', schemaUrl).then(
			schemaText => {
				var schema = JSON.parse(schemaText);
				jsonForm._schemas[schemaUrl] = schema;
			}
		);
	}
};

jsonForm._loadSchemaHint = function(schemaHintUrl)
{
	if (schemaHintUrl == undefined || schemaHintUrl in jsonForm._schemaHints)
	{
		return Promise.resolve(undefined);
	}
	else
	{
		return http.getString('text/plain', schemaHintUrl).then(
			schemaHintText => {
				var schemaHint = JSON.parse(schemaHintText);
				jsonForm._schemaHints[schemaHintUrl] = schemaHint;
			}
		);
	}
};

jsonForm.create = function(url, schemaUrl, schemaHintUrl, elementToFlash, flashOnSuccess)
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
	
	return jsonForm._loadSchema(schemaUrl)
		.then( () => jsonForm._loadSchemaHint(schemaHintUrl) )
		.then( () => {
			jsonForm._sessions[url] = {
					schemaUrl: schemaUrl,
					schemaHintUrl: schemaHintUrl,
					value: {},
					active: true
				};
			if (flashOnSuccess)
			{
				ui.flashClass(elementToFlash, 'flash-activate');
			}
		})
		.catch( error =>
			{
				console.error('Fetching json schema: ' + error);
				ui.flashClass(elementToFlash, 'flash-server-error');
			}
		);
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
	var schemaHintUrl = jsonForm._sessions[url].schemaHintUrl;
	
	if (jsonForm._schemas[schemaUrl] == undefined)
	{
		throw new Error('jsonForm unknown schema: ' + schemaUrl);
	}
	
	var panel = document.getElementById('json-form');
	ui.removeChildren(panel);
	
	jsonForm._sessions[url].active = true;
	jsonForm._currentUrl = url;

	jsonForm._layout(
		panel,
		jsonForm._schemas[schemaUrl],
		jsonForm._schemaHints[schemaHintUrl] || {} );
};

jsonForm._layout = function(panel, schema, hint)
{
	var el;
	
	el = document.createElement('h2');
	el.textContent = schema.title || uri.lastSegment(schema.id);
	panel.append(el);
	
	var properties = [];
	if ('/properties' in hint && 'order' in hint['/properties'])
	{
		for (var p of hint['/properties'].order)
		{
			if (properties.indexOf(p) === -1)
			{
				properties.push(p);
			}
		}
	}
	
	if (schema.properties != undefined)
	{
		var moreProps = Array.from( Object.keys( schema.properties ) );
		moreProps.sort();
		for (var p of moreProps)
		{
			if (properties.indexOf(p) === -1)
			{
				properties.push(p);
			}
		}
	}
	for (var key of properties)
	{
		el = document.createElement('span');
		el.textContent = key;
		el.classList.add('form-key');
		panel.append(el);
		
		el = jsonForm._formControl(schema.properties[key]);
		panel.append(el);
		
		panel.append(document.createElement('br'));
	}
};

jsonForm._formControl = function(subschema)
{
	var el;
	var el2;
	if (subschema.enum != undefined)
	{
		el = jsonForm._dropdown(subschema.enum.map(x => JSON.stringify(x)));
	}
	else if (subschema.type === 'string' || subschema.type === 'integer' || subschema.type === 'number')
	{
		el = document.createElement('input');
		el.type = 'text';
		if (subschema.format == undefined)
		{
			el.placeholder = subschema.type;
		}
		else
		{
			el.placeholder = '' + subschema.format;
		}
	}
	else if (subschema.type === 'boolean')
	{
		el = jsonForm._dropdown(['true','false','unspecified']);
	}
	else if (typeof subschema.type === 'string')
	{
		el = jsonForm._dropdown([subschema.type, 'unspecified']);
	}
	else if (subschema.type != undefined)
	{
		el = JSON.stringify(subschema.type);
	}
	else
	{
		el = 'any';
	}
	return el;
};

jsonForm._dropdown = function(options)
{
	var bar = document.createElement('span');
	bar.classList.add('button-bar');

	for (var option of options)
	{
		var el = document.createElement('span');
		el.classList.add('tab');
		el.textContent = option;
		bar.append(el);
	}

	return bar;
};
