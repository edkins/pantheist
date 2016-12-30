'use strict';

///////////////////////
//
// http
//
///////////////////////

var http = {};

http.home = "/";

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
