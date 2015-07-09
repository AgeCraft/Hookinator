package org.agecraft.hookinator;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;

import org.agecraft.hookinator.asm.CorePlugin;

import codechicken.core.launch.CodeChickenCorePlugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ModContainer extends DummyModContainer {

	public static HashMap<String, Object> map = Maps.newHashMap();

	static {
		map.put("name", "Hookinator");
		map.put("version", "@VERSION@");
	}

	public ModContainer() {
		super(MetadataCollection.from(MetadataCollection.class.getResourceAsStream("/mcmod.info"), "Hookinator").getMetadataForId("Hookinator", map));
	}

	@Override
	public Set<ArtifactVersion> getRequirements() {
		Set<ArtifactVersion> set = Sets.newHashSet();
		set.add(VersionParser.parseVersionReference("CodeChickenCore@[" + CodeChickenCorePlugin.version + ",)"));
		return set;
	}

	@Override
	public List<ArtifactVersion> getDependencies() {
		return Lists.newLinkedList(getRequirements());
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}

	@Override
	public File getSource() {
		return CorePlugin.location;
	}

	@Override
	public Class<?> getCustomResourcePackClass() {
		return getSource().isDirectory() ? FMLFolderResourcePack.class : FMLFileResourcePack.class;
	}

	@Subscribe
	public void preInit(FMLPreInitializationEvent event) {

	}
}
