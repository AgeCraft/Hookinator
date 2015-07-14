package org.agecraft.hookinator.asm.hooks;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import codechicken.lib.asm.ObfMapping;

public abstract class MethodHook extends ClassHook {

	public String name;
	public String desc;

	public MethodHook(String className, String name, String desc) {
		super(className);

		this.name = name;
		this.desc = desc;

		if(className.startsWith("net.minecraft.")) {
			this.mapping = new ObfMapping(className.replace('.', '/'), name, desc).toClassloading();
		}
	}

	public boolean matches(MethodNode method) {
		if(mapping != null && mapping.matches(method)) {
			return true;
		}
		return name.equals(method.name) && desc.equals(method.desc);
	}
	
	public MethodNode findMethod(ClassNode node) {
		for(MethodNode method : node.methods) {
			if(matches(method)) {
				return method;
			}
		}
		throw new RuntimeException("Method not found: " + toString());
	}

	@Override
	public void apply(ClassNode node) {
		apply(node, findMethod(node));
	}

	public abstract void apply(ClassNode node, MethodNode method);

	@Override
	public String toString() {
		return String.format("MethodHook[%s %s%s]", className, name, desc);
	}
}
