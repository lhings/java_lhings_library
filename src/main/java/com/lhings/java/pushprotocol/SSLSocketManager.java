package com.lhings.java.pushprotocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

import com.lhings.java.exception.LhingsException;
import com.lhings.java.logging.LhingsLogger;
import com.lhings.java.utils.ByteMan;

public class SSLSocketManager implements SocketManager{

	private static final int SOCKET_BUFFER_SIZE = 2048;
	private static final String SERVER_HOSTNAME = "www.lhings.com";
	private static final int SERVER_PORT = 3480;
	private static final int RECONNECT_RETRY_MAX_INTERVAL = 20000;
	private static final int RECONNECT_RETRY_INTERVAL = 100;
	private static Logger log = LhingsLogger.getLogger();

	private ByteArrayOutputStream readBuffer = new ByteArrayOutputStream(SOCKET_BUFFER_SIZE);
	private InputStream in;
	private OutputStream out;

	private Socket socket;
	private int clientPort;
	private int messageLength;

	public SSLSocketManager() {
		System.setProperty("javax.net.ssl.trustStore", "./lhings-java.keystore");
	}
	
	public void init() throws LhingsException {
		connect();
	}

	private void connect() {
		int interval = RECONNECT_RETRY_INTERVAL;
		boolean connected = false;
		Socket newSocket = null;
		while (!connected) {
			try {
				SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				newSocket = sslFactory.createSocket(SERVER_HOSTNAME, SERVER_PORT);
				newSocket.setSoTimeout(RECEIVE_TIMEOUT_MILLIS);
				this.socket = newSocket;
				out = socket.getOutputStream();
				in = socket.getInputStream();
				clientPort = socket.getLocalPort();
				connected = true;
			} catch (IOException e) {
				log.warn("Unable to connect. Retrying in " + interval + " ms");
				log.debug("Connection failed, see stack trace for details", e);
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				interval = interval * 2;
				if (interval > RECONNECT_RETRY_MAX_INTERVAL)
					interval = RECONNECT_RETRY_MAX_INTERVAL;
			}
		}
		log.info("Device socket ready, bound to port " + clientPort);
	}

	public void send(byte[] bytes) throws LhingsException {
		try {
			out.write(bytes);
		} catch (IOException e) {
			log.error("Write failed, closing socket", e);
			try {
				out.close();
			} catch (IOException e1) {
			}
			log.info("Connecting again...");
			connect();
		}

	}

	// DONDE lanzar el server local y modificar temperature sensor para que use
	// TCP como
	// socket manager y probar si funciona correctamente.

	public byte[] receive() {
		messageLength = -1;
		while (true) {
			int byteread;
			try {
				byteread = in.read();
			} catch (SocketTimeoutException ex) {
				// socket timed out and message is not read in its entirety
				return null;
			} catch (IOException e) {
				log.error("Exception while reading, closing socket", e);
				break;
			}
			if (byteread == -1) { // stream closed
				log.warn("Stream closed on server side.");
				break;
			}
			readBuffer.write(byteread);
			if (readBuffer.size() == 4) {
				byte[] bytes = readBuffer.toByteArray();
				messageLength = ByteMan.bytesToInteger(bytes[2], bytes[3]) + 20;
			}
			if (readBuffer.size() == messageLength) {
				byte[] bytes = readBuffer.toByteArray();
				messageLength = -1;
				readBuffer.reset();
				return bytes;
			}
		}
		try {
			in.close();
		} catch (IOException e) {

		}
		log.info("Connecting again...");
		connect();
		return null;
	}

	public int getPort() {
		return this.clientPort;
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			log.error("Unable to close socket.", e);
		}
	}

}
