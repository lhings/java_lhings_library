package com.lhings.example;

import com.lhings.library.Action;
import com.lhings.library.Event;
import com.lhings.library.LhingsDevice;
import com.lhings.library.Stats;


public class Oven extends LhingsDevice {

	private static final int degreesPerMinute = 50;
	private static final int ambientTemperature = 20;

	private int initialTemperature, setPointTemperature;
	private long bakeTime, stopTime, startTime;
	private boolean baking;
	private int i = 0;

	public Oven() {
		// fill in with your Lhings username and password
		super("user@example.com", "xxxxxxx", 5000, "My oven");

		bakeTime = System.currentTimeMillis();
		stopTime = System.currentTimeMillis();
		setPointTemperature = 0;
		initialTemperature = ambientTemperature;
		 baking = false;

	}

	@Override
	public void init() {
		// In init() you perform all the initialization tasks your device needs
		System.out.println("Oven initialization completed...");
	}

	@Override
	public void loop() {
		// loop will be called repeatedly by the library code
		try {
			// it is a good practice to include a sleep here in order to avoid busy waits and exhausting CPU resources
			Thread.sleep(50);
		} catch (InterruptedException e) {

		}
		i++;
		if (i % 100 == 0)
			System.out.println("Temp: " + thermometer() + ", setPoint:"
					+ targetTemp() + ", baking?:" + baking);
	}

	@Action(name = "bake", description = "action used to bake food", inputsName = {
			"temperature", "time" }, inputsType = { "integer", "integer" })
	public void bake(int temperature, int minutes) {
		initialTemperature = (int)thermometer();
		setPointTemperature = temperature;
		startTime = System.currentTimeMillis();
		bakeTime = startTime + minutes * 60000;
		baking = true;
		System.out.println("Baking started!");
	}

	@Event(name = "finished")
	public String isBakeFinished() {
		if (baking && System.currentTimeMillis() > bakeTime) {
			initialTemperature = (int)thermometer();
			baking = false;
			stopTime = System.currentTimeMillis();
			System.out.println("Bake finished!");
			// if the method returns and empty or non-empty string the event will be generated
			// no more than one event per second is allowed
			return "";
		} else {
			// if the method returns null no event will be generated
			return null;
		}
	}

	@Stats(name = "temperature", type = "integer")
	public int temperature() {
		return (int) thermometer();
	}

	@Stats(name = "targetTemperature", type = "integer")
	public int targetTemp() {
		return setPointTemperature;
	}

	@Stats(name = "hot", type = "boolean")
	public boolean hotOrNot() {
		return thermometer() > 30;
	}

	private long thermometer() {
		long t;
		if (baking ) {
			t = initialTemperature
					+ (long) ((System.currentTimeMillis() - startTime) / 60000f * degreesPerMinute);
			if (t > setPointTemperature)
				t = setPointTemperature;
		} else {
			t = initialTemperature
					- (long) ((System.currentTimeMillis() - stopTime) / 60000f * degreesPerMinute);
			if (t < ambientTemperature)
				t = ambientTemperature;
		}

		return t;
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		// starting your device is as easy as creating an instance!!
		Oven oven = new Oven();
		
	}

}
