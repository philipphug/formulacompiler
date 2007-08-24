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
package org.formulacompiler.compiler.internal.templates;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.formulacompiler.runtime.internal.ComputationTime;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;


abstract class AbstractExpressionTemplatesForBigDecimals
{
	private ComputationTime computationTime = null; // not supposed to be called at compile-time

	protected final Environment environment;

	public AbstractExpressionTemplatesForBigDecimals( Environment _env )
	{
		this.environment = _env;
	}


	// ------------------------------------------------ Utils


	BigDecimal util_round( BigDecimal a, int _maxFrac )
	{
		return RuntimeBigDecimal_v2.round( a, _maxFrac );
	}


	BigDecimal util_fromInt( int a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromLong( long a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromDouble( double a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromFloat( float a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromBigDecimal( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.toNum( a );
	}

	BigDecimal util_fromBigInteger( BigInteger a )
	{
		return a == null? BigDecimal.ZERO : new BigDecimal( a );
	}

	BigDecimal util_fromNumber( Number a )
	{
		return a == null? BigDecimal.ZERO : new BigDecimal( a.toString() );
	}

	BigDecimal util_fromBoolean( boolean a )
	{
		return RuntimeBigDecimal_v2.booleanToNum( a );
	}

	BigDecimal util_fromDate( Date a )
	{
		return RuntimeBigDecimal_v2.dateToNum( a, this.environment.timeZone() );
	}

	BigDecimal util_fromMsSinceUTC1970( long a )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.msSinceUTC1970ToNum( a, this.environment.timeZone() ) );
	}

	BigDecimal util_fromMs( long a )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.msToNum( a ) );
	}


	@ReturnsAdjustedValue
	byte util_toByte( BigDecimal a )
	{
		return a.byteValue();
	}

	@ReturnsAdjustedValue
	short util_toShort( BigDecimal a )
	{
		return a.shortValue();
	}

	@ReturnsAdjustedValue
	int util_toInt( BigDecimal a )
	{
		return a.intValue();
	}

	@ReturnsAdjustedValue
	long util_toLong( BigDecimal a )
	{
		return a.longValue();
	}

	@ReturnsAdjustedValue
	double util_toDouble( BigDecimal a )
	{
		return a.doubleValue();
	}

	@ReturnsAdjustedValue
	float util_toFloat( BigDecimal a )
	{
		return a.floatValue();
	}

	@ReturnsAdjustedValue
	BigInteger util_toBigInteger( BigDecimal a )
	{
		return a.toBigInteger();
	}

	@ReturnsAdjustedValue
	BigDecimal util_toBigDecimal( BigDecimal a )
	{
		return a;
	}

	@ReturnsAdjustedValue
	boolean util_toBoolean( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.booleanFromNum( a );
	}

	@ReturnsAdjustedValue
	char util_toCharacter( BigDecimal a )
	{
		return (char) a.intValue();
	}

	@ReturnsAdjustedValue
	Date util_toDate( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.dateFromNum( a, this.environment.timeZone() );
	}

	@ReturnsAdjustedValue
	long util_toMsSinceUTC1970( BigDecimal a )
	{
		return RuntimeDouble_v2.msSinceUTC1970FromNum( a.doubleValue(), this.environment.timeZone() );
	}

	@ReturnsAdjustedValue
	long util_toMs( BigDecimal a )
	{
		return RuntimeDouble_v2.msFromNum( a.doubleValue() );
	}

	@ReturnsAdjustedValue
	String util_toString( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.toExcelString( a, this.environment );
	}

	Number util_toNumber( BigDecimal a )
	{
		return a;
	}


	BigDecimal util_fromScaledLong( long a, int _scale )
	{
		return RuntimeBigDecimal_v2.fromScaledLong( a, _scale );
	}

	@ReturnsAdjustedValue
	long util_toScaledLong( BigDecimal a, int _scale )
	{
		return RuntimeBigDecimal_v2.toScaledLong( a, _scale );
	}


	// ------------------------------------------------ Array Fold


	BigDecimal foldArray( BigDecimal[] _a )
	{
		final BigDecimal[] a = _a;
		BigDecimal acc = foldInitial();
		int i = 1;
		for (BigDecimal ai : a) {
			acc = foldElement( acc, ai, i );
			i++;
		}
		return acc;
	}

	private BigDecimal foldInitial() // abstract, really
	{
		return null;
	}

	private BigDecimal foldElement( BigDecimal _acc, BigDecimal _d, int _i ) // abstract, really
	{
		return null;
	}


	// ------------------------------------------------ Operators


	@ReturnsAdjustedValue
	public BigDecimal op_MINUS( BigDecimal a )
	{
		return a.negate();
	}

