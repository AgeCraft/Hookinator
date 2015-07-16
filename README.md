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
### Setting up a hook loader
Hookinator allows normal mods to hook Minecraft base methods. To do this you need to add a manifest to your mod jar with a `HookLoader` attribute that holds the full name of a loader class. For example:
```
HookLoader: com.example.ExampleHookLoader
```
This loader class has to implement `org.agecraft.hookinator.api.IHookLoader` and implement the `load(IHookRegistry registry)` method. Hookinator will call this method and allow you to register your hooks.

### Registering hooks
To register a hook you have to call one of the function of the `IHookRegistry`. Here is a list of different hooks you can register:
```java
void createField(String className, String name, String desc, int access);

void createField(String className, String name, String desc, int access, Object value);

void changeField(String className, String name, String desc, int newAccess, String newDesc, Object newValue);

void changeFieldAccess(String className, String name, String desc, int newAccess);

void changeFieldDesc(String className, String name, String desc, String newDesc);

void changeFieldValue(String className, String name, String desc, Object newValue);

void changeFieldAccessAndDesc(String className, String name, String desc, int newAccess, String newDesc);

void changeFieldAccessAndValue(String className, String name, String desc, int newAccess, Object newValue);

void changeFieldDescAndValue(String className, String name, String desc, String newDesc, Object newValue);

void createMethod(String className, String name, String desc, int access, String[] exceptions);

void createMethod(String className, String name, String desc, int access, String[] exceptions, InsnList instructions);

void createMethod(String className, String name, String desc, int access, String[] exceptions, ASMBlock instructions);

void replaceMethod(String className, String name, String desc, String callClassName, String callName);

void replaceMethod(String className, String name, String desc, ASMBlock replacement);

void findAndReplaceMethodInstructions(String className, String name, String desc, ASMBlock needle, ASMBlock replacement);

void insertBeforeMethod(String className, String name, String desc, String callClassName, String callName);

void insertBeforeMethod(String className, String name, String desc, ASMBlock insertion);

void insertAfterMethod(String className, String name, String desc, String callClassName, String callName);

void insertAfterMethod(String className, String name, String desc, ASMBlock insertion);

void insertBeforeEachReturn(String className, String name, String desc, String callClassName, String callName);

void insertBeforeEachReturn(String className, String name, String desc, ASMBlock insertion);

void insertBeforeInstructions(String className, String name, String desc, ASMBlock needle, ASMBlock insertion);

void insertAfterInstructions(String className, String name, String desc, ASMBlock needle, ASMBlock insertion);
```

### Utility methods
The `IHookRegistry` also has some utility methods. You can add or remove custom hooks using:
```java
void addHook(IHook hook);
void removeHook(IHook hook);
```

And you can quickly load ASM blocks, it just passes the arguments to CodeChickenCore to handle the loading:
```java
Map<String, ASMBlock> loadASMBlocks(String path);
```

## Future plans
* Add more types of hooks
