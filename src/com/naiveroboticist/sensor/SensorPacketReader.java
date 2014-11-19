package com.naiveroboticist.sensor;

import java.nio.ByteBuffer;

public class SensorPacketReader {
	
	private static final byte PACKET_START = 0x13;
	private static final int LEN_IDX = 1;
	
	private ByteBuffer mPacketBuffer;
	
	public SensorPacketReader() {
		mPacketBuffer = ByteBuffer.allocateDirect(512);
	}
	
	public void clear() {
		mPacketBuffer.clear();
	}
	
	public boolean readPacket(byte[] buffer, int numBytes) throws InvalidPacketError {
		int packetStart = 0;
		
		if (mPacketBuffer.position() == 0) {
			packetStart = findPacketStartInBuffer(buffer, numBytes);
		}
		
		if (packetStart == -1) {
			return false; // The start byte wasn't found
		}
		
		for (int i=packetStart; i<numBytes; i++) {
			mPacketBuffer.put(buffer[i]);
		}
				
		if (mPacketBuffer.position() < 2) {
			return false; // Haven't read the length byte yet
		}
		
		if (mPacketBuffer.get(LEN_IDX) == 0) {
			mPacketBuffer.clear();
			throw new InvalidPacketError("Length is 0");
		}
		
		// +3 due to START byte, LENGTH byte & CHKSUM bytes
		int packetLength = mPacketBuffer.get(LEN_IDX) + 3;
		if (mPacketBuffer.position() < packetLength) {
			return false; // Haven't read the entire packet yet
		}
		
		if (! validChecksum(packetLength)) {
			mPacketBuffer.clear();
			throw new InvalidPacketError("Invalid checksum");
		}
		
		return true;
	}
		
	private boolean validChecksum(int packetLength) {
		int sum = 0;
		
		for (int i=0; i<packetLength; i++) {
			sum += mPacketBuffer.get(i);
		}
				
		return (sum & (byte) 0xff) == 0;
	}
	
	private int findPacketStartInBuffer(byte[] buffer, int numBytes) {
		int packetStart = -1;
		
		for (int i=0; i<numBytes; i++) {
			if (buffer[i] == PACKET_START) {
				packetStart = i;
				break;
			}
		}
		
		return packetStart;
	}

}
