package org.agecraft.hookinator.asm;

import java.io.File;
import java.util.ListIterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.agecraft.hookinator.Hookinator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import codechicken.lib.asm.ASMHelper;
import codechicken.lib.asm.ObfMapping;

public class Transformer implements IClassTransformer {

	public void transform(ClassNode node) throws Exception {
		if(node.name.equals("net/minecraftforge/fml/common/registry/ObjectHolderRegistry")) {
			System.out.println(node.name);
			for(MethodNode method : node.methods) {
				if(method.name.equals("scanClassForFields") && method.desc.equals("(Ljava/util/Map;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;Z)V")) {
					ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
					while(iterator.hasNext()) {
						AbstractInsnNode insn = iterator.next();
						if(insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
							MethodInsnNode methodInsn = (MethodInsnNode) insn;
							if(methodInsn.name.equals("isAnnotationPresent") && methodInsn.desc.equals("(Ljava/lang/Class;)Z")) {
								method.instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "org/agecraft/hookinator/Hookinator", "isAnnotationNotPresent", "(Ljava/lang/reflect/Field;Ljava/lang/Class;)Z", false));
							}
						}
					}
				}
			}
		}
		
		node.access &= ~Opcodes.ACC_PRIVATE;
		node.access &= ~Opcodes.ACC_PROTECTED;
		node.access |= Opcodes.ACC_PUBLIC;
		for(InnerClassNode inner : node.innerClasses) {
			inner.access &= ~Opcodes.ACC_PRIVATE;
			inner.access &= ~Opcodes.ACC_PROTECTED;
			inner.access |= Opcodes.ACC_PUBLIC;
		}
		for(FieldNode field : node.fields) {
			field.access &= ~Opcodes.ACC_PRIVATE;
			field.access &= ~Opcodes.ACC_PROTECTED;
			field.access |= Opcodes.ACC_PUBLIC;
		}
		for(MethodNode method : node.methods) {
			method.access &= ~Opcodes.ACC_PRIVATE;
			method.access &= ~Opcodes.ACC_PROTECTED;
			method.access |= Opcodes.ACC_PUBLIC;
		}
		
		if(Hookinator.hooks.containsKey(node.name)) {
			for(ObfMapping hook : Hookinator.hooks.get(node.name)) {
				for(MethodNode method : node.methods) {
					if(hook.matches(method)) {
						method.instructions.insert(generateHook(method));
					}
				}
			}
		}
	}
	
	public InsnList generateHook(MethodNode method) {
		InsnList list = new InsnList();
		
		String returnType = method.desc.split(")")[1];
		
		if(returnType.equals("V")) {
			list.add(new InsnNode(Opcodes.RETURN));
		} else {
			//TODO: cast to return type
			list.add(new InsnNode(Opcodes.ARETURN));
		}
		return list;
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
				//if(ASMHelper.config.getTag("dump_asm").getBooleanValue(true)) {
				//	ASMHelper.dump(bytes, new File("asm/hookinator/" + node.name.replace('/', '#') + ".txt"), false, false);
				//}
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
