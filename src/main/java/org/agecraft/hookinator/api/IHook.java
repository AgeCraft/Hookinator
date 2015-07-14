package org.agecraft.hookinator.api;

import org.objectweb.asm.tree.ClassNode;

public interface IHook {

	String getClassName();

	void apply(ClassNode node);
}
