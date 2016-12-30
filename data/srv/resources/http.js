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

http.withoutTrailingSlash = function(str)
{
	if (str.endsWith('/'))
	{
		return str.substring(0,str.length-1);
	}
	else
	{
		return str;
	}
};

http.lastSegment = function(uri)
{
	uri = http.withoutTrailingSlash(uri);
	var parts = http.splitUri(uri);
	if (parts.path === undefined)
	{
		return '';
	}
	var i = parts.path.lastIndexOf('/');
	if (i == -1)
	{
		return parts.path;
	}
	else
	{
		return parts.path.substring(i + 1);
	}
};

http.home = http.schemeAndAuthority('' + window.location);

http.getString = function(acceptContentType,url)
{
	return new Promise(
		function(resolve,reject)
		{
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.open('GET', url, true);
			xmlhttp.setRequestHeader('Accept', acceptContentType);
			xmlhttp.onload = function()
				{
					if (xmlhttp.status == 200 && typeof xmlhttp.responseText === 'string')
					{
						resolve(xmlhttp.responseText);
					}
					else
					{
						reject(xmlhttp.status + ' ' + xmlhttp.statusText + ':' + xmlhttp.responseText.substring(0,30));
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

http.getJson = function(url)
{
	return http.getString('application/json', url).then(
		text => {
			try
			{
				var obj = JSON.parse(text)
				return Promise.resolve(obj);
			}
			catch(e)
			{
				return Promise.reject(e);
			}
		}
	);
}

http.putString = function(url,contentType,text)
{
	return new Promise(
		function(resolve,reject)
		{
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.open('PUT', url, true);
			xmlhttp.setRequestHeader('Content-Type', contentType);
			xmlhttp.onload = function()
				{
					if (xmlhttp.status == 204)
					{
						resolve(undefined);
					}
					else
					{
						reject(xmlhttp.status + ' ' + xmlhttp.statusText + ':' + xmlhttp.responseText.substring(0,30));
					}
				};
			xmlhttp.onerror = function()
				{
					reject(xmlhttp.status + ' ' + xmlhttp.statusText);
				};
			xmlhttp.send(text);
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
						reject(xmlhttp.status + ' ' + xmlhttp.statusText + ':' + xmlhttp.responseText.substring(0,30));
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

http.del = function(url)
{
	return new Promise(
		function(resolve,reject)
		{
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.open('DELETE', url, true);
			xmlhttp.onload = function()
				{
					if (xmlhttp.status == 204)
					{
						resolve(undefined);
					}
					else
					{
						reject(xmlhttp.status + ' ' + xmlhttp.statusText + ':' + xmlhttp.responseText.substring(0,30));
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
