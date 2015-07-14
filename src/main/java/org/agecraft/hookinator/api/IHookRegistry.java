package org.agecraft.hookinator.api;

public interface IHookRegistry {

	void replaceMethod(String className, String name, String desc, String callClassName, String callName);

	void insertBeforeMethod(String className, String name, String desc, String callClassName, String callName);

	void insertAfterMethod(String className, String name, String desc, String callClassName, String callName);

	void insertBeforeEachReturn(String className, String name, String desc, String callClassName, String callName);
}
