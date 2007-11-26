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
package org.formulacompiler.compiler.internal.bytecode;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public class ArrayAccessorForFullDataCompiler extends ArrayAccessorCompiler
{

	public ArrayAccessorForFullDataCompiler( SectionCompiler _section, String _name,
			ExpressionNodeForArrayReference _node )
	{
		super( _section, "$arr$" + _name, _node );
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final DataType eltDataType = this.arrayNode.getDataType();
		final ExpressionCompiler eltCompiler = expressionCompiler( eltDataType );
		final Type eltType = eltCompiler.type();
		final String initName = methodName() + "$init";
		final String initDesc = "Z";
		
		// private boolean xy$init;
		final FieldVisitor fv = cw().visitField( Opcodes.ACC_PRIVATE, initName, initDesc, null, null );
		fv.visitEnd();

		// if (!this.xy$init) {
		final Label skipInit = mv.newLabel();
		mv.loadThis();
		mv.visitFieldInsn( Opcodes.GETFIELD, section().classInternalName(), initName, initDesc );
		mv.visitJumpInsn( Opcodes.IFNE, skipInit );

		// this.xy$init = true;
		mv.loadThis();
		mv.push( true );
		mv.visitFieldInsn( Opcodes.PUTFIELD, section().classInternalName(), initName, initDesc );
		
		// this.xy = { ?, c1, c2, ... }
		mv.loadThis();
		section().getArrayAccessorForConstDataOnly( this.arrayNode ).compileCall( mv );
		int i = 0;
		for (ExpressionNode elt : this.arrayNode.arguments()) {
			if (!(elt instanceof ExpressionNodeForConstantValue)) {
				mv.visitInsn( Opcodes.DUP );
				mv.visitIntInsn( Opcodes.BIPUSH, i );
				eltCompiler.compile( elt );
				mv.arrayStore( eltType );
			}
			i++;
		}
		// return this.xy;
		mv.visitInsn( Opcodes.ARETURN );

		// } else
		// return this.xy;
		mv.mark( skipInit );
		mv.loadThis();
		section().getArrayAccessorForConstDataOnly( this.arrayNode ).compileCall( mv );
		mv.visitInsn( Opcodes.ARETURN );
		
		if (section().hasReset()) {
			GeneratorAdapter reset = section().resetter();
			// this.xy$init = false;
			reset.loadThis();
			reset.push( false );
			reset.visitFieldInsn( Opcodes.PUTFIELD, section().classInternalName(), initName, initDesc );
		}
		
	}
	
	
}
