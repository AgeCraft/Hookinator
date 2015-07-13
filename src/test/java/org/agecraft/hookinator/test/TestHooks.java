package org.agecraft.hookinator.test;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import org.agecraft.hookinator.HookResult;

public class TestHooks {

	public static HookResult<IBlockState> getStateFromMeta(Block instance, int meta) {
		System.out.println("CALLED: Block.getStateFromMeta on " + Block.getIdFromBlock(instance));
		return new HookResult<IBlockState>();
	}
}
