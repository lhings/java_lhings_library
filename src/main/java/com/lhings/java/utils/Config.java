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


package com.lhings.java.utils;

import java.util.HashMap;


public class Config {
public static HashMap<String, Object> configProperties = new HashMap<String, Object>();
	
	static {
		// size of the buffer used to store incoming packets
		configProperties.put("bufferSize", "1000");

		configProperties.put("defaultMessageExpirationTime", 60);
		
		// set default clock to be used by server
		configProperties.put("clockClass", DeviceClock.class.getName());
		
	}

	public static Clock clock = new DeviceClock();
	
	
	public static Object getProperty(String property){
		return configProperties.get(property);
	}
}
