package org.agecraft.hookinator;

public class HookResult<T> {

	private boolean isCanceled = false;
	private T returnValue;
	
	public HookResult() {
		
	}
	
	public HookResult(T returnValue) {
		this(true, returnValue);
	}
	
	public HookResult(boolean isCanceled, T returnValue) {
		setCanceled(isCanceled);
		setReturnValue(returnValue);
	}
	
	public boolean isCanceled() {
		return isCanceled;
	}
	
	public void setCanceled(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}
	
	public T getReturnValue() {
		return returnValue;
	}
	
	public void setReturnValue(T returnValue) {
		this.returnValue = returnValue;
	}
}
