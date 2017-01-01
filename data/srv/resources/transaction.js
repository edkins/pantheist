var ui = {};
ui.expandedNodes = {};
ui.tab = undefined;

var Transaction = function()
{
	this.url = document.getElementById('address-bar').value;
	if (typeof this.url !== 'string')
	{
		throw new Error('url is not a string?');
	}
	this.data = undefined;
	this.expandedNodes = ui.expandedNodes;
	this.tab = ui.tab;
	this.error = undefined;
	this.kindUrlPresentation = {};
};

Transaction.fetch = function()
{
	var t = new Transaction();
	return t.fetch().then( x => t );
};

Transaction.prototype._absorbKindInfo = function(kindInfo)
{
	for (var kind of kindInfo.childResources)
	{
		if (kind.instancePresentation != undefined)
		{
			this.kindUrlPresentation[kind.url] = kind.instancePresentation;
		}
	}
}

Transaction.prototype.getKindIcon = function(kindUrl, expanded)
{
	var result = undefined;
	if (kindUrl !== undefined && this.kindUrlPresentation !== undefined && this.kindUrlPresentation[kindUrl] != undefined)
	{
		if (expanded && this.kindUrlPresentation[kindUrl].openIconUrl != undefined)
		{
			return this.kindUrlPresentation[kindUrl].openIconUrl;
		}
		if (this.kindUrlPresentation[kindUrl].iconUrl != undefined)
		{
			return this.kindUrlPresentation[kindUrl].iconUrl;
		}
	}
	
	return '/resources/images/red-ball.png';
}

Transaction.prototype.fetch = function()
{
	if (this._fetchedData === undefined)
	{
		return http.getJson(http.home + '/kind').then( kindInfo => {
			this._absorbKindInfo(kindInfo);
		
			return http.getJson(this.url).then(
				data => {
					this.data = data;
					this.error = undefined;
					return data;
				}
			).catch(
				error => {
					this.data = undefined;
					this.error = error;
					return undefined;
				}
			);
		})
	}
	else
	{
		return Promise.resolve(this.data);
	}
};

Transaction.prototype.getJson = function(url)
{
	if (url === this.url)
	{
		return this.fetch().then( data => {
			if (data === undefined)
			{
				return Promise.reject(this.error);
			}
			return data;
		});
	}
	else
	{
		return http.getJson(url);
	}
};