	@ReturnsAdjustedValue
	public BigDecimal op_INTERNAL_MIN( BigDecimal a, BigDecimal b )
	{
		/*
		 * Using a direct comparison like
		 * 
		 * return (a.compareTo( b ) <= 0) ? a : b;
		 * 
		 * generates too much code for inlining.
		 */
		return RuntimeBigDecimal_v2.min( a, b );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_INTERNAL_MAX( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v2.max( a, b );
	}


	// ------------------------------------------------ Numeric Functions


	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_ABS
	@ReturnsAdjustedValue
	public BigDecimal fun_ABS( BigDecimal a )
	{
		return a.abs();
	}
	// ---- fun_ABS

	public BigDecimal fun_ACOS( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_ACOS( a );
	}

	public BigDecimal fun_ASIN( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_ASIN( a );
	}

	public BigDecimal fun_ATAN( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_ATAN( a );
	}

	public BigDecimal fun_ATAN2( BigDecimal x, BigDecimal y )
	{
		return RuntimeBigDecimal_v2.fun_ATAN2( x, y );
	}

	public BigDecimal fun_COS( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_COS( a );
	}

	public BigDecimal fun_SIN( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_SIN( a );
	}

	public BigDecimal fun_TAN( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_TAN( a );
	}

	public BigDecimal fun_PI()
	{
		return RuntimeBigDecimal_v2.fun_PI();
	}

	public BigDecimal fun_ROUND( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v2.fun_ROUND( a, b );
	}

	public BigDecimal fun_ROUNDDOWN( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v2.fun_ROUNDDOWN( a, b );
	}

	public BigDecimal fun_ROUNDUP( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v2.fun_ROUNDUP( a, b );
	}

	public BigDecimal fun_TRUNC( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v2.fun_TRUNC( a, b );
	}

	public BigDecimal fun_TRUNC( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_TRUNC( a );
	}

	public BigDecimal fun_EVEN( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_EVEN( a );
	}

	public BigDecimal fun_ODD( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_ODD( a );
	}

	public BigDecimal fun_INT( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_INT( a );
	}

	public BigDecimal fun_EXP( BigDecimal p )
	{
		return BigDecimal.valueOf( Math.exp( p.doubleValue() ) );
	}

	public BigDecimal fun_LN( BigDecimal p )
	{
		return RuntimeBigDecimal_v2.fun_LN( p );
	}

	public BigDecimal fun_LOG( BigDecimal p )
	{
		return RuntimeBigDecimal_v2.fun_LOG10( p );
	}

	public BigDecimal fun_LOG( BigDecimal n, BigDecimal x )
	{
		return RuntimeBigDecimal_v2.fun_LOG( n, x );
	}

	public BigDecimal fun_LOG10( BigDecimal p )
	{
		return RuntimeBigDecimal_v2.fun_LOG10( p );
	}

	public BigDecimal fun_MOD( BigDecimal n, BigDecimal d )
	{
		return RuntimeBigDecimal_v2.fun_MOD( n, d );
	}


	// ------------------------------------------------ Date Functions


	public BigDecimal fun_DATE( BigDecimal _year, BigDecimal _month, BigDecimal _day )
	{
		return RuntimeBigDecimal_v2.fun_DATE( _year, _month, _day );
	}

	public BigDecimal fun_SECOND( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_SECOND( _date );
	}

	public BigDecimal fun_MINUTE( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_MINUTE( _date );
	}

	public BigDecimal fun_HOUR( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_HOUR( _date );
	}

	public BigDecimal fun_WEEKDAY( BigDecimal _date, BigDecimal _type )
	{
		return RuntimeBigDecimal_v2.fun_WEEKDAY( _date, _type );
	}

	public BigDecimal fun_WEEKDAY( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_WEEKDAY( _date, RuntimeBigDecimal_v2.ONE );
	}

	public BigDecimal fun_DAY( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_DAY( _date );
	}

	public BigDecimal fun_MONTH( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_MONTH( _date );
	}

	public BigDecimal fun_YEAR( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_YEAR( _date );
	}

	public BigDecimal fun_NOW()
	{
		return RuntimeBigDecimal_v2.fun_NOW( this.environment, this.computationTime );
	}

	public BigDecimal fun_TODAY()
	{
		return RuntimeBigDecimal_v2.fun_TODAY( this.environment, this.computationTime );
	}


	// ------------------------------------------------ Conversions Functions


	public BigDecimal fun_VALUE( String _text )
	{
		return RuntimeBigDecimal_v2.fun_VALUE( _text, this.environment );
	}


}
