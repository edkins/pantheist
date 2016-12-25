'use strict';

var Restless = {};

///////////////////////
//
// Public Interfaces:
//
// node
//      uri                     URI that this node represents
//      schema                  type of data this node is expected to contain
//      get()                   fetch data for this node
//      put(data)               put data for this node
// Restless
//      initialize(rootUrl)     set up the system targeting the given URL
//      node(uri)               return a node corresponding to this URI, or undefined
//      home                    returns home node
//
// Internal Interfaces:
//
// service
//      homeUri                 [optional] returns home URI
//      matchUri                service applies to URIs starting with this string
//      appliesTo(uri)          return whether the service applies to this URI
//      get(uri)                fetch data for this URI
//      put(uri,data)           put data for this URI
// TranslationStore
//      translate(uri)          returns translated URI
//      relevantRules(uri,pres) returns collection of mappings relevant to URI, presented as 'sortBySequence', 'sortByName' or 'mapByName'
//      addRule(name,matchUri,mapsToUri,matchDescendants)   tries to add a rule. Returns undefined if successful, an error message on failure.
//      putRules(rules)         accepts a list, returns a promise
// translationRule
//      name
//      owner
//      matchUri
//      mapsToUri
//      appliesTo(uri)          return whether this rule directly applies to the given URI
//      relevantTo(uri)         return whether this rule applies to the given URI or to one of its descendants.
//      translate(uri)          translate the given uri, if possible.
//      conflictsWith(name,matchUri)    returns true if adding another rule with the given name or matchUri would cause a conflict
//
// Schema: most fields are optional and may be missing.
//
// {
//    "clientUri": "client:{path}"             untranslated client URI that can be used in a call to Restless.node(uri)
//    "serverUrl": "http://{host}/{path}"      URL to access the corresponding resource on the server
//    "childResources": [                      A list of known child resources
//        {
//           "sequence": 0,                    the place in the list where this appeared
//           "name": "{name}",                 identifier which must be unique, nonempty and not contain slashes
//           "clientUri": "client:{path+name}" uri to locate it under, should equal the parent clientUri together with the "name" field
//        }
//    ],
//    "translationRules: [                     a list of relevant translation rules.
//        {
//           "sequence": 0,                    the place in the list where this appeared. Higher numbers will be applied in preference.
//           "name": "{name}",                 a unique name for this rule
//           "owner": "client:{path}",         which resource owns this rule. This is where you must issue patch requests if you want the rule changed or deleted.
//           "matchUri": "client:{prefix}",    uri prefix to match
//           "matchDescendants": true,         whether descendants are matched also
//           "mapsToUri": "{sc}:{prefix}",     uri prefix to map it to.
//           "applied": true                   whether this rule is directly applied to the resource in question
//        }
//    ]
// }
//
///////////////////////

///////////////////////
//
// Private: State
//
///////////////////////

Restless._knownNodes = {};
Restless._mainService;

///////////////////////
//
// Private: Helper Functions
//
///////////////////////

Restless._initializationCheck = function()
{
	if (Restless._mainService === undefined)
	{
		throw 'Not initialized';
	}
};

// May return:
//   undefined
//   number
//   empty string
//   string
//   function
//   boolean
//   null
//   array
//   object
Restless._describe = function(thing)
{
	if (thing === null)
	{
		return 'null';
	}
	else if (thing === '')
	{
		return 'empty string';
	}
	else if (Array.isArray(thing))
	{
		return 'array';
	}
	else
	{
		return typeof thing;
	}
};

Restless._nonEmptyString = function(str,name)
{
	if (typeof name !== 'string' || name === '')
	{
		throw '_nonEmptyString.name: expecting nonempty string, was ' + Restless._describe(name);
	}

	if (typeof str !== 'string' || str === '')
	{
		throw name + ': expecting nonempty string, was ' + Restless._describe(str);
	}
};

Restless._isString = function(str,name)
{
	Restless._nonEmptyString(name,'_isString.name');

	if (typeof str !== 'string')
	{
		throw name + ': expecting string, was ' + Restless._describe(str);
	}
};

Restless._stringOrUndefined = function(str,name)
{
	Restless._nonEmptyString(name,'_stringOrUndefined.name');

	if (str !== undefined && typeof str !== 'string')
	{
		throw name + ': expecting string or undefined, was ' + Restless._describe(str);
	}
};

Restless._isArrayOf = function(array,name,valueChecker)
{
	Restless._nonEmptyString(name,'_isArrayOf.name');
	Restless._isFunction(valueChecker,'_isArrayOf.valueChecker');

	if (!Array.isArray(array))
	{
		throw name + ': expecting array, was ' + Restless._describe(array);
	}
	for (var i = 0; i < array.length; i++)
	{
		valueChecker(array[i], name + '[' + i + ']');
	}
};

