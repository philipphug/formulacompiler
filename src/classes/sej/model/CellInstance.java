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
package sej.model;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;

import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;
import sej.engine.expressions.EvaluationContext;
import sej.engine.expressions.EvaluationFailed;
import sej.engine.expressions.ExpressionNode;


public abstract class CellInstance extends AbstractDescribable
{
	private final Row row;
	private final int columnIndex;
	private NumberFormat numberFormat;


	public CellInstance(Row _row)
	{
		this.row = _row;
		this.columnIndex = _row.getCells().size();
		_row.getCells().add( this );
	}


	public Row getRow()
	{
		return this.row;
	}


	public int getColumnIndex()
	{
		return this.columnIndex;
	}


	public NumberFormat getNumberFormat()
	{
		return this.numberFormat;
	}


	public void setNumberFormat( NumberFormat _numberFormat )
	{
		this.numberFormat = _numberFormat;
	}


	public abstract ExpressionNode getExpression();


	public abstract Object getValue();


	final Object evaluate( EvaluationContext _context ) throws EvaluationFailed, InvocationTargetException
	{
		return this.innerEvaluate( _context );
	}


	protected abstract Object innerEvaluate( EvaluationContext _context ) throws EvaluationFailed, InvocationTargetException;


	public String getCanonicalName()
	{
		return Sheet.getCanonicalNameForCellIndex( getColumnIndex(), getRow().getRowIndex() );
	}


	public CellIndex getCellIndex()
	{
		int iCol = getColumnIndex();
		int iRow = this.row.getRowIndex();
		Sheet sheet = this.row.getSheet();
		int iSheet = sheet.getSheetIndex();
		return new CellIndex( iSheet, iCol, iRow );
	}


	public abstract CellInstance cloneInto( Row _row );


	protected void copyTo( CellInstance _other )
	{
		_other.numberFormat = this.numberFormat;
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.append( "<cell id=\"" );
		_to.append( getCanonicalName() );
		_to.append( "\">" );
		_to.newLine();
		_to.indent();
		describeDefinitionTo( _to );
		_to.outdent();
		_to.appendLine( "</cell>" );
	}


	protected abstract void describeDefinitionTo( DescriptionBuilder _to ) throws IOException;


}
