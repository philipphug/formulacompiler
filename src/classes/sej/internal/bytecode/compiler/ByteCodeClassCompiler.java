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

import java.util.HashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

abstract class ByteCodeClassCompiler
{
	private final ClassWriter cw = new ClassWriter( true );
	private final ByteCodeEngineCompiler engineCompiler;


	public ByteCodeClassCompiler(ByteCodeEngineCompiler _compiler)
	{
		super();
		this.engineCompiler = _compiler;
	}


	protected ByteCodeEngineCompiler getEngineCompiler()
	{
		return this.engineCompiler;
	}

	ClassWriter cw()
	{
		return this.cw;
	}

	abstract String getClassBinaryName();

	protected String getPackageOf( String _internalName )
	{
		int p = _internalName.lastIndexOf( '/' );
		if (0 <= p) return _internalName.substring( 0, p + 1 );
		else return "";
	}


	protected Type initializeClass( Class _parentClassOrInterface, Type _parentTypeOrInterface, Type _otherInterface )
	{
		Type parentType;
		String[] interfaces;
		if (_parentClassOrInterface == null) {
			parentType = Type.getType( Object.class );
			interfaces = new String[] { _otherInterface.getInternalName() };
		}
		else if (_parentClassOrInterface.isInterface()) {
			parentType = Type.getType( Object.class );
			interfaces = new String[] { _otherInterface.getInternalName(), _parentTypeOrInterface.getInternalName() };
		}
		else {
			parentType = _parentTypeOrInterface;
			interfaces = new String[] { _otherInterface.getInternalName() };
		}
		cw().visit( Opcodes.V1_4, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, getClassBinaryName(), null,
				parentType.getInternalName(), interfaces );
		cw().visitSource( null, null );

		return parentType;
	}


	protected void finalizeClass()
	{
		cw().visitEnd();
	}


	private GeneratorAdapter initializer;

	protected GeneratorAdapter initializer()
	{
		return this.initializer;
	}

	protected void buildStaticInitializer()
	{
		MethodVisitor mv = cw().visitMethod( Opcodes.ACC_STATIC, "<clinit>", "()V", null, null );
		GeneratorAdapter ma = new GeneratorAdapter( mv, Opcodes.ACC_STATIC, "<clinit>", "()V" );
		ma.visitCode();
		this.initializer = ma;
	}

	protected void finalizeStaticInitializer()
	{
		if (this.initializer != null) {
			GeneratorAdapter ma = this.initializer;
			ma.visitInsn( Opcodes.RETURN );
			ma.visitMaxs( 0, 0 );
			ma.visitEnd();
			this.initializer = null;
		}
	}


	byte[] getClassBytes()
	{
		return cw().toByteArray();
	}


	void collectClassNamesAndBytes( HashMap<String, byte[]> _result )
	{
		_result.put( getClassBinaryName().replace( '/', '.' ), getClassBytes() );
	}

}