Restless._isMap = function(map,name)
{
	Restless._nonEmptyString(name,'_isMap.name');

	if (typeof map !== 'object' || Array.isArray(map))
	{
		throw name + ': expecting map, was ' + Restless._describe(map);
	}
};

Restless._isMapOf = function(map,name,valueChecker)
{
	Restless._nonEmptyString(name,'_isMapOf.name');
	Restless._isFunction(valueChecker,'_isMapOf.valueChecker');

	Restless._isMap(map,name);
	for (var key in map)
	{
		Restless._nonEmptyString(key, name + ' keys');
		valueChecker(map[key], name + '.' + key);
	}
};

Restless._isFunction = function(fn,name)
{
	Restless._nonEmptyString(name,'_isFunction.name');

	if (typeof fn !== 'function')
	{
		throw name + ': expection function, was ' + Restless._describe(fn);
	}
};

Restless._isBoolean = function(bool,name)
{
	Restless._nonEmptyString(name,'_isBoolean.name');

	if (typeof bool !== 'boolean')
	{
		throw name + ': expecting boolean, was ' + Restless._describe(bool);
	}
};

Restless._isObject = function(obj,name)
{
	Restless._nonEmptyString(name,'_isObject.name');

	if (typeof obj !== 'object')
	{
		throw name + ': expecting ' + type + ', was ' + Restless._describe(obj);
	}
};

Restless._hasField = function(obj,name,type,field,checker)
{
	Restless._nonEmptyString(name,'_hasField.name');
	Restless._nonEmptyString(field,'_hasField.field');
	Restless._nonEmptyString(type,'_hasField.type');
	Restless._isFunction(checker,'_hasField.checker');

	Restless._isObject(obj,name);
	if (!(field in obj))
	{
		throw name + ': expecting ' + type + ', was missing ' + field;
	}
	checker(obj[field], type + ' ' + name + '.' + field);
};

Restless._isService = function(service,name)
{
	Restless._nonEmptyString(name,'_isService.name');

	Restless._hasField(service,name,'service','homeUri',Restless._stringOrUndefined);
	Restless._hasField(service,name,'service','matchUri',Restless._nonEmptyString);
	Restless._hasField(service,name,'service','appliesTo',Restless._isFunction);
	Restless._hasField(service,name,'service','get',Restless._isFunction);
	Restless._hasField(service,name,'service','put',Restless._isFunction);
};

Restless._isEqualTo = function(value,name,expected)
{
	Restless._nonEmptyString(name,'_isEqualTo.name');
	
	if (value !== expected)
	{
		throw name + ': expecting ' + expected + ', was ' + value;
	}
};

Restless._noFieldsExcept = function(obj,name,fields)
{
	Restless._nonEmptyString(name,'_noFieldsExcept.name');
	Restless._isArrayOf(fields,'_noFieldsExcept.fields', Restless._nonEmptyString);
	
	for (var field in obj)
	{
		if (fields.indexOf(field) === -1)
		{
			throw name + ': unexpected field ' + field;
		}
	}
};

Restless._selectFields = function(inputObj,fields,sequence,functions)
{
	var result = {};
	for (var i = 0; i < fields.length; i++)
	{
		var field = fields[i];
		if (field === 'sequence' && sequence !== undefined)
		{
			result[field] = sequence;
		}
		else if (field in functions)
		{
			result[field] = functions[field](inputObj);
		}
		else
		{
			result[field] = inputObj[field];
		}
	}
	return result;
};

Restless._presentList = function(inputList,presentation,fields,functions)
{
	Restless._isArrayOf(inputList,'inputList', Restless._isObject);
	Restless._nonEmptyString(presentation,'presentation');
	Restless._isArrayOf(fields,'fields', Restless._nonEmptyString);
	Restless._isMapOf(functions,'functions', Restless._isFunction);

	var map = {};
	var list = [];
	
	switch(presentation)
	{
	case 'sortBySequence':
		for (var i = 0; i < inputList.length; i++)
		{
			list.push(Restless._selectFields(inputList[i],fields,i,functions));
		}
		return list;
	case 'sortByName':
		for (var i = 0; i < inputList.length; i++)
		{
			list.push(Restless._selectFields(inputList[i],fields,i,functions));
		}
		list.sort( function(x,y)
			{
				return x.name.localeCompare(y.name);
			}
		);
		return list;
	case 'mapByName':
		for (var i = 0; i < inputList.length; i++)
		{
			map[inputList[i].name] = Restless._selectFields(inputList[i],fields,i,functions);
		}
		return map;
	default:
		throw 'Unknown presentation: ' + presentation;
	}
};

