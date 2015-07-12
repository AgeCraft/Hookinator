package org.agecraft.hookinator;

import org.objectweb.asm.tree.MethodNode;

import codechicken.lib.asm.ObfMapping;

public class HookReference {

	public ObfMapping mapping;
	
	public String className;
	public String name;
	public String desc;
	
	public String callClassName;
	public String callName;
	public String callDesc;
	
	public HookReference(String className, String name, String desc, String callClassName, String callName) {
		this.className = className;
		this.name = name;
		this.desc = desc;
		
		this.callClassName = callClassName;
		this.callName = callName;
		this.callDesc = desc; //TODO: insert instance parameter if not static
		
		//TODO: create mapping if base class
		
//		this.hook = hook;
//		this.mapping = hook.className().startsWith("net.minecraft.") ? new ObfMapping(hook.className().replace('.', '/'), hook.name(), hook.desc()) : null;
//		
//		this.callClassName = methodClass.getName().replace('.', '/');
//		this.callName = method.getName();
//		Class<?>[] parameters = method.getParameterTypes();
//		StringBuilder sb = new StringBuilder();
//		for(int i = 0; i < parameters.length; i++) {
//			if(parameters[i].isPrimitive() || parameters[i].isArray()) {
//				sb.append(parameters[i].getName());
//			} else {
//				sb.append("L");
//				sb.append(parameters[i].getName().replace('.', '/'));
//				sb.append(";");
//			}
//		}
//		this.callDesc = sb.toString();
	}
	
	public boolean matches(MethodNode method) {
		if(mapping != null && mapping.matches(method)) {
			return true;
		}
		return name.equals(method.name) && desc.equals(method.desc);
	}
}
