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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DeviceClock extends DefaultExpirationPolicyClock {

	private SimpleDateFormat sdf;
	private long offset = 0;

	public DeviceClock() {
		super();
		TimeZone UTCtz = TimeZone.getTimeZone("UTC");
		sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z");
		sdf.setTimeZone(UTCtz);
	}

	@Override
	public long getUTCUnixTime() {
		return (System.currentTimeMillis() - offset) / 1000;
	}

	public String toString(long time) {
		return sdf.format(new Date(time * 1000));
	}

	@Override
	public String toString() {
		return toString(getUTCUnixTime());
	}

	public void setOffset(long millis) {
		this.offset = millis;

	}

}
