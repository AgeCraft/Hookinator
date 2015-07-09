package org.agecraft.hookinator.asm;

import java.io.File;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import codechicken.lib.asm.ASMHelper;

public class Transformer implements IClassTransformer {

	public void transform(ClassNode node) throws Exception {
		node.access &= ~Opcodes.ACC_PRIVATE;
		node.access &= ~Opcodes.ACC_PROTECTED;
		node.access |= Opcodes.ACC_PUBLIC;
		for(InnerClassNode inner : node.innerClasses) {
			inner.access &= ~Opcodes.ACC_PRIVATE;
			inner.access &= ~Opcodes.ACC_PROTECTED;
			inner.access |= Opcodes.ACC_PUBLIC;
		}
		for(FieldNode field : node.fields) {
			if((field.access & Opcodes.ACC_PRIVATE) != 0) {
				field.access &= ~Opcodes.ACC_PRIVATE;
				field.access |= Opcodes.ACC_PUBLIC;
			}
//			field.access &= ~Opcodes.ACC_PRIVATE;
//			field.access &= ~Opcodes.ACC_PROTECTED;
//			field.access |= Opcodes.ACC_PUBLIC;
		}
		for(MethodNode method : node.methods) {
			method.access &= ~Opcodes.ACC_PRIVATE;
			method.access &= ~Opcodes.ACC_PROTECTED;
			method.access |= Opcodes.ACC_PUBLIC;
		}
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
				transform(node);

				bytes = ASMHelper.createBytes(node, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
				if(ASMHelper.config.getTag("dump_asm").getBooleanValue(true)) {
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
