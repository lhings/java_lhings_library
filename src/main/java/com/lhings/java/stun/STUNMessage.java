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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lhings.java.utils.ByteMan;

/**
 * This class provides a representation of a STUN message as described in RFC
 * 5389 (Session Traversal Utilities for NAT), as well as method to encode an
 * decode STUN messages.
 * 
 * @author J. A. Lorenzo Fern��ndez
 * 
 */

public class STUNMessage {
	// declare logger
	private static final Logger log = LoggerFactory.getLogger(STUNMessage.class);
	// instantiate random number generator
	protected final static Random random = new Random(System.currentTimeMillis());

	// constant definitions
	protected static final byte[] magicCookie = { (byte) 0x21, (byte) 0x12, (byte) 0xA4, (byte) 0x42 };
	protected static final String softwareAttr = "LyncNat STUN v0.0";

	// STUN message method and class definitions
	public static final int CL_REQUEST = 0x0000;
	public static final int CL_INDICATION = 0x0010;
	public static final int CL_SUCCESS = 0x0100;
	public static final int CL_ERROR = 0x0110;

	// STUN message class fields
	protected byte[] bytes;
	protected Map<Integer, byte[]> attributes;

	protected String apiKey;

	/**
	 * This method creates the entire array of bytes that make up an attribute.
	 * 
	 * @param attrType
	 *            The integer code that identifies the attribute.
	 * @param value
	 *            An array of bytes with the value of the attribute.
	 * @return An array of bytes padded to a 32 bit boundary, as required by RFC
	 *         5389, containing the type-length-value information of the
	 *         attribute.
	 */
	protected static byte[] composeAttribute(int attrType, byte[] value) {
		byte[] attrBytes = new byte[getPaddedLength(value.length) + 4];
		byte[] type = ByteMan.integerToBytes(attrType);
		byte[] l = ByteMan.integerToBytes(value.length);
		attrBytes[0] = type[0];
		attrBytes[1] = type[1];
		attrBytes[2] = l[0];
		attrBytes[3] = l[1];
		for (int j = 0; j < value.length; j++) {
			attrBytes[4 + j] = value[j];
		}
		return attrBytes;
	}

	/**
	 * Factory method to create a STUNMessage instance from an array of bytes.
	 * 
	 * @param m
	 *            The STUNMessage will be created out of this byte array.
	 * @return A STUNMessage instance if the byte array represents a well formed
	 *         (according to RFC 5389) STUN message, null otherwise.
	 */
	public static STUNMessage getSTUNMessage(byte[] m) {
		if (isWellFormed(m))
			return new STUNMessage(m);
		else
			return null;
	}

	/**
	 * Returns the length in bytes of the nearest 32 bit boundary. This method
	 * is used internally by this class to compute the size of the value field
	 * in attributes
	 * 
	 * @param n
	 * @return
	 */
	private static int getPaddedLength(int n) {
		if (n == 0)
			return n;
		else if (n % 4 == 0)
			return n;
		else
			return n + 4 - n % 4;
	}

	/**
	 * Creates an empty STUN message backed by an array of bytes of the given
	 * length. The only field of the STUN message set by this method is the
	 * magic cookie field. All other fields must be set manually to their
	 * desired values before using the message.
	 * 
	 * @param length
	 *            Length in bytes of the byte array in which the message will be
	 *            stored.
	 */
	public STUNMessage(int length, String apiKey) {
		this.bytes = new byte[length];
		bytes[4] = magicCookie[0];
		bytes[5] = magicCookie[1];
		bytes[6] = magicCookie[2];
		bytes[7] = magicCookie[3];
		attributes = new HashMap<Integer, byte[]>();

		this.apiKey = apiKey;
	}

	/**
	 * Creates a new STUNMessage instance out of the given byte array.
	 * 
	 * @param m
	 */
	protected STUNMessage(byte[] m) {
		this.bytes = m;
		parseAttributes();

	}

