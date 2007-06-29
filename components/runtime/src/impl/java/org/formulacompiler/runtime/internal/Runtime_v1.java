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
package org.formulacompiler.runtime.internal;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class Runtime_v1
{

	// ---- Excel date conversion; copied from JExcelAPI (DateRecord.java)

	static final long SECS_PER_HOUR = 60 * 60;
	static final long SECS_PER_DAY = 24 * SECS_PER_HOUR;
	static final long MS_PER_SEC = 1000;
	static final long MS_PER_DAY = SECS_PER_DAY * MS_PER_SEC;
	static final int NON_LEAP_DAY = 61;
	static final int UTC_OFFSET_DAYS = 25569;
	static final int UTC_OFFSET_DAYS_1904 = 24107;
	static final boolean BASED_ON_1904 = false;


	public static byte unboxByte( Byte _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
	}

	public static short unboxShort( Short _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
	}

	public static int unboxInteger( Integer _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
	}

	public static long unboxLong( Long _boxed )
	{
		return (_boxed == null) ? 0L : _boxed;
	}

	public static float unboxFloat( Float _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
	}

	public static double unboxDouble( Double _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
	}

	public static boolean unboxBoolean( Boolean _boxed )
	{
		return (_boxed == null) ? false : _boxed;
	}

	public static char unboxCharacter( Character _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
	}


	public static Date now()
	{
		final long millis = System.currentTimeMillis();
		return new Date( millis / MS_PER_SEC * MS_PER_SEC );
	}

	public static Date today( TimeZone _timeZone )
	{
		final Calendar calendar = Calendar.getInstance( _timeZone );
		calendar.set( Calendar.HOUR_OF_DAY, 0 );
		calendar.set( Calendar.MINUTE, 0 );
		calendar.set( Calendar.SECOND, 0 );
		calendar.set( Calendar.MILLISECOND, 0 );
		return calendar.getTime();
	}

	public static long dateToMsSinceLocal1970( Date _date )
	{
		return dateToMsSinceLocal1970( _date, TimeZone.getDefault() );
	}

	public static long dateToMsSinceLocal1970( Date _date, TimeZone _timeZone )
	{
		final long msSinceUTC1970 = _date.getTime();
		final int timeZoneOffset = _timeZone.getOffset( msSinceUTC1970 );
		final long msSinceLocal1970 = msSinceUTC1970 + timeZoneOffset;
		return msSinceLocal1970;
	}


	// LATER Parse date and time values
	protected static Number parseNumber( String _text, boolean _parseBigDecimal, Locale _locale )
	{
		final String text = _text.toUpperCase( _locale );

		final NumberFormat numberFormat = NumberFormat.getNumberInstance( _locale );
		if (numberFormat instanceof DecimalFormat) {
			final DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
			decimalFormat.setParseBigDecimal( _parseBigDecimal );
		}
		Number result = parseNumber( text, numberFormat );
		if (result != null) {
			return result;
		}

		if (numberFormat instanceof DecimalFormat) {
			final DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
			final DecimalFormatSymbols formatSymbols = decimalFormat.getDecimalFormatSymbols();
			final char c = formatSymbols.getGroupingSeparator();
			if (Character.isSpaceChar( c )) {
				formatSymbols.setGroupingSeparator( '\u0020' );
				decimalFormat.setDecimalFormatSymbols( formatSymbols );
				result = parseNumber( text, decimalFormat );
				if (result != null) {
					return result;
				}
			}
		}

		final NumberFormat percentFormat = NumberFormat.getPercentInstance( _locale );
		if (numberFormat instanceof DecimalFormat) {
			final DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
			decimalFormat.setParseBigDecimal( _parseBigDecimal );
		}
		result = parseNumber( text, percentFormat );
		if (result != null) {
			return result;
		}

		final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols( _locale );
		final DecimalFormat scientificFormat = new DecimalFormat( "#0.###E0", formatSymbols );
		scientificFormat.setParseBigDecimal( _parseBigDecimal );
		result = parseNumber( text, numberFormat );
		if (result != null) {
			return result;
		}

		return null;
	}

	private static Number parseNumber( String _text, NumberFormat _numberFormat )
	{
		final ParsePosition parsePosition = new ParsePosition( 0 );
		final Number number = _numberFormat.parse( _text, parsePosition );
		final int index = parsePosition.getIndex();
		if (_text.length() == index) {
			return number;
		}
		else {
			return null;
		}
	}


	@Deprecated
	public static StringBuilder newStringBuilder( String _first )
	{
		return new StringBuilder( _first );
	}

	@Deprecated
	public static StringBuffer newStringBuffer( String _first )
	{
		return new StringBuffer( _first );
	}

	public static String stringFromObject( Object _obj )
	{
		return (_obj == null) ? "" : _obj.toString();
	}

	public static String stringFromString( String _str )
	{
		return (_str == null) ? "" : _str;
	}

	public static String stringFromBigDecimal( BigDecimal _value )
	{
		if (_value.compareTo( BigDecimal.ZERO ) == 0) return "0"; // avoid "0.0"
		final BigDecimal stripped = _value.stripTrailingZeros();
		final int scale = stripped.scale();
		final int prec = stripped.precision();
		final int ints = prec - scale;
		if (ints > 20) {
			return stripped.toString();
		}
		else {
			return stripped.toPlainString();
		}
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


	public static String emptyString()
	{
		return "";
	}

	private static String notNull( String _s )
	{
		return (_s == null) ? "" : _s;
	}


	public static String stdMID( String _s, int _start, int _len )
	{
		final int start = _start - 1;
		if (start < 0) return "";
		if (start >= _s.length()) return "";
		if (_len < 0) return "";
		final int pastEnd = (start + _len >= _s.length()) ? _s.length() : start + _len;
		return _s.substring( start, pastEnd );
	}

	public static String stdLEFT( String _s, int _len )
	{
		if (_len < 1) return "";
		if (_len >= _s.length()) return _s;
		return _s.substring( 0, _len );
	}

	public static String stdRIGHT( String _s, int _len )
	{
		if (_len < 1) return "";
		if (_len >= _s.length()) return _s;
		final int max = _s.length();
		final int len = (_len > max) ? max : _len;
		return _s.substring( max - len );
	}

	public static String stdSUBSTITUTE( String _s, String _src, String _tgt )
	{
		if (_s == null || _s.equals( "" )) return _s;
		if (_src == null || _src.equals( "" ) || _src.equals( _tgt )) return _s;
		return _s.replace( notNull( _src ), notNull( _tgt ) );
	}

	public static String stdSUBSTITUTE( String _s, String _src, String _tgt, int _occurrence )
	{
		if (_occurrence <= 0) return _s;
		if (_s == null || _s.equals( "" )) return _s;
		if (_src == null || _src == "" || _src.equals( _tgt )) return _s;
		int at = 0;
		int seen = 0;
		while (at < _s.length()) {
			final int p = _s.indexOf( _src, at );
			if (p < 0) break;
			if (++seen == _occurrence) {
				return _s.substring( 0, p ) + _tgt + _s.substring( p + _src.length() );
			}
			at = p + _src.length();
		}
		return _s;
	}

	public static String stdREPLACE( String _s, int _at, int _len, String _repl )
	{
		if (_at < 1) return "";
		if (_len < 0) return "";
		if (_s == null || _s.equals( "" )) return _repl;
		final int at = _at - 1;
		if (at >= _s.length()) return _s + _repl;
		if (at + _len >= _s.length()) return _s.substring( 0, at ) + _repl;
		return _s.substring( 0, at ) + _repl + _s.substring( at + _len );
	}

	public static boolean stdEXACT( String _a, String _b )
	{
		return _a.equals( _b );
	}

	public static int stdFIND( String _what, String _within, int _startingAt )
	{
		if (_what == null || _what.equals( "" )) return 1;
		if (_within == null || _within.equals( "" )) return 0;
		return _within.indexOf( _what, _startingAt - 1 ) + 1;
	}

	public static int stdSEARCH( String _what, String _within, int _startingAt )
	{
		if (_within == null || _within.equals( "" )) return 0;
		if (_what == null || _what.equals( "" )) return 1;
		if (_startingAt > _within.length()) return 0;

		final Pattern pattern = patternFor( _what.toLowerCase() );
		final Matcher matcher = pattern.matcher( _within.toLowerCase() );
		if (matcher.find( _startingAt - 1 )) {
			return matcher.start() + 1;
		}
		else {
			return 0;
		}
	}

	private static final Pattern patternFor( String _stringWithWildcards )
	{
		final StringBuilder src = new StringBuilder(); // using "(?i)" has trouble with umlauts
		int i = 0;
		while (i < _stringWithWildcards.length()) {
			char c = _stringWithWildcards.charAt( i++ );
			switch (c) {
				case '*':
					src.append( ".*" );
					break;
				case '?':
					src.append( "." );
					break;
				case '~':
					if (i < _stringWithWildcards.length()) {
						final char cc = _stringWithWildcards.charAt( i++ );
						appendLiteral( src, cc );
					}
					else {
						appendLiteral( src, c );
					}
					break;
				default:
					appendLiteral( src, c );
			}
		}
		return Pattern.compile( src.toString() );
	}

	private static final void appendLiteral( final StringBuilder _src, char _char )
	{
		_src.append( "\\u" );
		_src.append( Integer.toHexString( 0x10000 | _char ).substring( 1 ) );
	}


	public static String stdLOWER( String _s )
	{
		return _s.toLowerCase();
	}

	public static String stdUPPER( String _s )
	{
		return _s.toUpperCase();
	}


	public static String stdPROPER( String _s )
	{
		final StringBuilder sb = new StringBuilder();
		final String str = _s.toLowerCase();
		boolean wordMiddle = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt( i );
			if (Character.isLetter( c )) {
				if (wordMiddle) {
					sb.append( c );
				}
				else {
					sb.append( Character.toUpperCase( c ) );
					wordMiddle = true;
				}
			}
			else {
				sb.append( c );
				wordMiddle = false;
			}
		}
		return sb.toString();
	}

	public static String stdREPT( String _text, int _num )
	{
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < _num; i++) {
			sb.append( _text );
		}
		return sb.toString();
	}

	/**
	 * Strips leading and trailing blanks, and collapses runs of multiple blanks to just one.
	 */
	public static String stdTRIM( String _text )
	{
		final StringBuilder sb = new StringBuilder();
		boolean whiteSpaceMet = false;
		boolean nonWhiteSpaceMet = false;
		for (int i = 0; i < _text.length(); i++) {
			char c = _text.charAt( i );
			if (c == ' ') {
				if (nonWhiteSpaceMet) {
					whiteSpaceMet = true;
				}
			}
			else {
				nonWhiteSpaceMet = true;
				if (whiteSpaceMet) {
					sb.append( ' ' );
					whiteSpaceMet = false;
				}
				sb.append( c );
			}
		}
		return sb.toString();
	}

	public static String stdTEXT( Number _num, String _format, Environment _environment )
	{
		if ("__BOGUS__".equals( _format )) {
			// LATER This is a bogus implementation. It only serves to show how to get at the environment.
			return _num.longValue() + " in " + _environment.locale().getLanguage();
		}
		throw new IllegalArgumentException( "TEXT() is not properly supported yet." );
	}


	protected static final long[] FACTORIALS = { 1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800, 39916800,
			479001600 };


}
