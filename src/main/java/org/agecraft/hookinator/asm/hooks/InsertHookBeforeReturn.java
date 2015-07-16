package org.agecraft.hookinator.asm.hooks;

import java.util.ArrayList;
import java.util.Iterator;

import org.agecraft.hookinator.asm.CorePlugin;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.Lists;

public class InsertHookBeforeReturn extends HookMethod {

	public InsertHookBeforeReturn(String className, String name, String desc, String callClassName, String callName) {
		super(className, name, desc, callClassName, callName);

		String returnType = callDesc.split("\\)")[1];
		callDesc = callDesc.substring(0, callDesc.indexOf(")")) + returnType + ")" + returnType;
		callDescStatic = callDescStatic.substring(0, callDescStatic.indexOf(")")) + returnType + ")" + returnType;
	}

	@Override
	public void apply(ClassNode node, MethodNode method) {
		CorePlugin.logger.debug(String.format("Inserting hook before each return in method %s %s%s with calls to %s %s%s", className, name, desc, callClassName, callName, desc));

		Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
		while(iterator.hasNext()) {
			AbstractInsnNode insn = iterator.next();
			switch(insn.getOpcode()) {
				case Opcodes.RETURN:
				case Opcodes.ARETURN:
				case Opcodes.IRETURN:
				case Opcodes.DRETURN:
				case Opcodes.FRETURN:
				case Opcodes.LRETURN:
					method.instructions.insertBefore(insn, generate(method));
					break;
			}
		}
	}

	public InsnList generate(MethodNode method) {
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
		int returnIndex = resultIndex + 1;
		
		if(returnType.equals("I")) {
			list.add(new VarInsnNode(Opcodes.ISTORE, returnIndex));
		} else if(returnType.equals("D")) {
			list.add(new VarInsnNode(Opcodes.DSTORE, returnIndex));
		} else if(returnType.equals("F")) {
			list.add(new VarInsnNode(Opcodes.FSTORE, returnIndex));
		} else if(returnType.equals("J")) {
			list.add(new VarInsnNode(Opcodes.LSTORE, returnIndex));
		} else {
			list.add(new VarInsnNode(Opcodes.ASTORE, returnIndex));
		}

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
		
		if(returnType.equals("I")) {
			list.add(new VarInsnNode(Opcodes.ILOAD, returnIndex));
		} else if(returnType.equals("D")) {
			list.add(new VarInsnNode(Opcodes.DLOAD, returnIndex));
		} else if(returnType.equals("F")) {
			list.add(new VarInsnNode(Opcodes.FLOAD, returnIndex));
		} else if(returnType.equals("J")) {
			list.add(new VarInsnNode(Opcodes.LLOAD, returnIndex));
		} else {
			list.add(new VarInsnNode(Opcodes.ALOAD, returnIndex));
		}

		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, callClassName, callName, isStatic ? callDescStatic : callDesc, false));
		list.add(new VarInsnNode(Opcodes.ASTORE, resultIndex));

		list.add(new VarInsnNode(Opcodes.ALOAD, resultIndex));
		list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "org/agecraft/hookinator/api/HookResult", "isCanceled", "()Z", false));
		LabelNode label1 = new LabelNode();
		list.add(new JumpInsnNode(Opcodes.IFEQ, label1));

		list.add(new VarInsnNode(Opcodes.ALOAD, resultIndex));
		list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "org/agecraft/hookinator/api/HookResult", "getReturnValue", "()Ljava/lang/Object;", false));

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
}
