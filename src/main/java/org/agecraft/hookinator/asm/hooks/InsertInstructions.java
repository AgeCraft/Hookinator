package org.agecraft.hookinator.asm.hooks;

import org.agecraft.hookinator.asm.CorePlugin;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import codechicken.lib.asm.ASMBlock;
import codechicken.lib.asm.InsnComparator;
import codechicken.lib.asm.InsnListSection;

public class InsertInstructions extends MethodHook {

	public ASMBlock needle;
	public ASMBlock insertion;
	public boolean after;

	public InsertInstructions(String className, String name, String desc, ASMBlock needle, ASMBlock insertion, boolean after) {
		super(className, name, desc);

		this.needle = needle;
		this.insertion = insertion;
		this.after = after;
	}

	public InsertInstructions(String className, String name, String desc, ASMBlock insertion, boolean after) {
		this(className, name, desc, null, insertion, after);
	}

	@Override
	public void apply(ClassNode node, MethodNode method) {
		if(needle == null) {
			CorePlugin.logger.debug(String.format("Inserting instructions " + (after ? "after" : "before") + " method %s %s%s", className, name, desc));
			if(!after) {
				method.instructions.insert(insertion.rawListCopy());
			} else {
				method.instructions.add(insertion.rawListCopy());
			}
		} else {
			for(InsnListSection key : InsnComparator.findN(method.instructions, needle.list)) {
				CorePlugin.logger.debug(String.format("Inserting instructions " + (after ? "after" : "before") + " method %s %s%s @ %s - %s", className, name, desc, Integer.toString(key.start), Integer.toString(key.end)));
				ASMBlock injectBlock = insertion.copy().mergeLabels(needle.applyLabels(key));

				if(!after) {
					key.insertBefore(injectBlock.list.list);
				} else {
					key.insert(injectBlock.list.list);
				}
			}
		}
	}
}
