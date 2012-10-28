package org.powerbot.asm.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Huan
 */
public class OverrideClassAdapter extends ClassVisitor {
	private final String old_clazz;
	private final String new_clazz;

	public OverrideClassAdapter(final ClassVisitor delegate, final String old_clazz, final String new_clazz) {
		super(Opcodes.ASM4, delegate);
		this.old_clazz = old_clazz;
		this.new_clazz = new_clazz;
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, String desc, final String signature, final String[] exceptions) {
		if (desc.contains("L" + old_clazz + ";")) {
			desc = desc.replace("L" + old_clazz + ";", "L" + new_clazz + ";");
		}
		return new MethodAdapter(super.visitMethod(access, name, desc, signature, exceptions), old_clazz, new_clazz);
	}

	@Override
	public FieldVisitor visitField(final int access, final String name, String desc, final String signature, final Object value) {
		if (desc.equals("L" + old_clazz + ";")) {
			desc = "L" + new_clazz + ";";
		}

		return super.visitField(access, name, desc, signature, value);
	}

	static class MethodAdapter extends MethodVisitor {
		private final String old_clazz;
		private final String new_clazz;

		MethodAdapter(
				final MethodVisitor delegate,
				final String old_clazz,
				final String new_clazz) {
			super(Opcodes.ASM4, delegate);
			this.old_clazz = old_clazz;
			this.new_clazz = new_clazz;
		}

		@Override
		public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
		}

		@Override
		public void visitTypeInsn(final int opcode, String type) {
			if (type.equals(old_clazz)) {
				type = new_clazz;
			}
			super.visitTypeInsn(opcode, type);
		}

		@Override
		public void visitFieldInsn(final int opcode, final String owner, final String name, String desc) {
			if (desc.contains("L" + old_clazz + ";")) {
				desc = desc.replace("L" + old_clazz + ";", "L" + new_clazz + ";");
			}
			super.visitFieldInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitMethodInsn(final int opcode, String owner, final String name, String desc) {
			if (owner.equals(old_clazz)) {
				owner = new_clazz;
			}
			if (desc.contains("L" + old_clazz + ";")) {
				desc = desc.replace("L" + old_clazz + ";", "L" + new_clazz + ";");
			}
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitLineNumber(final int line, final Label start) {
		}
	}
}