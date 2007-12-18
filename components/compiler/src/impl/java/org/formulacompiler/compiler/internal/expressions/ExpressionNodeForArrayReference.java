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
package org.formulacompiler.compiler.internal.expressions;

import java.io.IOException;
import java.util.Collection;
import java.util.ListIterator;

import org.formulacompiler.describable.DescriptionBuilder;


public final class ExpressionNodeForArrayReference extends ExpressionNode
{
	private final ArrayDescriptor arrayDescriptor;

	public ExpressionNodeForArrayReference( ArrayDescriptor _descriptor, ExpressionNode... _args )
	{
		super( _args );
		this.arrayDescriptor = _descriptor;
	}


	public ArrayDescriptor arrayDescriptor()
	{
		return this.arrayDescriptor;
	}


	@Override
	public boolean hasConstantValue()
	{
		return areConstant( arguments() );
	}
	
	@Override
	public Object getConstantValue()
	{
		return this;
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForArrayReference( new ArrayDescriptor( arrayDescriptor() ) );
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		return arrayDescriptor().numberOfElements();
	}


	@Override
	public void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		arrayDescriptor().describeTo( _to );
		_to.append( '{' );
		boolean isFirst = true;
		for (ExpressionNode arg : arguments()) {
			if (isFirst) isFirst = false;
			else _to.append( ", " );
			arg.describeTo( _to, _cfg );
		}
		_to.append( '}' );
	}


	public final ExpressionNodeForArrayReference subArray( int _firstRow, int _nRows, int _firstCol, int _nCols )
	{
		final ArrayDescriptor myDesc = arrayDescriptor();
		final int myCols = myDesc.numberOfColumns();
		if (myDesc.numberOfSheets() != 1)
			throw new IllegalArgumentException( "Cannot handle arrays spanning sheets in subArray()" );

		final ArrayDescriptor subDesc = new ArrayDescriptor( myDesc, _firstRow, _firstCol,
				_nRows - myDesc.extent().row(), _nCols - myDesc.extent().col() );
		final ExpressionNodeForArrayReference sub = new ExpressionNodeForArrayReference( subDesc );

		final ListIterator<ExpressionNode> vals = arguments().listIterator( _firstRow * myCols );
		final int lastCol = _firstCol + _nCols - 1;
		for (int iRow = 0; iRow < _nRows; iRow++) {
			for (int iCol = 0; iCol < myCols; iCol++) {
				final ExpressionNode val = vals.next();
				if (_firstCol <= iCol && iCol <= lastCol) {
					sub.addArgument( val );
				}
			}
		}
		return sub;
	}
}
