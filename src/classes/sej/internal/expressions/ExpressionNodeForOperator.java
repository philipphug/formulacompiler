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
package sej.internal.expressions;

import java.io.IOException;

import sej.Operator;
import sej.describable.DescriptionBuilder;


public class ExpressionNodeForOperator extends ExpressionNode
{
	private final Operator operator;


	public ExpressionNodeForOperator(Operator _operator, ExpressionNode... _args)
	{
		super();
		this.operator = _operator;
		for (ExpressionNode arg : _args) {
			arguments().add( arg );
		}
	}


	public Operator getOperator()
	{
		return this.operator;
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForOperator( this.operator );
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		switch (arguments().size()) {

		case 0:
			_to.append( this.operator.getSymbol() );
			break;
		case 1:
			_to.append( "(" );
			if (this.operator.isPrefix()) _to.append( this.operator.getSymbol() );
			describeArgumentTo( _to, 0 );
			if (!this.operator.isPrefix()) _to.append( this.operator.getSymbol() );
			_to.append( ")" );
			break;
		case 2:
			_to.append( "(" );
			describeArgumentTo( _to, 0 );
			_to.append( " " );
			_to.append( this.operator.getSymbol() );
			_to.append( " " );
			describeArgumentTo( _to, 1 );
			_to.append( ")" );
			break;
		}
	}

}
