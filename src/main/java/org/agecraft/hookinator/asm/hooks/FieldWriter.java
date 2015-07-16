package org.agecraft.hookinator.asm.hooks;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class FieldWriter extends FieldHook {

	public int access;
	public Object value;

	public FieldWriter(String className, String name, String desc, int access, Object value) {
		super(className, name, desc);

		this.access = access;
		this.value = value;

		if(value != null && !(value instanceof Integer || value instanceof Double || value instanceof Float || value instanceof Long || value instanceof String)) {
			throw new RuntimeException("The field value must be an instance of Integer, Double, Float, Long or String to be created this way");
		}
	}

	public FieldWriter(String className, String name, String desc, int access) {
		this(className, name, desc, access, null);
	}

	@Override
	public void apply(ClassNode node) {
		node.visitField(access, name, desc, null, value);
	}

	@Override
	public void apply(ClassNode node, FieldNode field) {
	}
}
