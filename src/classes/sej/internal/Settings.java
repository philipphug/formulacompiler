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

import sej.internal.logging.Log;

/**
 * Holds global settings for SEJ.
 * 
 * @author peo
 */
public class Settings
{
	public static final Log LOG_LETVARS = logFor( "letVars" );
	public static final Log LOG_CONSTEVAL = logFor( "constEval" );

	public static final Log logFor( String _name )
	{
		final Log log = new Log();
		log.setEnabled( "true".equals( System.getProperty( "sej.internal.Settings.LOG." + _name + ".enabled" ) ) );
		return log;
	}

	
	private static boolean debugParserEnabled = false;

	public static boolean isDebugParserEnabled()
	{
		return debugParserEnabled;
	}

	public static void setDebugParserEnabled( boolean _debugParserEnabled )
	{
		debugParserEnabled = _debugParserEnabled;
	}

	
	private static boolean debugCompilationEnabled = false;

	public static boolean isDebugCompilationEnabled()
	{
		return debugCompilationEnabled;
	}

	public static void setDebugCompilationEnabled( boolean _debugCompilationEnabled )
	{
		debugCompilationEnabled = _debugCompilationEnabled;
	}


}
