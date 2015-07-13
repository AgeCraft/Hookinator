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

import org.agecraft.hookinator.HookReference;
import org.agecraft.hookinator.api.IHookLoader;
import org.agecraft.hookinator.api.IHookRegistry;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import codechicken.core.asm.DependancyLister;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class HookRegistry implements IHookRegistry {

	private static HookRegistry instance;
	private static Method defineClass;
	private static Field cacheClasses;

	protected Multimap<String, HookReference> hooks = HashMultimap.create();
	private ArrayList<IHookLoader> loaders = Lists.newArrayList();

	public HookRegistry() {
		if(defineClass == null) {
			try {
				defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE);
				defineClass.setAccessible(true);
				cacheClasses = LaunchClassLoader.class.getDeclaredField("cachedClasses");
				cacheClasses.setAccessible(true);
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static HookRegistry instance() {
		if(instance == null) {
			instance = new HookRegistry();
		}
		return instance;
	}

	@Override
	public void addHook(String className, String name, String desc, String callClassName, String callName) {
		HookReference reference = new HookReference(className, name, desc, callClassName, callName);
		hooks.put(reference.className, reference);
	}

	public void load() {
		for(IHookLoader loader : loaders) {
			loader.load(this);
		}
	}

	public void load(String transformer) {
		CorePlugin.logger.debug("Adding hook loader: " + transformer);
		try {
			Class<?> clazz = Class.forName(transformer, true, Launch.classLoader);
			if(!IHookLoader.class.isAssignableFrom(clazz)) {
				throw new Exception("Failed to add hook loader: " + transformer + " is not an instance of IHookLoader");
			}

			loaders.add((IHookLoader) clazz.newInstance());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void load(String transformer, JarFile jar, File jarFile) {
		CorePlugin.logger.debug("Adding hook loader: " + transformer);
		try {
			byte[] bytes;
			bytes = Launch.classLoader.getClassBytes(transformer);

			if(bytes == null) {
				String resourceName = transformer.replace('.', '/') + ".class";
				ZipEntry entry = jar.getEntry(resourceName);
				if(entry == null) {
					throw new Exception("Failed to add hook loader: " + transformer + ". Entry not found in jar file " + jarFile.getName());
				}
				bytes = readFully(jar.getInputStream(entry));
			}

			defineDependancies(bytes, jar, jarFile, new Stack<String>());
			Class<?> clazz = defineClass(transformer, bytes);

			if(!IHookLoader.class.isAssignableFrom(clazz)) {
				throw new Exception("Failed to add hook loader: " + transformer + " is not an instance of IHookLoader");
			}

			loaders.add((IHookLoader) clazz.newInstance());
		} catch(Exception e) {
			e.printStackTrace();
		}
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

				CorePlugin.logger.debug("Defining dependancy: " + dependancy);

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

	private static byte[] readFully(InputStream stream) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(stream.available());
		int r;
		while((r = stream.read()) != -1) {
			bos.write(r);
		}
		return bos.toByteArray();
	}
}
