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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.lhings.java.LhingsDevice;
import com.lhings.java.exception.LhingsException;
import com.lhings.java.logging.LhingsLogger;

public class ListenerThread implements Runnable {

	private static final int SOCKET_BUFFER_SIZE = 2048;
	private static final int RECEIVE_TIMEOUT_MILLIS = 40;
	private static final String SERVER_HOSTNAME = "www.lhings.com";
	private static final int SERVER_PORT = 3478;
	private static final InetSocketAddress SERVER_ADDRESS = new InetSocketAddress(
			SERVER_HOSTNAME, SERVER_PORT);
	private static Logger log = LhingsLogger.getLogger();

	private int clientPort;
	private DatagramSocket serverSocket;
	private BlockingQueue<byte[]> receivedMessages = new LinkedBlockingQueue<byte[]>();
	private BlockingQueue<byte[]> messagesToSend = new LinkedBlockingQueue<byte[]>();

	private boolean running = true;

	/**
	 * Listener thread initialization takes place here.
	 * 
	 * @return true if initialization is successful, false otherwise.
	 * @throws LhingsException
	 */
	private ListenerThread(int port) throws LhingsException {
		try {
			serverSocket = new DatagramSocket(null);
			serverSocket.setSoTimeout(RECEIVE_TIMEOUT_MILLIS);
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(port));
			this.clientPort = port;
			log.log(Level.DEBUG, "Listener thread ready, listening on port " + port);
		} catch (SocketException e) {
			log.log(Level.ERROR, "Listening socket could not be initialized, see stack trace for details.", e);
			throw new LhingsException(e);
		}
	}

	/**
	 * Instantiates and starts a new ListenerThread instance.
	 * 
	 * @param port
	 * @return
	 * @throws LhingsException
	 *             If the socket for push communications
	 */
	public static ListenerThread getInstance(int port, LhingsDevice device) throws LhingsException {
		ListenerThread lt = new ListenerThread(port);
		Thread listenerThread = new Thread(lt);
		listenerThread.setName("thread-listener-" + device.uuid());
		listenerThread.start();
		return lt;
	}

	public void run() {
		// main listening loop
		while (running) {
			boolean aMessageWasReceived = true;
			DatagramPacket messageReceived = new DatagramPacket(
					new byte[SOCKET_BUFFER_SIZE], 0, SOCKET_BUFFER_SIZE);
			//log.debug(messagesToSend.size());
			try {
				serverSocket.receive(messageReceived);
			} catch (SocketTimeoutException ex) {
				aMessageWasReceived = false;
			} catch (IOException e) {
				aMessageWasReceived = false;
				log.log(Level.ERROR, "A network IO error ocurred, see stack trace for details", e);
				continue;
			}
			
			if (aMessageWasReceived){
				// store message for main device thread consumption
				receivedMessages.add(Arrays.copyOf(messageReceived.getData(), messageReceived.getLength()));
			}
			
			// read new message to send, if any
			byte[] messageSent = messagesToSend.poll();
			if (messageSent == null)
				continue;
			
			try {
				DatagramPacket dp = new DatagramPacket(messageSent,
						messageSent.length, SERVER_ADDRESS);
				send(dp);
			} catch (IOException ex) {
				log.error("Network error sending message to server.");
			}
		}

	}

	public void stop() {
		running = false;
	}

	private void send(DatagramPacket dp) throws IOException {
		DatagramSocket ds = new DatagramSocket(null);
		ds.setReuseAddress(true);
		ds.bind(new InetSocketAddress(clientPort));
		ds.send(dp);
		ds.close();
	}

	public void send(byte[] message){
		messagesToSend.add(message);
	}

	public byte[] receive(){
		return receivedMessages.poll();
	}
}
