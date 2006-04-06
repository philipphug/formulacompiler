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
package sej.loader.excel.xls;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import sej.Spreadsheet;
import sej.SpreadsheetLoader;
import sej.engine.Runtime_v1;
import sej.loader.excel.ExcelLazyExpressionParser;
import sej.loader.excel.ExcelLoaderError;
import sej.model.CellIndex;
import sej.model.CellInstance;
import sej.model.CellRange;
import sej.model.CellWithConstant;
import sej.model.CellWithLazilyParsedExpression;
import sej.model.Row;
import sej.model.Sheet;
import sej.model.Workbook;

import jxl.CellType;
import jxl.DateCell;
import jxl.NumberFormulaCell;
import jxl.WorkbookSettings;


/**
 * Spreadsheet file loader implementation for the Microsoft Excel .xls format. Call the
 * {@code register()} method to register the loader with the central {@link SpreadsheetLoader}.
 * 
 * @author peo
 */
public class ExcelXLSLoader implements SpreadsheetLoader.FileLoader
{


	public static void register()
	{
		SpreadsheetLoader.registerLoader( new SpreadsheetLoader.Factory()
		{

			public SpreadsheetLoader.FileLoader newWorkbookLoader()
			{
				return new ExcelXLSLoader();
			}

			public boolean canHandle( File _file )
			{
				return _file.getName().toLowerCase().endsWith( ".xls" );
			}

		} );
	}


	public Spreadsheet loadFile( File _file ) throws IOException
	{

		jxl.Workbook xlsWorkbook;
		WorkbookSettings xlsSettings = new WorkbookSettings();
		xlsSettings.setLocale( Locale.ENGLISH );
		xlsSettings.setExcelDisplayLanguage( "EN" );
		xlsSettings.setExcelRegionalSettings( "EN" );
		try {
			xlsWorkbook = jxl.Workbook.getWorkbook( _file, xlsSettings );
		}
		catch (jxl.read.biff.BiffException e) {
			throw new ExcelLoaderError( e );
		}
		jxl.Sheet xlsSheet = xlsWorkbook.getSheet( 0 );

		Workbook workbook = new Workbook();
		Sheet sheet = new Sheet( workbook );

		loadRows( xlsSheet, sheet );
		loadNames( xlsWorkbook, workbook );

		return workbook;
	}


	private void loadRows( jxl.Sheet _xlsSheet, Sheet _sheet )
	{
		for (int iRow = 0; iRow < _xlsSheet.getRows(); iRow++) {
			jxl.Cell[] xlsRow = _xlsSheet.getRow( iRow );
			if (null == xlsRow) {
				_sheet.getRows().add( null );
			}
			else {
				Row row = new Row( _sheet );
				for (int iCell = 0; iCell < xlsRow.length; iCell++) {
					jxl.Cell xlsCell = xlsRow[ iCell ];
					if (null == xlsCell) {
						row.getCells().add( null );
					}
					else {
						loadCell( xlsCell, row );
					}
				}
			}
		}
	}


	private void loadCell( jxl.Cell _xlsCell, Row _row )
	{
		jxl.CellType xlsType = _xlsCell.getType();

		if (_xlsCell instanceof jxl.FormulaCell) {
			jxl.FormulaCell xlsFormulaCell = (jxl.FormulaCell) _xlsCell;
			CellWithLazilyParsedExpression exprCell = new CellWithLazilyParsedExpression( _row );
			try {
				exprCell.setExpressionParser( new ExcelLazyExpressionParser( exprCell, xlsFormulaCell.getFormula() ) );
			}
			catch (jxl.biff.formula.FormulaException e) {
				throw new ExcelLoaderError( e );
			}
			if (xlsFormulaCell instanceof NumberFormulaCell) {
				NumberFormulaCell xlsNumFormulaCell = ((NumberFormulaCell) xlsFormulaCell);
				exprCell.setNumberFormat( convertNumberFormat( xlsNumFormulaCell, xlsNumFormulaCell.getNumberFormat() ) );
			}
		}
		else if (jxl.CellType.EMPTY == xlsType) {
			_row.getCells().add( null );
		}
		else if (jxl.CellType.BOOLEAN == xlsType) {
			new CellWithConstant( _row, ((jxl.BooleanCell) _xlsCell).getValue() );
		}
		else if (jxl.CellType.NUMBER == xlsType) {
			jxl.NumberCell xlsNumCell = (jxl.NumberCell) _xlsCell;
			CellInstance numCell = new CellWithConstant( _row, xlsNumCell.getValue() );
			numCell.setNumberFormat( convertNumberFormat( xlsNumCell, xlsNumCell.getNumberFormat() ) );
		}
		else if (CellType.DATE == xlsType) {
			DateCell xlsDateCell = (jxl.DateCell) _xlsCell;
			new CellWithConstant( _row, Runtime_v1.dateToExcel( xlsDateCell.getDate() ) );
		}
		else if (jxl.CellType.LABEL == xlsType) {
			new CellWithConstant( _row, ((jxl.LabelCell) _xlsCell).getString() );
		}
	}


	private void loadNames( jxl.Workbook _xlsWorkbook, Workbook _workbook )
	{
		for (String name : _xlsWorkbook.getRangeNames()) {
			jxl.Range[] xlsRange = _xlsWorkbook.findByName( name );
			if (1 == xlsRange.length) {
				jxl.Cell xlsStart = xlsRange[ 0 ].getTopLeft();
				jxl.Cell xlsEnd = xlsRange[ 0 ].getBottomRight();
				CellIndex start = new CellIndex( 0, xlsStart.getColumn(), xlsStart.getRow() );
				if ((xlsStart.getColumn() == xlsEnd.getColumn()) && (xlsStart.getRow() == xlsEnd.getRow())) {
					_workbook.getNameMap().put( name, start );
				}
				else {
					CellIndex end = new CellIndex( 0, xlsEnd.getColumn(), xlsEnd.getRow() );
					CellRange range = new CellRange( start, end );
					_workbook.getNameMap().put( name, range );
				}
			}
		}
	}


	private NumberFormat convertNumberFormat( jxl.Cell _xlsCell, NumberFormat _numberFormat )
	{
		String formatString = _xlsCell.getCellFormat().getFormat().getFormatString();
		if (formatString.equals( "" )) {
			return null;
		}
		return _numberFormat;
	}


}
