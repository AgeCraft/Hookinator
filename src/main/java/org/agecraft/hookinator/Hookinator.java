package org.agecraft.hookinator;

import java.lang.reflect.Field;

import net.minecraftforge.fml.common.eventhandler.EventBus;

public class Hookinator {

	public static final EventBus BUS = new EventBus();

	public static boolean isAnnotationNotPresent(Field field, Class clazz) {
		return !field.isAnnotationPresent(clazz);
	}

	public static void registerListener(Object target) {
		BUS.register(target);
	}

	public static void unregisterListener(Object target) {
		BUS.unregister(target);
	}

	public static HookEvent hook(String className, String name, String desc, Object self, Object... args) {
		HookEvent event = new HookEvent(className, name, desc, self, args);
		BUS.post(event);
		return event;
	}
}
