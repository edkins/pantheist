'use strict';

var Pantheist = Pantheist || {};

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
Pantheist._describe = function(thing)
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

Pantheist.err = function()
{
	var pieces = arguments[0].split('{');
	var result = pieces[0];
	for (var i = 1; i < arguments.length || i < pieces.length; i++)
	{
		var piece = pieces[i] || '';
		var data;
		if (i < arguments.length)
		{
			data = arguments[i];
		}
		else
		{
			data = '???';
		}

		if (piece.startsWith('}'))
		{
			result += data + piece.substring(1);
		}
		else if (piece.startsWith('d}'))
		{
			result += Pantheist._describe(data) + piece.substring(2);
		}
		else
		{
			result += '???' + data + '???' + piece;
		}
	}
	throw new Error(result);
}

Pantheist.makeType = function(typeName,fieldNames,constructor,checker) {
	if (typeof typeName !== 'string' || typeName === '')
	{
		Pantheist.err('makeType.typeName: expecting NonEmptyString, was {d}', typeName);
	}
	if (!Array.isArray(fieldNames))
	{
		Pantheist.err('makeType.fieldNames.{}: expecting Array, was {d}', typeName, fieldNames);
	}
	if (constructor !== undefined && typeof constructor !== 'function')
	{
		Pantheist.err('makeType.constructor.{}: expecting FunctionOrUndefined, was {d}', typeName, constructor);
	}
	if (typeof checker !== 'function')
	{
		Pantheist.err('makeType.checker.{}: expecting Function, was {d}', typeName, checker);
	}
	
	var result;
	if (constructor === undefined)
	{
		result = function()
			{
				Pantheist.err('{} has no constructor', typeName);
			};
	}
	else
	{
		result = function()
			{
				constructor.apply(this,arguments);
				result.check(this,typeName+'.constructor');
			};
	}
	result.typeName = typeName;
	result._checker = checker;
	result._constructor = constructor;
	result._fieldNames = fieldNames;

	result.check = function(value,name)
		{
			if (typeof name !== 'string' || name === '')
			{
				Pantheist.err('{}.check.name: expecting NonEmptyString, was {d}', typeName, name);
			}
			if (this._checker(value,name) === false)
			{
				Pantheist.err('{}: expecting {}, was {d}', name, this.typeName, value);
			}
		};

	result.withTypeName = function(typeName)
		{
			if (typeof typeName !== 'string' || typeName === '')
			{
				Pantheist.err('{}.withTypeName.typeName: expecting NonEmptyString, was {d}', this.typeName, typeName);
			}
			return Pantheist.makeType(typeName,this._fieldNames,this._constructor,this._checker);
		};

	result.withConstructor = function(constructor)
		{
			if (typeof constructor !== 'function')
			{
				Pantheist.err('{}.withConstructor.constructor: expecting Function, was {d}', this.typeName, constructor);
			}
			if (this._constructor !== undefined)
			{
				Pantheist.err('{}.withConstructor: already has a constructor', this.typeName);
			}
			return Pantheist.makeType(typeName,this._fieldNames,constructor,this._checker);
		};

	result.withStandardConstructor = function()
		{
			var fieldNames = this._fieldNames;
			return this.withConstructor(
				function()
				{
					if (arguments.length !== fieldNames.length)
					{
						Pantheist.err('{}.standardConstructor: expecting {} arguments, got {}', this.typeName, fieldNames.length, arguments.length);
					}
					for (var i = 0; i < arguments.length; i++)
					{
						this[fieldNames[i]] = arguments[i];
					}
				}
			);
		};

	result.thatSatisfies = function(checker)
		{
			var original = this;
			if (typeof checker !== 'function')
			{
				Pantheist.err('{}.thatSatisfies.checker: expecting Function, was {d}', this.typeName, checker);
			}
			return Pantheist.makeType(typeName,this._fieldNames,this._constructor,
				function(value,name)
				{
					original.check(value,name);
					return checker(value,name);
				}
			);
		};

	result._withField = function(fieldName,fieldType,shouldAdd)
		{
			if (typeof fieldName !== 'string' || fieldName === '')
			{
				Pantheist.err('{}._withField.fieldName: expecting NonEmptyString, was {d}', this.typeName, fieldName);
			}
			
			if (typeof(fieldType) !== 'function' || typeof fieldType.check !== 'function')
			{
				Pantheist.err('{}._withField.{}.fieldType: expecting Type, was {d}', this.typeName, fieldName, fieldType);
			}
			
			var newFieldNames = Array.from(this._fieldNames);
			if (shouldAdd)
			{
				newFieldNames.push(fieldName);
			}
		
			var original = this;

			return Pantheist.makeType(typeName,newFieldNames,this._constructor,
				function(value,name)
				{
					original.check(value,name);
					if (!fieldName in value)
					{
						Pantheist.err('{}: expecting {}, missing field {}', name, this.typeName, fieldName);
					}
					fieldType.check(value[fieldName], name + '.' + fieldName + '[' + this.typeName + ']');
				}
			);
		};

	result.withField = function(fieldName,fieldType)
		{
			return this._withField(fieldName,fieldType,true);
		};

	result.withProperty = function(fieldName,fieldType)
		{
			return this._withField(fieldName,fieldType,false);
		};

	result.withMethod = function(fieldName)
		{
			return this._withField(fieldName,Pantheist.Function,false);
		};
	
	return result;
};

