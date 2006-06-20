/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are prohibited, unless you have been explicitly granted 
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sej.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

public final class Util
{
	private static final String ACCESSIBLE = "; cannot be accessed by SEJ";
	private static final String IMPLEMENTABLE = "; cannot be implemented by SEJ";


	private Util()
	{
		super();
	}

	
	public static String trimTrailingZerosAndPoint( String _string )
	{
		String result = _string;
		if (result.contains( "." )) {
			int l = result.length();
			while ('0' == result.charAt( l - 1 ))
				l--;
			if ('.' == result.charAt( l - 1 )) l--;
			result = result.substring( 0, l );
		}
		return result;
	}


	public static void validateIsAccessible( Class _class, String _role )
	{
		final int mods = _class.getModifiers();
		validate( Modifier.isPublic( mods ), _class, _role, "not public", ACCESSIBLE );
		if (_class.getEnclosingClass() != null) {
			validate( Modifier.isStatic( mods ), _class, _role, "enclosed but not static", ACCESSIBLE );
		}
		else {
			validate( !Modifier.isStatic( mods ), _class, _role, "static", ACCESSIBLE );
		}
	}

	public static void validateIsAccessible( Method _method, String _role )
	{
		final int mods = _method.getModifiers();
		validate( Modifier.isPublic( mods ), _method, _role, "not public", ACCESSIBLE );
		validate( !Modifier.isStatic( mods ), _method, _role, "static", ACCESSIBLE );
	}


	public static void validateIsImplementable( Class _class, String _role )
	{
		final int mods = _class.getModifiers();
		validate( Modifier.isPublic( mods ), _class, _role, "not public", IMPLEMENTABLE );
		validate( !Modifier.isFinal( mods ), _class, _role, "final", IMPLEMENTABLE );
	}

	public static void validateIsImplementable( Method _method, String _role )
	{
		final int mods = _method.getModifiers();
		validate( Modifier.isPublic( mods ), _method, _role, "not public", IMPLEMENTABLE );
		validate( !Modifier.isStatic( mods ), _method, _role, "static", IMPLEMENTABLE );
		validate( !Modifier.isFinal( mods ), _method, _role, "final", IMPLEMENTABLE );
	}

	private static void validate( boolean _mustBeTrue, Class _class, String _role, String _what, String _whatFor )
	{
		if (!_mustBeTrue) throw new IllegalArgumentException( _role + " " + _class + " is " + _what + _whatFor );
	}

	private static void validate( boolean _mustBeTrue, Method _method, String _role, String _what, String _whatFor )
	{
		if (!_mustBeTrue) throw new IllegalArgumentException( _role + " " + _method + " is " + _what + _whatFor );
	}


	public static void validateCallable( Class _class, Method _method )
	{
		if (!_method.getDeclaringClass().isAssignableFrom( _class ))
			throw new IllegalArgumentException( "Method '"
					+ _method + "' cannot be called on an object of type '" + _class + "'" );
	}


	public static void validateFactory( Class _factoryClass, Method _factoryMethod, Class _inputClass, Class _outputClass )
	{
		if (_factoryClass != null) {
			Util.validateIsImplementable( _factoryClass, "factoryClass" );

			if (_factoryMethod == null) throw new IllegalArgumentException( "factoryMethod not specified" );
			Util.validateIsImplementable( _factoryMethod, "factoryMethod" );

			if (_factoryMethod.getReturnType() != _outputClass)
				throw new IllegalArgumentException( "factoryMethod '"
						+ _factoryMethod + "' does not return outputClass '" + _outputClass + "'" );
			final Class[] params = _factoryMethod.getParameterTypes();
			if (params.length != 1 || params[ 0 ] != _inputClass)
				throw new IllegalArgumentException( "factoryMethod '"
						+ _factoryMethod + "' does not have single parameter of type inputClass '"
						+ _inputClass + "'" );

			final Map<String, Method> abstracts = Util.abstractMethodsOf( _factoryClass );
			abstracts.remove( "newComputation(Ljava/lang/Object;)Lsej/Computation;" );
			abstracts.remove( Util.nameAndSignatureOf(_factoryMethod ) );
			if (abstracts.size() > 0) {
				throw new IllegalArgumentException(
						"factoryClass is still abstract after implementing factoryMethod; offending method is '"
								+ abstracts.values().iterator().next() + "'" );
			}
		}
		else if (_factoryMethod != null) {
			throw new IllegalArgumentException( "factoryMethod is set, but factoryClass is not" );
		}
	}

	
	public static String signatureOf( Method _m )
	{
		return Type.getMethodDescriptor( _m );
	}


	public static String nameAndSignatureOf( Method _m )
	{
		return _m.getName() + signatureOf( _m );
	}


	public static Map<String, Method> abstractMethodsOf( final Class _class )
	{
		final Map<String, Method> result = new HashMap<String, Method>();
		collectAbstractMethods( _class, result );
		return result;
	}

	private static void collectAbstractMethods( Class _class, Map<String, Method> _result )
	{
		final Class superclass = _class.getSuperclass();
		if (superclass != Object.class && superclass != null) {
			collectAbstractMethods( superclass, _result );
		}
		for (Class intf : _class.getInterfaces()) {
			collectInterfaceMethods( intf, _result );
		}
		for (Method m : _class.getDeclaredMethods()) {
			if (!Modifier.isStatic( m.getModifiers() )) {
				if (Modifier.isAbstract( m.getModifiers() )) {
					_result.put( Util.nameAndSignatureOf( m ), m );
				}
				// Assumption: checking the size is cheaper than building the signature:
				else if (_result.size() > 0) {
					_result.remove( Util.nameAndSignatureOf( m ) );
				}
			}
		}
	}

	private static void collectInterfaceMethods( Class _intf, Map<String, Method> _result )
	{
		for (Class intf : _intf.getInterfaces()) {
			collectInterfaceMethods( intf, _result );
		}
		for (Method m : _intf.getDeclaredMethods()) {
			_result.put( Util.nameAndSignatureOf( m ), m );
		}
	}

}
