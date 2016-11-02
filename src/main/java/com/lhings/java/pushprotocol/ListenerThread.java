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

package com.lhings.java.pushprotocol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lhings.java.LhingsDevice;
import com.lhings.java.exception.LhingsException;

public class ListenerThread implements Runnable {

	private static Logger log = LoggerFactory.getLogger(ListenerThread.class);
	private static ListenerThread instance;
	private BlockingQueue<byte[]> receivedMessages = new LinkedBlockingQueue<byte[]>();
	private BlockingQueue<byte[]> messagesToSend = new LinkedBlockingQueue<byte[]>();

	private boolean running = true;
	private SocketManager socketMan;

	/**
	 * Listener thread initialization takes place here.
	 * 
	 * @return true if initialization is successful, false otherwise.
	 * @throws LhingsException
	 */
	private ListenerThread(SocketManager socketMan) throws LhingsException {
		this.socketMan = socketMan;
		socketMan.init();
	}

	/**
	 * Instantiates and starts a new ListenerThread instance.
	 * 
	 * @param port
	 * @return
	 * @throws LhingsException
	 *             If the socket for push communications
	 */
	public static ListenerThread getInstance(LhingsDevice device, SocketManager socketManager) throws LhingsException {
		if (instance != null)
			return instance;
		instance = new ListenerThread(socketManager);
		Thread listenerThread = new Thread(instance);
		listenerThread.setName("thr-list-" + device.uuid().substring(0, 5));
		device.setPort(socketManager.getPort());
		listenerThread.start();
		return instance;
	}

	public void run() {
		// main listening loop
		while (running) {
			try {
				byte[] messageReceived = null;
				messageReceived = socketMan.receive();

				if (messageReceived != null) {
					// store message for main device thread consumption
					receivedMessages.add(messageReceived);
				}
				// read new message to send, if any
				byte[] messageSent = messagesToSend.poll();
				
				if (messageSent == null)
					continue;
				
				socketMan.send(messageSent);
			} catch (LhingsException ex) {
				log.error("Network error sending message to server: {}", ex.getMessage());
			} catch (Exception ex) {
				StringWriter sw = new StringWriter();
				ex.printStackTrace(new PrintWriter(sw));
				log.error("Unexpected exception in ListenerThread. See stack trace for details. \n {}", sw.toString());
			}
		}

	}

	public void stop() {
		running = false;
	}

	public void send(byte[] message) {
		messagesToSend.add(message);
	}

	public byte[] remove() {
		return receivedMessages.poll();
	}

	public byte[] readMessage() {
		return receivedMessages.peek();
	}
}
