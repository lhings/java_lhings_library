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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Config {

	private static final Logger log = LoggerFactory.getLogger(Config.class);
	
	public static HashMap<String, Object> configProperties = new HashMap<String, Object>();

	static {
		// size of the buffer used to store incoming packets
		configProperties.put("bufferSize", "1000");

		configProperties.put("defaultMessageExpirationTime", 60);

		// set default clock to be used by server
		configProperties.put("clockClass", DeviceClock.class.getName());

	}

	public static Clock clock = new DeviceClock();

	public static Object getProperty(String property) {
		return configProperties.get(property);
	}

	public static String readJsonConfigFile(String filename) {
		StringBuffer contentsOfFile = new StringBuffer("");
		Scanner sc = null;
		try {
			sc = new Scanner(new File(filename));
			while (sc.hasNextLine()) {
				contentsOfFile.append(sc.nextLine());
			}
		} catch (FileNotFoundException e) {
			log.error("Configuration file " + filename + " not found. Create one and try again.");
		} finally {
			if (sc != null)
				sc.close();
		}
		return contentsOfFile.toString();
	}
}
