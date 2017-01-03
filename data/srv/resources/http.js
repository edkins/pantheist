'use strict';

///////////////////////
//
// http
//
///////////////////////

var http = {};

http.home = uri.schemeAndAuthority('' + window.location);

http._error = function(xmlhttp)
{
	if (xmlhttp.responseText === undefined || xmlhttp.responseText === '')
	{
		if (xmlhttp.status === 0)
		{
			return 'Unable to connect';
		}
		else
		{
			return xmlhttp.status + ' ' + xmlhttp.statusText;
		}
	}
	else
	{
		return xmlhttp.status + ' ' + xmlhttp.statusText + ':' + xmlhttp.responseText.substring(0,30);
	}
}

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
						reject(http._error(xmlhttp));
					}
				};
			xmlhttp.onerror = function()
				{
					reject(http._error(xmlhttp));
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
						reject(http._error(xmlhttp));
					}
				};
			xmlhttp.onerror = function()
				{
					reject(http._error(xmlhttp));
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
						reject(http._error(xmlhttp));
					}
				};
			xmlhttp.onerror = function()
				{
					reject(http._error(xmlhttp));
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
						reject(http._error(xmlhttp));
					}
				};
			xmlhttp.onerror = function()
				{
					reject(http._error(xmlhttp));
				};
			xmlhttp.send();
		}
	);
};
