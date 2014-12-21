package com.naiveroboticist.create;

import java.io.IOException;
import java.util.ArrayList;

import com.naiveroboticist.interfaces.RobotReaderWriter;
import com.naiveroboticist.sensor.InvalidPacketError;
import com.naiveroboticist.sensor.SensorPacketReader;
import com.naiveroboticist.utils.ByteMethods;

public class Commands {
    // Supported commands
    private static final byte START   = (byte) 0x80;
    private static final byte SAFE    = (byte) 0x83;
    private static final byte DRIVE   = (byte) 0x89;
    private static final byte LED     = (byte) 0x8b;
    private static final byte SONG    = (byte) 0x8c;
    private static final byte PLAY    = (byte) 0x8d;
    @SuppressWarnings("unused")
    private static final byte SENSORS = (byte) 0x8e;
    private static final byte PWMLSD  = (byte) 0x90;
    private static final byte STREAM  = (byte) 0x94;
    @SuppressWarnings("unused")
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
    
    @SuppressWarnings("unused")
    private static final byte VOLTAGE = 22;
    @SuppressWarnings("unused")
    private static final byte CURRENT = 23;
    @SuppressWarnings("unused")
    private static final byte ANALOG_PIN_SENSOR_PACKET = 33;
    
    // Drive straight
    private static final int DRV_FWD_RAD = 0x7fff;
    
    // Standard payloads
    private static final byte[] SONG_PAYLOAD = { 0x00, 0x01, 0x48, 0xa };
    private static final byte[] PLAY_PAYLOAD = { 0x00 };
    private static final byte[] LED_PAYLOAD = { LED_ADVANCE, LED_GREEN, LED_FULL_INTENSITY };
    public static final int WHEEL_DROP_VALUE = 0;
    public static final int DISTANCE_VALUE = 1;
    public static final int ANGLE_VALUE = 2;
    public static final int ANALOG_VALUE = 3;
    private static final byte[] STREAM_PAYLOAD = { 0x04, 0x07, 0x13, 0x14, 0x21 };

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
        mRobotRW.sendCommand(STREAM, STREAM_PAYLOAD);
        mRobotRW.sendCommand(LED, LED_PAYLOAD);
    }
    
    public int readAnalogPin() throws IOException, InvalidPacketError {
        return readAnalogPin(1);
    }
    
    public int readAnalogPin(int numSamples) throws IOException, InvalidPacketError {
        int totalValue = 0;
        
        SensorPacketReader spr = new SensorPacketReader();

        try {
            for (int sample=1; sample<=numSamples; sample++) {
                spr.readCompletePacket(mRobotRW, 1000);
                ArrayList<Integer> values = spr.getPacketValues();
                totalValue += values.get(3).intValue();
                mLogs.add(spr.formatPacketBuffer());
            }
        } catch (Exception e) {
            mLogs.add(e.getLocalizedMessage());
        }

        if (totalValue == 0) {
            return -1;
        } else {
            return Math.round(totalValue / numSamples);
        }
    }
    
    public ArrayList<Integer> readStream() throws IOException, InvalidPacketError {
        SensorPacketReader spr = new SensorPacketReader();
        spr.readCompletePacket(mRobotRW, 1000);
        return spr.getPacketValues();
    }
    
    public void pwmLowSideDrivers(byte dutyCycle0, byte dutyCycle1, byte dutyCycle2) throws IOException {
        byte[] payload = { dutyCycle2, dutyCycle1, dutyCycle2 };
        mRobotRW.sendCommand(PWMLSD, payload);
    }
    
    public void drive(int velocity, int radius) throws IOException {
        mRobotRW.sendCommand(SAFE);
        if (Math.abs(radius) < 0.0001) {
            radius = DRV_FWD_RAD;
        }
        int[] words = { velocity, radius };
        byte[] buffer = ByteMethods.wordsToBytes(words); //{ uB(fwd), lB(fwd), uB(rad), lB(rad) };
        mRobotRW.sendCommand(DRIVE, buffer);
   }
    
    public void stop() throws IOException {
        drive(0, 0);
    }
    
    public void rotate(int velocity) throws IOException {
        drive(velocity, 1);
    }
    
    public void rotate_cw(int velocity) throws IOException {
        rotate(-velocity);
    }
    
    public void rotate_ccw(int velocity) throws IOException {
        rotate(velocity);
    }
}
