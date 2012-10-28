package org.powerbot.asm.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class AddInterfaceAdapter extends ClassVisitor {
	private final String inter;

	public AddInterfaceAdapter(final ClassVisitor delegate, final String inter) {
		super(Opcodes.ASM4, delegate);
		this.inter = inter;
	}

	@Override
	public void visit(
			final int version,
			final int access,
			final String name,
			final String signature,
			final String superName,
			final String[] interfaces) {
		final String[] inters = new String[interfaces.length + 1];
		System.arraycopy(interfaces, 0, inters, 0, interfaces.length);
		inters[interfaces.length] = inter;
		super.visit(version, access, name, signature, superName, inters);
	}
}
