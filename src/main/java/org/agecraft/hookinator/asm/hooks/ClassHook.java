package org.agecraft.hookinator.asm.hooks;

import org.objectweb.asm.tree.ClassNode;

import codechicken.lib.asm.ObfMapping;

public abstract class ClassHook {

	public String className;

	public ObfMapping mapping;

	public ClassHook(String className) {
		this.className = className;

		if(className.startsWith("net.minecraft.")) {
			this.mapping = new ObfMapping(className.replace('.', '/')).toClassloading();
		}
	}

	public abstract void apply(ClassNode node);

	@Override
	public String toString() {
		return String.format("ClassHook[%s]", className);
	}
}
