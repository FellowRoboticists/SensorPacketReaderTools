package com.naiveroboticist.sensor;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SensorPacketReaderTest {
	
	SensorPacketReader spr;

	@Before
	public void setUp() throws Exception {
		spr = new SensorPacketReader();
	}

	@After
	public void tearDown() throws Exception {
		spr = null;
	}

	@Test
	public void testReadFullPacket() throws InvalidPacketError {
		byte[] buffer = { 0x13, 4, 33, 21, 22, 1, 0x00 };
		
		buffer[5] = calculateChecksum(buffer, 2, 3);
		buffer[6] = calculateChecksum(buffer, 0, 6);
		
		assertTrue(spr.readPacket(buffer, buffer.length));
	}
	
	@Test
	public void testReadFullPacketGivenPartials() throws InvalidPacketError {
		byte[] buffer = { 0x13, 4, 33, 21, 22, 1, 0x00 };
		
		byte cs1 = calculateChecksum(buffer, 2, 3);
		buffer[5] = cs1;
		byte cs2 = calculateChecksum(buffer, 0, 6);
		
		byte[] buffer1 = { 0x00, 0x00 };
		byte[] buffer2 = { 0x00, 0x13 };
		byte[] buffer3 = { 4 };
		byte[] buffer4 = { 33, 21, 22 };
		byte[] buffer5 = { cs1 };
		byte[] buffer6 = { cs2 };
		
		assertFalse(spr.readPacket(buffer1, buffer1.length));
		assertFalse(spr.readPacket(buffer2, buffer2.length));
		assertFalse(spr.readPacket(buffer3, buffer3.length));
		assertFalse(spr.readPacket(buffer4, buffer4.length));
		assertFalse(spr.readPacket(buffer5, buffer5.length));
		assertTrue (spr.readPacket(buffer6, buffer6.length));

	}
	
	@Test(expected=InvalidPacketError.class)
	public void testReadFullPacketWithLengthOfZero() throws InvalidPacketError {
		byte[] buffer = { 0x00, 0x13, 0 };
		
		spr.readPacket(buffer, buffer.length);
	}
	
	@Test(expected=InvalidPacketError.class)
	public void testReadFullPacketWithBadChecksum() throws InvalidPacketError {
		byte[] buffer = { 0x13, 4, 33, 21, 22, 1, 0x00 };

		spr.readPacket(buffer, buffer.length);
	}
	
	private byte calculateChecksum(byte[] buffer, int start, int count) {
		int sum = 0;
		
		for (int i=0; i<count; i++) {
			sum += buffer[start + i];
			// System.out.printf("Value: %d, Sum: %d\n", buffer[start + i], sum);
		}
		
		return (byte)(-sum & 0xff);
	}

}
