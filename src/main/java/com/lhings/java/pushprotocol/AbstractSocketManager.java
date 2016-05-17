package com.lhings.java.pushprotocol;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.uuid.impl.UUIDUtil;
import com.lhings.java.stun.LyncnatProtocol;
import com.lhings.java.stun.STUNMessage;

public abstract class AbstractSocketManager implements SocketManager {

	protected static String uuidFirstKeepalive;
	protected Set<String> managedUuids = new HashSet<String>();
	
	protected boolean messageNeedsToBeSent(byte[] bytes) {
		STUNMessage m = STUNMessage.getSTUNMessage(bytes);
		if (m.getMethod() != LyncnatProtocol.mKeepAlive)
			return true;
		
		String uuid = UUIDUtil.uuid(m.getAttribute(LyncnatProtocol.attrLyncportId)).toString();
		if (uuidFirstKeepalive == null) {
			uuidFirstKeepalive = uuid;
			managedUuids.add(uuid);
			return true;
		}
		
		if (uuidFirstKeepalive.equals(uuid))
			return true;
		
		if (!managedUuids.contains(uuid)) {
			managedUuids.add(uuid);
			return true;
		}
		
		return false;
		
	}

}
