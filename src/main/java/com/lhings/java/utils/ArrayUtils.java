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

import java.util.Arrays;
import java.util.List;

/** 
 * This class provides several utility methods to work with arrays
 * @author jose
 *
 */
public class ArrayUtils {

	/**
	 * This method concatenates two arrays and returns a new array with the result
	 * @param first
	 * @param second
	 * @return
	 */
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	/**
	 * Returns the array resulting from the concatenation of all the arrays given as arguments.
	 * @param arrays
	 * @return
	 */
	public static <T> T[] addAll(T[]... arrays){
		if (arrays.length == 0)
			return null;
		else if (arrays.length == 1)
			return arrays[0];
		else if (arrays.length == 2)
			return concat(arrays[0], arrays[1]);
		T[] array = concat(arrays[0], arrays[1]);
		for (int j=2; j<arrays.length; j++)
			array = concat(array, arrays[j]);
		return array; 
	}

	/**
	 * This method concatenates two arrays and returns a new array with the result
	 * @param first
	 * @param second
	 * @return
	 */
	public static byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	/**
	 * Returns the array resulting from the concatenation of all the arrays given as arguments.
	 * @param arrays
	 * @return
	 */
	public static byte[] addAll(byte[]... arrays){
		if (arrays.length == 0)
			return null;
		else if (arrays.length == 1)
			return arrays[0];
		else if (arrays.length == 2)
			return concat(arrays[0], arrays[1]);
		byte[] array = concat(arrays[0], arrays[1]);
		for (int j=2; j<arrays.length; j++)
			array = concat(array, arrays[j]);
		return array; 
	}
	
	public static byte[] addAll(List<byte[]> arrays){
		byte[] result = {};
		for (byte[] array : arrays){
			result = concat(result, array);
		}
		return result;
	}
	
}
