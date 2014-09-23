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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.lhings.java.logging.LhingsLogger;

/**
 * This class provides static methods for the manipulation of sequences of bits
 * and bytes.
 * 
 * @author José Antonio Lorenzo <joanlofe@gmail.com>
 * 
 *         First created 22/05/2012
 * 
 */
public class ByteMan {

	private static Logger log = LhingsLogger.getLogger();

	public static String byteArrayToHexString(byte[] m) {
		return Hex.encodeHexString(m);
	}

	public static byte[] hexStringToByteArray(String str){
		byte[] byteArray;
		try {
			byteArray = Hex.decodeHex(str.toCharArray());
		} catch (DecoderException e) {
			byteArray = null;
		}
		return byteArray;
	}
	
	/**
	 * Converts a 16 bit integer to an array of two bytes
	 * 
	 * @param n
	 *            The integer to be converted
	 * @return
	 */
	public static byte[] integerToBytes(int n) {
		byte[] bytes = { (byte) (n >> 8), (byte) (n & 0xff) };
		return bytes;
	}

	/**
	 * Converts an array of two bytes to an integer. If an array of more than
	 * two bytes is given, only the first two bytes are used to calculate the
	 * returned value.
	 * 
	 * @param b
	 *            The big endian array of two bytes.
	 * @return The 16 bit integer represented by the two bytes.
	 */
	public static int bytesToInteger(byte[] b) {
		return bytesToInteger(b[0], b[1]);
	}

	public static int bytesToInteger(byte b0, byte b1) {
		return (b0 & 0xFF) << 8 | (0xFF & b1);
	}

	/**
	 * Converts a 32 bit integer to an array of four bytes
	 * 
	 * @param n
	 *            The integer to be converted
	 * @return
	 */
	public static byte[] integer32ToBytes(int n) {
		byte[] bytes = { (byte) (n >> 24), (byte) ((n & 0xff0000) >> 16),
				(byte) ((n & 0xff00) >> 8), (byte) (n & 0xff) };
		return bytes;
	}

	/**
	 * Converts an array of bytes to an integer
	 * 
	 * @param b
	 * @return
	 */
	public static long bytesToInteger32(byte[] b) {
		long value = 0;
		for (int i = 0; i < b.length; i++) {
			value = (value << 8) + (b[i] & 0xff);
		}
		return value;
	}

	/**
	 * Calculates the SHA-1 hash of the given string.
	 * 
	 * @param strLyncportId
	 *            The string to be hashed.
	 * @return
	 */
	public static byte[] sha1Hash(String strLyncportId) {
		byte[] bytes = strLyncportId.getBytes(Charset.forName("utf-8"));
		return sha1Hash(bytes);
	}

	public static byte[] sha1Hash(byte[] bytes) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.ERROR, "Error while working out SHA-1 hash", e);
			return null;
		}
		return md.digest(bytes);
	}
	
	public static void main(String[] args) throws DecoderException {
		byte[] a = { (byte) 0x12, (byte) 0xfa, (byte) 0xa4, (byte) 0xc5 };
		for (byte b : a)
			System.out.print(b + ", ");
		System.out.println();
		String str = Hex.encodeHexString(a);
		System.out.println(str);
		byte[] c = Hex.decodeHex(str.toCharArray());
		for (byte b : c)
			System.out.print(b + ", ");
		System.out.println();
	}

	public static byte[] long2ipv4(long i) {
		byte[] ip = new byte[4];
		ip[0] = (byte)((i >> 24) & 0xFFL);
		ip[1] = (byte)((i >> 16) & 0xFFL);
		ip[2] = (byte)((i >> 8) & 0xFFL);
		ip[3] = (byte)(i & 0xFFL);
		return ip;
	}

	public static long ipv42long(String ip) {
		String[] addrArray = ip.split("\\.");
		long num = 0;
		for (int i = 0; i < addrArray.length; i++) {
			int power = 3 - i;
			num += ((Integer.parseInt(addrArray[i]) % 256 * Math
					.pow(256, power)));
		}
		return num;
	}

	public static long ipv42long(byte[] ip) {
		long p = (ip[0] & 0xFFL) << 24;
		p += (ip[1] & 0xFFL) << 16;
		p += (ip[2] & 0xFFL) << 8;
		p += ip[3] & 0xFFL;
		return p;
	}
	
	public static byte[] toByteArray(double value) {
		byte[] bytes = new byte[8];
		ByteBuffer.wrap(bytes).putDouble(value);
		return bytes;
	}
	
	public static double toDouble(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getDouble();
	}
	
}
