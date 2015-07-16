package org.agecraft.hookinator.asm.hooks;

import java.util.Iterator;

import org.agecraft.hookinator.asm.CorePlugin;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import codechicken.lib.asm.ASMBlock;

public class InsertBeforeReturn extends MethodHook {

	public ASMBlock insertion;

	public InsertBeforeReturn(String className, String name, String desc, ASMBlock insertion) {
		super(className, name, desc);

		this.insertion = insertion;
	}

	@Override
	public void apply(ClassNode node, MethodNode method) {
		CorePlugin.logger.debug(String.format("Inserting instructions before each return in method %s %s%s", className, name, desc));

		Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
		while(iterator.hasNext()) {
			AbstractInsnNode insn = iterator.next();
			switch(insn.getOpcode()) {
				case Opcodes.RETURN:
				case Opcodes.ARETURN:
				case Opcodes.IRETURN:
				case Opcodes.DRETURN:
				case Opcodes.FRETURN:
				case Opcodes.LRETURN:
					method.instructions.insertBefore(insn, insertion.rawListCopy());
					break;
			}
		}
	}
}
