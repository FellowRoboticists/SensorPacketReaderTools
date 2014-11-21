package com.naiveroboticist.utils;

public class ByteMethods {

    public static byte uB(int word) {
        return (byte) (word >> 8);
    }
    
    public static byte lB(int word) {
        return (byte) (word & 0x000000ff);
    }
    
    public static byte[] wordsToBytes(int[] words) {
        byte[] buffer = new byte[words.length * 2];
        
        for (int i=0; i<words.length; i++) {
            int bufferIdx = i * 2;
            buffer[bufferIdx] = uB(words[i]);
            buffer[bufferIdx + 1] = lB(words[i]);
        }
        
        return buffer;
    }
    
    public static int bytesToWord(byte ub, byte lb) {
        return (ub << 8) | lb;
    }
    
    public static int seek(byte[] buffer, int numBytes, byte marker) {
        int pos = -1;
        for (int i=0; i<numBytes && pos < 0; i++) {
            if (marker == buffer[i]) {
                pos = i;
            }
        }
        return pos;
    }
    
    public static int sensorValueAsWord(byte[] buffer, int numBytes, byte sensor) {
        int pos = seek(buffer, numBytes, sensor);
        
        int value = -1;
        if (pos > 0) {
            value = bytesToWord(buffer[pos + 1], buffer[pos + 2]);
        }
        
        return value;
    }
    
    public static String formatByteBuffer(byte[] buffer, int numBytes) {
        StringBuffer sbFormatted = new StringBuffer();
        sbFormatted.append("Byte Buffer (");
        sbFormatted.append(numBytes);
        sbFormatted.append("): ");
        for (int i=0; i<numBytes; i++) {
            if (i > 0) {
                sbFormatted.append(", ");
            }
            sbFormatted.append((int) buffer[i]);
        }
        return sbFormatted.toString();
    }
}
