package org.agecraft.hookinator.test;

import net.minecraft.block.Block;

import org.agecraft.hookinator.HookResult;

public class TestHooks {

	public static HookResult<Boolean> isFullBlock(Block instance) {
		System.out.println("CALLED: Block.isFullBlock()Z on " + Block.getIdFromBlock(instance));
		return new HookResult<Boolean>(true);
	}
}
