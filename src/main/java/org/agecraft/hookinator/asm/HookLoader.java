package org.agecraft.hookinator.asm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import org.agecraft.hookinator.IHookLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import codechicken.core.asm.DependancyLister;
import codechicken.lib.asm.ObfMapping;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class HookLoader {

	public static Logger logger = LogManager.getLogger("Hookinator");

	private static ArrayList<IHookLoader> loaders = Lists.newArrayList();
	protected static Multimap<String, ObfMapping> hooks = HashMultimap.create();

	private static Method defineClass;
	private static Field cacheClasses;

	public static void init() {
		try {
			defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE);
			defineClass.setAccessible(true);
			cacheClasses = LaunchClassLoader.class.getDeclaredField("cachedClasses");
			cacheClasses.setAccessible(true);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void load() {
		for(IHookLoader loader : loaders) {
			loader.load();
		}
	}
	
	public static ObfMapping registerHook(String className, String name, String desc) {
		ObfMapping hook = new ObfMapping(className, name, desc);
		registerHook(hook);
		return hook;
	}

	public static void registerHook(ObfMapping hook) {
		if(!hook.isMethod()) {
			throw new IllegalStateException("Only methods can be hooked");
		}
		hooks.put(hook.javaClass(), hook);
	}

	public static void unregisterHook(ObfMapping hook) {
		hooks.remove(hook.javaClass(), hook);
	}

	public static void add(String transformer, JarFile jar, File jarFile) {
		logger.debug("Adding Hookinator loader: " + transformer);
		try {
			byte[] bytes;
			bytes = Launch.classLoader.getClassBytes(transformer);

			if(bytes == null) {
				String resourceName = transformer.replace('.', '/') + ".class";
				ZipEntry entry = jar.getEntry(resourceName);
				if(entry == null) {
					throw new Exception("Failed to add loader: " + transformer + ". Entry not found in jar file " + jarFile.getName());
				}
				bytes = readFully(jar.getInputStream(entry));
			}

			defineDependancies(bytes, jar, jarFile);
			Class<?> clazz = defineClass(transformer, bytes);

			if(!IHookLoader.class.isAssignableFrom(clazz)) {
				throw new Exception("Failed to add loader: " + transformer + " is not an instance of IHookLoader");
			}

			IHookLoader classTransformer;
			try {
				classTransformer = (IHookLoader) clazz.getDeclaredConstructor(File.class).newInstance(jarFile);
			} catch(NoSuchMethodException nsme) {
				classTransformer = (IHookLoader) clazz.newInstance();
			}
			loaders.add(classTransformer);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void defineDependancies(byte[] bytes, JarFile jar, File jarFile) throws Exception {
		defineDependancies(bytes, jar, jarFile, new Stack<String>());
	}

	private static void defineDependancies(byte[] bytes, JarFile jar, File jarFile, Stack<String> depStack) throws Exception {
		ClassReader reader = new ClassReader(bytes);
		DependancyLister lister = new DependancyLister(Opcodes.ASM4);
		reader.accept(lister, 0);

		depStack.push(reader.getClassName());

		for(String dependancy : lister.getDependancies()) {
			if(depStack.contains(dependancy)) {
				continue;
			}
			try {
				Launch.classLoader.loadClass(dependancy.replace('/', '.'));
			} catch(ClassNotFoundException cnfe) {
				ZipEntry entry = jar.getEntry(dependancy + ".class");
				if(entry == null) {
					throw new Exception("Dependency " + dependancy + " not found in jar file " + jarFile.getName());
				}
				byte[] depbytes = readFully(jar.getInputStream(entry));
				defineDependancies(depbytes, jar, jarFile, depStack);

				logger.debug("Defining dependancy: " + dependancy);

				defineClass(dependancy.replace('/', '.'), depbytes);
			}
		}

		depStack.pop();
	}

	private static Class<?> defineClass(String classname, byte[] bytes) throws Exception {
		Class<?> clazz = (Class<?>) defineClass.invoke(Launch.classLoader, classname, bytes, 0, bytes.length);
		((Map<String, Class<?>>) cacheClasses.get(Launch.classLoader)).put(classname, clazz);
		return clazz;
	}

	public static byte[] readFully(InputStream stream) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(stream.available());
		int r;
		while((r = stream.read()) != -1) {
			bos.write(r);
		}
		return bos.toByteArray();
	}
}
