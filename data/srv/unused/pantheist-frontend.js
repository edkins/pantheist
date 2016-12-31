'use strict';

var Pantheist = Pantheist || {};

///////////////////////
//
// Public Interfaces:
//
// node
//      uri                     URI that this node represents
//      schema                  type of data this node is expected to contain
//      get()                   fetch data for this node
//      put(data)               put data for this node
// Pantheist
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
//    "clientUri": "pantheist:path"             untranslated client URI that can be used in a call to Pantheist.node(uri)
//    "serverUrl": "http://host/path"          URL to access the corresponding resource on the server
//    "childResources": [                      A list of known child resources
//        {
//           "sequence": 0,                    the place in the list where this appeared
//           "name": "{name}",                 identifier which must be unique, nonempty and not contain slashes
//           "clientUri": "pantheist:path"      uri to locate it under, should equal the parent clientUri together with the "name" field
//        }
//    ],
//    "translationRules: [                     a list of relevant translation rules.
//        {
//           "sequence": 0,                    the place in the list where this appeared. Higher numbers will be applied in preference.
//           "owner": "pantheist:path",         which resource owns this rule. This is where you must issue patch requests if you want the rule changed or deleted.
//           "matchUri": "pantheist:prefix",    uri prefix to match
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

Pantheist._knownNodes = {};
Pantheist._mainService;

///////////////////////
//
// Private: Helper Functions
//
///////////////////////

Pantheist._initializationCheck = function()
{
	if (Pantheist._mainService === undefined)
	{
		Pantheist.err('Not initialized');
	}
};

Pantheist._presentList = function(inputList,presentation,fn)
{
	Pantheist.ArrayOf(Pantheist.Object).check(inputList,'inputList');
	Pantheist.NonEmptyString.check(presentation,'presentation');
	Pantheist.Function.check(fn,'fn');

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
		Pantheist.err('Unknown presentation: {}', presentation);
	}
};

Pantheist._justScheme = function(uri)
{
	Pantheist.NonEmptyString.check(uri,'uri');
	var i = uri.indexOf(':');
	return (i !== -1 && i === uri.length - 1);
}

Pantheist._uriTidy = function(uri)
{
	Pantheist.NonEmptyString.check(uri,'uri');
	if (!Pantheist._justScheme(uri) && !uri.endsWith('/'))
	{
		return uri + '/';
	}
	return uri;
}

Pantheist._uriPrefix = function(uri,prefix)
{
	return Pantheist._uriTidy(uri).startsWith(Pantheist._uriTidy(prefix));
};

Pantheist._uriEqual = function(uri,uri2)
{
	return Pantheist._uriTidy(uri) === Pantheist._uriTidy(uri2);
};

///////////////////////
//
// Helper functions that return promises
//
///////////////////////

