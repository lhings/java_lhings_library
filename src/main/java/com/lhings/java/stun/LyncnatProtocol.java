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

/**
 * This class, together with the class WorkflowFactory, provides the core
 * specifications of the LyncNat protocol. In particular this class provides
 * all the constants for the definition of attributes and methods to be used
 * inside the STUN messages used as transport for the protocol.
 * 
 * Constants for message class are not defined in this class, but in
 * STUNMessage class.
 * 
 * @author Jos�� Antonio Lorenzo <joanlofe@gmail.com>
 *
 * First created 16/05/2012
 *
 */

public class LyncnatProtocol {
	
	// method code definitions (RFC 5389)
	public static final int mBinding = 0x0001;

	// STUN message attribute type definitions
	// comprehension required (RFC 5389)
	public static final int attrMappedAddress = 0x0001;
	public static final int attrUsername = 0x0006;
	public static final int attrMessageIntegrity = 0x0008;
	public static final int attrErrorCode = 0x0009;
	public static final int attrUnknownAttributes = 0x000A;
	public static final int attrRealm = 0x0014;
	public static final int attrNonce = 0x0015;
	public static final int attrXORMappedAddress = 0x0020;
	
	// comprehension optional (RFC 5389)
	public static final int attrSoftware = 0x8022;
	public static final int attrAlternateServer = 0x8023;
	public static final int attrFingerprint = 0x8028;
	
	// error codes (RFC 5389)
	public static final int errTryAlternate = 300;
	public static final int errBadRequest = 400;
	public static final int errUnauthorized = 401;
	public static final int errUnknownAttribute = 420;
	public static final int errStaleNonce = 438;
	public static final int errServerError = 500;
	
	
	// LyncNat protocol defined methods
	public static final int mKeepAlive = 0xEEA;
	public static final int mChangeName = 0xCAE;
	public static final int mStatusRequest = 0xAEE;
	public static final int mFiledownRequest = 0xFED;
	public static final int mFirminstRequest = 0xFEE;
	public static final int mP2PLocate = 0x2CA;
	public static final int mP2PConnNotify = 0x2CF;
	public static final int mP2PLink = 0x2D0;
	public static final int mP2PConnClose = 0xCCE;
	public static final int mNoNAT = 0xAAA;
	public static final int mPing = 0xEE0;
	public static final int mLog = 0x00A;
	public static final int mReset = 0xEE1;
	public static final int mDelete = 0xEEE;
	public static final int mSubscribe = 0xEE2;
	public static final int mUnsubscribe = 0xEE3;
	public static final int mEvent = 0xEE4;
	public static final int mNotification = 0xEE5;
	public static final int mAction = 0xEE5;
	public static final int mCustomCommand = 0xEE6;
	
	// LyncNat protocol defined attributes
	public static final int attrTimestamp = 0x0C01;
	public static final int attrBeginSession = 0x0C02;
	public static final int attrLyncportId = 0x0C03;
	public static final int attrLyncportIdCallee = 0x0C04;
	public static final int attrLyncportIdCaller = 0x0C05;
	public static final int attrNewName = 0x0C06;
	public static final int attrLyncportConnectToPort = 0x0C07;
	public static final int attrFilename = 0x0C08;
	public static final int attrURL = 0x0C09;
	public static final int attrFileIntegrity = 0x0C10;
	public static final int attrFileLength = 0x0C11;
	public static final int attrName = 0x0C12;
	public static final int attrServerTime = 0x0C13;
	public static final int attrExpirationPolicy = 0x0C14;
	public static final int attrLogMessage = 0x0C15;
	public static final int attrErrorLevel = 0x0C16;
	public static final int attrEventName = 0x0C17;
	public static final int attrPayload = 0x0C18;
	public static final int attrArguments = 0x0C19;
	public static final int attrGcmRegId = 0x0C20;
	
	//LyncNat protocol defined error codes
	public static final int errInvalidName = 600;
	public static final int errOutOfMemory = 601;
	public static final int errBadFile = 602;
	public static final int errInstallFailed = 603;
	public static final int errNotAvailable = 604;
	public static final int errBadTimestamp = 605;
	public static final int errDevQuotaExceeded = 606;
	public static final int errDuplicateUuid = 607;
	
	public static final int MVC_MAPPING_RULES_TARGET_DEVICE = 1;
	public static final int MVC_MAPPING_RULES_TARGET_APP = 2;
}
