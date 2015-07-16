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

import org.agecraft.hookinator.api.IHook;
import org.agecraft.hookinator.api.IHookLoader;
import org.agecraft.hookinator.api.IHookRegistry;
import org.agecraft.hookinator.asm.hooks.ChangeField;
import org.agecraft.hookinator.asm.hooks.FieldWriter;
import org.agecraft.hookinator.asm.hooks.InsertBeforeReturn;
import org.agecraft.hookinator.asm.hooks.InsertHook;
import org.agecraft.hookinator.asm.hooks.InsertHookBeforeReturn;
import org.agecraft.hookinator.asm.hooks.InsertInstructions;
import org.agecraft.hookinator.asm.hooks.MethodWriter;
import org.agecraft.hookinator.asm.hooks.ReplaceInstructions;
import org.agecraft.hookinator.asm.hooks.ReplaceMethod;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;

import codechicken.core.asm.DependancyLister;
import codechicken.lib.asm.ASMBlock;
import codechicken.lib.asm.ASMReader;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class HookRegistry implements IHookRegistry {

	private static HookRegistry instance;
	private static Method defineClass;
	private static Field cacheClasses;

	protected Multimap<String, IHook> hooks = HashMultimap.create();
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
	public void addHook(IHook hook) {
		hooks.put(hook.getClassName(), hook);
	}

	@Override
	public void removeHook(IHook hook) {
		hooks.remove(hook.getClassName(), hook);
	}

	@Override
	public Map<String, ASMBlock> loadASMBlocks(String path) {
		return ASMReader.loadResource(path);
	}

	@Override
	public void createField(String className, String name, String desc, int access) {
		addHook(new FieldWriter(className, name, desc, access));
	}

	@Override
	public void createField(String className, String name, String desc, int access, Object value) {
		addHook(new FieldWriter(className, name, desc, access, value));
	}

	@Override
	public void changeField(String className, String name, String desc, int newAccess, String newDesc, Object newValue) {
		addHook(new ChangeField(className, name, desc, newAccess, newDesc, newValue));
	}

	@Override
	public void changeFieldAccess(String className, String name, String desc, int newAccess) {
		addHook(new ChangeField(className, name, desc, newAccess));
	}

	@Override
	public void changeFieldDesc(String className, String name, String desc, String newDesc) {
		addHook(new ChangeField(className, name, desc, newDesc));
	}

	@Override
	public void changeFieldValue(String className, String name, String desc, Object newValue) {
		addHook(new ChangeField(className, name, desc, newValue));
	}

	@Override
	public void changeFieldAccessAndDesc(String className, String name, String desc, int newAccess, String newDesc) {
		addHook(new ChangeField(className, name, desc, newAccess, newDesc));
	}

	@Override
	public void changeFieldAccessAndValue(String className, String name, String desc, int newAccess, Object newValue) {
		addHook(new ChangeField(className, name, desc, newAccess, newValue));
	}

	@Override
	public void changeFieldDescAndValue(String className, String name, String desc, String newDesc, Object newValue) {
		addHook(new ChangeField(className, name, desc, newDesc, newValue));
	}

	@Override
	public void createMethod(String className, String name, String desc, int access, String[] exceptions) {
		addHook(new MethodWriter(className, name, desc, access, exceptions));
	}

	@Override
	public void createMethod(String className, String name, String desc, int access, String[] exceptions, InsnList instructions) {
		addHook(new MethodWriter(className, name, desc, access, exceptions, instructions));
	}

	@Override
	public void createMethod(String className, String name, String desc, int access, String[] exceptions, ASMBlock instructions) {
		addHook(new MethodWriter(className, name, desc, access, exceptions, instructions));
	}

	@Override
	public void replaceMethod(String className, String name, String desc, String callClassName, String callName) {
		addHook(new ReplaceMethod(className, name, desc, callClassName, callName));
	}

	@Override
	public void replaceMethod(String className, String name, String desc, ASMBlock replacement) {
		addHook(new ReplaceInstructions(className, name, desc, replacement));
	}

	@Override
	public void findAndReplaceMethodInstructions(String className, String name, String desc, ASMBlock needle, ASMBlock replacement) {
		addHook(new ReplaceInstructions(className, name, desc, needle, replacement));
	}

	@Override
	public void insertBeforeMethod(String className, String name, String desc, String callClassName, String callName) {
		addHook(new InsertHook(className, name, desc, callClassName, callName, false));
	}

	@Override
	public void insertBeforeMethod(String className, String name, String desc, ASMBlock insertion) {
		addHook(new InsertInstructions(className, name, desc, insertion, false));
	}

	@Override
	public void insertAfterMethod(String className, String name, String desc, String callClassName, String callName) {
		addHook(new InsertHook(className, name, desc, callClassName, callName, true));
	}

	@Override
	public void insertAfterMethod(String className, String name, String desc, ASMBlock insertion) {
		addHook(new InsertInstructions(className, name, desc, insertion, true));
	}

	@Override
	public void insertBeforeEachReturn(String className, String name, String desc, String callClassName, String callName) {
		addHook(new InsertHookBeforeReturn(className, name, desc, callClassName, callName));
	}

	@Override
	public void insertBeforeEachReturn(String className, String name, String desc, ASMBlock insertion) {
		addHook(new InsertBeforeReturn(className, name, desc, insertion));
	}

	@Override
	public void insertAfterInstructions(String className, String name, String desc, ASMBlock needle, ASMBlock insertion) {
		addHook(new InsertInstructions(className, name, desc, needle, insertion, false));
	}

	@Override
	public void insertBeforeInstructions(String className, String name, String desc, ASMBlock needle, ASMBlock insertion) {
		addHook(new InsertInstructions(className, name, desc, needle, insertion, true));
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