Restless._justScheme = function(uri)
{
	Restless._nonEmptyString(uri,'uri');
	var i = uri.indexOf(':');
	return (i !== -1 && i === uri.length - 1);
}

Restless._uriTidy = function(uri)
{
	Restless._nonEmptyString(uri,'uri');
	if (!Restless._justScheme(uri) && !uri.endsWith('/'))
	{
		return uri + '/';
	}
	return uri;
}

Restless._uriPrefix = function(uri,prefix)
{
	return Restless._uriTidy(uri).startsWith(Restless._uriTidy(prefix));
};

Restless._uriEqual = function(uri,uri2)
{
	return Restless._uriTidy(uri) === Restless._uriTidy(uri2);
};

///////////////////////
//
// Helper functions that return promises
//
///////////////////////

Restless._splitByField = function(data,name,catchAll,fieldTargets)
{
	Restless._nonEmptyString(name,'name');
	Restless._stringOrUndefined(catchAll,'catchAll');
	Restless._isMapOf(fieldTargets, 'fieldTargets', Restless._nonEmptyString);

	if (typeof(data) !== 'object')
	{
		return Promise.reject(context + ' expected object, got a ' + Restless._describe(data));
	}

	var result = {};
	for (field in fieldTargets)
	{
		var category = fieldTargets[field];
		result[category] = {};
	}
	
	if (catchAll !== undefined)
	{
		result[catchAll] = {};
	}
	
	for (var field in data)
	{
		var category;
		if (field in fieldTargets)
		{
			category = fieldTargets[field];
		}
		else if (catchAll !== undefined)
		{
			category = catchAll;
		}
		else
		{
			return Promise.reject(context + ' contains unexpected field: ' + field);
		}
		result[category][field] = data[field];
	}
	return Promise.resolve(result);
}

///////////////////////
//
// HttpService
//
///////////////////////

Restless.HttpService = function()
{
	this.homeUri = undefined;
	this.matchUri = 'http://';
};

Restless.HttpService.prototype.appliesTo = function(uri)
{
	Restless._isString(uri,'uri');
	return Restless._uriPrefix(uri, this.matchUri);
}

Restless.HttpService.prototype.get = function(url)
{
	if (!Restless._uriPrefix(url,this.matchUri))
	{
		return Promise.reject('HttpService only serves things starting with ' + this.matchUri);
	}
	
	return new Promise(
		function(resolve,reject)
		{
			var xmlhttp = new XmlHttpRequest();
			xmlhttp.open('GET', url, true);
			xmlhttp.onload = function()
				{
					resolve(xmlhttp.responseText);
				};
			xmlhttp.onerror = function()
				{
					reject(null);
				};
			xmlhttp.send();
		}
	);
};

Restless.HttpService.prototype.put = function(url,data)
{
	return Promise.reject('Put currently disabled for http');
};

///////////////////////
//
// Node
//
///////////////////////

Restless.Node = function(service,uri)
{
	this.service = service;
	this.uri = uri;
};

Restless.Node.prototype.get = function()
{
	return this.service.get(this.uri);
};

Restless.Node.prototype.put = function(data)
{
	return this.service.put(this.uri,data);
};

///////////////////////
//
// ErrorNode
//
///////////////////////

Restless.ErrorNode = function(errorMsg)
{
	this.errorMsg = errorMsg;
	this.uri = undefined;
	this.schema = undefined;
};

Restless.ErrorNode.prototype.get = function()
{
	return Promise.reject(this.errorMsg);
};

Restless.ErrorNode.prototype.put = function(data)
{
	return Promise.reject(this.errorMsg);
};

///////////////////////
//
// TranslationStore
//
///////////////////////

Restless.TranslationStore = function(fromPrefix)
{
	this._mappings = [];
	this._fromPrefixes = [];
	this._toPrefixes = [];
	this.matchUri = 'internal:translation-store/';
	this.homeUri = 'internal:translation-store/';
};

Restless.TranslationStore.prototype.appliesTo = function(uri)
{
	Restless._isString(uri,'uri');
	return Restless._uriPrefix(uri, this.matchUri);
}

Restless.TranslationStore.prototype.addFromPrefix = function(uri)
{
	Restless._nonEmptyString(uri,'uri');
	this._fromPrefixes.push(uri);
}