	protected void parseAttributes() {
		attributes = new HashMap<Integer, byte[]>();
		int position = 20; // start reading bytes just after message header
		while (position < bytes.length) {
			int attrType = ByteMan.bytesToInteger(new byte[] { bytes[position], bytes[position + 1] });
			int attrLength = ByteMan.bytesToInteger(new byte[] { bytes[position + 2], bytes[position + 3] });
			byte[] attr = Arrays.copyOfRange(bytes, position + 4, position + attrLength + 4);
			int shift = attrLength % 4;
			if (shift != 0)
				shift = 4 - shift;
			// to take into account the padding to 32 bits boundary and the 4
			// bytes of type length
			position += attrLength + shift + 4;
			attributes.put(attrType, attr);
		}
	}

	/**
	 * Generates the random transaction ID all STUN messages must have.
	 * 
	 * @return A random array of 12 bytes
	 */
	public static byte[] generateTransactionID() {
		byte[] trID = new byte[12];
		STUNMessage.random.nextBytes(trID);
		return trID;
	}

	/**
	 * Returns the underlying array of bytes of this STUN message.
	 * 
	 * @return An array of bytes.
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Returns the attribute of the STUN message related to the given key.
	 * 
	 * @param key
	 *            Integer class instance containing the attribute key.
	 * @return The attribute corresponding to that key or null if there is no
	 *         such attribute.
	 */
	public byte[] getAttribute(Integer key) {
		if (attributes == null)
			return null;
		else
			return attributes.get(key);
	}

	/**
	 * Returns the attribute of the STUN message related to the given key.
	 * 
	 * @param key
	 *            Integer number that represents the attribute key.
	 * @return The attribute corresponding to that key or null if there is no
	 *         such attribute.
	 */
	public byte[] getAttribute(int key) {
		return getAttribute(Integer.valueOf(key));
	}

	/**
	 * Returns true if the message class is request
	 * 
	 * @return
	 */
	public boolean isRequest() {
		return this.getMessageClass() == STUNMessage.CL_REQUEST;
	}

	/**
	 * Returns true if the message class is success response
	 * 
	 * @return
	 */
	public boolean isSuccessResponse() {
		return this.getMessageClass() == STUNMessage.CL_SUCCESS;
	}

	/**
	 * Returns true if the message class is error response
	 * 
	 * @return
	 */
	public boolean isErrorResponse() {
		return this.getMessageClass() == STUNMessage.CL_ERROR;
	}

	/**
	 * Returns true if the message class is indication
	 * 
	 * @return
	 */
	public boolean isIndication() {
		return this.getMessageClass() == STUNMessage.CL_INDICATION;
	}

	/**
	 * Checks if the given sequence of bytes is a well formed STUN message.
	 * Basically, what is checked is that the given sequence of bytes is
	 * compliant with section 6 of RFC5389.
	 * 
	 * @param m
	 *            The bytes that make up the supposed STUN message.
	 * @return true if the message is a STUN message, false otherwise.
	 */
	public static boolean isWellFormed(byte[] m) {
		// header of a STUN message is at least 20 bytes long
		if (m.length < 20) {
			log.warn("STUN message shorter than 20 bytes");
			return false;
		}
		// check two first bits are 00
		if ((m[0] & 0xC0) != 0) {
			log.warn("Two first bits in header are not zero");
			return false;
		}
		// check magic cookie
		if (!(m[4] == magicCookie[0] && m[5] == magicCookie[1] && m[6] == magicCookie[2] && m[7] == magicCookie[3])) {
			log.warn("Magic cookie is not correct");
			return false;
		}
		// check message length
		int length = ByteMan.bytesToInteger(m[2], m[3]);
		if (length != m.length - 20) {
			log.warn("Incorrect message length");
			return false;
		}

		return true;
	}

