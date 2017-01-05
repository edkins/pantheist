'use strict';

var uri = {};


uri.split = function(u)
{
	if (typeof u !== 'string')
	{
		throw new Error('uri must be string');
	}

	// taken from rfc3986, with the slashes escaped.
	var uriRegex = /^(([^:\/?#]+):)?(\/\/([^\/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?/;
	
	var match = u.match(uriRegex);
	
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

uri.build = function(scheme,authority,path)
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

uri.schemeAndAuthority = function(u)
{
	var parts = uri.split(u);
	return uri.build(parts.scheme, parts.authority, undefined);
};

uri.withoutTrailingSlash = function(str)
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

uri.lastSegment = function(u)
{
	u = uri.withoutTrailingSlash(u);
	var parts = uri.split(u);
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

uri.splitTemplate = function(template)
{
	var parts = uri.split(template);
	var segments = parts.path.split('/');
	var result = [];
	var first = true;
	for (var seg of segments)
	{
		if (first && seg === '')
		{
			// ignore empty segment at the beginning
		}
		else if (seg.startsWith('{') && seg.endsWith('}'))
		{
			result.push({literal:false, name:seg.substring(1,seg.length-1)});
		}
		else
		{
			result.push({literal:true, name:seg});
		}
	}
	return result;
};

uri.templateHasParameters = function(template)
{
	var segments = uri.splitTemplate(template);
	for (var seg of segments)
	{
		if (!seg.literal)
		{
			return true;
		}
	}
	return false;
};
