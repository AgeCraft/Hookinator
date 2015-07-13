package org.agecraft.hookinator.asm;

import java.io.File;
import java.util.ArrayList;

import net.minecraft.launchwrapper.IClassTransformer;

import org.agecraft.hookinator.HookReference;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import codechicken.lib.asm.ASMHelper;

import com.google.common.collect.Lists;

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
			for(HookReference reference : HookRegistry.instance().hooks.get(name)) {
				for(MethodNode method : node.methods) {
					if(reference.matches(method)) {
						addedHook = true;
						method.instructions.insert(generateHook(reference, method));
						CorePlugin.logger.debug(String.format("Inserted hook %s %s%s into %s %s", reference.callClassName, reference.callName, reference.desc, reference.className, reference.name));
					}
				}
			}
		}
		return addedHook;
	}

	public InsnList generateHook(HookReference reference, MethodNode method) {
		InsnList list = new InsnList();

		String args = method.desc.substring(1).split("\\)")[0];
		ArrayList<String> arguments = Lists.newArrayList();
		int typeStart = -1;
		for(int i = 0; i < args.length(); i++) {
			char c = args.charAt(i);
			if(typeStart == -1 && c != 'L') {
				arguments.add(Character.toString(c));
			} else if(typeStart == -1 && (c == '[' || c == 'L')) {
				typeStart = i;
			} else if(c == 'L' || c == ';') {
				arguments.add(args.substring(typeStart + 1, i));
			}
		}
		String returnType = method.desc.split("\\)")[1];
		if(returnType.startsWith("L")) {
			returnType = returnType.substring(1, returnType.length() - 1);
		}

		boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
		int resultIndex = method.maxLocals;

		if(!isStatic) {
			list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		}

		for(int i = 0; i < arguments.size(); i++) {
			String type = arguments.get(i);
			int index = isStatic ? i : i + 1;
			if(type.equals("I")) {
				list.add(new VarInsnNode(Opcodes.ILOAD, index));
			} else if(type.equals("D")) {
				list.add(new VarInsnNode(Opcodes.DLOAD, index));
			} else if(type.equals("F")) {
				list.add(new VarInsnNode(Opcodes.FLOAD, index));
			} else if(type.equals("J")) {
				list.add(new VarInsnNode(Opcodes.LLOAD, index));
			} else {
				list.add(new VarInsnNode(Opcodes.ALOAD, index));
			}
		}

		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, reference.callClassName, reference.callName, isStatic ? reference.callDescStatic : reference.callDesc, false));
		list.add(new VarInsnNode(Opcodes.ASTORE, resultIndex));

		list.add(new VarInsnNode(Opcodes.ALOAD, resultIndex));
		list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "org/agecraft/hookinator/HookResult", "isCanceled", "()Z", false));
		LabelNode label1 = new LabelNode();
		list.add(new JumpInsnNode(Opcodes.IFEQ, label1));

		list.add(new VarInsnNode(Opcodes.ALOAD, resultIndex));
		list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "org/agecraft/hookinator/HookResult", "getReturnValue", "()Ljava/lang/Object;", false));

		if(returnType.equals("V")) {
			list.add(new InsnNode(Opcodes.RETURN));
		} else if(returnType.endsWith("Z") || returnType.endsWith("B") || returnType.endsWith("S") || returnType.endsWith("I")) {
			list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Integer"));
			list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
			list.add(new InsnNode(Opcodes.IRETURN));
		} else if(returnType.endsWith("D")) {
			list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Double"));
			list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false));
			list.add(new InsnNode(Opcodes.DRETURN));
		} else if(returnType.endsWith("F")) {
			list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Float"));
			list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));
			list.add(new InsnNode(Opcodes.FRETURN));
		} else if(returnType.endsWith("J")) {
			list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Long"));
			list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false));
			list.add(new InsnNode(Opcodes.LRETURN));
		} else {
			list.add(new TypeInsnNode(Opcodes.CHECKCAST, returnType));
			list.add(new InsnNode(Opcodes.ARETURN));
		}

		list.add(label1);

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
