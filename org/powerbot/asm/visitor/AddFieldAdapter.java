package org.powerbot.asm.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class AddFieldAdapter extends ClassVisitor {
	public static class Field {
		public int access;
		public String name;
		public String desc;
	}

	private final Field[] fields;

	public AddFieldAdapter(final ClassVisitor delegate, final Field[] fields) {
		super(Opcodes.ASM4, delegate);
		this.fields = fields;
	}

	@Override
	public void visitEnd() {
		for (final Field f : fields) {
			super.visitField(f.access, f.name, f.desc, null, null);
		}
		super.visitEnd();
	}
}