	protected static byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	/**
	 * This method returns the transport address contained in the
	 * XOR-MAPPED-ADDRESS of a STUN binding success response. WARNING: all the
	 * implementation is done in network byte order. This may have to be changed
	 * when porting the code to a microcontroller.
	 * 
	 * @return An InetSocketAddress instance containing the transport address or
	 *         null if the message is not a STUN binding success response.
	 * @throws UnknownHostException
	 */
	public InetSocketAddress getMappedAddress() throws UnknownHostException {
		if (!this.isSuccessResponse()) {
			log.warn("Tried to get a mapped address out of a non success response message.");
			return null;
		}
		byte[] xorMappedAddress = attributes.get(0x0020);
		if (xorMappedAddress[1] != 0x01) {
			log.warn("IPv6 networking is not currently supported. Returning null.");
			return null;
		}

		byte[] port = { (byte) (xorMappedAddress[2] ^ 0x21), (byte) (xorMappedAddress[3] ^ 0x12) };
		int portNumber = ByteMan.bytesToInteger(port);
		byte[] ip = { (byte) (xorMappedAddress[4] ^ 0x21), (byte) (xorMappedAddress[5] ^ 0x12), (byte) (xorMappedAddress[6] ^ 0xA4),
				(byte) (xorMappedAddress[7] ^ 0x42) };

		return new InetSocketAddress(InetAddress.getByAddress(ip), portNumber);

	}

	public String toString() {
		String method = "0x" + Integer.toHexString(getMethod());
		String cl;

		switch (this.getMessageClass()) {
		case STUNMessage.CL_REQUEST:
			cl = "request";
			break;
		case STUNMessage.CL_INDICATION:
			cl = "indication";
			break;
		case STUNMessage.CL_SUCCESS:
			cl = "success";
			break;
		case STUNMessage.CL_ERROR:
			cl = "error";
			break;
		default:
			cl = null;
			break;
		}

		int messageLength = this.getLength();

		StringBuffer s = new StringBuffer("Message type: " + method + " " + cl + "(0x"
				+ Integer.toHexString(ByteMan.bytesToInteger(new byte[] { bytes[0], bytes[1] })) + ")\n");
		s.append("Message length: " + messageLength + " bytes\n");
		s.append("Transaction ID: " + ByteMan.byteArrayToHexString(Arrays.copyOfRange(bytes, 8, 20)) + "\n");
		if (this.getMessageClass() == STUNMessage.CL_ERROR) {
			byte[] errorData = this.getAttribute(LyncnatProtocol.attrErrorCode);
			String errorReason = new String(Arrays.copyOfRange(errorData, 4, errorData.length));
			s.append("Error reason: " + errorReason);
		}
		return s.toString();
	}

	/**
	 * This method returns the error code and the error message of the STUN
	 * message, if any.
	 * 
	 * @return If the STUN message is an error response, it returns an array of
	 *         Object, being the first object an Integer instance with the error
	 *         code, and the second object an String instance with the error
	 *         message. If the STUN message is not an error response this method
	 *         returns null.
	 */
	public Object[] getErrorCode() {
		if (getMessageClass() == STUNMessage.CL_ERROR) {
			byte[] errorAttr = getAttribute(LyncnatProtocol.attrErrorCode);
			int hundreds = (int) errorAttr[2];
			int remainder = (int) errorAttr[3];
			int errorCode = 100 * hundreds + remainder;
			String errorMessage = "";
			try {
				errorMessage = new String(Arrays.copyOfRange(errorAttr, 4, errorAttr.length), "utf-8");
			} catch (UnsupportedEncodingException e) {
				log.error("Ooops! Unsupported encoding exception. See stack trace for details.", e);
			}
			Object[] returnValue = { Integer.valueOf(errorCode), errorMessage };
			return returnValue;
		} else
			return null;
	}

	/* *************************************************
	 * only getter and setter methods from this line on
	 * *************************************************
	 */

	/**
	 * Writes the given attribute to the desired position of the backing byte
	 * array of this STUN message.
	 * 
	 * @param attr
	 *            The code of the attribute to be set. See LyncnatProtocol
	 *            class.
	 * @param value
	 *            The value of the attribute to be set.
	 * @param position
	 *            The position of the byte array at which the attribute should
	 *            be added.
	 */
	public void setAttribute(int attr, byte[] value, int position) {
		byte[] attribute = ByteMan.integerToBytes(attr);
		byte[] len = ByteMan.integerToBytes(value.length);
		System.arraycopy(attribute, 0, bytes, position, 2);
		System.arraycopy(len, 0, bytes, position + 2, 2);
		System.arraycopy(value, 0, bytes, position + 4, value.length);
		attributes.put(attr, value);
	}

