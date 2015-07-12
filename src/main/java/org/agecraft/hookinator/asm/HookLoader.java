package org.agecraft.hookinator.asm;

import java.io.IOException;
import java.util.List;

import org.agecraft.hookinator.HookReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.CharSource;
import com.google.common.io.LineProcessor;

public class HookLoader {

	public static Logger logger = LogManager.getLogger("Hookinator");

	protected static Multimap<String, HookReference> hooks = HashMultimap.create();

//	public static void load() {
//		logger.info("Loading hooks");
//		loaders.add(TestHooks.class);
//		for(Class<?> loaderClass : loaders) {
//			for(int i = 0; i < loaderClass.getDeclaredMethods().length; i++) {
//				Method method = loaderClass.getDeclaredMethods()[i];
//				if(Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Hook.class)) {
//					Hook hook = method.getAnnotation(Hook.class);
//					logger.info("Added hook " + hook);
//					hooks.put(hook.className(), new HookReference(hook, loaderClass, method));
//				}
//			}
//		}
//		logger.info("Found " + hooks.size() + " hooks");
//	}
	
	public static void load(String name, CharSource source) throws IOException {
		logger.debug("Adding hook file: " + name);
		source.readLines(new LineProcessor<Void>() {
			
			@Override
			public Void getResult() {
				return null;
			}
			
			@Override
			public boolean processLine(String input) throws IOException {
				String line = Iterables.getFirst(Splitter.on("#").limit(2).split(input), "").trim();
				if(line.length() == 0) {
					return true;
				}
				List<String> parts = Lists.newArrayList(Splitter.on(" ").trimResults().split(line));
				if(parts.size() != 5) {
					throw new RuntimeException("Invalid hook config file line: " + input); 
				}
				
				HookReference reference = new HookReference(parts.get(0), parts.get(1), parts.get(2), parts.get(3), parts.get(4));
				hooks.put(reference.className, reference);
				
				return true;
			}
		});
	}
}