Pantheist.registerSimpleType = function(typeName,checker)
{
	if (typeName in Pantheist) {
		Pantheist.err('{} already registered', typeName);
	}
	Pantheist[typeName] = Pantheist.makeType(typeName,[],undefined,checker);
}


//////////////////////////////////
//
// Simple types
//
//////////////////////////////////


Pantheist.registerSimpleType('String',
	function(value) {
		return typeof value === 'string';
	}
);

Pantheist.registerSimpleType('NonEmptyString',
	function(value) {
		return typeof value === 'string' && value !== '';
	}
);

Pantheist.registerSimpleType('StringOrUndefined',
	function(value) {
		return value === undefined || typeof value === 'string';
	}
);

Pantheist.registerSimpleType('Function',
	function(value) {
		return typeof value === 'function';
	}
);

Pantheist.registerSimpleType('Object',
	function(value) {
		return typeof value === 'object';
	}
);

Pantheist.Type = Pantheist.Function
	.withTypeName('Type')
	.withField('typeName',Pantheist.NonEmptyString)
	.withMethod('check')
	.withMethod('withTypeName')
	.withMethod('withField')
	.withMethod('withProperty')
	.withMethod('withMethod')
	.withMethod('withConstructor')
	.withMethod('withStandardConstructor');

Pantheist.registerSimpleType('Array',
	function(value) {
		return Array.isArray(value);
	}
);

Pantheist.ArrayOf = function(elementType) {
	Pantheist.Type.check(elementType,'ArrayOf.elementType');

	return Pantheist.makeType('ArrayOf('+elementType.typeName+')', [], undefined,
		function(value,name)
		{
			Pantheist.Array.check(value,name);
			for (var i = 0; i < value.length; i++)
			{
				elementType.check(value[i], name + '[' + i + ']');
			}
		}
	);
};

Pantheist.registerSimpleType('Map',
	function(value,name) {
		if (typeof map !== 'object' || Array.isArray(map))
		{
			return false;
		}
		for (var key in value)
		{
			Pantheist.NonEmptyString.check(key, name+'.keys[Map]');
		}
	}
);

Pantheist.MapOf = function(elementType) {
	Pantheist.Type.check(elementType,'MapOf.elementType');

	return Pantheist.makeType('MapOf('+elementType.typeName+')', [], undefined,
		function(value,name)
		{
			Pantheist.MapOf.check(value);
			for (var key in value)
			{
				elementType.check(value[key], name+'.'+key);
			}
		}
	);
};

Pantheist.registerSimpleType('Boolean',
	function(value) {
		return typeof(value) === 'boolean';
	}
);
