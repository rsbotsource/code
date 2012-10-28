package org.powerbot.asm.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SetSignatureAdapter extends ClassVisitor {
	public static class Signature {
		public String name;
		public String desc;
		public int new_access;
		public String new_name;
		public String new_desc;
	}

	private final Signature[] signatures;

	public SetSignatureAdapter(final ClassVisitor delegate, final Signature[] signatures) {
		super(Opcodes.ASM4, delegate);
		this.signatures = signatures;
	}

	@Override
	public MethodVisitor visitMethod(
			final int access,
			final String name,
			final String desc,
			final String signature,
			final String[] exceptions) {
		for (final Signature s : signatures) {
			if (s.name.equals(name) && (s.desc.equals("") || s.desc.equals(desc))) {
				return super.visitMethod(
						s.new_access == -1 ? access : s.new_access,
						s.new_name,
						s.new_desc.equals("") ? desc : s.new_desc,
						signature,
						exceptions
				);
			}
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
}
