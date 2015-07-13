package org.agecraft.hookinator.test;

import org.agecraft.hookinator.api.IHookLoader;
import org.agecraft.hookinator.api.IHookRegistry;

public class HookLoader implements IHookLoader {

	@Override
	public void load(IHookRegistry registry) {
		registry.addHook("net.minecraft.block.Block", "func_176203_a", "(I)Lnet/minecraft/block/state/IBlockState;", "org.agecraft.hookinator.test.TestHooks", "getStateFromMeta");
	}
}
