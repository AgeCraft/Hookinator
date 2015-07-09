# Hookinator

A simple Forge coremod that allows for easy hooking of Minecraft base methods

## Dependencies
* [Minecraft Forge](http://minecraftforge.net) 11.14.1.1419 or higher
* [CodeChickenCore](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1279956-chickenbones-mods) 1.0.5.34 or higher
* [CodeChickenLib](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1279956-chickenbones-mods) 1.1.2.133 or higher

## Installation
1. [Download](http://files.minecraftforge.net) and install Minecraft Forge
2. [Download](http://chickenbones.net/Pages/links.html) and install CodeChickenCore
3. Download Hookinator and put the file in the mods folder

## Usage
Hookinator allows normal mods to hook Minecraft base methods. To do this you need to add a manifest to your mod jar with a `HookLoader` attribute that holds the full name of a loader class. For example:
```
HookLoader: com.example.ExampleHookLoader
```
This loader class has to implement `org.agecraft.hookinator.IHookLoader` and implement the `load()` method. Hookinator will call this method and allow you to register your hooks.

### Registering hooks
To register a hook you have to call `HookLoader.registerHook()`, this method accepts either an `ObfMapping` from CodeChickenLib or three strings so Hookinator can create it for you. The first string is the full class name, the second string is the name of the method and the third is the method description. The method name has to be a srg name to work in an obfuscated environment.
```java
@Override
public void load() {
	// Block.isFullBlock()
	HookLoader.registerHook("net/minecraft/block/Block", "func_149730_j", "()Z");
}
```

### Registering listeners
Hookinator uses an event bus to handle events, so you need to register a listener to receive your hook events. This CAN'T be done in `IHookLoader.load()` because of the way FML handles event buses, so you need to register it somewhere in your during your main mod. The faster you register you listener, the sooner you can receive events. In the future this will be possible during the load method, which is useful for the early hook.

### Hook events
This is an example hook event.
```java
@SubscribeEvent
public void onHookEvent(HookEvent event) {
	if(event.className.equals("net.minecraft.block.Block") && event.name.equals("func_149730_j") && event.desc.equals("()Z")) {
		event.setCanceled(true);
		event.returnValue = false;
	}
}
```

## Future plans
* Make bus registering possible during hook loading
* Allow in-line function calling instead of hook events for often called functions in for example rendering
* Make hook detection easier than comparing three strings
