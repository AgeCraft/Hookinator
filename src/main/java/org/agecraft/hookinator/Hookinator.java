package org.agecraft.hookinator;

import java.lang.reflect.Field;

import codechicken.lib.asm.ObfMapping;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class Hookinator {
	
	public static Multimap<String, ObfMapping> hooks = HashMultimap.create();

	public static void registerHook(ObfMapping hook) {
		if(!hook.isMethod()) {
			throw new IllegalStateException("Only methods can be hooked");
		}
		hooks.put(hook.javaClass(), hook);
	}
	
	public static void unregisterHook(ObfMapping hook) {
		hooks.remove(hook.javaClass(), hook);
	}
	
	public static boolean isAnnotationNotPresent(Field field, Class clazz) {
		return !field.isAnnotationPresent(clazz);
	}
}
