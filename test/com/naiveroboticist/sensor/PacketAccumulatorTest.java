package com.naiveroboticist.sensor;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.naiveroboticist.interfaces.IRobotReader;

public class PacketAccumulatorTest {
    private PacketAccumulator mCut;

    @Before
    public void setUp() throws Exception {
        IRobotReader rrw = new IRobotReader() {

            @Override
            public int read(byte[] buffer, int timeoutMillis)
                    throws IOException {
                return 0;
            }
        
        };
        
        PacketReader packetReader = new PacketReader(rrw, 13);
        
        Map<Byte,PacketAccumulator.AccumulatorType> acc = new TreeMap<Byte,PacketAccumulator.AccumulatorType>();
        acc.put(new Byte((byte)0x07), PacketAccumulator.AccumulatorType.Value);
        acc.put(new Byte((byte)0x13), PacketAccumulator.AccumulatorType.Sum);
        acc.put(new Byte((byte)0x14), PacketAccumulator.AccumulatorType.Sum);
        acc.put(new Byte((byte)0x21), PacketAccumulator.AccumulatorType.Value);
        
        mCut = new PacketAccumulator(packetReader, acc);
    }

    @After
    public void tearDown() throws Exception {
        mCut = null;
    }

    @Test
    public void testIncrementNoPreviousValue() {
        Byte sensor = new Byte((byte)0x07);
        mCut.incrementSensorValue(sensor, 12);
        
        assertEquals(12, mCut.getSensorValue(sensor));
    }
 
    @Test
    public void testIncrementWithPreviousValue() {
        Byte sensor = new Byte((byte)0x07);
        mCut.incrementSensorValue(sensor, 12);
        mCut.incrementSensorValue(sensor, 88);
        
        assertEquals(100, mCut.getSensorValue(sensor));
    }

    @Test
    public void testSetSensorValueNonExistent() {
        Byte sensor = new Byte((byte)0x07);
        mCut.setSensorValue(sensor, 12);
        
        assertEquals(12, mCut.getSensorValue(sensor));
    }

    @Test
    public void testSetSensorValueWithPreviousValue() {
        Byte sensor = new Byte((byte)0x07);
        mCut.setSensorValue(sensor, 12);
        mCut.setSensorValue(sensor, 88);
        
        assertEquals(88, mCut.getSensorValue(sensor));
    }
    
    @Test
    public void testPerformAccumulationFirstTime() {
        byte[] packetBuffer = { 0x13, 
                0x0b, 
                0x07, 0x01,
                0x13, 0x00, 0x02,
                0x14, 0x00, 0x01,
                0x21, 0x00, 0x10, 
                115 };
        Packet packet = new Packet(512);
        packet.put(packetBuffer, 0, 14);
        
        mCut.performAccumulation(packet);

        assertEquals(1, mCut.getSensorValue(new Byte((byte)0x07)));
        assertEquals(2, mCut.getSensorValue(new Byte((byte)0x13)));
        assertEquals(1, mCut.getSensorValue(new Byte((byte)0x14)));
        assertEquals(16, mCut.getSensorValue(new Byte((byte)0x21)));
    }

    @Test
    public void testPerformAccumulationNextTime() {
        byte[] packetBuffer1 = { 0x13, 
                0x0b, 
                0x07, 0x01,
                0x13, 0x00, 0x02,
                0x14, 0x00, 0x01,
                0x21, 0x00, 0x10, 
                115 };
        Packet packet1 = new Packet(512);
        packet1.put(packetBuffer1, 0, 14);
        mCut.performAccumulation(packet1);

        byte[] packetBuffer2 = { 0x13, 
                0x0b, 
                0x07, 0x02,
                0x13, 0x00, 0x04,
                0x14, 0x00, 0x03,
                0x21, 0x00, 0x12, 
                115 };
        Packet packet2 = new Packet(512);
        packet2.put(packetBuffer2, 0, 14);
        mCut.performAccumulation(packet2);

        assertEquals(2, mCut.getSensorValue(new Byte((byte)0x07)));
        assertEquals(6, mCut.getSensorValue(new Byte((byte)0x13)));
        assertEquals(4, mCut.getSensorValue(new Byte((byte)0x14)));
        assertEquals(18, mCut.getSensorValue(new Byte((byte)0x21)));
    }
 }
