package com.naiveroboticist.sensor;

public class Packet {
    private static final int LEN_IDX = 1;
    private static final byte PACKET_START = 0x13;

    private static final int[] PACKET_PAYLOAD_SIZES = {
        0, 0, 0, 0, 0, 0, 0, // Not used
        1, // * Bumps and Wheel Drops -  7 - 1 byte
        1, // * Wall                     8 - 1 byte
        1, // * Cliff Left               9 - 1 byte
        1, // * Cliff Front Left        10 - 1 byte
        1, // * Cliff Front Right       11 - 1 byte
        1, // * Cliff Right             12 - 1 byte
        1, // * Virtual Wall            13 - 1 byte
        1, // * Low Side Driver UnderC  14 - 1 byte
        1, // * Unused bytes            15 - 1 byte
        1, // * Unused bytes            16 - 1 byte
        1, // * Infrared byte           17 - 1 byte
        1, // * Buttons                 18 - 1 byte
        2, // * Distance                19 - 2 bytes (signed)
        2, // * Angle                   20 - 2 bytes (signed)
        1, // * Charging State          21 - 1 byte
        2, // * Voltage                 22 - 2 bytes 
        2, // * Current                 23 - 2 bytes
        1, // * Battery Temperature     24 - 1 byte (signed)
        2, // * Battery Charge          25   2 bytes
        2, // * Battery Capacity        26 - 2 bytes
        2, // * Wall Signal             27 - 2 bytes
        2, // * Cliff Left Signal       28 - 2 bytes
        2, // * Cliff Left Front Signal 29 - 2 bytes
        2, // * Cliff Front Right Sign  30 - 2 bytes
        2, // * Cliff Right Signal      31 - 2 bytes
        1, // * Cargo Bay Digital Input 32 - 1 byte
        2, // * Cargo Bay Analog Signal 33 - 2 bytes
        1, // * Charging Sources Avail  34 - 1 byte
        1, // * OI Mode                 35 - 1 byte
        1, // * Song Number             36 - 1 byte
        1, // * Song Playing            37 - 1 byte
        1, // * Number of Stream Pkts   38 - 1 byte
        2, // * Requested Velocity      39 - 2 bytes (signed)
        2, // * Requested Radius        40 - 2 bytes (signed)
        2, // * Requested Right Velo    41 - 2 bytes (signed)
        2 // * Requested Left Velo     42 - 2 bytes (signed)
    };

    private byte[] mPacketBuffer;
    private int mCurrentPosition;

    public Packet(int initialSize) {
        mPacketBuffer = new byte[initialSize];
        mCurrentPosition = 0;
    }
    
    public void clear() {
        mCurrentPosition = 0;
    }
    
    public int position() {
        return mCurrentPosition;
    }
    
    public void put(byte b) {
        mPacketBuffer[mCurrentPosition++] = b;
    }
    
    public void put(byte[] buffer, int start, int numBytes) {
        for (int i=start; i<numBytes; i++) {
            put(buffer[i]);
        }
    }
    
    public byte get(int index) {
        return mPacketBuffer[index];
    }
    
    public boolean isEmpty() {
        return mCurrentPosition == 0;
    }
    
    public boolean isLengthByteRead() {
        return mCurrentPosition > LEN_IDX;
    }
    
    public boolean isCompletePacket() throws InvalidPacketError {
        return mCurrentPosition > (packetLength() + 2);
    }
    
    public int packetLength() throws InvalidPacketError {
        if (! isLengthByteRead()) {
            throw new InvalidPacketError("Length byte has not yet been read");
        }
        return mPacketBuffer[LEN_IDX];
    }
    
    public boolean validChecksum() throws InvalidPacketError {
        byte sum = 0;
        
        int packetLength = packetLength() + 3;
        
        for (int i=0; i<packetLength; i++) {
            sum += get(i);
        }

        return (byte)(sum & (byte) 0xff) == 0;
    }
    
    public String formatPacketBuffer() throws InvalidPacketError {
        int packetLength = packetLength() + 3;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<packetLength; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("" + (int)get(i));
        }
        return sb.toString();
    }


    public int getSensorValue(byte sensor) throws InvalidPacketError {
        int lastIndex = mPacketBuffer[LEN_IDX] + 2;
        int value = 0;
        int index = LEN_IDX + 1;
        while (index < lastIndex) {
            // Value at index is the sensor
            byte currentSensor = mPacketBuffer[index++];
            if (currentSensor < 0 || currentSensor >= PACKET_PAYLOAD_SIZES.length) {
                throw new InvalidPacketError("Invalid sensor value: " + currentSensor + ": Packet position = " + index + " Last Index = " + lastIndex);
            }
            int numBytes = PACKET_PAYLOAD_SIZES[currentSensor];
            if (numBytes == 1) {
                value = mPacketBuffer[index++];
            } else if (numBytes == 2) {
                value = (mPacketBuffer[index++] << 8) | mPacketBuffer[index++];
            } else {
                throw new InvalidPacketError("Invalid payload size: " + numBytes);
            }
            if (currentSensor == sensor) {
                break;
            }
        }
        
        return value;
    }
    
    public Packet nextPacket() throws InvalidPacketError {
        Packet pkt = new Packet(mPacketBuffer.length);
        
        int fullPacketLength = packetLength() + 3;
        
        // Make sure the remainder of the buffer is worth keeping
        if (mCurrentPosition >= fullPacketLength && mPacketBuffer[fullPacketLength] == PACKET_START) {
            // Copy the remainder of the packet buffer to the next packet.
            // Don't want to miss any goodies.
            pkt.put(mPacketBuffer, fullPacketLength, mCurrentPosition);
        }        
        
        return pkt;
    }
    
    public static byte calculateChecksum(byte[] buffer, int start, int count) {
        byte sum = 0;
        
        for (int i=0; i<count; i++) {
            sum += buffer[start + i];
        }
        
        return (byte)(-sum & 0xff);
    }


}
