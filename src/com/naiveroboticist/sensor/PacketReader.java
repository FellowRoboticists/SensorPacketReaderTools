package com.naiveroboticist.sensor;

import java.io.IOException;
//import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.naiveroboticist.interfaces.IRobotReader;

public class PacketReader implements Runnable {
    private static final int PACKET_SIZE = 512;
    private static final int TIMEOUT_MILLIS = 1000;
    private static final int MAX_TRIES = 100;
    private static final byte PACKET_START = 0x13;

    private IRobotReader mRobotRW;
    private Packet mPacketBuffer;
    private int mExpectedPacketLength;
    private Queue<Packet> mPacketQueue;
    private ArrayList<String> mLogs;
    private boolean mContinueReading = true;
    private boolean mSinglePacketRead = false;

    public PacketReader(IRobotReader robotReaderWriter, int packetLength, boolean singlePacketRead) {
        this(robotReaderWriter, packetLength);
        mSinglePacketRead = singlePacketRead;
    }
    
    public PacketReader(IRobotReader robotReaderWriter, int packetLength) {
        mRobotRW = robotReaderWriter;
        mPacketBuffer = new Packet(PACKET_SIZE);
        mExpectedPacketLength = packetLength;
        mPacketQueue = new LinkedList<Packet>();
        mLogs = new ArrayList<String>();
    }

    @Override
    public void run() {
        try {
            while (mContinueReading) {
                try {
                    System.out.println("Trying to read complete packet");
                    readCompletePacket();
                    System.out.println("Done reading complete packet: " + mPacketBuffer.formatPacketBuffer());
                    addPacket(mPacketBuffer);
                    mPacketBuffer = mPacketBuffer.nextPacket();
                    if (mSinglePacketRead) { mContinueReading = false; }
                } catch (InvalidPacketError e) {
                    System.out.println("InvalidPacketError: " + e.getLocalizedMessage() + "|" + e.getStackTrace()[0]);
                    addMessage("InvalidPacketError: " + e.getLocalizedMessage() + "|" + e.getStackTrace()[0]);
                }
            }
        } catch (IOException ex) {
            addMessage("IOException: " + ex.getLocalizedMessage() + "|" + ex.getStackTrace()[0]);
        }
    }
    
    // Synchronized api to continue reading
    public synchronized void stopReading() {
        mContinueReading = false;
    }

    // The synchronized interface for access to the logs
    
    public synchronized void addMessage(String message) {
        if (mLogs.size() < 100) {
            mLogs.add(message);
        }
    }
    
    public synchronized String fullMessages() {
        StringBuilder sb = new StringBuilder();
        int max = 20;
        if (mLogs.size() < max) { max = mLogs.size(); }
        for (int i=0; i<max; i++) {
//        for (int i=0; i<mLogs.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(mLogs.get(i));
        }
        
        return sb.toString();
    }
    
    public synchronized void clearLog() {
        mLogs.clear();
    }
    
    // The synchronized interface for access to the packets that
    // have been read
    
    public synchronized void addPacket(Packet packet) {
        mPacketQueue.add(packet);
    }
    
    public synchronized Packet removePacket() {
        return mPacketQueue.remove();
    }
    
    public synchronized int numPackets() {
        return mPacketQueue.size();
    }
    
    // General private methods

    private void readCompletePacket() throws IOException, InvalidPacketError {
        int tries = 0;
        mPacketBuffer.clear();
        byte[] buffer = new byte[100];
        
        boolean doneReading = false;
        while (! doneReading) {
            tries++;
            if (tries > MAX_TRIES) {
                throw new InvalidPacketError("Over " + MAX_TRIES + " to read packet. Dude, something's wrong.");
            }
            int numBytes = mRobotRW.read(buffer, TIMEOUT_MILLIS);
            // addMessage(formatBuffer(buffer, numBytes));
            doneReading = readPacket(buffer, numBytes);
        }
        // addMessage("Done Reading");
    }
    
    private boolean readPacket(byte[] buffer, int numBytes) throws InvalidPacketError {
        int packetStart = 0;
        
        if (numBytes == 0) {
            return false; // Nothing to do
        }
        
        if (mPacketBuffer.isEmpty()) {
            packetStart = findPacketStartInBuffer(buffer, numBytes);
        }
 
        if (packetStart == -1) {
            return false; // The start byte wasn't found
        }

        mPacketBuffer.put(buffer, packetStart, numBytes);
                
        if (! mPacketBuffer.isLengthByteRead()) {
            return false;
        }
        
        int len = mPacketBuffer.packetLength();
        if (len == 0 || len != mExpectedPacketLength) {
            mPacketBuffer.clear();
            return false;
        }
        
        // +2 due to START byte, LENGTH byte & CHKSUM bytes
        if (! mPacketBuffer.isCompletePacket()) {
            return false; // Haven't read the entire packet yet
        }
        
        if (! mPacketBuffer.validChecksum()) {
            String fpb = mPacketBuffer.formatPacketBuffer();
            mPacketBuffer.clear();
            throw new InvalidPacketError("Invalid checksum:" + fpb);
        }
                
        return true;
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
    
    @SuppressWarnings("unused")
    private String formatBuffer(byte[] buffer, int numBytes) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("" + numBytes + "|");
        for (int i=0; i<numBytes; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("" + (int)buffer[i]);
        }
        
        return sb.toString();
    }

}
