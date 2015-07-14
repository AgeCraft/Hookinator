package org.agecraft.hookinator.asm.hooks;

import java.util.ArrayList;

import org.agecraft.hookinator.asm.CorePlugin;
import org.objectweb.asm.Opcodes;
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

public class MethodHookCall extends MethodHook {

	public String callClassName;
	public String callName;
	public String callDesc;
	public String callDescStatic;

	public boolean after;

	public MethodHookCall(String className, String name, String desc, String callClassName, String callName, boolean after) {
		super(className, name, desc);

		this.callClassName = callClassName.replace('.', '/');
		this.callName = callName;
		this.callDesc = "(L" + className.replace('.', '/') + ";" + desc.substring(1, desc.indexOf(")")) + ")Lorg/agecraft/hookinator/api/HookResult;";
		this.callDescStatic = desc.substring(0, desc.indexOf(")")) + ")Lorg/agecraft/hookinator/api/HookResult;";

		this.after = after;
	}

	@Override
	public void apply(ClassNode node, MethodNode method) {
		if(!after) {
			method.instructions.insert(generate(this, method));
		} else {
			method.instructions.insertBefore(method.instructions.getLast(), generate(this, method));
		}
		CorePlugin.logger.debug(String.format("Inserted hook %s %s%s %s %s %s", callClassName, callName, desc, after ? " after " : " before ", className, name));
	}

	@Override
	public String toString() {
		return String.format("MethodHookCall[%s %s%s --> %s%s%s]", className, name, desc, callClassName.replace('/', '.'), callName, callDesc);
	}

	public static InsnList generate(MethodHookCall hook, MethodNode method) {
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

		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, hook.callClassName, hook.callName, isStatic ? hook.callDescStatic : hook.callDesc, false));
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
