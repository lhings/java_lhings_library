package com.lhings.java.pushprotocol;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.uuid.impl.UUIDUtil;
import com.lhings.java.stun.LyncnatProtocol;
import com.lhings.java.stun.STUNMessage;

public abstract class AbstractSocketManager implements SocketManager {

	protected final static long keepaliveTimeout = 3 * 60 * 1000; // 3 minutes
	private static final Logger log = LoggerFactory.getLogger(AbstractSocketManager.class);

	protected Long timeLastKeepaliveAnswerWasReceived;
	
	protected static String uuidFirstKeepalive;
	protected ConcurrentHashMap<String, Boolean> keepaliveSuccess = new ConcurrentHashMap<String, Boolean>();
//	protected Set<String> managedUuids = new HashSet<String>();
	
	protected boolean isKeepAliveMessageAnswer(byte[] bytes) {
		STUNMessage m = STUNMessage.getSTUNMessage(bytes);
		int method = m.getMethod();
		int messageClass = m.getMessageClass();
		if (method == LyncnatProtocol.mKeepAlive && (messageClass == STUNMessage.CL_SUCCESS || messageClass == STUNMessage.CL_ERROR)) {
			timeLastKeepaliveAnswerWasReceived = System.currentTimeMillis();
			String uuid = UUIDUtil.uuid(m.getAttribute(LyncnatProtocol.attrLyncportId)).toString();
			keepaliveSuccess.put(uuid, Boolean.TRUE);
			if(uuidFirstKeepalive != null && !uuid.equals(uuidFirstKeepalive))
				log.debug("No more keepalives will be sent for device {}.", uuid);
			return true;
		}
		else
			return false;
	}
	
	protected boolean messageNeedsToBeSent(byte[] bytes) {
		STUNMessage m = STUNMessage.getSTUNMessage(bytes);
		if (m.getMethod() != LyncnatProtocol.mKeepAlive)
			return true;
		
		String uuid = UUIDUtil.uuid(m.getAttribute(LyncnatProtocol.attrLyncportId)).toString();
		if (uuidFirstKeepalive == null) {
			uuidFirstKeepalive = uuid;
			log.debug("Device {} will be the only one sending keepalives.", uuid);
			log.debug("Keepalive sent for device {}", uuid);
			keepaliveSuccess.put(uuid, Boolean.FALSE);
			return true;
		}
		
		if (uuidFirstKeepalive.equals(uuid)) {
			log.debug("Keepalive sent for device {}", uuid);
			return true;
		}
		
		if (keepaliveSuccess.get(uuid) == null || keepaliveSuccess.get(uuid) == false) {
			keepaliveSuccess.put(uuid, Boolean.FALSE);
			log.debug("Keepalive sent for device {}", uuid);
			return true;
		}
		
//		if (uuidFirstKeepalive == null) {
//			uuidFirstKeepalive = uuid;
//			managedUuids.add(uuid);
//			log.debug("Device {} will be the only one sending keepalives.", uuid);
//			LhingsDevice.keepaliveResponses.put(m.getTransactionID(), info);
//			return true;
//		}
//		
//		if (uuidFirstKeepalive.equals(uuid)) {
//			log.debug("Keepalive sent for device {}", uuid);
//			LhingsDevice.keepaliveResponses.put(m.getTransactionID(), info);
//			return true;
//		}
//		
//		if (!managedUuids.contains(uuid)) {
//			managedUuids.add(uuid);
//			log.debug("Keepalive allowed for device {}. No more keepalives will be allowed for this device.", uuid);
//			LhingsDevice.keepaliveResponses.put(m.getTransactionID(), info);
//			return true;
//		}
		
		return false;
		
	}

	protected void checkKeepaliveTimeout() throws IOException {
		if (timeLastKeepaliveAnswerWasReceived != null) {
			long timeSinceLastKeepaliveAck = System.currentTimeMillis() - timeLastKeepaliveAnswerWasReceived;
			if (timeSinceLastKeepaliveAck > keepaliveTimeout) {
				timeLastKeepaliveAnswerWasReceived = null;
				throw new IOException("Too much time without news from server, connection seems to be down. Reconnecting...");
			}
		}
	}
	
}
