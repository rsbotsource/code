package org.powerbot.asm.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AddGetterAdapter extends ClassVisitor implements Opcodes {
	public static class Field {
		public int getter_access;
		public String getter_name;
		public String getter_desc;
		public String owner;
		public String name;
		public String desc;
	}

	private final boolean virtual;
	private final Field[] fields;
	private String owner;

	public AddGetterAdapter(final ClassVisitor delegate, final boolean virtual, final Field[] fields) {
		super(Opcodes.ASM4, delegate);
		this.virtual = virtual;
		this.fields = fields;
	}

	@Override
	public void visit(
			final int version,
			final int access,
			final String name,
			final String signature,
			final String superName,
			final String[] interfaces) {
		owner = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public void visitEnd() {
		for (final Field f : fields) {
			visitGetter(f.getter_access, f.getter_name, f.getter_desc, virtual ? null : f.owner, f.name, f.desc);
		}
		super.visitEnd();
	}

	private void visitGetter(
			final int getter_access,
			final String getter_name,
			final String getter_desc,
			final String owner,
			final String name,
			final String desc) {
		final MethodVisitor mv = super.visitMethod(getter_access, getter_name, getter_desc, null, null);
		mv.visitCode();
		if (owner == null) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, this.owner, name, desc);
		} else {
			mv.visitFieldInsn(GETSTATIC, owner, name, desc);
		}
		final int op = getReturnOpcode(desc);
		mv.visitInsn(op);
		mv.visitMaxs(op == LRETURN || op == DRETURN ? 2 : 1, (getter_access & ACC_STATIC) == 0 ? 1 : 0);
		mv.visitEnd();
	}

	private int getReturnOpcode(String desc) {
		desc = desc.substring(desc.indexOf(")") + 1);
		if (desc.length() > 1) {
			return ARETURN;
		}
		final char c = desc.charAt(0);
		switch (c) {
		case 'I':
		case 'Z':
		case 'B':
		case 'S':
		case 'C':
			return IRETURN;
		case 'J':
			return LRETURN;
		case 'F':
			return FRETURN;
		case 'D':
			return DRETURN;
		}
		throw new RuntimeException("bad_return");
	}
}
