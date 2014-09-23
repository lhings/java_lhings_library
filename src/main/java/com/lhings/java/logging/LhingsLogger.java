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


package com.lhings.java.logging;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;




public class LhingsLogger {

	private static final Level loggingLevel = Level.ALL;
	private static Logger log = Logger.getLogger("lhingsjavalogger");
	
	static {
//		LhingsLogger.init("./");
		log.removeAllAppenders();
		ConsoleAppender consoleLog = new ConsoleAppender();
		consoleLog.setLayout(new PatternLayout("%-8r [%15t] %-5p %25C - %m%n"));
		consoleLog.setThreshold(loggingLevel);
		consoleLog.setTarget("System.err");
		consoleLog.activateOptions();
		log.addAppender(consoleLog);
		
	}
	
//	public static void init(String path){
//		FileInputStream fis;
//		Properties properties = new Properties();
//		try {
//			fis = new FileInputStream(path + "logs/log4j.properties");
//			properties.load(fis);
//			fis.close();
//		} catch (FileNotFoundException e) {
//			BasicConfigurator.configure();
//			log.warn("Log4j logging subsystem could not found file log4j.properties. Using defaults");
//			properties = null;
//		} catch (IOException e) {
//			BasicConfigurator.configure();
//			log.warn("Log4j logging subsystem could not found file log4j.properties. Using defaults");
//			properties = null;
//		}
//		if(properties!=null)
//			PropertyConfigurator.configure(properties);
//	}
	
	public static Logger getLogger(){
		return log;
	}
	
}
