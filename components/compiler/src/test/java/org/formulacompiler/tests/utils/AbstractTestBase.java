/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.tests.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.internal.IOUtil;

import junit.framework.TestCase;

public abstract class AbstractTestBase extends TestCase
{

	protected AbstractTestBase()
	{
		super();
	}

	protected AbstractTestBase( String _name )
	{
		super( _name );
	}


	protected CallFrame getInput( String _name ) throws SecurityException, NoSuchMethodException
	{
		return new CallFrame( Inputs.class.getMethod( _name ) );
	}

	protected CallFrame getOutput( String _name ) throws SecurityException, NoSuchMethodException
	{
		return new CallFrame( OutputsWithoutReset.class.getMethod( _name ) );
	}


	protected void assertEqualStreams( String _message, InputStream _expected, InputStream _actual ) throws Exception
	{
		int offset = 0;
		while (true) {
			final int e = _expected.read();
			final int a = _actual.read();
			if (e != a) {
				assertEquals( _message + " at offset " + offset, e, a );
			}
			offset++;
			if (e < 0) break;
		}
	}


	protected void assertEqualFiles( String _nameOfExpectedFile, String _nameOfActualFile ) throws Exception
	{
		assertEqualFiles( new File( _nameOfExpectedFile ), new File( _nameOfActualFile ) );
	}

	protected void assertEqualFiles( File _nameOfExpectedFile, File _nameOfActualFile ) throws Exception
	{
		final InputStream exp = new BufferedInputStream( new FileInputStream( _nameOfExpectedFile ) );
		final InputStream act = new BufferedInputStream( new FileInputStream( _nameOfActualFile ) );
		assertEqualStreams( "Comparing files " + _nameOfExpectedFile + " and " + _nameOfActualFile, exp, act );
	}


	protected void assertEqualReaders( String _message, Reader _expected, Reader _actual ) throws Exception
	{
		int line = 1;
		final BufferedReader expected = _expected instanceof BufferedReader? (BufferedReader) _expected
				: new BufferedReader( _expected );
		final BufferedReader actual = _actual instanceof BufferedReader? (BufferedReader) _actual : new BufferedReader(
				_actual );
		while (true) {
			final String e = expected.readLine();
			final String a = actual.readLine();
			if (e == null && a == null) break;
			if (e == null || !e.equals( a )) {
				assertEquals( _message + " at line " + line, e, a );
			}
			line++;
		}
	}


	protected void assertEqualTextFiles( String _nameOfExpectedFile, String _nameOfActualFile ) throws Exception
	{
		assertEqualTextFiles( new File( _nameOfExpectedFile ), new File( _nameOfActualFile ) );
	}

	protected void assertEqualTextFiles( File _nameOfExpectedFile, File _nameOfActualFile ) throws Exception
	{
		final Reader exp = new FileReader( _nameOfExpectedFile );
		final Reader act = new FileReader( _nameOfActualFile );
		assertEqualReaders( "Comparing files " + _nameOfExpectedFile + " and " + _nameOfActualFile, exp, act );
	}


	protected void assertEqualToFile( String _nameOfExpectedFile, String _actual ) throws Exception
	{
		final String expected = normalizeLineEndings( readStringFrom( new File( _nameOfExpectedFile ) ) );
		final String actual = normalizeLineEndings( _actual );
		assertEquals( _nameOfExpectedFile, expected, actual );
	}


	protected static String readStringFrom( File _source ) throws IOException
	{
		return IOUtil.readStringFrom( _source );
	}

	protected static void writeStringTo( String _value, File _target ) throws IOException
	{
		IOUtil.writeStringTo( _value, _target );
	}

	protected static String normalizeLineEndings( String _s )
	{
		return IOUtil.normalizeLineEndings( _s );
	}


}
