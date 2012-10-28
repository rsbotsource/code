package org.powerbot.asm.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SetSuperAdapter extends ClassVisitor {
	private String superName;
	private final String newSuperName;

	public SetSuperAdapter(final ClassVisitor delegate, final String superName) {
		super(Opcodes.ASM4, delegate);
		newSuperName = superName;
	}

	@Override
	public void visit(
			final int version,
			final int access,
			final String name,
			final String signature,
			final String superName,
			final String[] interfaces) {
		this.superName = superName;
		super.visit(version, access, name, signature, newSuperName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(
			final int access,
			final String name,
			final String desc,
			final String signature,
			final String[] exceptions) {
		if (name.equals("<init>")) {
			return new MethodVisitor(Opcodes.ASM4, super.visitMethod(access, name, desc, signature, exceptions)) {
				@Override
				public void visitMethodInsn(final int opcode, String owner, final String name, final String desc) {
					if (opcode == Opcodes.INVOKESPECIAL && owner.equals(superName)) {
						owner = newSuperName;
					}
					super.visitMethodInsn(opcode, owner, name, desc);
				}
			};
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
}
