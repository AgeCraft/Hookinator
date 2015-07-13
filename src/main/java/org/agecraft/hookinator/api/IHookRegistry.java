package org.agecraft.hookinator.api;

public interface IHookRegistry {

	void addHook(String className, String name, String desc, String callClassName, String callName);
}
