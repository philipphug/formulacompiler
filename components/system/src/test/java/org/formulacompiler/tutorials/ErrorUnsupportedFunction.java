/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.tutorials;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;

public class ErrorUnsupportedFunction extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{

	public void testBindInfo() throws Exception
	{
		// ---- BindInfo
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"Info"/**/ );
		try {
			/**/builder.compile();/**/
			fail();
		}
		catch (/**/CompilerException.UnsupportedExpression e/**/) {
			if (getSpreadsheetExtension().equals( ".xls" )) {
				String err = /**/"Unsupported function INFO encountered in expression 1.0+INFO( <<? B1); error location indicated by <<?."
						+ "\nCell containing expression is Sheet1!A1."/**/;
				assertEquals( err, e.getMessage() );
			}
			else {
				String err = /**/"Unsupported function INFO encountered in expression 1+INFO( <<? [.B1]); error location indicated by <<?."
						+ "\nCell containing expression is Sheet1!A1."/**/;
				assertEquals( err, e.getMessage() );
			}
		}
		// ---- BindInfo
	}

	public void testBindReferencesInfo() throws Exception
	{
		// ---- BindReferencesInfo
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"ReferencesInfo"/**/ );
		try {
			builder.compile();
			fail();
		}
		catch (CompilerException.UnsupportedExpression e) {
			if (getSpreadsheetExtension().equals( ".xls" )) {
				String err = "Unsupported function INFO encountered in expression 1.0+INFO( <<? B1); error location indicated by <<?."
						+ "\nCell containing expression is Sheet1!A1."
						+ /**/"\nReferenced by cell Sheet1!A2."/**/;
				assertEquals( err, e.getMessage() );
			}
			else {
				String err = "Unsupported function INFO encountered in expression 1+INFO( <<? [.B1]); error location indicated by <<?."
						+ "\nCell containing expression is Sheet1!A1."
						+ /**/"\nReferenced by cell Sheet1!A2."/**/;
				assertEquals( err, e.getMessage() );
			}
		}
		// ---- BindReferencesInfo
	}

	public void testBindIndependent() throws Exception
	{
		// ---- BindIndependent
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"Independent"/**/ );
		SaveableEngine engine = builder.compile();
		MyFactory factory = (MyFactory) engine.getComputationFactory();
		MyComputation computation = factory.newComputation( new MyInputs() );
		/**/assertEquals( 3, computation.result() );/**/
		// ---- BindIndependent
	}

	public void testParsedButUnsupportedFunction() throws Exception
	{
		// ---- BindParsedButUnsupported
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"Unsupported"/**/ );
		try {
			builder.compile();
			fail();
		}
		catch (CompilerException.UnsupportedExpression e) {
			String err = /**/"Function ASC is not supported for double engines."/**/
					+ /**/"\nIn expression  >> ASC( Sheet1!B4 ) << ; error location indicated by >>..<<."/**/
					+ "\nCell containing expression is Sheet1!A4."
					+ "\nReferenced by cell Sheet1!A4.";
			assertEquals( err, e.getMessage() );
		}
		// ---- BindParsedButUnsupported
	}


	private EngineBuilder builderForComputationOfCellNamed( String _cellName ) throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		String path = "src/test/data/org/formulacompiler/tutorials/ErrorUnsupportedFunction" + getSpreadsheetExtension();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( MyFactory.class );
		Cell cell = builder.getSpreadsheet().getCell( _cellName );
		builder.getRootBinder().defineOutputCell( cell, MyComputation.class.getMethod( "result" ) );
		return builder;
	}

	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( ErrorUnsupportedFunction.class );
	}

	public static interface MyFactory
	{
		public MyComputation newComputation( MyInputs _inputs );
	}

	public static class MyInputs
	{
		public int value()
		{
			return 1;
		}
	}

	public static interface MyComputation
	{
		public int result();
	}

}
