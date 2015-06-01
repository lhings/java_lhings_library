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
 * Interface to be implemented by all possible clock
 * implementations that will be used by the Lyncnat
 * server.
 * 
 * @author José Antonio Lorenzo <joanlofe@gmail.com>
 *
 * First created 30/07/2012
 *
 */

public interface Clock {
	/**
	 * Returns the current Unix UTC time as
	 * indicated by the system clock.
	 * @return Unix UTC time in seconds.
	 */
	public long getUTCUnixTime();
	
	/**
	 * Checks that the given timestamp is not beyond the expiration time
	 * configured in the server. 
	 * @param timestamp The timestamp to check in seconds.
	 * @return
	 */
	public boolean isTimestampStale(long timestamp);
	
	/**
	 * Sets the offset that must be added to the time given by
	 * this clock to obtain a value of time synchronized with
	 * the value of the server clock. Used to fix system time
	 * upon receiving a bad timestamp message.
	 * @param millis
	 */
	public void setOffset(long millis);
}