Restless.TranslationStore.prototype.addToPrefix = function(uri)
{
	Restless._nonEmptyString(uri,'uri');
	this._toPrefixes.push(uri);
}

Restless.TranslationStore.prototype._findAppliedRule = function(uri)
{
	Restless._nonEmptyString(uri,'uri');
	
	// The last one in the list that matches is the one that applies.
	// So traverse the list backwards.
	for (var i = this._mappings.length-1; i >= 0; i--)
	{
		if (this._mappings[i].appliesTo(uri))
		{
			return this._mappings[i];
		}
	}
	return undefined;
};

Restless.TranslationStore.prototype.translate = function(uri)
{
	var rule = this._findAppliedRule(uri);
	if (rule !== undefined)
	{
		return rule.translate(uri);
	}
	return undefined;
};

Restless.TranslationStore.prototype.relevantRules = function(uri,presentation)
{
	Restless._nonEmptyString(uri,'uri');
	Restless._nonEmptyString(presentation,'presentation');
	
	var appliedRule = this._findAppliedRule(uri);
	var filteredList = this._mappings.filter(function(m)
		{
			return m.relevantTo(uri);
		});
	return Restless._presentList(
		filteredList,
		presentation,
		['sequence','name','owner','matchUri','mapsToUri','matchDescendants','applied'],
		{
			applied: function(r)
				{
					return r === appliedRule;
				}
		});
};

Restless.TranslationStore.prototype._canAcceptMatchUri = function(matchUri)
{
	Restless._nonEmptyString(matchUri,'matchUri');
	for (var i = 0; i < this._fromPrefixes.length; i++)
	{
		if (Restless._uriPrefix(matchUri, this._fromPrefixes[i]))
		{
			return true;
		}
	}
	return false;
};

Restless.TranslationStore.prototype._canAcceptMapsToUri = function(mapsToUri)
{
	Restless._nonEmptyString(mapsToUri,'mapsToUri');
	for (var i = 0; i < this._toPrefixes.length; i++)
	{
		if (Restless._uriPrefix(mapsToUri, this._toPrefixes[i]))
		{
			return true;
		}
	}
	return false;
};

Restless.TranslationStore.prototype.addRule = function(name,matchUri,mapsToUri,matchDescendants)
{
	Restless._nonEmptyString(name,'name');
	Restless._nonEmptyString(matchUri,'matchUri');
	Restless._nonEmptyString(mapsToUri,'mapsToUri');
	Restless._isBoolean(matchDescendants,'matchDescendants');
	
	// Only accept rules that map from a valid namespace to a valid namespace
	if (!this._canAcceptMatchUri(matchUri))
	{
		return "can't accept matchUri";
	}
	if (!this._canAcceptMapsToUri(mapsToUri))
	{
		return "can't accept mapsToUri";
	}
	for (var i = 0; i < this._mappings.length; i++)
	{
		if (this._mappings[i].conflictsWith(name,matchUri))
		{
			return "conflicts with previous mapping";
		}
	}
	this._mappings.push( new Restless.TranslationRule(name,matchUri,mapsToUri,matchDescendants) );
	return undefined;
};

Restless.TranslationStore.prototype.putRules = function(rules)
{
	if (!Array.isArray(rules))
	{
		return Promise.reject('rules must be array');
	}
	
	// Try adding rules to a new translation store. If any of them fail then we won't clobber our own store.
	var experimentalStore = new Restless.TranslationStore();
	experimentalStore._fromPrefixes = Array.from(this._fromPrefixes);
	experimentalStore._toPrefixes = Array.from(this._toPrefixes);

	for (var i = 0; i < rules.length; i++)
	{
		var rule = rules[i];
		var err = experimentalStore.addRule(rule.name, rule.matchUri, rule.mapsToUri, rule.matchDescendants);
		if (err !== undefined)
		{
			return Promise.reject('Failed to add rule ' + rule.name + ': ' + err);
		}
	}
	
	this._mappings = experimentalStore._mappings;
	return Promise.resolve(undefined);
};

///////////////////////
//
// Private: TranslationLayer
//
///////////////////////

Restless.TranslationLayer = function(translationStore,underlyingServices)
{
	Restless._isObject(translationStore,'translationStore');
	Restless._isArrayOf(underlyingServices,'underlyingServices',Restless._isService);
	this._translationStore = translationStore;
	this._underlyingServices = underlyingServices;
	this.matchUri = 'client:';
	this.homeUri = 'client:';
};

Restless.TranslationLayer.prototype.appliesTo = function(uri)
{
	Restless._isString(uri,'uri');
	return Restless._uriPrefix(uri, this.matchUri);
}

