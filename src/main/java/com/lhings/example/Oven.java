/* Copyright 2014 Lyncos Technologies S. L.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */


package com.lhings.example;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.lhings.java.LhingsDevice;
import com.lhings.java.annotations.Action;
import com.lhings.java.annotations.DeviceInfo;
import com.lhings.java.annotations.Event;
import com.lhings.java.annotations.StatusComponent;
import com.lhings.java.exception.LhingsException;
import com.lhings.java.model.Device;


@DeviceInfo(deviceType="virtualoven")
public class Oven extends LhingsDevice {

	private static final int degreesPerMinute = 50;
	private static final int ambientTemperature = 20;

	private int initialTemperature, setPointTemperature;
	private long bakeTime, stopTime, startTime;
	private boolean baking;
	private int i = 0;
	private String uuidPlugLhings;
	
	@Event
	String finished;

	@StatusComponent(name = "temperature")
	private int temperature;
		
	@StatusComponent
	private int targetTemperature;
	
	@StatusComponent
	private boolean hot;
	
	public Oven(String username, String password, String name) throws IOException, LhingsException {
		// fill in with your Lhings username and password
		super(username, password, name);

		bakeTime = System.currentTimeMillis();
		stopTime = System.currentTimeMillis();
		setPointTemperature = 0;
		initialTemperature = ambientTemperature;
		baking = false;

	}

	public static void main(String[] args) throws InterruptedException, IOException, LhingsException{
		LhingsDevice device = new Oven("user@example.com", "mypassword", "My Lhings Java Oven");
		device.setLoopFrequency(20);
		device.start();
		// stop device after 6 minutes
		Thread.sleep(360000);
		device.stop();
	}
	
	
	
	@Override
	public void setup() {
		// In setup() you perform all the initialization tasks your device needs
		System.out.println("Oven initialization completed...");
	}

	@Override
	public void loop() {
		if (isBakeFinished()){
			sendEvent("finished");
		}
		
		temperature = (int) thermometer();
		targetTemperature = setPointTemperature;
		
		hot = temperature > 30;
		
		i++;
		if (i % 100 == 0){
			System.out.println("Temp: " + thermometer() + ", setPoint:"
					+ targetTemperature + ", baking?:" + baking);
			try {
				storeStatus();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (LhingsException e) {
				e.printStackTrace();
			}
		}
	}

	@Action(name = "bake", description = "action used to bake food", argumentNames = {
			"temperature", "time" })
	public void bake(int temperature, int minutes) {
		initialTemperature = (int)thermometer();
		setPointTemperature = temperature;
		startTime = System.currentTimeMillis();
		bakeTime = startTime + minutes * 60000;
		baking = true;
		System.out.println("Baking started!");
	}

	@Action(name = "show other devices", argumentNames = { })
	public void show() throws LhingsException, IOException{
		List<Device> devices = this.getDevices();
		System.out.println("Devices in the same account as me:");
		for (Device device : devices){
			System.out.println(device.getName() + " - " + device.getUuidString());
			if (device.getName().equalsIgnoreCase("pluglhings"))
				uuidPlugLhings = device.getUuidString();
		}
		
		
		if (uuidPlugLhings != null){
			Map<String, Object> status = getStatus(uuidPlugLhings);
			System.out.println("Battery level is: " + status.get("BATTERY STATUS").toString());
		} else {
			System.out.println("No PlugLhings device found");
		}
			
			
		
		
	}
	
	public boolean isBakeFinished() {
		if (baking && System.currentTimeMillis() > bakeTime) {
			initialTemperature = (int)thermometer();
			baking = false;
			stopTime = System.currentTimeMillis();
			System.out.println("Bake finished!");
			return true;
		} else {
			return false;
		}
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

	
}
