package com.naiveroboticist.sensor;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.naiveroboticist.interfaces.IRobotReader;

public class PacketReaderTest {
    
    class TestRobotReader implements IRobotReader {
        private int mReadNumber;
        private byte[] mBuffer1;
        private byte[] mBuffer2;
        private byte[] mBuffer3;
        private byte[] mBuffer4;
        private byte[] mBuffer5;
        private byte[] mBuffer6;

        public TestRobotReader() {
            mReadNumber = 0;
            byte[] buffer = { 
                    0x13,
                    0x0b, 0x07, 0x00, 0x13, 
                    0x00, 
                    0x00, 0x14, 0x00, 0x00, 0x21, 
                    0x00, 0x3e, 85                    }; // Checksum
                         
            mBuffer1 = new byte[1];
            mBuffer1[0] = buffer[0];
            
            mBuffer2 = new byte[4];
            for (int i=0; i<4; i++) {
                mBuffer2[i] = buffer[1 + i];
            }

            mBuffer3 = new byte[1];
            mBuffer3[0] = buffer[5];
            
            mBuffer4 = new byte[5];
            for (int i=0; i<5; i++) {
                mBuffer4[i] = buffer[6 + i];
            }

            mBuffer5 = new byte[3];
            for (int i=0; i<3; i++) {
                mBuffer5[i] = buffer[11 + i];
            }
            
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

    private PacketReader mCut;

    @Before
    public void setUp() throws Exception {
        IRobotReader rrw = new IRobotReader() {

            @Override
            public int read(byte[] buffer, int timeoutMillis)
                    throws IOException {
                return 0;
            }
        
        };
        
        mCut = new PacketReader(rrw, 13);
    }

    @After
    public void tearDown() throws Exception {
        mCut = null;
    }

    @Test
    public void testRun() throws InterruptedException {
        mCut = new PacketReader(new TestRobotReader(), 11);
        new Thread(mCut).start();
        Thread.sleep(10);
        mCut.stopReading();
        
        assertEquals(1, mCut.numPackets());
    }

    @Test
    public void testAddMessage() {
        mCut.addMessage("Message1");
        mCut.addMessage("Message2");
        
        assertEquals("Message1, Message2", mCut.fullMessages());
    }

    @Test
    public void testClearLog() {
        mCut.addMessage("Message1");
        mCut.addMessage("Message2");
        
        assertEquals("Message1, Message2", mCut.fullMessages());
        
        mCut.clearLog();

        assertEquals("", mCut.fullMessages());
    }

    @Test
    public void testAddPacket() {
        byte[] packetBuffer = { 0x13, 0x02, 0x07, 0x01, 0x01 };
        Packet pkt = new Packet(512);
        pkt.put(packetBuffer, 0, 5);
        
        mCut.addPacket(pkt);
        assertEquals(1, mCut.numPackets());
    }

    @Test
    public void testRemovePacket() {
        byte[] packetBuffer = { 0x13, 0x02, 0x07, 0x01, 0x01 };
        
        Packet pb1 = new Packet(512);
        pb1.put(packetBuffer, 0, 5);
        Packet pb2 = new Packet(512);
        pb2.put(packetBuffer, 0, 5);
        
        mCut.addPacket(pb1);
        mCut.addPacket(pb2);
        
        assertEquals(2, mCut.numPackets());
        
        assertNotNull(mCut.removePacket());

        assertEquals(1, mCut.numPackets());
        
        assertNotNull(mCut.removePacket());

        assertEquals(0, mCut.numPackets());
    }

}
