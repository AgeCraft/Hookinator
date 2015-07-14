package org.agecraft.hookinator.asm;

import java.io.File;

import net.minecraft.launchwrapper.IClassTransformer;

import org.agecraft.hookinator.asm.hooks.ClassHook;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import codechicken.lib.asm.ASMHelper;

public class Transformer implements IClassTransformer {

	public boolean transform(ClassNode node) throws Exception {
		String name = node.name.replace('/', '.');

		node.access &= ~Opcodes.ACC_PRIVATE;
		node.access &= ~Opcodes.ACC_PROTECTED;
		node.access |= Opcodes.ACC_PUBLIC;

		for(InnerClassNode inner : node.innerClasses) {
			inner.access &= ~Opcodes.ACC_PRIVATE;
			inner.access &= ~Opcodes.ACC_PROTECTED;
			inner.access |= Opcodes.ACC_PUBLIC;
		}

		boolean isObjectHolder = name.equals("net.minecraft.init.Blocks") || name.equals("net.minecraft.init.Items");
		if(node.visibleAnnotations != null) {
			for(AnnotationNode annotation : node.visibleAnnotations) {
				if(annotation.desc.equals("net/minecraftforge/fml/common/registry/GameRegistry$ObjectHolder")) {
					isObjectHolder = true;
				}
			}
		}
		if(node.invisibleAnnotations != null) {
			for(AnnotationNode annotation : node.invisibleAnnotations) {
				if(annotation.desc.equals("net/minecraftforge/fml/common/registry/GameRegistry$ObjectHolder")) {
					isObjectHolder = true;
				}
			}
		}
		if(!isObjectHolder) {
			for(FieldNode field : node.fields) {
				field.access &= ~Opcodes.ACC_PRIVATE;
				field.access &= ~Opcodes.ACC_PROTECTED;
				field.access |= Opcodes.ACC_PUBLIC;
			}
		}

		for(MethodNode method : node.methods) {
			method.access &= ~Opcodes.ACC_PRIVATE;
			method.access &= ~Opcodes.ACC_PROTECTED;
			method.access |= Opcodes.ACC_PUBLIC;
		}

		boolean addedHook = false;
		if(HookRegistry.instance().hooks.containsKey(name)) {
			for(ClassHook hook : HookRegistry.instance().hooks.get(name)) {
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
