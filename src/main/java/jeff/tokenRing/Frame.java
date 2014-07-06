package jeff.tokenRing;

import java.util.ArrayList;

/**
 * This class represents a frame or token moving accross the token ring.
 * It can marshal and unmarshal messages
 * @author jeff
 *
 */
public class Frame {
	private int priority;
	private int reservationLevel;
	private boolean isToken;
	private int destinationAddress;
	private int sourceAddress;
	private String data;
	
	private boolean shutdownRequested; 
	
	private boolean destReceived;
	private boolean destAccepted;
	
	

	/**
	 * Generic constructor that completes a frame
	 */
	public Frame(int priority, int reservationLevel, boolean isToken, int destinationAddress,
			int sourceAddress, String data, boolean destReceived, boolean destAccepted, boolean shutdownRequested) {
		this.priority = priority;
		this.reservationLevel = reservationLevel;
		this.isToken = isToken;
		this.destinationAddress = destinationAddress;
		this.sourceAddress = sourceAddress;
		this.data = data;
		this.destReceived = destReceived;
		this.destAccepted = destAccepted;
		this.shutdownRequested = shutdownRequested;
	}
	
	/**
	 * Generic constructor that completes a frame
	 */
	public Frame(int priority, int reservationLevel, boolean isToken, int destinationAddress,
			int sourceAddress, String data, boolean destReceived, boolean destAccepted) {
		this(priority, reservationLevel, isToken, destinationAddress, sourceAddress, data, destReceived, destAccepted, false);
	}
	
	/**
	 * Convert a Frame to a binary message. This is done in accordance with the
	 * Assignment. Note that the most significant bit between the reservation level
	 * and the priority is used to request nodes to shut down. 
	 * @return
	 */
	public byte[] marshal() {
		ArrayList<Byte> msg = new ArrayList<Byte>();
		
		if ( !shutdownRequested ) {
			msg.add(new Byte((byte) (priority*32 + reservationLevel))); 
		} else {
			msg.add(new Byte((byte) (priority*32 + 16 + reservationLevel))); 		
		}
		
		if ( isToken ) {
			msg.add(new Byte((byte) 0));
		} else {
			msg.add(new Byte((byte) 1));			
		}
		
		msg.add((byte) destinationAddress);
		msg.add((byte) sourceAddress);
		msg.add((byte) data.length());
		
		
		//to return a primative array without using a library...
		byte[] ret = new byte[msg.size() + data.length() + 1];
		byte[] dataBytes = data.getBytes();
		
		for ( int i = 0; i < msg.size(); i++ ) {
			ret[i] = msg.get(i);
		}
		
		for ( int i = msg.size(); i < msg.size() + data.length(); i++ ) {
			ret[i] = dataBytes[i - msg.size()];
		}
		
		
		int fs = 0;
		if ( isDestAccepted() ) {
			fs += 1;
		} if ( isDestReceived() ) {
			fs += 2;
		}
		ret[ret.length-1] = (byte) fs;
		
		return ret;
	}
	
	/**
	 * Convert a binary message to a Frame
	 * This is done in accordance with the assigment
	 * @param msg binary message
	 * @return An instance of the frame class
	 */
	public static Frame unmarshal(byte[] msg) {
		Byte b = msg[0];
		
		int priority = b >> 5;
		int reservationLevel = b & 0x07;
		
		boolean shutdownRequested = false; 
		if ( (b & 0x10) == 0x10 ) {
			shutdownRequested = true;
		}
		boolean isToken = true;
		if ( (msg[1] & 0x01) == 0x01) {
			isToken = false;
		}
		int destinationAddress = 0x00ff & msg[2];
		int sourceAddress = 0x00ff & msg[3];
		
		int datLength = 0x00ff & msg[4];
		
		String data = "";
		for ( int i = 0; i < datLength; i++ ) {
			data += (char)msg[i + 5];
		}
		
		int fs = msg[5+datLength];
		boolean isReceived = false;
		boolean isAccepted = false;
		if ( fs > 1 ) { 
			isReceived = true;
		} 
		if ( ((byte)fs & 0x01) == 0x01 ) {
			isAccepted = true;
		}
		//build and return the frame
		return new Frame(priority, reservationLevel, isToken, 
				destinationAddress, sourceAddress, data, isReceived, isAccepted, shutdownRequested);
	}
	
	
	public static Frame defaultTokenFrame(int priority) {
		return new Frame(priority, priority, true, 0, 0, "", false, false);
	}
	
	/************************************************************
	 * Generic Getter and Setter methods
	 *************************************************************/
	public int getPriority() {
		return priority;
	}
	public int getReservationLevel() {
		return reservationLevel;
	}
	public boolean isToken() {
		return isToken;
	}
	public int getDestinationAddress() {
		return destinationAddress;
	}
	public int getSourceAddress() {
		return sourceAddress;
	}
	public String getData() {
		return data;
	}
	public boolean isDestReceived() {
		return destReceived;
	}
	public boolean isDestAccepted() {
		return destAccepted;
	}
	public void setDestReceived(boolean destReceived) {
		this.destReceived = destReceived;
	}

	public void setDestAccepted(boolean destAccepted) {
		this.destAccepted = destAccepted;
	}

	public boolean isShutdownRequested() {
		return shutdownRequested;
	}

	public void setShutdownRequested(boolean shutdownRequested) {
		this.shutdownRequested = shutdownRequested;
	}
	
	
	
}
