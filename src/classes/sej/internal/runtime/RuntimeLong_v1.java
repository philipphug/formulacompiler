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
package sej.internal.runtime;

import java.math.BigDecimal;
import java.util.Date;

import sej.runtime.ScaledLongSupport;


public final class RuntimeLong_v1 extends Runtime_v1
{
	public static long[] ONE_AT_SCALE = ScaledLongSupport.ONE;
	public static long[] HALF_AT_SCALE = new long[ ONE_AT_SCALE.length ];

	static {
		for (int i = 0; i < ONE_AT_SCALE.length; i++) {
			HALF_AT_SCALE[ i ] = ONE_AT_SCALE[ i ] / 2;
		}
	}


	public static final class Context
	{
		final int scale;
		final long one;
		final double oneAsDouble;

		public Context(final int _scale)
		{
			super();
			this.scale = _scale;
			if (_scale < 0 || _scale >= ONE_AT_SCALE.length) {
				throw new IllegalArgumentException( "Scale is out of range" );
			}
			this.one = ONE_AT_SCALE[ _scale ];
			this.oneAsDouble = this.one;
		}

		double toDouble( long _value )
		{
			if (_value == 0) {
				return 0.0;
			}
			else if (this.scale == 0) {
				return _value;
			}
			else {
				return _value / this.oneAsDouble;
			}
		}

		long fromDouble( double _value )
		{
			if (_value == 0.0) {
				return 0L;
			}
			else if (this.scale == 0) {
				return (long) _value;
			}
			else {
				return (long) (_value * this.oneAsDouble);
			}
		}

		long fromBoxedDouble( Number _value )
		{
			if (_value == null) {
				return 0L;
			}
			else {
				return fromDouble( _value.doubleValue() );
			}
		}

		@Deprecated
		long fromNumber( Number _value )
		{
			if (_value == null) {
				return 0L;
			}
			else if (this.scale == 0) {
				return _value.longValue();
			}
			else {
				return _value.longValue() * this.one;
			}
		}

		long fromBigDecimal( BigDecimal _value )
		{
			if (_value == null) {
				return 0L;
			}
			else if (this.scale == 0) {
				return _value.longValue();
			}
			else {
				return RuntimeBigDecimal_v1.toScaledLong( _value, this.scale );
			}
		}

		public BigDecimal toBigDecimal( long _value )
		{
			if (_value == 0) {
				return RuntimeBigDecimal_v1.ZERO;
			}
			else if (this.scale == 0) {
				return BigDecimal.valueOf( _value );
			}
			else {
				return RuntimeBigDecimal_v1.fromScaledLong( _value, this.scale );
			}
		}

	}


	public static long max( final long a, final long b )
	{
		return a >= b ? a : b;
	}

	public static long min( final long a, final long b )
	{
		return a <= b ? a : b;
	}

	public static long pow( final long x, final long n, Context _cx )
	{
		return _cx.fromDouble( Math.pow( _cx.toDouble( x ), _cx.toDouble( n ) ) );
	}

	public static long round( final long _val, final int _maxFrac, Context _cx )
	{
		if (_val == 0 || _maxFrac >= _cx.scale) {
			return _val;
		}
		else {
			final int truncateAt = _cx.scale - _maxFrac;
			final long shiftFactor = ONE_AT_SCALE[ truncateAt ];
			final long roundingCorrection = HALF_AT_SCALE[ truncateAt ];
			if (_val >= 0) {
				// I have: 123456 (scale = 3)
				// I want: 123500 (_maxFrac = 1)
				// So: (v + 50) / 100 * 100
				return (_val + roundingCorrection) / shiftFactor * shiftFactor;
			}
			else {
				// I have: -123456 (scale = 3)
				// I want: -123500 (_maxFrac = 1)
				// So: (v - 50) / 100 * 100
				return (_val - roundingCorrection) / shiftFactor * shiftFactor;
			}
		}
	}

	@Deprecated
	public static long stdROUND( final long _val, final long _maxFrac, Context _cx )
	{
		if (_cx.scale == 0) return round( _val, (int) _maxFrac, _cx );
		return round( _val, (int) (_maxFrac / _cx.one), _cx );
	}

	@Deprecated
	public static long stdTODAY( Context _cx )
	{
		return dateToNum( today(), _cx );
	}


	public static boolean booleanFromNum( final long _val )
	{
		return (_val != 0);
	}

	public static long booleanToNum( final boolean _val, Context _cx )
	{
		return _val ? _cx.one : 0;
	}

	public static Date dateFromNum( final long _val, Context _cx )
	{
		return RuntimeDouble_v1.dateFromNum( toDouble( _val, _cx ) );
	}

	public static long dateToNum( final Date _val, Context _cx )
	{
		return fromDouble( RuntimeDouble_v1.dateToNum( _val ), _cx );
	}

	@Deprecated
	public static long fromNumber( Number _val, Context _cx )
	{
		return _cx.fromNumber( _val );
	}

	public static long fromDouble( double _val, Context _cx )
	{
		return _cx.fromDouble( _val );
	}

	public static long fromBoxedDouble( Number _val, Context _cx )
	{
		return _cx.fromBoxedDouble( _val );
	}

	public static double toDouble( long _val, Context _cx )
	{
		return _cx.toDouble( _val );
	}

	public static long fromBigDecimal( BigDecimal _val, Context _cx )
	{
		return _cx.fromBigDecimal( _val );
	}

	public static BigDecimal toBigDecimal( long _val, Context _cx )
	{
		return _cx.toBigDecimal( _val );
	}


	public static String toExcelString( long _val, Context _cx )
	{
		return toExcelString( _val, _cx.scale );
	}

	public static String toExcelString( long _value, int _scale )
	{
		if (_value == 0) {
			return "0";
		}
		else if (_scale == 0) {
			return Long.toString( _value );
		}
		else {
			return stringFromBigDecimal( RuntimeBigDecimal_v1.fromScaledLong( _value, _scale ) );
		}
	}


	public static long op_EXP( final long x, final long n, Context _cx )
	{
		return _cx.fromDouble( Math.pow( _cx.toDouble( x ), _cx.toDouble( n ) ) );
	}

	public static long fun_ROUND( final long _val, final long _maxFrac, Context _cx )
	{
		if (_cx.scale == 0) return round( _val, (int) _maxFrac, _cx );
		return round( _val, (int) (_maxFrac / _cx.one), _cx );
	}

	public static long fun_TODAY( Context _cx )
	{
		return dateToNum( today(), _cx );
	}

	public static long fun_FACT( long _a )
	{
		if (_a < 0) {
			return 0; // Excel #NUM!
		}
		else {
			int a = (int) _a;
			if (a < FACTORIALS.length) {
				return FACTORIALS[ a ];
			}
			else {
				throw new ArithmeticException( "Overflow in FACT() using (scaled) long." );
			}
		}
	}

}
