package com.naiveroboticist.sensor;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.naiveroboticist.interfaces.IRobotReader;

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
	
	@Test // (expected=InvalidPacketError.class)
	public void testReadFullPacketWithLengthOfZero() throws InvalidPacketError {
		byte[] buffer = { 0x00, 0x13, 0 };
		
		spr.readPacket(buffer, buffer.length);
	}
	
	@Test(expected=InvalidPacketError.class)
	public void testReadFullPacketWithBadChecksum() throws InvalidPacketError {
		byte[] buffer = { 0x13, 4, 33, 21, 22, 1, 0x00 };

		spr.readPacket(buffer, buffer.length);
	}
	
	@Test
	public void testGetPacketValues() throws InvalidPacketError {
       byte[] buffer = { 0x13, 3, 33, 21, 22, 0x00 };
        
       buffer[5] = calculateChecksum(buffer, 0, 5);
        
       assertTrue(spr.readPacket(buffer, buffer.length));
       
       ArrayList<Integer> values = spr.getPacketValues();
       assertEquals(1, values.size());
       
       assertEquals(5398, values.get(0).intValue());
	}
	
    @Test
    public void testGetPacketValuesALot() throws InvalidPacketError {
       byte[] buffer = { 
               0x13, 
               8, // length overall (not including final checksum. 
               33, 21, 22, 
               7, 10,
               42, 18, 20,
               0x00 // Final checksum 
               };
        
       buffer[10] = calculateChecksum(buffer, 0, 10);
        
       assertTrue(spr.readPacket(buffer, buffer.length));
       
       ArrayList<Integer> values = spr.getPacketValues();
       assertEquals(3, values.size());
       
       assertEquals(5398, values.get(0).intValue());
       assertEquals(10, values.get(1).intValue());
       assertEquals(4628, values.get(2).intValue());
    }
    
    class TestRobotReaderWriter implements IRobotReader {
        private int mReadNumber;
        private byte[] mBuffer1;
        private byte[] mBuffer2;
        private byte[] mBuffer3;
        private byte[] mBuffer4;
        private byte[] mBuffer5;
        private byte[] mBuffer6;

        public TestRobotReaderWriter() {
            mReadNumber = 0;
            byte[] buffer = { 
                    0x13, 
                    8, // length overall (not including final checksum. 
                    33, 21, 22, 
                    7, 10,
                    42, 18, 100,
                    0x00 // Final checksum 
                    };
             
            buffer[10] = calculateChecksum(buffer, 0, 10);
            
            mBuffer1 = new byte[1];
            mBuffer1[0] = buffer[0];
            
            mBuffer2 = new byte[1];
            mBuffer2[0] = buffer[1];
            
            mBuffer3 = new byte[3];
            for (int i=0; i<3; i++) {
                mBuffer3[i] = buffer[2 + i];
            }
            
            mBuffer4 = new byte[2];
            for (int i=0; i<2; i++) {
                mBuffer4[i] = buffer[5 + i];
            }

            mBuffer5 = new byte[3];
            for (int i=0; i<3; i++) {
                mBuffer5[i] = buffer[7 + i];
            }
            
            mBuffer6 = new byte[1];
            mBuffer6[0] = buffer[10];
        }

        @Override
        public int read(byte[] buffer, int timeoutMillis) throws IOException {
            mReadNumber++;
            switch (mReadNumber) {
            case 1:
                return copyBuffer(mBuffer1, mBuffer1.length, buffer);
            case 2:
                return copyBuffer(mBuffer2, mBuffer2.length, buffer);
            case 3:
                return copyBuffer(mBuffer3, mBuffer3.length, buffer);
            case 4:
                return copyBuffer(mBuffer4, mBuffer4.length, buffer);
            case 5:
                return copyBuffer(mBuffer5, mBuffer5.length, buffer);
            case 6:
                return copyBuffer(mBuffer6, mBuffer6.length, buffer);
            }
            return 0;
        }
        
        private int copyBuffer(byte[] source, int numBytes, byte[] dest) {
            for (int i=0; i<numBytes; i++) {
                dest[i] = source[i];
            }
            return numBytes;
        }
        
    };
    
    @Test
    public void testReadCompletePacket() throws IOException, InvalidPacketError {
        IRobotReader rrw = new TestRobotReaderWriter();
        
        spr.readCompletePacket(rrw, 1000);
        ArrayList<Integer> values = spr.getPacketValues();
        assertEquals(3, values.size());
        
        assertEquals(5398, values.get(0).intValue());
        assertEquals(10, values.get(1).intValue());
        assertEquals(4708, values.get(2).intValue());

    }
    
	private byte calculateChecksum(byte[] buffer, int start, int count) {
		int sum = 0;
		
		for (int i=0; i<count; i++) {
			sum += buffer[start + i];
			System.out.printf("Value: %d, Sum: %d\n", buffer[start + i], sum);
		}
		
		return (byte)(-sum & 0xff);
	}

}
