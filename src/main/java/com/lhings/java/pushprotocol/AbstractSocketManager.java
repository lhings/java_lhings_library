package com.lhings.java.pushprotocol;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
	protected Set<String> managedUuids = new HashSet<String>();
	
	protected boolean isKeepAliveMessageAnswer(byte[] bytes) {
		STUNMessage m = STUNMessage.getSTUNMessage(bytes);
		int method = m.getMethod();
		int messageClass = m.getMessageClass();
		if (method == LyncnatProtocol.mKeepAlive && (messageClass == STUNMessage.CL_SUCCESS || messageClass == STUNMessage.CL_ERROR)) {
			timeLastKeepaliveAnswerWasReceived = System.currentTimeMillis();
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
			managedUuids.add(uuid);
			log.debug("Device {} will be the only one sending keepalives.", uuid);
			return true;
		}
		
		if (uuidFirstKeepalive.equals(uuid)) {
			log.debug("Keepalive sent for device {}", uuid);
			return true;
		}
		
		if (!managedUuids.contains(uuid)) {
			managedUuids.add(uuid);
			log.debug("Keepalive allowed for device {}. No more keepalives will be allowed for this device.", uuid);
			return true;
		}
		
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