	public int getMessageClass() {
		int a = ByteMan.bytesToInteger(bytes[0], bytes[1]);
		return a & 0x0110;
	}

	public void setMessageClass(int messageClass) {
		// this.messageClass = messageClass;
		switch (messageClass) {
		case STUNMessage.CL_REQUEST:
			bytes[1] = (byte) (bytes[1] & (~(1 << 4)));
			bytes[0] = (byte) (bytes[1] & (~(1 << 4)));
			break;
		case STUNMessage.CL_INDICATION:
			bytes[1] = (byte) (bytes[1] | (1 << 4));
			bytes[0] = (byte) (bytes[1] & (~(1 << 4)));
			break;
		case STUNMessage.CL_SUCCESS:
			bytes[1] = (byte) (bytes[1] & (~(1 << 4)));
			bytes[0] = (byte) (bytes[1] | (1 << 4));
			break;
		case STUNMessage.CL_ERROR:
			bytes[1] = (byte) (bytes[1] | (1 << 4));
			bytes[0] = (byte) (bytes[1] | (1 << 4));
			break;
		default:
			break;
		}
	}

	public int getLength() {
		byte[] l = { this.bytes[2], this.bytes[3] };
		return ByteMan.bytesToInteger(l);
	}

	public TransactionID getTransactionID() {
		return new TransactionID(getTransactionIDBytes());
	}

	public byte[] getTransactionIDBytes() {
		return Arrays.copyOfRange(bytes, 8, 20);
	}

	public void setTransactionID(byte[] transactionID) {
		System.arraycopy(transactionID, 0, bytes, 8, 12);
	}

	public void setTransactionID() {
		setTransactionID(generateTransactionID());
	}

	public void setMessageType(int cl, int method) {
		int p1 = ((method & 0xf00) << 2) + ((method & 0x080) << 2) + ((method & 0x070) << 1) + (method & 0x00f);
		int p2;
		switch (cl) {
		case STUNMessage.CL_REQUEST:
			p2 = 0x0;
			break;
		case STUNMessage.CL_INDICATION:
			p2 = 0x0010;
			break;
		case STUNMessage.CL_SUCCESS:
			p2 = 0x0100;
			break;
		case STUNMessage.CL_ERROR:
		default:
			p2 = 0x0110;
			break;
		}
		setMessageType(p1 + p2);
	}

	public void setMessageType(int messageType) {
		byte[] mt = ByteMan.integerToBytes(messageType);
		setMessageType(mt);
	}

	public void setMessageType(byte[] messageType) {
		System.arraycopy(messageType, 0, bytes, 0, 2);
	}

	public int getMessageType() {
		return ByteMan.bytesToInteger(bytes[0], bytes[1]);
	}

	/**
	 * This method fills the backing byte array with the given attributes. The
	 * backing byte array must be long enough to accomodate all the given
	 * attributes plus the message integrity attribute, which is automatically
	 * calculated by this method.
	 * 
	 * @param attributes
	 *            The attributes to be added. If null, only the message
	 *            integrity is added.
	 */
	public void setPayload(Map<Integer, byte[]> attributes) {
		int position = 20;
		for (Entry<Integer, byte[]> attr : attributes.entrySet()) {
			byte[] type = ByteMan.integerToBytes(attr.getKey());
			byte[] length = ByteMan.integerToBytes(attr.getValue().length);
			byte[] payload = attr.getValue();
			System.arraycopy(type, 0, bytes, position, 2);
			System.arraycopy(length, 0, bytes, position + 2, 2);
			System.arraycopy(payload, 0, bytes, position + 4, payload.length);
			int remainder = payload.length % 4;
			// update position
			if (remainder == 0)
				position += payload.length + 4;
			else
				position += payload.length + 8 - remainder;
		}
		this.attributes = attributes;
	}

