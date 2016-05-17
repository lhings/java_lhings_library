package com.lhings.java.pushprotocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.lhings.java.exception.LhingsException;
import com.lhings.java.logging.LhingsLogger;

public class UDPSocketManager extends AbstractSocketManager {

	private static final int SOCKET_BUFFER_SIZE = 2048;
	private static final String SERVER_HOSTNAME = "www.lhings.com";
	private static final int SERVER_PORT = 3478;
	private static final InetSocketAddress SERVER_ADDRESS = new InetSocketAddress(SERVER_HOSTNAME, SERVER_PORT);
	private static Logger log = LhingsLogger.getLogger();

	private DatagramSocket clientSocket;
	private int clientPort;

	public void send(byte[] bytes) throws LhingsException {
		try {
			DatagramPacket dp = new DatagramPacket(bytes, bytes.length, SERVER_ADDRESS);
			DatagramSocket ds = new DatagramSocket(null);
			ds.setReuseAddress(true);
			ds.bind(new InetSocketAddress(clientPort));
			ds.send(dp);
			ds.close();
		} catch (IOException ex) {
			log.error("Network error sending message to server.");
			throw new LhingsException(ex);
		}

	}

	public byte[] receive() {
		byte[] messageBytes;
		DatagramPacket messageReceived = new DatagramPacket(new byte[SOCKET_BUFFER_SIZE], 0, SOCKET_BUFFER_SIZE);
		try {
			clientSocket.receive(messageReceived);
			messageBytes = Arrays.copyOf(messageReceived.getData(), messageReceived.getLength());
		} catch (SocketTimeoutException ex) {
			messageBytes = null;
		} catch (IOException e) {
			messageBytes = null;
			log.log(Level.ERROR, "A network IO error ocurred, see stack trace for details", e);
		}
		return messageBytes;
	}

	public void init() throws LhingsException {
		try {
			clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(RECEIVE_TIMEOUT_MILLIS);
			clientSocket.setReuseAddress(true);
			this.clientPort = clientSocket.getLocalPort();
			log.log(Level.INFO, "Device socket ready, bound to port " + this.clientPort);
		} catch (SocketException e) {
			log.log(Level.ERROR, "Device socket could not be initialized, see stack trace for details.", e);
			throw new LhingsException(e);
		}

	}

	public void close() {
		
	}
	
	public int getPort() {
		return this.clientPort;
	}

	

}
