package org.powerbot.asm.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.powerbot.game.loader.script.CodeReader;

public class AddMethodAdapter extends ClassVisitor {
	public static class Method {
		public int access;
		public String name;
		public String desc;
		public byte[] code;
		public int max_stack;
		public int max_locals;
	}

	private final Method[] methods;

	public AddMethodAdapter(final ClassVisitor delegate, final Method[] methods) {
		super(Opcodes.ASM4, delegate);
		this.methods = methods;
	}

	@Override
	public void visitEnd() {
		for (final Method m : methods) {
			final MethodVisitor mv = super.visitMethod(m.access, m.name, m.desc, null, null);
			mv.visitCode();
			new CodeReader(m.code).accept(mv);
			mv.visitMaxs(m.max_stack, m.max_locals);
			mv.visitEnd();
		}
		super.visitEnd();
	}
}
