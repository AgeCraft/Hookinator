package org.agecraft.hookinator.asm;

import java.io.File;
import java.util.Map;

import codechicken.core.launch.CodeChickenCorePlugin;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@TransformerExclusions({"org.agecraft.hookinator.asm"})
public class CorePlugin implements IFMLLoadingPlugin, IFMLCallHook {

	public static File location;

	@Override
	public String[] getASMTransformerClass() {
		CodeChickenCorePlugin.versionCheck(CodeChickenCorePlugin.mcVersion, "Hookinator");
		return new String[]{"org.agecraft.hookinator.asm.Transformer"};
	}

	@Override
	public String getModContainerClass() {
		return "org.agecraft.hookinator.ModContainer";
	}

	@Override
	public String getSetupClass() {
		return "org.agecraft.hookinator.asm.CorePlugin";
	}

	@Override
	public void injectData(Map<String, Object> data) {
		location = (File) data.get("coremodLocation");
		if(location == null) {
			location = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		}
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	@Override
	public Void call() {
		return null;
	}
}
