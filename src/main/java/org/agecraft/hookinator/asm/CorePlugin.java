package org.agecraft.hookinator.asm;

import java.io.File;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import codechicken.core.launch.CodeChickenCorePlugin;

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
		discoverHookinatorMods();
		return null;
	}

	public void discoverHookinatorMods() {
		HookLoader.init();
		File modsDir = new File(CodeChickenCorePlugin.minecraftDir, "mods");
		for(File file : modsDir.listFiles()) {
			scanMod(file);
		}
		File versionModsDir = new File(CodeChickenCorePlugin.minecraftDir, "mods/" + CodeChickenCorePlugin.currentMcVersion);
		if(versionModsDir.exists()) {
			for(File file : versionModsDir.listFiles()) {
				scanMod(file);
			}
		}
		HookLoader.load();
	}

	public void scanMod(File file) {
		if(!file.getName().endsWith(".jar") && !file.getName().endsWith(".zip")) {
			return;
		}
		try {
			JarFile jar = new JarFile(file);
			try {
				Manifest manifest = jar.getManifest();
				if(manifest == null) {
					return;
				}
				Attributes attr = manifest.getMainAttributes();
				if(attr == null) {
					return;
				}
				String hookLoader = attr.getValue("HookLoader");
				if(hookLoader != null) {
					HookLoader.add(hookLoader, jar, file);
				}
			} finally {
				jar.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
