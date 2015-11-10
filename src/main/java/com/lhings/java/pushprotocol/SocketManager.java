package com.lhings.java.pushprotocol;

import com.lhings.java.exception.LhingsException;

public interface SocketManager {

	public static final int RECEIVE_TIMEOUT_MILLIS = 40;
	
	/**
	 * Initializes the socket and opens the connection if needed
	 * @throws LhingsException
	 */
	public void init() throws LhingsException;
	
	/**
	 * Sends through the socket all the bytes that make up the given byte array.
	 * @param bytes
	 */
	public void send(byte[] bytes) throws LhingsException;
	
	/**
	 * Reads from the socket to see if any STUN message was received. This call should block a 
	 * maximum of RECEIVE_TIMEOUT_MILLIS milliseconds.
	 * @return The raw bytes that make up the STUN message or null if nothing was received.
	 */
	public byte[] receive();
	
	/**
	 * Closes the connection.
	 */
	public void close();
	
	/**
	 * Returns the local port used to connect to Lhings.
	 * @return
	 */
	public int getPort();
}
