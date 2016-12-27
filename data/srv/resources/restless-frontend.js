'use strict';

var Restless = Restless || {};

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
//      addRule(matchUri,mapsToUri)   tries to add a rule. Returns undefined if successful, an error message on failure.
//      putRules(rules)         accepts a list, returns a promise
// translationRule
//      name
//      owner
//      matchUri
//      mapsToUri
//      appliesTo(uri)          return whether this rule directly applies to the given URI
//      relevantTo(uri)         return whether this rule applies to the given URI or to one of its descendants.
//      translate(uri)          translate the given uri, if possible.
//
// Schema: most fields are optional and may be missing.
//
// {
//    "clientUri": "restless:path"             untranslated client URI that can be used in a call to Restless.node(uri)
//    "serverUrl": "http://host/path"          URL to access the corresponding resource on the server
//    "childResources": [                      A list of known child resources
//        {
//           "sequence": 0,                    the place in the list where this appeared
//           "name": "{name}",                 identifier which must be unique, nonempty and not contain slashes
//           "clientUri": "restless:path"      uri to locate it under, should equal the parent clientUri together with the "name" field
//        }
//    ],
//    "translationRules: [                     a list of relevant translation rules.
//        {
//           "sequence": 0,                    the place in the list where this appeared. Higher numbers will be applied in preference.
//           "owner": "restless:path",         which resource owns this rule. This is where you must issue patch requests if you want the rule changed or deleted.
//           "matchUri": "restless:prefix",    uri prefix to match
//           "matchDescendants": true,         whether descendants are matched also
//           "mapsToUri": "scheme:prefix",     uri prefix to map it to.
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
		Restless.err('Not initialized');
	}
};

Restless._presentList = function(inputList,presentation,fn)
{
	Restless.ArrayOf(Restless.Object).check(inputList,'inputList');
	Restless.NonEmptyString.check(presentation,'presentation');
	Restless.Function.check(fn,'fn');

	var map = {};
	var list = [];
	
	switch(presentation)
	{
	case 'sortBySequence':
		for (var i = 0; i < inputList.length; i++)
		{
			list.push(fn(inputList[i],i));
		}
		return list;
	case 'sortByName':
		for (var i = 0; i < inputList.length; i++)
		{
			list.push(fn(inputList[i],i));
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
			map[inputList[i].name] = fn(inputList[i],i);
		}
		return map;
	default:
		Restless.err('Unknown presentation: {}', presentation);
	}
};

Restless._justScheme = function(uri)
{
	Restless.NonEmptyString.check(uri,'uri');
	var i = uri.indexOf(':');
	return (i !== -1 && i === uri.length - 1);
}

