package org.agecraft.hookinator;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class HookEvent extends Event {

	public String className;
	public String name;
	public String desc;
	public boolean isStatic;
	public boolean isVoid;
	public Object instance;
	public Object[] arguments;
	public Object returnValue;

	public HookEvent(String className, String name, String desc, Object instance, Object... arguments) {
		super();
		this.className = className;
		this.name = name;
		this.desc = desc;
		this.isStatic = instance == null;
		this.isVoid = desc.endsWith("V");
		this.instance = instance;
		this.arguments = arguments;
		this.returnValue = null;
	}
}
