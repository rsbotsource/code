package org.powerbot.asm.visitor;

import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.powerbot.game.loader.script.CodeReader;

public class InsertCodeAdapter extends ClassVisitor {
	private final String method_name;
	private final String method_desc;
	private final Map<Integer, byte[]> fragments;
	private final int max_locals;
	private final int max_stack;

	public InsertCodeAdapter(
			final ClassVisitor delegate,
			final String method_name,
			final String method_desc,
			final Map<Integer, byte[]> fragments,
			final int max_locals,
			final int max_stack) {
		super(Opcodes.ASM4, delegate);
		this.method_name = method_name;
		this.method_desc = method_desc;
		this.fragments = fragments;
		this.max_locals = max_locals;
		this.max_stack = max_stack;
	}

	@Override
	public MethodVisitor visitMethod(
			final int access,
			final String name,
			final String desc,
			final String signature,
			final String[] exceptions) {
		final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (name.equals(method_name) && desc.equals(method_desc)) {
			return new FragmentVisitor(mv, fragments, max_locals, max_stack);
		}
		return mv;
	}

	private static class FragmentVisitor extends MethodVisitor {
		private FragmentVisitor(
				final MethodVisitor delegate,
				final Map<Integer, byte[]> fragments,
				final int max_locals,
				final int max_stack) {
			super(Opcodes.ASM4, delegate);
			this.fragments = fragments;
			this.max_locals = max_locals;
			this.max_stack = max_stack;
		}

		private final Map<Integer, byte[]> fragments;
		private final int max_locals;
		private final int max_stack;

		private int idx = 0;

		@Override
		public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
		}

		@Override
		public void visitInsn(final int opcode) {
			checkFragments();
			super.visitInsn(opcode);
		}

		@Override
		public void visitIntInsn(final int opcode, final int operand) {
			checkFragments();
			super.visitIntInsn(opcode, operand);
		}

		@Override
		public void visitVarInsn(final int opcode, final int var) {
			checkFragments();
			super.visitVarInsn(opcode, var);
		}

		@Override
		public void visitTypeInsn(final int opcode, final String type) {
			checkFragments();
			super.visitTypeInsn(opcode, type);
		}

		@Override
		public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
			checkFragments();
			super.visitFieldInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
			checkFragments();
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitJumpInsn(final int opcode, final Label label) {
			checkFragments();
			super.visitJumpInsn(opcode, label);
		}

		@Override
		public void visitLabel(final Label label) {
			checkFragments();
			super.visitLabel(label);
		}

		@Override
		public void visitLdcInsn(final Object cst) {
			checkFragments();
			super.visitLdcInsn(cst);
		}

		@Override
		public void visitIincInsn(final int var, final int increment) {
			checkFragments();
			super.visitIincInsn(var, increment);
		}

		@Override
		public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
			checkFragments();
			super.visitTableSwitchInsn(min, max, dflt, labels);
		}

		@Override
		public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
			checkFragments();
			super.visitLookupSwitchInsn(dflt, keys, labels);
		}

		@Override
		public void visitMultiANewArrayInsn(final String desc, final int dims) {
			checkFragments();
			super.visitMultiANewArrayInsn(desc, dims);
		}

		@Override
		public void visitLineNumber(final int line, final Label start) {
		}

		@Override
		public void visitMaxs(final int maxStack, final int maxLocals) {
			if (max_stack == -1) {
				super.visitMaxs(maxStack, maxLocals);
			} else {
				super.visitMaxs(max_stack, max_locals);
			}
		}

		private void checkFragments() {
			if (fragments.containsKey(++idx)) {
				new CodeReader(fragments.get(idx)).accept(mv);
			}
		}
	}
}