Restless._uriTidy = function(uri)
{
	Restless.NonEmptyString.check(uri,'uri');
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
	Restless.NonEmptyString.check(name,'name');
	Restless.StringOrUndefined.check(catchAll,'catchAll');
	Restless.MapOf(Restless.NonEmptyString).check(fieldTargets, 'fieldTargets');

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
// Service interface
//
///////////////////////

Restless.Service = Restless.Object
	.withTypeName('Service')
	.withField('homeUri', Restless.StringOrUndefined)
	.withField('matchUri', Restless.NonEmptyString)
	.withMethod('appliesTo')
	.withMethod('get')
	.withMethod('put');

///////////////////////
//
// HttpService
//
///////////////////////

Restless.HttpService = Restless.Service
	.withTypeName('HttpService')
	.withConstructor(
		function(matchUri)
		{
			Restless.String.check(matchUri,'matchUri');
			if (!matchUri.startsWith('http://'))
			{
				Restless.err('matchUri must start http:// for http service');
			}
			this.homeUri = matchUri;
			this.matchUri = 'http://';
		});

Restless.HttpService.prototype.appliesTo = function(uri)
{
	Restless.String.check(uri,'uri');
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
// Uri
//
///////////////////////

Restless.registerSimpleType('Uri',
	function(value,name)
	{
		Restless.NonEmptyString.check(value,name);
		var uriComponents = Restless.splitUri(value);
		return uriComponents.scheme !== undefined;
	}
);

///////////////////////
//
// Node
//
///////////////////////

Restless.Node = Restless.Object
	.withTypeName('Node')
	.withField('service', Restless.Service)
	.withField('uri', Restless.Uri)
	.withStandardConstructor();

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

Restless.TranslationStore = Restless.Object
	.withTypeName('TranslationStore')
	.withField('matchUri', Restless.Uri)
	.withField('homeUri', Restless.Uri)
	.withConstructor(
		function()
		{
			this._mappings = [];
			this.matchUri = 'restless:internal/translation-store/';
			this.homeUri = 'restless:internal/translation-store/';
		});

Restless.TranslationStore.prototype.appliesTo = function(uri)
{
	Restless.String.check(uri,'uri');
	return Restless._uriPrefix(uri, this.matchUri);
}

Restless.TranslationStore.prototype._findAppliedRule = function(uri)
{
	Restless.NonEmptyString.check(uri,'uri');
	
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
	Restless.NonEmptyString.check(uri,'uri');
	Restless.NonEmptyString.check(presentation,'presentation');
	
	var appliedRule = this._findAppliedRule(uri);
	var filteredList = this._mappings.filter(function(m)
		{
			return m.relevantTo(uri);
		});
	return Restless._presentList(
		filteredList,
		presentation,
		function(r,i) {
			return {
				sequence: i,
				matchUri: r.matchPattern.stringForm,
				mapsToUri: r.mapsToPattern.stringForm,
				applied: r === appliedRule
			};
		});
};

Restless.TranslationStore.prototype.addRule = function(matchUri,mapsToUri)
{
	Restless.NonEmptyString.check(matchUri,'matchUri');
	Restless.NonEmptyString.check(mapsToUri,'mapsToUri');
	
	var matchPattern = Restless.UriPattern.fromString(matchUri);
	var mapsToPattern = Restless.UriPattern.fromString(mapsToUri);
	
	this._mappings.push( new Restless.TranslationRule(matchPattern,mapsToPattern) );
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
		var err = experimentalStore.addRule(rule.matchUri, rule.mapsToUri);
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
// TranslationLayer
//
///////////////////////

Restless.TranslationLayer = function(translationStore,underlyingServices)
{
	Restless.Object.check(translationStore,'translationStore');
	Restless.ArrayOf(Restless.Service).check(underlyingServices,'underlyingServices');
	this._translationStore = translationStore;
	this._underlyingServices = underlyingServices;
	this.matchUri = 'restless:';
	this.homeUri = 'restless:';

	Restless.Service.check(this,'TranslationLayer.constructor');
};

Restless.TranslationLayer.prototype.appliesTo = function(uri)
{
	Restless.String.check(uri,'uri');
	return Restless._uriPrefix(uri,this.matchUri);
};

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
	Restless.NonEmptyString.check(uri,'uri');
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
// UriPattern
//
///////////////////////

Restless.UriPatternSegment = Restless.Object
	.withTypeName('UriPatternSegment')
	.withMethod('match')
	.withField('stringForm',Restless.String)
	.withField('isLiteral',Restless.Boolean)

Restless.LiteralSegment = Restless.UriPatternSegment
	.withTypeName('LiteralSegment')
	.withField('segment',Restless.String)
	.withConstructor(
		function(segment)
		{
			this.segment = segment;
			this.isLiteral = true;
		});

Restless.LiteralSegment.prototype.match = function(segments)
{
	Restless.ArrayOf(Restless.String).check(segments,'segments');
	if (segments.length >= 1 && this.segment === segments[0])
	{
		return {count:1, capture:undefined};
	}
	return undefined;
};

Object.defineProperty(Restless.LiteralSegment.prototype, 'stringForm', {
	get: function()
	{
		return this.segment;
	}
});

Restless.MatchSingleSegment = Restless.UriPatternSegment
	.withTypeName('MatchSingleSegment')
	.withConstructor(
		function()
		{
			this.isLiteral = false;
		});

Restless.MatchSingleSegment.prototype.match = function(segments)
{
	Restless.ArrayOf(Restless.String).check(segments,'segments');
	if (segments.length >= 1)
	{
		return {count:1, capture:segment};
	}
	return undefined;
};

Restless.MatchSingleSegment.prototype.stringForm = function()
{
	return "{}";
};

Restless.UriPattern = Restless.Object
	.withTypeName('UriPattern')
	.withField('scheme', Restless.NonEmptyString)
	.withField('authority', Restless.StringOrUndefined)
	.withField('segments', Restless.ArrayOf(Restless.UriPatternSegment))
	.withMethod('match')
	.withMethod('toUri')
	.withMethod('hasPrefix')
	.withProperty('stringForm',Restless.NonEmptyString)
	.withStandardConstructor();

Restless.UriPattern.prototype.match = function(uri)
{
	Restless.String.check(uri,'uri');
	
	var uriComponents = Restless.splitUri(uri);
	if (uriComponents.scheme !== this.scheme)
	{
		return undefined;
	}
	
	var segments = uriComponents.path.split('/');
	
	var j = 0;
	var result = [];
	for (var i = 0; i < this.segments.length; i++)
	{
		var match = this.segments[i].match(segments.slice(j))
		if (match === undefined)
		{
			return undefined;
		}
		if (match.capture !== undefined)
		{
			result.push(match.capture);
		}
		j += match.count;
	}
	if (j !== segments.length)
	{
		return undefined;
	}
	return result;
};

Restless.UriPattern.prototype._schemeAndAuthority = function()
{
	if (this.authority === undefined)
	{
		return this.scheme + ':';
	}
	else
	{
		return this.scheme + '://' + this.authority;
	}
};

Restless.UriPattern.prototype.toUri = function(capturedItems)
{
	if (capturedItems.length !== 0)
	{
		Restless.err('Dealing with captured items is not handled yet');
	}
	return this._schemeAndAuthority() + this.segments.map(
		function(seg)
		{
			if (!seg.isLiteral())
			{
				Restless.err('Dealing with non-literal not handled yet');
			}
			return seg.segment;
		}).join('/');
};

Restless.UriPattern.prototype.hasPrefix = function(prefix)
{
	Restless.String.check(prefix,'prefix');
	var uriComponents = Restless.splitUri(prefix);
	if (uriComponents.scheme !== this.scheme)
	{
		return false;
	}
	if (uriComponents.query !== undefined || uriComponents.fragment !== undefined)
	{
		return false;
	}
	var segments = uriComponents.path.split('/');
	for (var i = 0; i < segments.length; i++)
	{
		// Special treatment for empty segment at the end of prefix.
		if (i === segments.length - 1 && segments[i] === '')
		{
			if (i >= this.segments.length)
			{
				return false;
			}
		}
		else
		{
			if (i >= this.segments.length || !this.segments[i].isLiteral || this.segments[i].segment !== segments[i])
			{
				return false;
			}
		}
	}
	return true;
};

Object.defineProperty(Restless.UriPattern.prototype, 'stringForm', {
	get: function()
	{
		return this._schemeAndAuthority() + this.segments.map(s => s.stringForm).join('/');
	}
});

Restless.UriPatternSegment.fromString = function(segment)
{
	Restless.String.check(segment,'segment');
	
	var literalRegex = /^[a-zA-Z0-9-._~]*$/;
	if (literalRegex.test(segment))
	{
		return new Restless.LiteralSegment(segment);
	}
	else
	{
		Restless.err('Invalid uri segment: {}', segment);
	}
};

Restless.splitUri = function(uri)
{
	Restless.String.check(uri,'uri');
	
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

Restless.UriPattern.fromString = function(patternString)
{
	Restless.NonEmptyString.check(patternString,'patternString');
	
	var uriComponents = Restless.splitUri(patternString);
	if (uriComponents.scheme === undefined || uriComponents.query !== undefined || uriComponents.fragment !== undefined)
	{
		Restless.err('Invalid uri pattern string: {}', patternString);
	}
	
	var segments = uriComponents.path.split('/').map(Restless.UriPatternSegment.fromString);
	return new Restless.UriPattern(uriComponents.scheme, uriComponents.authority, segments);
};

Restless.uriAddSegment = function(uri,segment)
{
	Restless.NonEmptyString.check(segment,'segment');
	var segmentRegex = /^[a-zA-Z0-9.-_~]+$/;
	if (!segmentRegex.test(segment))
	{
		Restless.err('Invalid segment for uriAddSegment: {}', segment);
	}
	
	var uriComponents = Restless.splitUri(uri);
	if (uriComponents.scheme === undefined || uriComponents.query !== undefined || uriComponents.fragment !== undefined)
	{
		Restless.err('Invalid uri for uriAddSegment: {}', uri);
	}
	
	var path;
	if (uriComponents.path.endsWith('/') || (uriComponents.path === '' && uriComponents.authority === undefined))
	{
		path = uriComponents.path + segment;
	}
	else
	{
		path = uriComponents.path + '/' + segment;
	}
	
	if (uriComponents.authority === undefined)
	{
		return uriComponents.scheme + ':' + path;
	}
	else
	{
		return uriComponents.scheme + '://' + uriComponents.authority + path;
	}
};

///////////////////////
//
// TranslationRule
//
///////////////////////

Restless.TranslationRule = Restless.Object
	.withTypeName('TranslationRule')
	.withField('matchPattern', Restless.UriPattern)
	.withField('mapsToPattern', Restless.UriPattern)
	.withStandardConstructor();

Restless.TranslationRule.prototype.appliesTo = function(uri)
{
	Restless.NonEmptyString.check(uri,'uri');
	return this.matchPattern.match(uri) !== undefined;
};

Restless.TranslationRule.prototype.relevantTo = function(uri)
{
	Restless.NonEmptyString.check(uri,'uri');
	return this.appliesTo(uri) || this.matchPattern.hasPrefix(uri);
};

Restless.TranslationRule.prototype.translate = function(uri)
{
	Restless.NonEmptyString.check(uri,'uri');
	var match = this.matchPattern.match(uri);
	if (match !== undefined)
	{
		return this.mapsToPattern.toUri(match);
	}
	return undefined;
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
		return new Restless.ErrorNode('Main service does not apply to this uri');
	}
};

Restless.initialize = function(rootUrl)
{
	Restless.NonEmptyString.check(rootUrl,'rootUrl');
	Restless._knownNodes = {};
	var httpService = new Restless.HttpService(rootUrl);
	var translationStore = new Restless.TranslationStore();
	var translationLayer = new Restless.TranslationLayer(translationStore, [httpService]);

	var serverUri = Restless.uriAddSegment(translationLayer.homeUri, 'server');

	translationStore.addRule(serverUri, httpService.homeUri);

	Restless._mainService = translationLayer;
};
