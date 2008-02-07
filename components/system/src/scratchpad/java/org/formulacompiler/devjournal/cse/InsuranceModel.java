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
package org.formulacompiler.devjournal.cse;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.Debug;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

public final class InsuranceModel
{

	public static void main( String[] args ) throws Exception
	{
		new InsuranceModel().run();
	}


	private final void run() throws Exception
	{
		EngineBuilder b = SpreadsheetCompiler.newEngineBuilder();
		b.loadSpreadsheet( "src/scratchpad/java/org/formulacompiler/devjournal/cse/InsuranceModel.xls" );
		b.setInputClass( Inputs.class );
		b.setOutputClass( Outputs.class );
		b.getByNameBinder().inputs().bindAllMethodsToNamedCells();
		b.getByNameBinder().outputs().bindAllMethodsToNamedCells();
		SaveableEngine e = b.compile();

		Debug.saveEngine( e, "temp/scratchpad/cse.jar" );

		ComputationFactory f = e.getComputationFactory();

		int[] ages = new int[] { 21, 24, 28, 34, 51, 57, 69 };
		double[] amts = new double[] { 3000.0, 10000.0, 15000.0 };
		for (int age : ages) {
			for (double amt : amts) {
				Outputs o = (Outputs) f.newComputation( new Inputs( age, amt ) );
				double cost = o.getInsuranceCostPerYear();
				System.out.printf( "Insuring $%.2f at age %d costs $%.2f per year.\n", amt, age, cost );
			}
		}
	}


	public static final class Inputs
	{
		private int age;
		private double amt;

		public Inputs( int _age, double _amt )
		{
			this.age = _age;
			this.amt = _amt;
		}

		public double getInsuredAmount()
		{
			return this.amt;
		}

		public int getAge()
		{
			return this.age;
		}

	}


	public static interface Outputs
	{
		double getInsuranceCostPerYear();
	}


	// ---- OutputsCurrent
	public static final class OutputsCurrent implements Outputs
	{
		private static final double[] COST_TABLE = { 150, 180, 240 /* ... all of 37 elts */};
		private final Inputs i;

		public OutputsCurrent( Inputs _inputs )
		{
			this.i = _inputs;
		}

		private double getAge()
		{
			return this.i.getAge();
		}

		private double getInsuredAmount()
		{
			return this.i.getInsuredAmount();
		}

		public double getInsuranceCostPerYear()
		{
			int r;
			switch (r = ((int) getMatchingRow()) - 1) {

				case 27:
					return getInsuredAmount() * (0.02 + Math.abs( getAge() - 50 ) / 25 * 0.01);

				case 28:
					return getInsuredAmount() * 0.03;
				case 29:
					return getInsuredAmount() * 0.03;
				case 30:
					return getInsuredAmount() * 0.03;
				case 31:
					return getInsuredAmount() * 0.04;
				case 32:
					return getInsuredAmount() * 0.04;
				case 33:
					return getInsuredAmount() * 0.04;

					/* ... for all rows of this form */

				default:
					if (r >= 0 && r < COST_TABLE.length) {
						return COST_TABLE[ r ];
					}
					return 0;
			}
		}

		private final double getMatchingRow()
		{
			int r = 1;

			// This is what really happens:
			if (((getAge() > 20.0 || getInsuredAmount() > 5000.0) ? 1.0 : 0.0) != 0.0) {
				r++;

				// This is what could be made to happen with a bit of peephole optimization:
				if (getAge() > 20.0 || getInsuredAmount() > 6000.0) {
					r++;

					if (getAge() > 20.0 || getInsuredAmount() > 8000.0) {
						r++;

						// ... for all of 37 cases */

					}
					else {
						r = 0;
					}
				}
			}

			return r;
		}

	}

	// ---- OutputsCurrent


	// ---- OutputsDesired
	public static final class OutputsDesired implements Outputs
	{
		private static final double[] COST_TABLE = { 150, 180, 240 /* ... all of 37 elts */};
		private static final double[] PERCENT_TABLE = { .03, .03, .03 /* ... all of 37 elts */};
		private static final double[] AGE_UPTO = { 20, 20, 20 /* ... all of 37 elts */};
		private static final double[] INSURED_AMOUNT_UPTO = { 5000, 6000, 8000 };

		private final Inputs i;

		public OutputsDesired( Inputs _inputs )
		{
			this.i = _inputs;
		}

		private double getAge()
		{
			return this.i.getAge();
		}

		private double getInsuredAmount()
		{
			return this.i.getInsuredAmount();
		}

		public double getInsuranceCostPerYear()
		{
			int r;
			switch (r = ((int) getMatchingRow()) - 1) {

				case 27:
					return getInsuredAmount() * (0.02 + Math.abs( getAge() - 50 ) / 25 * 0.01);

				case 28:
				case 29:
				case 30:
				case 31:
				case 32:
				case 33:
					return getStyle3( 0, r + 1, 7 );

				default:
					if (r >= 0 && r < COST_TABLE.length) {
						return COST_TABLE[ r ];
					}
					return 0;

			}
		}

		private double getStyle3( int _s, int _r, int _c )
		{
			return getInsuredAmount() * PERCENT_TABLE[ _r - 13 ];
		}

		private final double getMatchingRow()
		{
			for (int r = 13; r < 50; r++) {
				if (getMatch( 1, r, 3 )) return r;
			}
			return 0;
		}

		/**
		 * The boolean return here is again a peephole optimization.
		 */
		private final boolean getMatch( int _s, int _r, int _c )
		{
			return (getAge() <= getAgeUpto( _s, _r, _c + 1 ) && getInsuredAmount() <= getInsuredAmountUpto( _s, _r, _c + 2 ));
		}

		/**
		 * If AFC sees that all the cells that can be referenced relatively are constant, then there
		 * is no switch, just a table.
		 */
		private double getAgeUpto( int _s, int _r, int _c )
		{
			return AGE_UPTO[ _r - 13 ];
		}

		private double getInsuredAmountUpto( int _s, int _r, int _c )
		{
			return INSURED_AMOUNT_UPTO[ _r - 13 ];
		}

	}
	// ---- OutputsDesired


}
