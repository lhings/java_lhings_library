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


package com.lhings.java.stun;

import java.util.Arrays;

import com.lhings.java.utils.ByteMan;

/**
 * This class provides a wrapper for arrays that has an appropriate
 * implementation of the methods equals and hashCode. It is provided so that it
 * can be used to wrap the bytes of the attribute transaction id of STUN
 * messages. In this way it can be used as key in HashMaps.
 * 
 * @author J. A. Lorenzo Fernández <joanlofe@gmail.com>
 * 
 *         First created 12/05/2012
 */
public class TransactionID {
	private final byte[] data;

	public TransactionID(byte[] data) {
		if (data == null) {
			throw new NullPointerException();
		}
		this.data = data;
	}

	public byte[] getBytes(){
		return data;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TransactionID)) {
			return false;
		}
		return Arrays.equals(data, ((TransactionID) other).data);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}
	
	@Override
	public String toString(){
		return ByteMan.byteArrayToHexString(data);
	}

}