	/**
	 * Sets the attribute MESSAGE-INTEGRITY of this STUN message.
	 * 
	 * @param username
	 *            The username whose password should be used to calculate the
	 *            message integrity attribute.
	 */
	public void setMessageIntegrity(String username) {
		byte[] hmacSha1 = getMessageIntegrity(username);
		int position = bytes.length - 24;
		System.arraycopy(ByteMan.integerToBytes(LyncnatProtocol.attrMessageIntegrity), 0, bytes, position, 2);
		System.arraycopy(ByteMan.integerToBytes(20), 0, bytes, position + 2, 2);
		System.arraycopy(hmacSha1, 0, bytes, position + 4, 20);
		attributes.put(LyncnatProtocol.attrMessageIntegrity, hmacSha1);
	}

	public void setMessageIntegrity() {
		setMessageIntegrity(attributes.get(LyncnatProtocol.attrUsername));
	}

	public void setMessageIntegrity(byte[] username) {
		setMessageIntegrity(new String(username, Charset.forName("utf-8")));
	}

	public byte[] getMessageIntegrity() {
		String username = new String(attributes.get(LyncnatProtocol.attrUsername), Charset.forName("utf-8"));
		return getMessageIntegrity(username);
	}

	public byte[] getMessageIntegrity(String username) {
		byte[] input = Arrays.copyOfRange(bytes, 0, bytes.length - 24);
		return getMessageIntegrity(username, input);
	}

	/**
	 * This method works out the message integrity attribute using HMAC-SHA1 and
	 * returns it.
	 * 
	 * @param username
	 * @param input
	 * @return
	 */
	public byte[] getMessageIntegrity(String username, byte[] input) {
		String sKey = getSecretKey(username);
		if (sKey == null) {
			// the given username does not exist
			log.warn("User " + username + " does not exist");
			return null;
		}
		// Get an hmac_sha1 key from the raw key bytes
		byte[] keyBytes = sKey.getBytes();
		SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

		// Get an hmac_sha1 Mac instance and initialize with the signing key
		Mac mac = null;
		try {
			mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

		// Compute the hmac on input data bytes
		byte[] rawHmac = mac.doFinal(input);

		return rawHmac;
	}

	/**
	 * This method checks if the message integrity attribute of a STUN message
	 * is correct or not.
	 * 
	 * @return true if message integrity is correct, false otherwise.
	 */
	public boolean validMessageIntegrity() {
		byte[] attrUsername = attributes.get(LyncnatProtocol.attrUsername);
		if (attrUsername == null)
			// attribute username was not supplied so validation fails
			return false;
		byte[] claimedMI = attributes.get(LyncnatProtocol.attrMessageIntegrity);
		byte[] realMI = getMessageIntegrity();
		boolean areEqual = Arrays.equals(claimedMI, realMI);
		if (!areEqual)
			log.debug("Message reported MI " + ByteMan.byteArrayToHexString(claimedMI) + " but " + ByteMan.byteArrayToHexString(realMI) + " was found.");
		return areEqual;
	}

	private String getSecretKey(String username) {
		return apiKey;
	}

	public int getMethod() {
		int a = ByteMan.bytesToInteger(bytes[0], bytes[1]);
		return getMethod(a);
	}

	public static int getMethod(int messageType) {
		int r = (messageType & 0x000f) + ((messageType & 0x00e0) >> 1) + ((messageType & 0x0e00) >> 2) + ((messageType & 0x3000) >> 2);
		return r;
	}

	public void setMethod(int messageMethod) {
		setMessageType(getMessageClass(), messageMethod);
	}

	/**
	 * Sets the correct value of the length field in the STUN message.
	 */
	public void setLength() {
		byte[] b = ByteMan.integerToBytes(bytes.length - 20);
		bytes[2] = b[0];
		bytes[3] = b[1];
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

}
