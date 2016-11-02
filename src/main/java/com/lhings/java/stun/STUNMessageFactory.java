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
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.uuid.impl.UUIDUtil;
import com.lhings.java.model.Action;
import com.lhings.java.model.Argument;
import com.lhings.java.model.ArgumentType;
import com.lhings.java.model.Device;
import com.lhings.java.model.NameValueBean;
import com.lhings.java.model.StateVar;
import com.lhings.java.utils.ArrayUtils;
import com.lhings.java.utils.ByteMan;
import com.lhings.java.utils.Config;


/**
 * This class is used to create new STUN messages, at least the ones allowed by
 * the LyncNat protocol.
 * 
 * 
 * @author José Antonio Lorenzo <joanlofe@gmail.com>
 * 
 *         First created 16/05/2012
 * 
 */

public class STUNMessageFactory {

	private static final Logger log = LoggerFactory.getLogger(STUNMessageFactory.class);
	private String apiKey;
	
	public STUNMessageFactory(){}
	
	private STUNMessageFactory(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * Factory method to create a STUNMessage with the given attributes. This
	 * method does not add to the message the attribute MESSAGE-INTEGRITY. The
	 * message returned does not have the parameter transaction_id set.
	 * 
	 * @param roomForMessageIntegrity
	 *            If false, no space will be left in the STUNMessage to add the
	 *            message integrity attribute.
	 * @param cl
	 *            Class of the STUN message. See definitions in STUNMessage
	 *            class.
	 * @param method
	 *            Method of the STUN message. See definitions in Lyncnat
	 *            protocol class.
	 * @param attrs
	 *            A map with the values of all the attributes that should be
	 *            added to the message.
	 * @return
	 */
	public STUNMessage getSTUNMessage(boolean roomForMessageIntegrity, int cl,
			int method, Map<Integer, byte[]> attrs) {
		// work out message length
		int length = 20;
		for (byte[] array : attrs.values()) {
			int remainder = array.length % 4;
			if (remainder == 0)
				length += array.length + 4;
			else
				length += array.length + 8 - remainder;
		}
		// add the length of the MESSAGE-INTEGRITY attribute
		if (roomForMessageIntegrity)
			length += 24;
		STUNMessage s = new STUNMessage(length, apiKey);
		s.setMessageType(cl, method);
		s.setLength();
		s.setPayload(attrs);
		return s;
	}

	public STUNMessage getSTUNMessage(int cl, int method,
			Map<Integer, byte[]> attrs) {
		return getSTUNMessage(true, cl, method, attrs);
	}

	/**
	 * Factory method to create a STUNMessage with the given attributes. This
	 * method automatically calculates and adds to the message the attribute
	 * MESSAGE-INTEGRITY. The message returned may have the parameter
	 * transaction_id set or not, depending on the value of addTrId.
	 * 
	 * @param cl
	 *            Class of the STUN message. See definitions in STUNMessage
	 *            class.
	 * @param method
	 *            Method of the STUN message. See definitions in Lyncnat
	 *            protocol class.
	 * @param attrs
	 *            A map with the values of all the attributes that should be
	 *            added to the message.
	 * @param addTrid
	 *            If true, a new transaction Id will be automatically added.
	 *            Otherwise, the message will be returned without the
	 *            transaction Id set.
	 * @param addMessageIntegrity
	 *            If true, message integrity will be automatically added. Note
	 *            that for this to be done, LyncnatProtocol.attrUsername must be
	 *            provide in the attrs Hashmap. If false, message integrity will
	 *            not be automatically added, but it still will be possible to
	 *            do it afterwards by calling STUNMessage.setMessageIntegrity.
	 * @return
	 */
	public STUNMessage getSTUNMessage(int cl, int method,
			Map<Integer, byte[]> attrs, boolean addTrId,
			boolean addMessageIntegrity) {
		STUNMessage s = getSTUNMessage(cl, method, attrs);
		if (addTrId)
			s.setTransactionID();
		if (addMessageIntegrity)
			s.setMessageIntegrity();
		return s;
	}

	public STUNMessage getSTUNMessage(int cl, int method,
			Map<Integer, byte[]> attrs, boolean addTrId) {
		return getSTUNMessage(cl, method, attrs, addTrId, true);
	}

	public STUNMessage getSuccessResponse(STUNMessage mRequest) {
		return getSuccessResponse(null, mRequest, false);
	}

	public STUNMessage getSuccessResponse(STUNMessage mRequest, Map<Integer, byte[]> additionalAttrs){
		return getSuccessResponse(null, mRequest, false, additionalAttrs);
	}
	
	public STUNMessage getSuccessResponse(String username,
			STUNMessage mRequest, boolean addUsername) {
		return getSuccessResponse(username, mRequest, addUsername, null);
	}
	
	public STUNMessage getSuccessResponse(String username,
			STUNMessage mRequest, boolean addUsername, Map<Integer, byte[]> additionalAttrs) {
		// length of header + message integrity attribute + timestamp
		int mType = STUNMessage.getMethod(mRequest.getMessageType());
		HashMap<Integer, byte[]> attrs = new HashMap<Integer, byte[]>();
		if(additionalAttrs != null){
			for (Integer key: additionalAttrs.keySet()){
				attrs.put(key, additionalAttrs.get(key));
			}
		}
		attrs.put(LyncnatProtocol.attrTimestamp,
				ByteMan.integer32ToBytes((int) Config.clock.getUTCUnixTime()));
		if (username == null)
			attrs.put(LyncnatProtocol.attrUsername,
					mRequest.getAttribute(LyncnatProtocol.attrUsername));
		else {
			try {
				attrs.put(LyncnatProtocol.attrUsername,
						username.getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				log.error("Unsupported encoding", e);
			}
		}
		STUNMessage mResponse = getSTUNMessage(STUNMessage.CL_SUCCESS, mType,
				attrs, false, true);
		mResponse.setTransactionID(mRequest.getTransactionIDBytes());

		if (username == null)
			username = new String(
					mRequest.getAttribute(LyncnatProtocol.attrUsername),
					Charset.forName("utf-8"));

		mResponse.setMessageIntegrity(username);
		return mResponse;
	}

	public STUNMessage getBindingSuccessResponse(STUNMessage mRequest,
			InetSocketAddress clientAddress) {
		return getBindingSuccessResponse(mRequest, clientAddress, null);
	}

	public STUNMessage getBindingSuccessResponse(STUNMessage mRequest,
			InetSocketAddress clientAddress, byte[] uuid) {
		STUNMessage mResponse;
		if (uuid == null)
			// length of header + XOR-MAPPED-ADDRESS + message integrity attribute
			mResponse = new STUNMessage(80, apiKey);
		else
			// length of header + XOR-MAPPED-ADDRESS + message integrity attribute + uuid attribute
			mResponse = new STUNMessage(96, apiKey);
		int mType = mRequest.getMessageType();
		mResponse.setMessageType(STUNMessage.CL_SUCCESS, mType);
		mResponse.setLength();
		mResponse.setTransactionID(mRequest.getTransactionIDBytes());
		byte[] xorMappedAddress = getXorMappedAddress(clientAddress);
		HashMap<Integer, byte[]> map = new HashMap<Integer, byte[]>();
		// add XOR-MAPPED-ADDRESS attribute
		map.put(LyncnatProtocol.attrXORMappedAddress, xorMappedAddress);
		byte[] ConfigTime = ByteMan.integer32ToBytes((int) Config.clock
				.getUTCUnixTime());
		map.put(LyncnatProtocol.attrTimestamp, ConfigTime);
		map.put(LyncnatProtocol.attrServerTime, ConfigTime);
		int expirationTime = 60;
		map.put(LyncnatProtocol.attrExpirationPolicy,
				ByteMan.integer32ToBytes(expirationTime));
		if (uuid != null)
			map.put(LyncnatProtocol.attrLyncportId, uuid);
		// add attributes to message
		mResponse.setPayload(map);
		// add message integrity
		String username = new String(
				mRequest.getAttribute(LyncnatProtocol.attrUsername),
				Charset.forName("utf-8"));
		mResponse.setMessageIntegrity(username);
		return mResponse;
	}

	private byte[] getXorMappedAddress(InetSocketAddress clientAddress) {
		byte[] mappedAddress = clientAddress.getAddress().getAddress();
		byte[] xorMappedAddress = { (byte) (mappedAddress[0] ^ 0x21),
				(byte) (mappedAddress[1] ^ 0x12),
				(byte) (mappedAddress[2] ^ 0xA4),
				(byte) (mappedAddress[3] ^ 0x42) };
		byte[] port = ByteMan.integerToBytes(clientAddress.getPort());
		byte[] xorPort = { (byte) (port[0] ^ 0x21), (byte) (port[1] ^ 0x12) };
		// build the attribute for the response
		byte[] attrValue = { 0x00, (byte) 0x01, xorPort[0], xorPort[1],
				xorMappedAddress[0], xorMappedAddress[1], xorMappedAddress[2],
				xorMappedAddress[3] };
		return attrValue;
	}

	public STUNMessage getErrorResponse(STUNMessage mRequest, int errorCode,
			String reason) {
		return getErrorResponse(mRequest, errorCode, reason, null);
	}

	public STUNMessage getErrorResponse(STUNMessage mRequest, int errorCode,
			String reason, String username) {
		// length of the message integrity + length of ERROR-CODE attribute +
		// header

		// if the error code is 4xx Lhingnat won't be able to add a message
		// integrity attribute, take this into account
		boolean is4xx = errorCode / 100 == 4;

		int rlen = reason.length();
		int remainder = rlen % 4;
		if (remainder != 0)
			rlen += 4 - remainder;
		// total length is rlen + 4 bytes needed for class number codification
		// of error code
		rlen += 4;
		byte[] value = new byte[rlen];
		// set the header of the value of ERROR-CODE attribute
		value[0] = 0;
		value[1] = 0;
		value[2] = ByteMan.integerToBytes(errorCode / 100)[1];
		value[3] = ByteMan.integerToBytes(errorCode % 100)[1];
		// set the reason phrase
		byte[] bReason = reason.getBytes();
		System.arraycopy(bReason, 0, value, 4, bReason.length);
		// create the STUN message
		HashMap<Integer, byte[]> attrs = new HashMap<Integer, byte[]>();
		attrs.put(LyncnatProtocol.attrErrorCode, value);
		attrs.put(LyncnatProtocol.attrServerTime,
				ByteMan.integer32ToBytes((int) Config.clock.getUTCUnixTime()));
		int expirationTime = (Integer) Config
				.getProperty("defaultMessageExpirationTime");
		attrs.put(LyncnatProtocol.attrExpirationPolicy,
				ByteMan.integer32ToBytes(expirationTime));

		int mType = mRequest.getMethod();
		byte[] usr = null;
		if (!is4xx) {
			usr = mRequest.getAttribute(LyncnatProtocol.attrUsername);
			if (usr == null)
				usr = username.getBytes();
			attrs.put(LyncnatProtocol.attrUsername, usr);
		}
		STUNMessage mResponse;
		if (!is4xx) {
			mResponse = getSTUNMessage(STUNMessage.CL_ERROR, mType, attrs,
					false, false);
			mResponse.setTransactionID(mRequest.getTransactionIDBytes());
			mResponse.setMessageIntegrity(usr);
		} else {
			mResponse = getSTUNMessage(false, STUNMessage.CL_ERROR, mType,
					attrs);
			mResponse.setTransactionID(mRequest.getTransactionIDBytes());
		}
		return mResponse;
	}

	public STUNMessage getBadTimestampMessage(STUNMessage mRequest) {
		// length of the message integrity + length of ERROR-CODE attribute +
		// header
		int errorCode = LyncnatProtocol.errBadTimestamp;
		int length = 76;

		byte[] errorCodeBytes = new byte[4];
		// set the header of the value of ERROR-CODE attribute
		errorCodeBytes[0] = 0;
		errorCodeBytes[1] = 0;
		errorCodeBytes[2] = ByteMan.integerToBytes(errorCode / 100)[1];
		errorCodeBytes[3] = ByteMan.integerToBytes(errorCode % 100)[1];

		// create the STUN message
		STUNMessage mResponse = new STUNMessage(length, apiKey);
		int mType = mRequest.getMessageType();
		mResponse.setMessageType(STUNMessage.CL_ERROR, mType);
		mResponse.setLength();
		byte[] trId = mRequest.getTransactionIDBytes();
		mResponse.setTransactionID(trId);
		HashMap<Integer, byte[]> attributes = new HashMap<Integer, byte[]>();
		byte[] timestamp = ByteMan.integer32ToBytes((int) Config.clock
				.getUTCUnixTime());
		attributes.put(LyncnatProtocol.attrTimestamp, timestamp);
		attributes.put(LyncnatProtocol.attrErrorCode, errorCodeBytes);
		attributes.put(LyncnatProtocol.attrServerTime, timestamp);
		int expirationTime = (Integer) Config
				.getProperty("defaultMessageExpirationTime");
		attributes.put(LyncnatProtocol.attrExpirationPolicy,
				ByteMan.integer32ToBytes(expirationTime));

		mResponse.setPayload(attributes);
		mResponse.setMessageIntegrity(mRequest
				.getAttribute(LyncnatProtocol.attrUsername));

		return mResponse;
	}

	/**
	 * Creates a Begin session message request.
	 * 
	 * @param username
	 *            Name of the user account of Lyncnat to which the LyncPort
	 *            belongs.
	 * @param name
	 *            Name of the Lyncport. It is used to work out the Lyncport-id
	 * @param includeName
	 *            If true, the attribute NAME will be also added to the message
	 *            (this would indicate that this is the first time that Lyncport
	 *            begins session in Lyncnat).
	 * @return
	 */
	public STUNMessage getStartSessionMessage(String username, String name,
			String uuid, boolean includeName) {
		Map<Integer, byte[]> attrs = addCommonAttrs(username, uuid);
		if (includeName)
			attrs.put(LyncnatProtocol.attrName, name.getBytes());
		// set BEGIN-SESSION attribute to true (0x01)
		attrs.put(LyncnatProtocol.attrBeginSession, new byte[] { (byte) 0x01 });
		return getSTUNMessage(STUNMessage.CL_REQUEST, LyncnatProtocol.mBinding,
				attrs, true);
	}

	public STUNMessage getStartSessionMessage(String username, String name,
			byte[] uuid, boolean includeName) {
		Map<Integer, byte[]> attrs = addCommonAttrs(username, uuid);
		if (includeName)
			attrs.put(LyncnatProtocol.attrName, name.getBytes());
		// set BEGIN-SESSION attribute to true (0x01)
		attrs.put(LyncnatProtocol.attrBeginSession, new byte[] { (byte) 0x01 });
		return getSTUNMessage(STUNMessage.CL_REQUEST, LyncnatProtocol.mBinding,
				attrs, true);
	}

	/**
	 * Creates a register device request message
	 * @param username The username to which the device to be registered belongs.
	 * @param name The name that will be given to the device.
	 * @return
	 */
	public STUNMessage getRegisterDeviceMessage(String username, String name){
		LinkedHashMap<Integer, byte[]> attrs = new LinkedHashMap<Integer, byte[]>();
		attrs.put(LyncnatProtocol.attrUsername, username.getBytes());
		attrs.put(LyncnatProtocol.attrTimestamp,
				ByteMan.integer32ToBytes((int) Config.clock.getUTCUnixTime()));
		attrs.put(LyncnatProtocol.attrName, name.getBytes());
		attrs.put(LyncnatProtocol.attrBeginSession, new byte[] { (byte) 0x02 });
		return getSTUNMessage(STUNMessage.CL_REQUEST, LyncnatProtocol.mBinding,
				attrs, true);
	}
	
	/**
	 * Creates a end session message request.
	 * 
	 * @param username
	 *            Name of the user account of Lyncnat to which the LyncPort
	 *            belongs.
	 * @param name
	 *            Name of the Lyncport. It is used to work out the Lyncport-id
	 * @return
	 */
	public STUNMessage getEndSessionMessage(String username, String uuid) {
		Map<Integer, byte[]> attrs = addCommonAttrs(username, uuid);
		attrs.put(LyncnatProtocol.attrBeginSession, new byte[] { (byte) 0x00 });
		return getSTUNMessage(STUNMessage.CL_REQUEST, LyncnatProtocol.mBinding,
				attrs, true);
	}

	/**
	 * Creates a no NAT message request.
	 * 
	 * @param username
	 *            Name of the user account of Lyncnat to which the LyncPort
	 *            belongs.
	 * @param name
	 *            Name of the Lyncport. It is used to work out the Lyncport-id
	 * @return
	 */
	public STUNMessage getNoNATMessage(String username, String name) {
		Map<Integer, byte[]> attrs = addCommonAttrs(username, name);
		return getSTUNMessage(STUNMessage.CL_REQUEST, LyncnatProtocol.mNoNAT,
				attrs);
	}

	/**
	 * Creates a no LOG message request.
	 * 
	 * @param username
	 *            Name of the user account of Lyncnat to which the LyncPort
	 *            belongs.
	 * @param uuid
	 *            Unique identifier of this lhingport device.
	 * @return
	 */
	public STUNMessage getLogMessageRequest(String username, String uuid,
			int errorLevel, String message) {
		Map<Integer, byte[]> attrs = addCommonAttrs(username, uuid);
		byte[] errLevelBytes = { (byte) errorLevel };
		attrs.put(LyncnatProtocol.attrErrorLevel, errLevelBytes);
		attrs.put(LyncnatProtocol.attrLogMessage, message.getBytes());

		return getSTUNMessage(STUNMessage.CL_REQUEST, LyncnatProtocol.mLog,
				attrs, true, true);
	}

	/**
	 * Returns the pairs argument value contained in the arguments attribute of a STUNMessage.
	 * @param argsAttr A byte array containing the payload of the arguments attribute.
	 * @param action The action whose inputs are expected to be contained in the arguments attribute given.
	 * @return A map whose keys are the names of the arguments and whose values are the values of the arguments.
	 */
	public static Map<String, Object> processArgumentsAttribute(byte[] argsAttr,
			Action action) {
		int numArgs = (int) argsAttr[0];
		Map<String, Object> argValues = new HashMap<String, Object>();
		if (numArgs < 1){
			// no args, return empty map
			return argValues;
		}
		Map<String, String> argsMap = new HashMap<String, String>();
		
		List<Argument> list = action.getInputs();
		for (Argument arg : list) {
			argsMap.put(arg.getName(), arg.getType());
		}
		// data starts at position numArgs + 2, so initial offset in the array
		// is this value
		int offset = numArgs + 2;
		byte stringMask = argsAttr[numArgs + 1];
		for (int j = 0; j < numArgs; j++) {
			int rawArgLength = ByteMan.bytesToInteger((byte)0x00, argsAttr[j + 1]);
			int argLength = rawArgLength + 4;
			byte[] argument = Arrays.copyOfRange(argsAttr, offset, offset
					+ argLength);
			
			// processing depends on the argument being of type string or other
			boolean isString = ((stringMask >> j) & 1) == 1;
			if (!isString) {
				// argument is not a string
				// retrieve name of argument
				String name = new String(Arrays.copyOfRange(argument, 4,
						argument.length), Charset.forName("utf-8"));
				// retrieve type of argument
				String type = argsMap.get(name);
				byte[] byteValue = Arrays.copyOfRange(argument, 0, 4);
				Object value = Integer.valueOf((int) ByteMan
						.bytesToInteger32(byteValue));
				switch (ArgumentType.getType(type)) {
				case TIMESTAMP:
				case INTEGER:
					break;
				case FLOAT:
					value = Float.intBitsToFloat((Integer) value);
					break;
				case BOOLEAN:
					value = new Boolean(value.equals(Integer.valueOf(1)));
					break;
				default:
					throw new IllegalArgumentException();
				}
				argValues.put(name, value);
			} else {
				// argument is a string
				int valueLength = ByteMan.bytesToInteger(argument[0], argument[1]);
//				int nameLength = ByteMan.bytesToInteger(argument[2], argument[3]);
				String value = new String(Arrays.copyOfRange(argument, 4, 4+valueLength), Charset.forName("utf-8"));
				String name = new String(Arrays.copyOfRange(argument, 4+valueLength, argument.length), Charset.forName("utf-8"));
				argValues.put(name, value);
			}
			offset += argument.length;
		}
		return argValues;
	}
	
	public STUNMessage getEventMessage(String username, String uuid,
			String name, String payload) {
		Map<Integer, byte[]> attrs = addCommonAttrs(username, uuid);
		try {
			attrs.put(LyncnatProtocol.attrName, name.getBytes("utf-8"));
			attrs.put(LyncnatProtocol.attrPayload, payload.getBytes("utf-8"));
		} catch (UnsupportedEncodingException ex) {
			log.error("Unsupported encoding", ex);
			return null;
		}
		return getSTUNMessage(STUNMessage.CL_REQUEST, LyncnatProtocol.mEvent,
				attrs, true, true);
	}

	public STUNMessage getSubscribeMessage(String username, String uuid,
			String feedName, boolean subscribe) {
		Map<Integer, byte[]> attrs = addCommonAttrs(username, uuid);
		try {
			attrs.put(LyncnatProtocol.attrEventName, feedName.getBytes("utf-8"));
		} catch (UnsupportedEncodingException ex) {
			log.error("Unsupported encoding", ex);
			return null;
		}
		int method;
		if (subscribe)
			method = LyncnatProtocol.mSubscribe;
		else
			method = LyncnatProtocol.mUnsubscribe;
		return getSTUNMessage(STUNMessage.CL_REQUEST, method, attrs, true, true);
	}

	/**
	 * Creates a Keep-alive message request.
	 * 
	 * @param username
	 *            Name of the user account of Lyncnat to which the LyncPort
	 *            belongs.
	 * @param name
	 *            Name of the Lyncport. It is used to work out the Lyncport-id
	 * @return
	 */
	public STUNMessage getKeepAliveMessage(String username, String uuid) {
		Map<Integer, byte[]> attrs = addCommonAttrs(username, uuid);
		return getSTUNMessage(STUNMessage.CL_REQUEST,
				LyncnatProtocol.mKeepAlive, attrs, true, true);
	}

	/**
	 * Creates a STUNMessage instance that can be used to convey the command to
	 * perform an action to a device.
	 * 
	 * @param username
	 *            Username of the account to which the device belongs.
	 * @param uuid
	 *            UUID of the device that will perform the action
	 * @param action
	 *            An Action instance containing the description of the action to
	 *            be performed
	 * @param argValues
	 *            A Map instance with the name of the input arguments of the
	 *            actions as keys, and the Object instances that represent the
	 *            value of the parameters as values.
	 * @return The STUNMessage instance signed and ready to be sent to the
	 *         target device.
	 * @throws BadRequestException 
	 */

//	public STUNMessage getActionMessage(String username, String uuid,
//			Action action, Map<String, Object> argValues) throws BadRequestException {
//		return getActionMessage(username, uuid, action, argValues, null, true, true);
//	}

	
	/**
	 * Creates a STUNMessage instance that can be used to convey the command to
	 * perform an action to a device.
	 * 
	 * @param username
	 *            Username of the account to which the device belongs.
	 * @param uuid
	 *            UUID of the device that will perform the action
	 * @param action
	 *            An Action instance containing the description of the action to
	 *            be performed
	 * @param argValues
	 *            A Map instance with the name of the input arguments of the
	 *            actions as keys, and the Object instances that represent the
	 *            value of the parameters as values.
	 * @param payload
	 *            An array of bytes containing the value of the payload attribute
	 *            for this message. If null, payload attribute will not be sent.
	 * @return The STUNMessage instance signed and ready to be sent to the
	 *         target device.
	 * @throws BadRequestException 
	 */
//	public STUNMessage getActionMessage(String username, String uuid,
//			Action action, Map<String, Object> argValues, byte[] payload) throws BadRequestException {
//		return getActionMessage(username, uuid, action, argValues, payload, true, true);
//	}
//	
//	public STUNMessage getActionMessage(String username, String uuid,
//			Action action, Map<String, Object> argValues, boolean addTrId,
//			boolean setMessageIntegrity) throws BadRequestException {
//		return getActionMessage(username, uuid, action, argValues, null, addTrId, setMessageIntegrity);
//	}
	
	
	/**
	 * Creates a STUNMessage instance that can be used to convey the command to
	 * perform an action to a device.
	 * 
	 * @param username
	 *            Username of the account to which the device belongs.
	 * @param uuid
	 *            UUID of the device that will perform the action.
	 * @param action
	 *            An Action instance containing the description of the action to
	 *            be performed
	 * @param argValues
	 *            A Map instance with the name of the input arguments of the
	 *            actions as keys, and the Object instances that represent the
	 *            value of the parameters as values.
	 * @param addTrId
	 *            If true a new transaction id is automatically added.
	 * @param setMessageIntegrity
	 *            If true the message integrity of the message is set.
	 * @return
	 * @throws BadRequestException 
	 */
//	public STUNMessage getActionMessage(String username, String uuid,
//			Action action, Map<String, Object> argValues, byte[] payload, boolean addTrId,
//			boolean setMessageIntegrity) throws BadRequestException {
//		Map<Integer, byte[]> attrs = addCommonAttrs(username, uuid);
//
//		try {
//			attrs.put(LyncnatProtocol.attrName,
//					action.getName().getBytes("utf-8"));
//			attrs.put(LyncnatProtocol.attrArguments,
//					getArgumentsAttribute(action, argValues));
//			if (payload != null)
//				attrs.put(LyncnatProtocol.attrPayload,
//						payload);
//		} catch (UnsupportedEncodingException e) {
//			log.error("Strange encoding error, see stack trace for details", e);
//			return null;
//		}
//
//		return getSTUNMessage(STUNMessage.CL_REQUEST, LyncnatProtocol.mAction,
//				attrs, addTrId, setMessageIntegrity);
//	}

	/**
	 * Processes a LyncnatProtocol.attrArgument attribute and extracts the name
	 * value pairs contained in it.
	 * 
	 * @param attrBytes
	 *            The raw bytes of the attribute, directly extracted from the
	 *            STUN message
	 * @param sender
	 *            The device which sent the STUNMessage
	 * @return A list with the name value pairs.
	 */
	public static List<NameValueBean> getStateVarsFromAttribute(
			byte[] attrBytes, Device sender) {
		// parse state variables for the given array of bytes. the format to
		// parse is the same as the produced
		// by the method STUNMessageFactory.getArgumentsAttribute
		List<NameValueBean> stateVarValues = new ArrayList<NameValueBean>();
		int numStateVars = (int) attrBytes[0];
		if (numStateVars == 0)
			// there are no state vars, return an empty list
			return stateVarValues;
		// get the mask to know which arguments are strings
		byte stringMask = attrBytes[numStateVars + 1];
		// copy the section with the lengths of the state vars
		byte[] lengths = Arrays.copyOfRange(attrBytes, 1, numStateVars + 1);
		// slice attrBytes so that the array starts with the data of the first
		// state var
		attrBytes = Arrays.copyOfRange(attrBytes, numStateVars + 2,
				attrBytes.length);

		// build a map with the type of the state vars by name
		Map<String, String> types = new LinkedHashMap<String, String>();
		for (StateVar var : sender.getStateVariables()) {
			types.put(var.getName(), var.getType());
		}

		for (int j = 0; j < numStateVars; j++) {

			// get the first four bytes of the definition of the stateVar
			byte[] fourBytes = Arrays.copyOfRange(attrBytes, 0, 4);
			byte[] payload = Arrays.copyOfRange(attrBytes, 4, lengths[j] + 4);
			try {
				if ((stringMask & (1 << j)) == (1 << j)) {
					// the statevar is of type string
					int valueLength = ByteMan.bytesToInteger(fourBytes[0],
							fourBytes[1]);
					String name = new String(Arrays.copyOfRange(payload,
							valueLength, payload.length), "utf-8");
					if (types.get(name) == null) {
						log.warn("Variable with name " + name
								+ " does not exist for device "
								+ sender.getUuidString());
					} else {
						String value = new String(Arrays.copyOfRange(payload,
								0, valueLength), "utf-8");
						stateVarValues.add(new NameValueBean(name, value));
					}
				} else {
					// the statevar is not a string
					String name = new String(payload, "utf-8");
					String type = types.get(name);
					if (type == null)
						log.warn("Variable with name " + name
								+ " does not exist for device "
								+ sender.getUuidString());
					else {
						Integer number = Integer.valueOf((int) ByteMan
								.bytesToInteger32(fourBytes));
						if (type.equalsIgnoreCase("integer")) {
							stateVarValues.add(new NameValueBean(name, number));
						} else if (type.equalsIgnoreCase("boolean")) {
							if (number == 0)
								stateVarValues.add(new NameValueBean(name,
										Boolean.FALSE));
							else
								stateVarValues.add(new NameValueBean(name,
										Boolean.TRUE));
						} else if (type.equalsIgnoreCase("float")) {
							Float f = Float.intBitsToFloat(number.intValue());
							stateVarValues.add(new NameValueBean(name, f));
						} else if (type.equalsIgnoreCase("timestamp")) {
							stateVarValues.add(new NameValueBean(name,
									new Date(number * 1000)));
						} else
							log.warn("Variable with type " + type
									+ " does not exist for device "
									+ sender.getUuidString());
					}
				}
			} catch (UnsupportedEncodingException ex) {
				log.error("Unsupported encoding", ex);
				return null;
			}

			// slice attrBytes so that the array starts with the data of the
			// first state var
			attrBytes = Arrays.copyOfRange(attrBytes, lengths[j] + 4,
					attrBytes.length);
		}

		return stateVarValues;
	}

	public static byte[] buildArgumentsAttribute(List<Argument> args, Map<String, Object> argValues) {
		// note that limiting the stringMask to one byte, limits the maximum
		// capacity
		// of arguments an action message can transmit to 8
		byte stringMask = 0x00;
		int numArgs = args.size();
		
		// allocate array for header and fill it with the right information
		byte[] header = new byte[numArgs + 2];
		header[0] = (byte) numArgs;
		List<byte[]> arrays = new ArrayList<byte[]>();
		arrays.add(header);
		// determine the number of arguments that are strings
		for (int j = 0; j < numArgs; j++) {
			// get the bytes for the string argument
			Argument arg = args.get(j);
			if (arg.getType().equalsIgnoreCase("string")) {
				// set this argument in the mask as string
				stringMask = (byte) (stringMask | (1 << j));
				byte[] name = arg.getName().getBytes(Charset.forName("utf-8"));
				int lengthName = name.length;
				byte[] strValue = ((String) argValues.get(arg.getName()))
						.getBytes(Charset.forName("utf-8"));
				int lengthPayload = 0;
				if (strValue != null)
					lengthPayload = strValue.length;
				// build the four bytes that contain information about the
				// string and its length
				byte[] lengthInfo = { 0x00, (byte) lengthPayload, 0x00,
						(byte) lengthName };
				// note that the length of the concatenated array must not
				// exceed 256 bytes
				byte[] totalPayload = ArrayUtils.addAll(lengthInfo, strValue,
						name);
				arrays.add(totalPayload);
				header[j + 1] = (byte) (totalPayload.length - 4);
			} else {
				// get the bytes for the non-string argument
				byte[] name = arg.getName().getBytes(Charset.forName("utf-8"));
				header[j + 1] = (byte) name.length;
				byte[] payload = null;
				String type = arg.getType().toLowerCase();
				switch (ArgumentType.getType(type)) {
				case BOOLEAN:
					payload = new byte[4];
					payload[0] = 0;
					payload[1] = 0;
					payload[2] = 0;
					if ((Boolean) argValues.get(arg.getName()))
						payload[3] = 1;
					else
						payload[3] = 0;
					break;
				case INTEGER:
					payload = ByteMan.integer32ToBytes((Integer) argValues
							.get(arg.getName()));
					break;
				case FLOAT:
					payload = ByteMan.integer32ToBytes(Float
							.floatToRawIntBits((Float) argValues.get(arg
									.getName())));
					break;
				case TIMESTAMP:
					int tstValue = (int) (((Date)argValues.get(arg.getName())).getTime()/1000);
					payload = ByteMan.integer32ToBytes(tstValue);
					break;
				default:
					break;

				}

				if (payload != null)
					arrays.add(ArrayUtils.concat(payload, name));
			}
		}
		header[numArgs + 1] = stringMask;
		byte[] returnArgument = ArrayUtils.addAll(arrays);
		return returnArgument;
	}
	
	
	/**
	 * Adds the attributes common to most STUNMessages to the given HashMap.
	 * Currently adds attributes LYNCPORT-ID, USERNAME, and TIMESTAMP
	 * 
	 * @param attrs
	 * @param username
	 * @param name
	 */
	private Map<Integer, byte[]> addCommonAttrs(String username, byte[] uuid) {
		LinkedHashMap<Integer, byte[]> attrs = new LinkedHashMap<Integer, byte[]>();
		attrs.put(LyncnatProtocol.attrUsername, username.getBytes());
		attrs.put(LyncnatProtocol.attrTimestamp,
				ByteMan.integer32ToBytes((int) Config.clock.getUTCUnixTime()));
		attrs.put(LyncnatProtocol.attrLyncportId, uuid);
		return attrs;
	}

	private Map<Integer, byte[]> addCommonAttrs(String username, String uuid) {
		byte[] bytes = UUIDUtil.asByteArray(UUIDUtil.uuid(uuid));
		return addCommonAttrs(username, bytes);
	}

	public static STUNMessageFactory getInstance(String apiKey) {
		return new STUNMessageFactory(apiKey);
	}
}
