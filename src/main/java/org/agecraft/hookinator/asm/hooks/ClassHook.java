package org.agecraft.hookinator.asm.hooks;

import org.agecraft.hookinator.api.IHook;
import org.objectweb.asm.tree.ClassNode;

import codechicken.lib.asm.ObfMapping;

public abstract class ClassHook implements IHook {

	public String className;

	public ObfMapping mapping;

	public ClassHook(String className) {
		this.className = className;

		if(className.startsWith("net.minecraft.")) {
			this.mapping = new ObfMapping(className.replace('.', '/')).toClassloading();
		}
	}
	
	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public abstract void apply(ClassNode node);
	
	@Override
	public String toString() {
		return String.format("ClassHook[%s]", className);
	}
}
