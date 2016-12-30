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
};

Transaction.fetch = function()
{
	var t = new Transaction();
	return t.fetch().then( x => t );
};

Transaction.prototype.fetch = function()
{
	if (this._fetchedData === undefined)
	{
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
		return this.fetch();
	}
	else
	{
		return http.getJson(url);
	}
};
