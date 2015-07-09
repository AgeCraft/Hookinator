package org.agecraft.hookinator;

import java.lang.reflect.Field;

public class Hookinator {

	public static boolean isAnnotationNotPresent(Field field, Class clazz) {
		return !field.isAnnotationPresent(clazz);
	}
}
