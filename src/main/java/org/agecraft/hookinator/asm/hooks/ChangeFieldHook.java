package org.agecraft.hookinator.asm.hooks;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class ChangeFieldHook extends FieldHook {

	public int newAccess;
	public String newDesc;
	public Object newValue;
	
	public ChangeFieldHook(String className, String name, String desc, int newAccess, String newDesc, Object newValue) {
		super(className, name, desc);
		
		this.newAccess = newAccess;
		this.newDesc = newDesc;
		this.newValue = newValue;
		
		if(newValue != null && !(newValue instanceof Integer || newValue instanceof Double || newValue instanceof Float || newValue instanceof Long || newValue instanceof String)) {
			throw new RuntimeException("The new field value must be an instance of Integer, Double, Float, Long or String to be changed this way");
		}
	}
	
	public ChangeFieldHook(String className, String name, String desc, int newAccess) {
		this(className, name, desc, newAccess, null, null);
	}
	
	public ChangeFieldHook(String className, String name, String desc, String newDesc) {
		this(className, name, desc, -1, newDesc, null);
	}
	
	public ChangeFieldHook(String className, String name, String desc, Object newValue) {
		this(className, name, desc, -1, null, newValue);
	}
	
	public ChangeFieldHook(String className, String name, String desc, int newAccess, String newDesc) {
		this(className, name, desc, newAccess, newDesc, null);
	}
	
	public ChangeFieldHook(String className, String name, String desc, int newAccess, Object newValue) {
		this(className, name, desc, newAccess, null, newValue);
	}
	
	public ChangeFieldHook(String className, String name, String desc, String newDesc, Object newValue) {
		this(className, name, desc, -1, newDesc, newValue);
	}

	@Override
	public void apply(ClassNode node, FieldNode field) {
		if(newAccess != -1) {
			field.access = newAccess;
		}
		if(newDesc != null) {
			field.desc = newDesc;
		}
		if(newValue != null) {
			if((field.access & Opcodes.ACC_STATIC) == 0) {
				throw new RuntimeException("Only the value of static fields can be changed this way");
			}
			field.value = newValue;
		}
	}
	
	@Override
	public String toString() {
		return String.format("ChangeFieldHook[%s %s %s --> %s (access: %s, value: %s)]", className, name, desc, newDesc, Integer.toString(newAccess), newValue == null ? "null" : newValue.toString());
	}
}
