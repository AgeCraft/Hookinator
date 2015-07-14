package org.agecraft.hookinator.api;

import java.util.Map;

import codechicken.lib.asm.ASMBlock;

public interface IHookRegistry {

	void addHook(IHook hook);
	
	void removeHook(IHook hook);
	
	Map<String, ASMBlock> loadASMBlocks(String path);
	
	void changeField(String className, String name, String desc, int newAccess, String newDesc, Object newValue);

	void changeFieldAccess(String className, String name, String desc, int newAccess);
	
	void changeFieldDesc(String className, String name, String desc, String newDesc);
	
	void changeFieldValue(String className, String name, String desc, Object newValue);
	
	void changeFieldAccessAndDesc(String className, String name, String desc, int newAccess, String newDesc);
	
	void changeFieldAccessAndValue(String className, String name, String desc, int newAccess, Object newValue);
	
	void changeFieldDescAndValue(String className, String name, String desc, String newDesc, Object newValue);

	void replaceMethod(String className, String name, String desc, String callClassName, String callName);

	void insertBeforeMethod(String className, String name, String desc, String callClassName, String callName);

	void insertAfterMethod(String className, String name, String desc, String callClassName, String callName);

	void insertBeforeEachReturn(String className, String name, String desc, String callClassName, String callName);
}
