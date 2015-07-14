package org.agecraft.hookinator.asm.hooks;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import codechicken.lib.asm.ObfMapping;

public abstract class FieldHook extends ClassHook {

	public String name;
	public String desc;

	public FieldHook(String className, String name, String desc) {
		super(className);

		this.name = name;
		this.desc = desc;

		if(className.startsWith("net.minecraft.")) {
			this.mapping = new ObfMapping(className.replace('.', '/'), name, desc).toClassloading();
		}
	}

	public boolean matches(FieldNode field) {
		if(mapping != null && mapping.matches(field)) {
			return true;
		}
		return name.equals(field.name) && desc.equals(field.desc);
	}

	@Override
	public void apply(ClassNode node) {
		for(FieldNode field : node.fields) {
			if(matches(field)) {
				apply(node, field);
			}
		}
	}

	public abstract void apply(ClassNode node, FieldNode field);

	@Override
	public String toString() {
		return String.format("FieldHook[%s %s %s]", className, name, desc);
	}
}