Restless.TranslationLayer.prototype._findService = function(uri)
{
	for (var i = 0; i < this._underlyingServices.length; i++)
	{
		if (Restless._uriPrefix(uri, this._underlyingServices[i].matchUri))
		{
			return this._underlyingServices[i];
		}
	}
	return undefined;
};

Restless.TranslationLayer.prototype.addPrefixesToStore = function()
{
	this._translationStore.addFromPrefix(this.matchUri);
	for (var i = 0; i < this._underlyingServices.length; i++)
	{
		this._translationStore.addToPrefix(this._underlyingServices[i].matchUri);
	}
};

Restless.TranslationLayer.prototype.get = function(uri)
{
	if (!this.appliesTo(uri))
	{
		return Promise.reject('Not in translation space');
	}

	var translationRules = this._translationStore.relevantRules(uri,'sortBySequence');
	return Promise.resolve({
		clientUri: uri,
		translationRules: translationRules
	});
};

Restless.TranslationLayer.prototype.put = function(uri,data)
{
	Restless._nonEmptyString(uri,'uri');
	var translationStore = this._translationStore;

	if (!this.appliesTo(uri))
	{
		return Promise.reject('Not in translation space');
	}
	
	return Restless._splitByField(data,'data',undefined,{
		clientUri: 'check',
		translationRules: 'translation'
	}).then( categorized => {
		if ( categorized.check.clientUri !== uri )
		{
			return Promise.reject('clientUri is incorrect');
		}
		return translationStore.putRules( categorized.translation.translationRules );
	});
};

// Disabled for now: this would actually get the data from the underlying service
Restless.TranslationLayer.prototype.getzzz = function(uri)
{
	var translatedUri = this._translationStore.translate(uri);
	if (translatedUri === undefined)
	{
		return Promise.reject('No translation rule for this uri');
	}

	var service = this._findService(translatedUri);
	if (service === undefined)
	{
		return Promise.reject('No service to handle translated uri ' + translatedUri);
	}
	
	return service.get(uri);
};

///////////////////////
//
// TranslationRule
//
///////////////////////

Restless.TranslationRule = function(name,matchUri,mapsToUri,matchDescendants)
{
	Restless._nonEmptyString(name,'name');
	Restless._nonEmptyString(matchUri,'matchUri');
	Restless._nonEmptyString(mapsToUri,'mapsToUri');
	Restless._isBoolean(matchDescendants,'matchDescendants');
	this.name = name;
	this.owner = matchUri;
	this.matchUri = matchUri;
	this.mapsToUri = mapsToUri;
	this.matchDescendants = matchDescendants;
};

Restless.TranslationRule.prototype.appliesTo = function(uri)
{
	Restless._nonEmptyString(uri,'uri');
	if (this.matchDescendants)
	{
		return Restless._uriPrefix(uri, this.matchUri);
	}
	else
	{
		return Restless._uriEqual(uri, this.matchUri);
	}
};

Restless.TranslationRule.prototype.relevantTo = function(uri)
{
	Restless._nonEmptyString(uri,'uri');
	return this.appliesTo(uri) || Restless._uriPrefix(this.matchUri, uri);
};

Restless.TranslationRule.prototype.translate = function(uri)
{
	Restless._nonEmptyString(uri,'uri');
	if (this.appliesTo(uri))
	{
		return this.mapsToUri + uri.substring(this.matchUri.length);
	}
	return undefined;
};

Restless.TranslationRule.prototype.conflictsWith = function(name,matchUri)
{
	return this.name === name || this.matchUri === matchUri;
};

/////////////
//
// Public
//
/////////////

Object.defineProperty(Restless, 'home', {
	get: function()
	{
		Restless._initializationCheck();
		return Restless.node( Restless._mainService.homeUri );
	}
});

Restless.node = function(uri)
{
	Restless._initializationCheck();
	if (uri in Restless._knownNodes)
	{
		return Restless._knownNodes[uri];
	}
	else if (Restless._mainService.appliesTo(uri))
	{
		var node = new Restless.Node(Restless._mainService,uri);
		Restless._knownNodes[uri] = node;
		return node;
	}
	else
	{
		return new Restless.ErrorNode('Service name not recognized');
	}
};

Restless.initialize = function(rootUrl)
{
	Restless._knownNodes = {};
	var httpService = new Restless.HttpService(rootUrl);
	var translationStore = new Restless.TranslationStore();
	var translationLayer = new Restless.TranslationLayer(translationStore, [httpService]);

	translationLayer.addPrefixesToStore();

	Restless._isService(translationLayer,'mainService');

	Restless._mainService = translationLayer;
};
