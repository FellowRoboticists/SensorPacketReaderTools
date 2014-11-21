package com.naiveroboticist.create;

import java.io.IOException;
import java.util.ArrayList;

import com.naiveroboticist.interfaces.RobotReaderWriter;
import com.naiveroboticist.sensor.InvalidPacketError;
import com.naiveroboticist.sensor.SensorPacketReader;
// import com.naiveroboticist.utils.ByteMethods;

public class Commands {
    // Supported commands
    private static final byte START   = (byte) 0x80;
    private static final byte SAFE    = (byte) 0x83;
    @SuppressWarnings("unused")
    private static final byte DRIVE   = (byte) 0x89;
    private static final byte LED     = (byte) 0x8b;
    private static final byte SONG    = (byte) 0x8c;
    private static final byte PLAY    = (byte) 0x8d;
    @SuppressWarnings("unused")
    private static final byte SENSORS = (byte) 0x8e;
    private static final byte PWMLSD  = (byte) 0x90;
    @SuppressWarnings("unused")
    private static final byte STREAM  = (byte) 0x94;
    private static final byte QUERY_LIST = (byte) 0x95;
    
    // LED values
    private static final byte LED_ADVANCE = 0x08;
    @SuppressWarnings("unused")
    private static final byte LED_PLAY = 0x02;
    
    private static final byte LED_GREEN = 0x00;
    @SuppressWarnings("unused")
    private static final byte LED_RED = (byte) 0xff;
    
    @SuppressWarnings("unused")
    private static final byte LED_OFF = 0x00;
    private static final byte LED_FULL_INTENSITY = (byte) 0xff;
    
    private static final byte VOLTAGE = 22;
    private static final byte CURRENT = 23;
    private static final byte ANALOG_PIN_SENSOR_PACKET = 33;
    
    // Drive straight
    @SuppressWarnings("unused")
    private static final int DRV_FWD_RAD = 0x7fff;
    
    // Standard payloads
    private static final byte[] SONG_PAYLOAD = { 0x00, 0x01, 0x48, 0xa };
    private static final byte[] PLAY_PAYLOAD = { 0x00 };
    private static final byte[] LED_PAYLOAD = { LED_ADVANCE, LED_GREEN, LED_FULL_INTENSITY };
    @SuppressWarnings("unused")
    private static final byte[] STREAM_PAYLOAD = { 0x03, 0x07, 0x13, 0x14 };

    private RobotReaderWriter mRobotRW;
    private ArrayList<String> mLogs;

    public Commands(RobotReaderWriter robotRW) {
        mRobotRW = robotRW;
        mLogs = new ArrayList<String>();
    }
    
    public ArrayList<String> getLogs() {
        return mLogs;
    }
    
    public void clearLogs() {
        mLogs.clear();
    }

    public void initialize() throws IOException {
        mRobotRW.sendCommand(START);
        mRobotRW.sendCommand(SAFE);
        mRobotRW.sendCommand(SONG, SONG_PAYLOAD);
        mRobotRW.sendCommand(PLAY, PLAY_PAYLOAD);
        //mRobotRW.sendCommand(STREAM, STREAM_PAYLOAD);
        mRobotRW.sendCommand(LED, LED_PAYLOAD);
    }
    
    public int readAnalogPin() throws IOException, InvalidPacketError {
        return readAnalogPin(1);
    }
    
    public int readAnalogPin(int numSamples) throws IOException, InvalidPacketError {
        int totalValue = 0;
        byte[] queryPayload = { 2, VOLTAGE, ANALOG_PIN_SENSOR_PACKET, CURRENT }; // One data package, analog input
        // byte[] queryPayload = { 1, ANALOG_PIN_SENSOR_PACKET }; // One data package, analog input
        // byte[] buffer = new byte[100];
        
        SensorPacketReader spr = new SensorPacketReader();

        for (int sample=1; sample<=numSamples; sample++) {
            mRobotRW.sendCommand(QUERY_LIST, queryPayload);
//            try {
//                Thread.sleep(50);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                // e.printStackTrace();
//            }
            spr.readCompletePacket(mRobotRW, 1000);
            ArrayList<Integer> values = spr.getPacketValues();
            totalValue += values.get(1).intValue();
            mLogs.add(spr.formatPacketBuffer());
//            int totalBytes = 0;
//            byte[] bbuffer = new byte[256];
//            int numBytes = 0;
//            while (totalBytes < 6) {
//                numBytes = mRobotRW.read(buffer, 10000);
//                if (numBytes > 0) {
//                    for (int i=0; i<numBytes; i++) {
//                        bbuffer[totalBytes + i] = buffer[i];
//                    }
//                    totalBytes += numBytes;
//                }
//            }
//            mLogs.add(ByteMethods.formatByteBuffer(bbuffer, totalBytes));
//            if (totalBytes >= 2) {
//                int value = ByteMethods.bytesToWord(bbuffer[0], bbuffer[1]);
//            // int value = ByteMethods.sensorValueAsWord(buffer, numBytes, (byte)ANALOG_PIN_SENSOR_PACKET);
//                if (value > 0) {
//                    totalValue += value;
//                }
//            }
        }

        if (totalValue == 0) {
            return -1;
        } else {
            return Math.round(totalValue / numSamples);
        }
    }
    
    public void pwmLowSideDrivers(byte dutyCycle0, byte dutyCycle1, byte dutyCycle2) throws IOException {
        byte[] payload = { dutyCycle2, dutyCycle1, dutyCycle2 };
        mRobotRW.sendCommand(PWMLSD, payload);
    }
}
