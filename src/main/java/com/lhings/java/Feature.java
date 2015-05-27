package com.lhings.java;

import java.util.HashMap;
import java.util.Map;

public abstract class Feature {

	protected String name;
	private long loopOnceEveryNTimes;
	private long counter = 0;
	private Map<String, String> eventNameAliases = new HashMap<String, String>();
	
	LhingsDevice parentDevice;

	public Feature(String name){
		this(name, 1);
	}
	
	public Feature(String name, long loopOnceEveryNTimes){
		this.loopOnceEveryNTimes = loopOnceEveryNTimes;
		this.name = name;
	}
	
	public abstract void loop();
	
	public abstract void setup();
	
	void loopEvery(){
		if (counter % loopOnceEveryNTimes == 0){
			loop();
		}
		counter ++;
	}
	
	protected void sendEvent(String name){
		sendEvent(name, "");
	}
	
	protected void sendEvent(String name, String payload){
		String alias = eventNameAliases.get(name);
		if (alias == null)
			alias = name;
		parentDevice.sendEvent(alias, payload);
		
	}
	
	void setAliasForEvent(String event, String alias){
		eventNameAliases.put(event, alias);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
