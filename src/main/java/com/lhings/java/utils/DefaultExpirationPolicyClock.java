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




/**
 * This abstract class defines the default expiration
 * policy of the messages. The way current unix time is
 * obtained should be defined by implementing classes.
 * All implementing classes should call super() in their
 * constructors.
 * 
 * @author José Antonio Lorenzo <joanlofe@gmail.com>
 *
 * First created 05/08/2012
 *
 */
public abstract class DefaultExpirationPolicyClock implements Clock {

	int expirationTime;
	
	public DefaultExpirationPolicyClock(){
		expirationTime = (Integer) Config.configProperties.get("defaultMessageExpirationTime");
	}
	
	public abstract long getUTCUnixTime();

	public boolean isTimestampStale(long timestamp) {
		long currentTime = getUTCUnixTime();
		if ((timestamp > currentTime+expirationTime) || (timestamp < currentTime - expirationTime))
			return true;
		else
			return false;
	}

}
