package com.naiveroboticist.utils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ByteMethodsTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testLowerByte() {
        byte lb = ByteMethods.lB(1111);
        assertEquals((byte)87, lb);
    }

    @Test
    public void testUpperByte() {
        byte ub = ByteMethods.uB(1111);
        assertEquals((byte)4, ub);
    }
    
    @Test
    public void testWordToBytes() {
        int[] words = { 1111, 8008 };
        byte[] buffer = ByteMethods.wordsToBytes(words);
        
        assertEquals(4, buffer.length);
        assertEquals(4, buffer[0]);
        assertEquals(87, buffer[1]);
        assertEquals(31, buffer[2]);
        assertEquals(72, buffer[3]);
    }
    
    @Test
    public void testBytesToWord() {
        assertEquals(8008, ByteMethods.bytesToWord((byte)31, (byte)72));
    }
    
    @Test
    public void testSeekNonExistentMarker() {
        byte[] buffer = { 0x00, 0x01, 0x02 };
        assertEquals(-1, ByteMethods.seek(buffer, 3, (byte)0x04));
    }

    @Test
    public void testSeekExistentMarker() {
        byte[] buffer = { 0x00, 0x01, 0x02 };
        assertEquals(1, ByteMethods.seek(buffer, 3, (byte)0x01));
    }
    
    @Test
    public void testSeekBeyondEndOfBuffer() {
        byte[] buffer = { 0x00, 0x01, 0x02 };
        assertEquals(-1, ByteMethods.seek(buffer, 2, (byte)0x02));
    }
    
    @Test
    public void testSensorValueAsWordValid() {
        byte[] buffer = { 0x00, 0x01, 0x02, 0x03 };
        assertEquals(515, ByteMethods.sensorValueAsWord(buffer,  4, (byte)0x01));
    }

    @Test
    public void testSensorValueAsWordNotFound() {
        byte[] buffer = { 0x00, 0x01, 0x02, 0x03 };
        assertEquals(-1, ByteMethods.sensorValueAsWord(buffer,  4, (byte)0x04));
    }
    
    @Test
    public void testFormatByteBuffer() {
        byte[] buffer = { 0x00, 0x01, 0x02, 0x03 };
        String result = ByteMethods.formatByteBuffer(buffer, buffer.length);
        assertEquals("Byte Buffer (4): 0, 1, 2, 3", result);
    }

}
