package com.naiveroboticist.sensor;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PacketTest {
    private Packet mCut;

    @Before
    public void setUp() throws Exception {
        byte[] packetBuffer = { 0x13, 
                                0x0b, 
                                0x07, 0x00,
                                0x13, 0x23, 0x18,
                                0x14, 0x00, 0x00,
                                0x21, 0x01, 0x1f, 
                                115 };
        mCut = new Packet(512);
        mCut.put(packetBuffer, 0, 14);
    }

    @After
    public void tearDown() throws Exception {
        mCut = null;
    }

    @Test
    public void testGetSensorValue() throws InvalidPacketError {
        assertEquals(0, mCut.getSensorValue((byte) 0x07));
        assertEquals(8984, mCut.getSensorValue((byte) 0x13));
        assertEquals(0, mCut.getSensorValue((byte) 0x14));
        assertEquals(287, mCut.getSensorValue((byte) 0x21));
    }
    
    @Test
    public void testPosition() {
        assertEquals(14, mCut.position());
    }
    
    @Test
    public void testClear() {
        assertEquals(14, mCut.position());
        mCut.clear();
        assertEquals(0, mCut.position());
    }
    
    @Test
    public void testPutByte() {
        mCut.put((byte)0x99);
        assertEquals(15, mCut.position());
        assertEquals((byte)0x99, mCut.get(14));
    }
    
    @Test
    public void testEmpty() {
        assertFalse(mCut.isEmpty());
        mCut.clear();
        assertTrue(mCut.isEmpty());
    }
    
    @Test
    public void testIsLengthByteRead() {
        assertTrue(mCut.isLengthByteRead());
        mCut.clear();
        assertFalse(mCut.isLengthByteRead());
        mCut.put((byte) 0x99);
        assertFalse(mCut.isLengthByteRead());
        mCut.put((byte) 0x99);
        assertTrue(mCut.isLengthByteRead());
    }
    
    @Test 
    public void testIsCompletePacket() throws InvalidPacketError {
        assertTrue(mCut.isCompletePacket());
        mCut.clear();
        // assertFalse(mCut.isCompletePacket());
        
        byte[] buffer = { 0x13, 0x02, 0x07, 0x00 };
        mCut.put(buffer, 0, 4);
        assertFalse(mCut.isCompletePacket());
        mCut.put((byte)0x03);
        assertTrue(mCut.isCompletePacket());
    }
    
    @Test
    public void testPacketLength() throws InvalidPacketError {
        assertEquals(11, mCut.packetLength());
    }
    
    @Test
    public void testValidChecksum() throws InvalidPacketError {
        assertFalse(mCut.validChecksum());
        
        byte[] packetBuffer = { 0x13, 
                0x0b, 
                0x07, 0x00,
                0x13, 0x23, 0x18,
                0x14, 0x00, 0x00,
                0x21, 0x01, 0x1f };
        mCut = new Packet(512);
        mCut.put(packetBuffer, 0, 13);
        mCut.put(Packet.calculateChecksum(packetBuffer, 0, 13));
        assertTrue(mCut.validChecksum());

    }
    
    @Test
    public void testFormatPacketBuffer() throws InvalidPacketError {
        assertEquals("19, 11, 7, 0, 19, 35, 24, 20, 0, 0, 33, 1, 31, 115", mCut.formatPacketBuffer());
    }
    
    @Test
    public void testNextPacketEmpty() throws InvalidPacketError {
        Packet nextPkt = mCut.nextPacket();
        assertNotNull(nextPkt);
        assertTrue(nextPkt.isEmpty());
    }
    
    @Test
    public void testNextPacketValid() throws InvalidPacketError {
        byte[] packetBuffer = { 0x13, 
                0x0b, 
                0x07, 0x00,
                0x13, 0x23, 0x18,
                0x14, 0x00, 0x00,
                0x21, 0x01, 0x1f, 
                115,
                0x13,
                0x0b };
        mCut = new Packet(512);
        mCut.put(packetBuffer, 0, 16);

        Packet nextPkt = mCut.nextPacket();
        assertNotNull(nextPkt);
        assertFalse(nextPkt.isEmpty());
        assertEquals((byte)0x13, nextPkt.get(0));
        assertEquals(2, nextPkt.position());
        assertEquals((byte)0x13, nextPkt.get(0));
        assertEquals((byte)0x0b, nextPkt.get(1));
    }
    
    @Test
    public void testNextPacketInvalid() throws InvalidPacketError {
        byte[] packetBuffer = { 0x13, 
                0x0b, 
                0x07, 0x00,
                0x13, 0x23, 0x18,
                0x14, 0x00, 0x00,
                0x21, 0x01, 0x1f, 
                115,
                0x12, // Not a start byte; should not
                0x0b };
        mCut = new Packet(512);
        mCut.put(packetBuffer, 0, 16);

        Packet nextPkt = mCut.nextPacket();
        assertNotNull(nextPkt);
        assertTrue(nextPkt.isEmpty());
    }
}
