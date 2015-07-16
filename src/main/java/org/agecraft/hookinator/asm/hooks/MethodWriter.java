package org.agecraft.hookinator.asm.hooks;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import codechicken.lib.asm.ASMBlock;

public class MethodWriter extends MethodHook {

	public int access;
	public String[] exceptions;
	public InsnList list;

	public MethodWriter(String className, String name, String desc, int access, String[] exceptions, InsnList list) {
		super(className, name, desc);

		this.access = access;
		this.exceptions = exceptions;
		this.list = list;
	}

	public MethodWriter(String className, String name, String desc, int access, String[] exceptions, ASMBlock block) {
		this(className, name, desc, access, exceptions, block.rawListCopy());
	}

	public MethodWriter(String className, String name, String desc, int access, String[] exceptions) {
		this(className, name, desc, access, exceptions, (InsnList) null);
	}

	@Override
	public void apply(ClassNode node) {
		apply(node, (MethodNode) node.visitMethod(access, name, desc, null, exceptions));
	}

	@Override
	public void apply(ClassNode node, MethodNode method) {
		if(list != null) {
			list.accept(method);
		}
	}
}
