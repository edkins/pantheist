'use strict';

///////////////////////
//
// http
//
///////////////////////

var http = {};

http.splitUri = function(uri)
{
	if (typeof uri !== 'string')
	{
		throw new Error('uri must be string');
	}

	// taken from rfc3986, with the slashes escaped.
	var uriRegex = /^(([^:\/?#]+):)?(\/\/([^\/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?/;
	
	var match = uri.match(uriRegex);
	
	if (match === undefined)
	{
		// Not sure it's possible to fail to match, but just in case.
		return {
			scheme: undefined,
			authority: undefined,
			path: undefined,
			query: undefined,
			fragment: undefined
		};
	}
	
	return {
		scheme: match[2].toLowerCase(),
		authority: match[4],
		path: match[5],
		query: match[7],
		fragment: match[9]
	};
};

http.buildUri = function(scheme,authority,path)
{
	var result = '';
	if (scheme !== undefined)
	{
		result += scheme + ':';
	}
	if (authority !== undefined)
	{
		result += '//' + authority;
		if (path !== undefined && path !== '' && !path.startsWith('/'))
		{
			throw new Error('Path must start with slash if authority is present');
		}
	}
	if (path !== undefined)
	{
		result += path;
	}
	return result;
};

http.schemeAndAuthority = function(uri)
{
	var parts = http.splitUri(uri);
	return http.buildUri(parts.scheme, parts.authority, undefined);
};

http.home = http.schemeAndAuthority('' + window.location);

http.getJson = function(url)
{
	return new Promise(
		function(resolve,reject)
		{
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.open('GET', url, true);
			xmlhttp.setRequestHeader('Accept', 'application/json');
			xmlhttp.onload = function()
				{
					if (xmlhttp.status == 200)
					{
						var obj;
						try
						{
							obj = JSON.parse(xmlhttp.responseText)
							resolve(obj);
						}
						catch(e)
						{
							reject(e);
						}
					}
					else
					{
						reject(xmlhttp.status + ' ' + xmlhttp.statusText);
					}
				};
			xmlhttp.onerror = function()
				{
					reject(xmlhttp.status + ' ' + xmlhttp.statusText);
				};
			xmlhttp.send();
		}
	);
};

http.post = function(url,contentType,data)
{
	return new Promise(
		function(resolve,reject)
		{
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.open('POST', url, true);
			if (contentType !== undefined)
			{
				xmlhttp.setRequestHeader('ContentType', contentType);
			}
			xmlhttp.onload = function()
				{
					if (xmlhttp.status == 202 || xmlhttp.status == 204)
					{
						resolve(undefined);
					}
					else
					{
						reject(xmlhttp.status + ' ' + xmlhttp.statusText);
					}
				};
			xmlhttp.onerror = function()
				{
					reject(xmlhttp.status + ' ' + xmlhttp.statusText);
				};
			xmlhttp.send(data);
		}
	);
};
