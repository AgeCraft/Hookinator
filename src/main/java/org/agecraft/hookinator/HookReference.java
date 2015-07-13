package org.agecraft.hookinator;

import org.objectweb.asm.tree.MethodNode;

import codechicken.lib.asm.ObfMapping;

public class HookReference {

	public String className;
	public String name;
	public String desc;

	public String callClassName;
	public String callName;
	public String callDesc;
	public String callDescStatic;

	public ObfMapping mapping;

	public HookReference(String className, String name, String desc, String callClassName, String callName) {
		this.className = className;
		this.name = name;
		this.desc = desc;

		this.callClassName = callClassName.replace('.', '/');
		this.callName = callName;
		this.callDesc = "(L" + className.replace('.', '/') + ";" + desc.substring(1, desc.indexOf(")")) + ")Lorg/agecraft/hookinator/HookResult;";
		this.callDescStatic = desc.substring(0, desc.indexOf(")")) + ")Lorg/agecraft/hookinator/HookResult;";

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

	@Override
	public String toString() {
		return String.format("Hook[%s %s%s --> %s%s%s]", className, name, desc, callClassName.replace('/', '.'), callName, callDesc);
	}
}
