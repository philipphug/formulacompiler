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
package sej.internal.bytecode.compiler;

import java.util.List;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.model.ExpressionNodeForRangeValue;

class ByteCodeHelperCompilerForIndex extends ByteCodeHelperCompiler
{
	private final ExpressionNodeForFunction node;


	ByteCodeHelperCompilerForIndex(ByteCodeSectionCompiler _section, ExpressionNodeForFunction _node)
	{
		super( _section );
		this.node = _node;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final List<ExpressionNode> args = this.node.arguments();
		if (args.size() > 0) {
			final ExpressionNode firstArg = args.get( 0 );
			if (firstArg instanceof ExpressionNodeForRangeValue) {
				final ExpressionNodeForRangeValue rangeNode = (ExpressionNodeForRangeValue) firstArg;
				switch (this.node.cardinality()) {

					case 2:
						compileOneDimensionalIndexFunction( rangeNode, args.get( 1 ) );
						return;

					case 3:
						if (args.get( 1 ) == null) {
							compileOneDimensionalIndexFunction( rangeNode, args.get( 2 ) );
							return;
						}
						break;

				}
			}
		}
		unsupported( this.node );
	}


	private void compileOneDimensionalIndexFunction( ExpressionNodeForRangeValue _rangeNode, ExpressionNode _indexNode )
			throws CompilerException
	{
		final List<ExpressionNode> vals = _rangeNode.arguments();

		final ByteCodeNumericType num = section().numericType();
		final Type numType = num.type();
		final String arrayFieldName = methodName() + "_Consts";
		final String arrayType = "[" + num.descriptor();

		compileStaticArrayField( vals, arrayFieldName );

		final GeneratorAdapter mv = mv();

		// final int i = <idx-expr> - 1;
		final int l_i = mv.newLocal( Type.INT_TYPE );
		compileExpr( _indexNode );
		num.compileIntFromNum( mv );
		mv.push( 1 );
		mv.visitInsn( Opcodes.ISUB );
		mv.storeLocal( l_i );

		// gen switch

		// return (i >= 0 && i < getStaticIndex_Consts.length) ? getStaticIndex_Consts[ i ] : 0;
		final Label outOfRange = mv.newLabel();
		mv.loadLocal( l_i );
		mv.visitJumpInsn( Opcodes.IFLT, outOfRange );

		mv.loadLocal( l_i );
		mv.visitFieldInsn( Opcodes.GETSTATIC, section().classInternalName(), arrayFieldName, arrayType );
		mv.arrayLength();
		mv.visitJumpInsn( Opcodes.IF_ICMPGE, outOfRange );

		mv.visitFieldInsn( Opcodes.GETSTATIC, section().classInternalName(), arrayFieldName, arrayType );
		mv.loadLocal( l_i );
		mv.arrayLoad( numType );
		mv.visitInsn( num.returnOpcode() );

		mv.mark( outOfRange );
		num.compileZero( mv );
	}


	private void compileStaticArrayField( final List<ExpressionNode> _vals, final String _name )
			throws CompilerException
	{
		final int n = _vals.size();

		final ByteCodeNumericType num = section().numericType();
		final Type numType = num.type();
		final String arrayType = "[" + num.descriptor();

		// private final static double[] xy
		final FieldVisitor fv = cw().visitField( Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC, _name,
				arrayType, null, null );
		fv.visitEnd();

		// ... new double[ n ]
		final GeneratorAdapter ci = section().initializer();
		ci.push( n );
		ci.newArray( numType );

		// ... { c1, c2, ... }
		int i = 0;
		for (ExpressionNode val : _vals) {
			ci.visitInsn( Opcodes.DUP );
			ci.visitIntInsn( Opcodes.BIPUSH, i++ );
			if (val instanceof ExpressionNodeForConstantValue) {
				ExpressionNodeForConstantValue constVal = (ExpressionNodeForConstantValue) val;
				num.compileConst( ci, constVal.getValue() );
			}
			else {
				num.compileZero( ci ); // FIXME is this necessary?
			}
			ci.arrayStore( numType );
		}

		// ... xy *=* new double[] { ... }
		ci.visitFieldInsn( Opcodes.PUTSTATIC, section().classInternalName(), _name, arrayType );
	}


}
