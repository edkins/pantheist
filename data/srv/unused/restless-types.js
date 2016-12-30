'use strict';

var Restless = Restless || {};

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

Restless.err = function()
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
			result += Restless._describe(data) + piece.substring(2);
		}
		else
		{
			result += '???' + data + '???' + piece;
		}
	}
	throw new Error(result);
}

Restless.makeType = function(typeName,fieldNames,constructor,checker) {
	if (typeof typeName !== 'string' || typeName === '')
	{
		Restless.err('makeType.typeName: expecting NonEmptyString, was {d}', typeName);
	}
	if (!Array.isArray(fieldNames))
	{
		Restless.err('makeType.fieldNames.{}: expecting Array, was {d}', typeName, fieldNames);
	}
	if (constructor !== undefined && typeof constructor !== 'function')
	{
		Restless.err('makeType.constructor.{}: expecting FunctionOrUndefined, was {d}', typeName, constructor);
	}
	if (typeof checker !== 'function')
	{
		Restless.err('makeType.checker.{}: expecting Function, was {d}', typeName, checker);
	}
	
	var result;
	if (constructor === undefined)
	{
		result = function()
			{
				Restless.err('{} has no constructor', typeName);
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
				Restless.err('{}.check.name: expecting NonEmptyString, was {d}', typeName, name);
			}
			if (this._checker(value,name) === false)
			{
				Restless.err('{}: expecting {}, was {d}', name, this.typeName, value);
			}
		};

	result.withTypeName = function(typeName)
		{
			if (typeof typeName !== 'string' || typeName === '')
			{
				Restless.err('{}.withTypeName.typeName: expecting NonEmptyString, was {d}', this.typeName, typeName);
			}
			return Restless.makeType(typeName,this._fieldNames,this._constructor,this._checker);
		};

	result.withConstructor = function(constructor)
		{
			if (typeof constructor !== 'function')
			{
				Restless.err('{}.withConstructor.constructor: expecting Function, was {d}', this.typeName, constructor);
			}
			if (this._constructor !== undefined)
			{
				Restless.err('{}.withConstructor: already has a constructor', this.typeName);
			}
			return Restless.makeType(typeName,this._fieldNames,constructor,this._checker);
		};

	result.withStandardConstructor = function()
		{
			var fieldNames = this._fieldNames;
			return this.withConstructor(
				function()
				{
					if (arguments.length !== fieldNames.length)
					{
						Restless.err('{}.standardConstructor: expecting {} arguments, got {}', this.typeName, fieldNames.length, arguments.length);
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
				Restless.err('{}.thatSatisfies.checker: expecting Function, was {d}', this.typeName, checker);
			}
			return Restless.makeType(typeName,this._fieldNames,this._constructor,
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
				Restless.err('{}._withField.fieldName: expecting NonEmptyString, was {d}', this.typeName, fieldName);
			}
			
			if (typeof(fieldType) !== 'function' || typeof fieldType.check !== 'function')
			{
				Restless.err('{}._withField.{}.fieldType: expecting Type, was {d}', this.typeName, fieldName, fieldType);
			}
			
			var newFieldNames = Array.from(this._fieldNames);
			if (shouldAdd)
			{
				newFieldNames.push(fieldName);
			}
		
			var original = this;

			return Restless.makeType(typeName,newFieldNames,this._constructor,
				function(value,name)
				{
					original.check(value,name);
					if (!fieldName in value)
					{
						Restless.err('{}: expecting {}, missing field {}', name, this.typeName, fieldName);
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
			return this._withField(fieldName,Restless.Function,false);
		};
	
	return result;
};

Restless.registerSimpleType = function(typeName,checker)
{
	if (typeName in Restless) {
		Restless.err('{} already registered', typeName);
	}
	Restless[typeName] = Restless.makeType(typeName,[],undefined,checker);
}


//////////////////////////////////
//
// Simple types
//
//////////////////////////////////


Restless.registerSimpleType('String',
	function(value) {
		return typeof value === 'string';
	}
);

Restless.registerSimpleType('NonEmptyString',
	function(value) {
		return typeof value === 'string' && value !== '';
	}
);

Restless.registerSimpleType('StringOrUndefined',
	function(value) {
		return value === undefined || typeof value === 'string';
	}
);

Restless.registerSimpleType('Function',
	function(value) {
		return typeof value === 'function';
	}
);

Restless.registerSimpleType('Object',
	function(value) {
		return typeof value === 'object';
	}
);

Restless.Type = Restless.Function
	.withTypeName('Type')
	.withField('typeName',Restless.NonEmptyString)
	.withMethod('check')
	.withMethod('withTypeName')
	.withMethod('withField')
	.withMethod('withProperty')
	.withMethod('withMethod')
	.withMethod('withConstructor')
	.withMethod('withStandardConstructor');

Restless.registerSimpleType('Array',
	function(value) {
		return Array.isArray(value);
	}
);

Restless.ArrayOf = function(elementType) {
	Restless.Type.check(elementType,'ArrayOf.elementType');

	return Restless.makeType('ArrayOf('+elementType.typeName+')', [], undefined,
		function(value,name)
		{
			Restless.Array.check(value,name);
			for (var i = 0; i < value.length; i++)
			{
				elementType.check(value[i], name + '[' + i + ']');
			}
		}
	);
};

Restless.registerSimpleType('Map',
	function(value,name) {
		if (typeof map !== 'object' || Array.isArray(map))
		{
			return false;
		}
		for (var key in value)
		{
			Restless.NonEmptyString.check(key, name+'.keys[Map]');
		}
	}
);

Restless.MapOf = function(elementType) {
	Restless.Type.check(elementType,'MapOf.elementType');

	return Restless.makeType('MapOf('+elementType.typeName+')', [], undefined,
		function(value,name)
		{
			Restless.MapOf.check(value);
			for (var key in value)
			{
				elementType.check(value[key], name+'.'+key);
			}
		}
	);
};

Restless.registerSimpleType('Boolean',
	function(value) {
		return typeof(value) === 'boolean';
	}
);
