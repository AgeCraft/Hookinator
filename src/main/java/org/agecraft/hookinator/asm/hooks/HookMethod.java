package org.agecraft.hookinator.asm.hooks;

public abstract class HookMethod extends MethodHook {

	public String callClassName;
	public String callName;
	public String callDesc;
	public String callDescStatic;

	public HookMethod(String className, String name, String desc, String callClassName, String callName) {
		super(className, name, desc);

		this.callClassName = callClassName.replace('.', '/');
		this.callName = callName;
		this.callDesc = "(L" + className.replace('.', '/') + ";" + desc.substring(1);
		this.callDescStatic = this.desc;
	}

	@Override
	public String toString() {
		return String.format("%s[%s %s%s --> %s %s%s]", getClass().getSimpleName(), className, name, desc, callClassName.replace('/', '.'), callName, callDesc);
	}
}
