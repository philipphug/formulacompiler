/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.tests.serialization;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;


public final class BigDecimalSerializationTest extends AbstractSerializationTest
{

	@Override
	protected NumericType getNumericType()
	{
		return SpreadsheetCompiler.BIGDECIMAL_SCALE8;
	}

	@Override
	protected String getTypeSuffix()
	{
		return "_BigDecimal";
	}

	@Override
	protected Number getResult( Outputs _outputs )
	{
		return _outputs.getResult_BigDecimal();
	}

}
