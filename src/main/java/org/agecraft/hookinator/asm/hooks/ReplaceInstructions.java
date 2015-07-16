package org.agecraft.hookinator.asm.hooks;

import org.agecraft.hookinator.asm.CorePlugin;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import codechicken.lib.asm.ASMBlock;
import codechicken.lib.asm.InsnComparator;
import codechicken.lib.asm.InsnListSection;

public class ReplaceInstructions extends MethodHook {

	public ASMBlock needle;
	public ASMBlock replacement;

	public ReplaceInstructions(String className, String name, String desc, ASMBlock needle, ASMBlock replacement) {
		super(className, name, desc);

		this.needle = needle;
		this.replacement = replacement;
	}

	public ReplaceInstructions(String className, String name, String desc, ASMBlock replacement) {
		this(className, name, desc, null, replacement);
	}

	@Override
	public void apply(ClassNode node, MethodNode method) {
		if(needle == null) {
			CorePlugin.logger.debug(String.format("Replacing method %s %s%s", className, name, desc));

			method.instructions.clear();
			method.instructions.add(replacement.rawListCopy());
		} else {
			for(InsnListSection key : InsnComparator.findN(method.instructions, needle.list)) {
				CorePlugin.logger.debug(String.format("Replacing method %s %s%s @ %s - %s", className, name, desc, Integer.toString(key.start), Integer.toString(key.end)));

				ASMBlock replaceBlock = replacement.copy().pullLabels(needle.applyLabels(key));
				key.insert(replaceBlock.list.list);
			}
		}
	}
}