Pantheist._splitByField = function(data,name,catchAll,fieldTargets)
{
	Pantheist.NonEmptyString.check(name,'name');
	Pantheist.StringOrUndefined.check(catchAll,'catchAll');
	Pantheist.MapOf(Pantheist.NonEmptyString).check(fieldTargets, 'fieldTargets');

	if (typeof(data) !== 'object')
	{
		return Promise.reject(context + ' expected object, got a ' + Pantheist._describe(data));
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

Pantheist.Service = Pantheist.Object
	.withTypeName('Service')
	.withField('homeUri', Pantheist.StringOrUndefined)
	.withField('matchUri', Pantheist.NonEmptyString)
	.withMethod('appliesTo')
	.withMethod('get')
	.withMethod('put');

///////////////////////
//
// HttpService
//
///////////////////////

Pantheist.HttpService = Pantheist.Service
	.withTypeName('HttpService')
	.withConstructor(
		function(matchUri)
		{
			Pantheist.String.check(matchUri,'matchUri');
			if (!matchUri.startsWith('http://'))
			{
				Pantheist.err('matchUri must start http:// for http service');
			}
			this.homeUri = matchUri;
			this.matchUri = 'http://';
		});

Pantheist.HttpService.prototype.appliesTo = function(uri)
{
	Pantheist.String.check(uri,'uri');
	return Pantheist._uriPrefix(uri, this.matchUri);
}

Pantheist.HttpService.prototype.get = function(url)
{
	if (!Pantheist._uriPrefix(url,this.matchUri))
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

Pantheist.HttpService.prototype.put = function(url,data)
{
	return Promise.reject('Put currently disabled for http');
};

///////////////////////
//
// Uri
//
///////////////////////

Pantheist.registerSimpleType('Uri',
	function(value,name)
	{
		Pantheist.NonEmptyString.check(value,name);
		var uriComponents = Pantheist.splitUri(value);
		return uriComponents.scheme !== undefined;
	}
);

///////////////////////
//
// Node
//
///////////////////////

Pantheist.Node = Pantheist.Object
	.withTypeName('Node')
	.withField('service', Pantheist.Service)
	.withField('uri', Pantheist.Uri)
	.withStandardConstructor();

Pantheist.Node.prototype.get = function()
{
	return this.service.get(this.uri);
};

Pantheist.Node.prototype.put = function(data)
{
	return this.service.put(this.uri,data);
};

///////////////////////
//
// ErrorNode
//
///////////////////////

Pantheist.ErrorNode = function(errorMsg)
{
	this.errorMsg = errorMsg;
	this.uri = undefined;
	this.schema = undefined;
};

Pantheist.ErrorNode.prototype.get = function()
{
	return Promise.reject(this.errorMsg);
};

Pantheist.ErrorNode.prototype.put = function(data)
{
	return Promise.reject(this.errorMsg);
};

///////////////////////
//
// TranslationStore
//
///////////////////////

Pantheist.TranslationStore = Pantheist.Object
	.withTypeName('TranslationStore')
	.withField('matchUri', Pantheist.Uri)
	.withField('homeUri', Pantheist.Uri)
	.withConstructor(
		function()
		{
			this._mappings = [];
			this.matchUri = 'pantheist:internal/translation-store/';
			this.homeUri = 'pantheist:internal/translation-store/';
		});

Pantheist.TranslationStore.prototype.appliesTo = function(uri)
{
	Pantheist.String.check(uri,'uri');
	return Pantheist._uriPrefix(uri, this.matchUri);
}

Pantheist.TranslationStore.prototype._findAppliedRule = function(uri)
{
	Pantheist.NonEmptyString.check(uri,'uri');
	
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

Pantheist.TranslationStore.prototype.translate = function(uri)
{
	var rule = this._findAppliedRule(uri);
	if (rule !== undefined)
	{
		return rule.translate(uri);
	}
	return undefined;
};

Pantheist.TranslationStore.prototype.relevantRules = function(uri,presentation)
{
	Pantheist.NonEmptyString.check(uri,'uri');
	Pantheist.NonEmptyString.check(presentation,'presentation');
	
	var appliedRule = this._findAppliedRule(uri);
	var filteredList = this._mappings.filter(function(m)
		{
			return m.relevantTo(uri);
		});
	return Pantheist._presentList(
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

Pantheist.TranslationStore.prototype.addRule = function(matchUri,mapsToUri)
{
	Pantheist.NonEmptyString.check(matchUri,'matchUri');
	Pantheist.NonEmptyString.check(mapsToUri,'mapsToUri');
	
	var matchPattern = Pantheist.UriPattern.fromString(matchUri);
	var mapsToPattern = Pantheist.UriPattern.fromString(mapsToUri);
	
	this._mappings.push( new Pantheist.TranslationRule(matchPattern,mapsToPattern) );
	return undefined;
};

Pantheist.TranslationStore.prototype.putRules = function(rules)
{
	if (!Array.isArray(rules))
	{
		return Promise.reject('rules must be array');
	}
	
	// Try adding rules to a new translation store. If any of them fail then we won't clobber our own store.
	var experimentalStore = new Pantheist.TranslationStore();
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

Pantheist.TranslationLayer = function(translationStore,underlyingServices)
{
	Pantheist.Object.check(translationStore,'translationStore');
	Pantheist.ArrayOf(Pantheist.Service).check(underlyingServices,'underlyingServices');
	this._translationStore = translationStore;
	this._underlyingServices = underlyingServices;
	this.matchUri = 'pantheist:';
	this.homeUri = 'pantheist:';

	Pantheist.Service.check(this,'TranslationLayer.constructor');
};

Pantheist.TranslationLayer.prototype.appliesTo = function(uri)
{
	Pantheist.String.check(uri,'uri');
	return Pantheist._uriPrefix(uri,this.matchUri);
};

Pantheist.TranslationLayer.prototype._findService = function(uri)
{
	for (var i = 0; i < this._underlyingServices.length; i++)
	{
		if (Pantheist._uriPrefix(uri, this._underlyingServices[i].matchUri))
		{
			return this._underlyingServices[i];
		}
	}
	return undefined;
};

Pantheist.TranslationLayer.prototype.get = function(uri)
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

Pantheist.TranslationLayer.prototype.put = function(uri,data)
{
	Pantheist.NonEmptyString.check(uri,'uri');
	var translationStore = this._translationStore;

	if (!this.appliesTo(uri))
	{
		return Promise.reject('Not in translation space');
	}
	
	return Pantheist._splitByField(data,'data',undefined,{
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
Pantheist.TranslationLayer.prototype.getzzz = function(uri)
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

Pantheist.UriPatternSegment = Pantheist.Object
	.withTypeName('UriPatternSegment')
	.withMethod('match')
	.withField('stringForm',Pantheist.String)
	.withField('isLiteral',Pantheist.Boolean)

Pantheist.LiteralSegment = Pantheist.UriPatternSegment
	.withTypeName('LiteralSegment')
	.withField('segment',Pantheist.String)
	.withConstructor(
		function(segment)
		{
			this.segment = segment;
			this.isLiteral = true;
		});

Pantheist.LiteralSegment.prototype.match = function(segments)
{
	Pantheist.ArrayOf(Pantheist.String).check(segments,'segments');
	if (segments.length >= 1 && this.segment === segments[0])
	{
		return {count:1, capture:undefined};
	}
	return undefined;
};

Object.defineProperty(Pantheist.LiteralSegment.prototype, 'stringForm', {
	get: function()
	{
		return this.segment;
	}
});

Pantheist.MatchSingleSegment = Pantheist.UriPatternSegment
	.withTypeName('MatchSingleSegment')
	.withConstructor(
		function()
		{
			this.isLiteral = false;
		});

Pantheist.MatchSingleSegment.prototype.match = function(segments)
{
	Pantheist.ArrayOf(Pantheist.String).check(segments,'segments');
	if (segments.length >= 1)
	{
		return {count:1, capture:segment};
	}
	return undefined;
};

Pantheist.MatchSingleSegment.prototype.stringForm = function()
{
	return "{}";
};

Pantheist.UriPattern = Pantheist.Object
	.withTypeName('UriPattern')
	.withField('scheme', Pantheist.NonEmptyString)
	.withField('authority', Pantheist.StringOrUndefined)
	.withField('segments', Pantheist.ArrayOf(Pantheist.UriPatternSegment))
	.withMethod('match')
	.withMethod('toUri')
	.withMethod('hasPrefix')
	.withProperty('stringForm',Pantheist.NonEmptyString)
	.withStandardConstructor();

Pantheist.UriPattern.prototype.match = function(uri)
{
	Pantheist.String.check(uri,'uri');
	
	var uriComponents = Pantheist.splitUri(uri);
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

Pantheist.UriPattern.prototype._schemeAndAuthority = function()
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

Pantheist.UriPattern.prototype.toUri = function(capturedItems)
{
	if (capturedItems.length !== 0)
	{
		Pantheist.err('Dealing with captured items is not handled yet');
	}
	return this._schemeAndAuthority() + this.segments.map(
		function(seg)
		{
			if (!seg.isLiteral())
			{
				Pantheist.err('Dealing with non-literal not handled yet');
			}
			return seg.segment;
		}).join('/');
};

Pantheist.UriPattern.prototype.hasPrefix = function(prefix)
{
	Pantheist.String.check(prefix,'prefix');
	var uriComponents = Pantheist.splitUri(prefix);
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

Object.defineProperty(Pantheist.UriPattern.prototype, 'stringForm', {
	get: function()
	{
		return this._schemeAndAuthority() + this.segments.map(s => s.stringForm).join('/');
	}
});

Pantheist.UriPatternSegment.fromString = function(segment)
{
	Pantheist.String.check(segment,'segment');
	
	var literalRegex = /^[a-zA-Z0-9-._~]*$/;
	if (literalRegex.test(segment))
	{
		return new Pantheist.LiteralSegment(segment);
	}
	else
	{
		Pantheist.err('Invalid uri segment: {}', segment);
	}
};

Pantheist.splitUri = function(uri)
{
	Pantheist.String.check(uri,'uri');
	
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

Pantheist.UriPattern.fromString = function(patternString)
{
	Pantheist.NonEmptyString.check(patternString,'patternString');
	
	var uriComponents = Pantheist.splitUri(patternString);
	if (uriComponents.scheme === undefined || uriComponents.query !== undefined || uriComponents.fragment !== undefined)
	{
		Pantheist.err('Invalid uri pattern string: {}', patternString);
	}
	
	var segments = uriComponents.path.split('/').map(Pantheist.UriPatternSegment.fromString);
	return new Pantheist.UriPattern(uriComponents.scheme, uriComponents.authority, segments);
};

Pantheist.uriAddSegment = function(uri,segment)
{
	Pantheist.NonEmptyString.check(segment,'segment');
	var segmentRegex = /^[a-zA-Z0-9.-_~]+$/;
	if (!segmentRegex.test(segment))
	{
		Pantheist.err('Invalid segment for uriAddSegment: {}', segment);
	}
	
	var uriComponents = Pantheist.splitUri(uri);
	if (uriComponents.scheme === undefined || uriComponents.query !== undefined || uriComponents.fragment !== undefined)
	{
		Pantheist.err('Invalid uri for uriAddSegment: {}', uri);
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

Pantheist.TranslationRule = Pantheist.Object
	.withTypeName('TranslationRule')
	.withField('matchPattern', Pantheist.UriPattern)
	.withField('mapsToPattern', Pantheist.UriPattern)
	.withStandardConstructor();

Pantheist.TranslationRule.prototype.appliesTo = function(uri)
{
	Pantheist.NonEmptyString.check(uri,'uri');
	return this.matchPattern.match(uri) !== undefined;
};

Pantheist.TranslationRule.prototype.relevantTo = function(uri)
{
	Pantheist.NonEmptyString.check(uri,'uri');
	return this.appliesTo(uri) || this.matchPattern.hasPrefix(uri);
};

Pantheist.TranslationRule.prototype.translate = function(uri)
{
	Pantheist.NonEmptyString.check(uri,'uri');
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

Object.defineProperty(Pantheist, 'home', {
	get: function()
	{
		Pantheist._initializationCheck();
		return Pantheist.node( Pantheist._mainService.homeUri );
	}
});

Pantheist.node = function(uri)
{
	Pantheist._initializationCheck();
	if (uri in Pantheist._knownNodes)
	{
		return Pantheist._knownNodes[uri];
	}
	else if (Pantheist._mainService.appliesTo(uri))
	{
		var node = new Pantheist.Node(Pantheist._mainService,uri);
		Pantheist._knownNodes[uri] = node;
		return node;
	}
	else
	{
		return new Pantheist.ErrorNode('Main service does not apply to this uri');
	}
};

Pantheist.initialize = function(rootUrl)
{
	Pantheist.NonEmptyString.check(rootUrl,'rootUrl');
	Pantheist._knownNodes = {};
	var httpService = new Pantheist.HttpService(rootUrl);
	var translationStore = new Pantheist.TranslationStore();
	var translationLayer = new Pantheist.TranslationLayer(translationStore, [httpService]);

	var serverUri = Pantheist.uriAddSegment(translationLayer.homeUri, 'server');

	translationStore.addRule(serverUri, httpService.homeUri);

	Pantheist._mainService = translationLayer;
};
