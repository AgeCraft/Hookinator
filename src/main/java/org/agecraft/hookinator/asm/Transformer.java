package org.agecraft.hookinator.asm;

import java.io.File;

import net.minecraft.launchwrapper.IClassTransformer;

import org.agecraft.hookinator.api.IHook;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import codechicken.lib.asm.ASMHelper;

public class Transformer implements IClassTransformer {

	public boolean transform(ClassNode node) throws Exception {
		String name = node.name.replace('/', '.');
		boolean addedHook = false;
		if(HookRegistry.instance().hooks.containsKey(name)) {
			for(IHook hook : HookRegistry.instance().hooks.get(name)) {
				addedHook = true;
				hook.apply(node);
			}
		}
		return addedHook;
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		try {
			ClassNode node = new ClassNode();
			ClassReader reader = new ClassReader(bytes);
			ClassVisitor cv = node;
			reader.accept(cv, ClassReader.EXPAND_FRAMES);

			try {
				boolean addedHook = transform(node);

				bytes = ASMHelper.createBytes(node, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
				if(addedHook && ASMHelper.config.getTag("dump_asm").getBooleanValue(true)) {
					ASMHelper.dump(bytes, new File("asm/hookinator/" + node.name.replace('/', '#') + ".txt"), false, false);
				}
			} catch(Exception e) {
				ASMHelper.dump(bytes, new File("asm/hookinator/" + node.name.replace('/', '#') + ".txt"), false, false);
				throw e;
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return bytes;
	}
}
