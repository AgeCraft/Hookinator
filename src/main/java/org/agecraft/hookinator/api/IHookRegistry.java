package org.agecraft.hookinator.api;

import java.util.Map;

import org.objectweb.asm.tree.InsnList;

import codechicken.lib.asm.ASMBlock;

public interface IHookRegistry {

	void addHook(IHook hook);

	void removeHook(IHook hook);

	Map<String, ASMBlock> loadASMBlocks(String path);

	void createField(String className, String name, String desc, int access);

	void createField(String className, String name, String desc, int access, Object value);

	void changeField(String className, String name, String desc, int newAccess, String newDesc, Object newValue);

	void changeFieldAccess(String className, String name, String desc, int newAccess);

	void changeFieldDesc(String className, String name, String desc, String newDesc);

	void changeFieldValue(String className, String name, String desc, Object newValue);

	void changeFieldAccessAndDesc(String className, String name, String desc, int newAccess, String newDesc);

	void changeFieldAccessAndValue(String className, String name, String desc, int newAccess, Object newValue);

	void changeFieldDescAndValue(String className, String name, String desc, String newDesc, Object newValue);

	void createMethod(String className, String name, String desc, int access, String[] exceptions);

	void createMethod(String className, String name, String desc, int access, String[] exceptions, InsnList instructions);

	void createMethod(String className, String name, String desc, int access, String[] exceptions, ASMBlock instructions);

	void replaceMethod(String className, String name, String desc, String callClassName, String callName);

	void replaceMethod(String className, String name, String desc, ASMBlock replacement);

	void findAndReplaceMethodInstructions(String className, String name, String desc, ASMBlock needle, ASMBlock replacement);

	void insertBeforeMethod(String className, String name, String desc, String callClassName, String callName);

	void insertBeforeMethod(String className, String name, String desc, ASMBlock insertion);

	void insertAfterMethod(String className, String name, String desc, String callClassName, String callName);

	void insertAfterMethod(String className, String name, String desc, ASMBlock insertion);

	void insertBeforeEachReturn(String className, String name, String desc, String callClassName, String callName);

	void insertBeforeEachReturn(String className, String name, String desc, ASMBlock insertion);

	void insertBeforeInstructions(String className, String name, String desc, ASMBlock needle, ASMBlock insertion);

	void insertAfterInstructions(String className, String name, String desc, ASMBlock needle, ASMBlock insertion);
}
