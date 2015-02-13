package com.naiveroboticist.sensor;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class PacketAccumulator implements Runnable {
    public enum AccumulatorType {
        Sum, Value
    }
    
    private PacketReader mPacketReader; 
    private Map<Byte,Integer> mAccumulatedValues;
    private Map<Byte,AccumulatorType> mAccumulation;
    private boolean mContinueAccumulating = true;

    public PacketAccumulator(PacketReader packetReader, Map<Byte,AccumulatorType> accumulation) {
        mPacketReader = packetReader;
        mAccumulation = accumulation;
        mAccumulatedValues = new TreeMap<Byte,Integer>();
        mContinueAccumulating = true;
    }

    @Override
    public void run() {
        while (mContinueAccumulating) {
            if (mPacketReader.numPackets() > 0) {
                performAccumulation(mPacketReader.removePacket());
            }
        }
    }
    
    // Synchronized api to continue accumulator
    public synchronized void stopAccumulating() {
        mContinueAccumulating = false;
    }
    
    // Synchronized api for accumulators
    
    public synchronized void incrementSensorValue(Byte sensor, int additionalValue) {
        int currentValue = 0;
        if (mAccumulatedValues.containsKey(sensor)) {
            currentValue = mAccumulatedValues.get(sensor).intValue();
        }
        mAccumulatedValues.put(sensor, new Integer(currentValue + additionalValue));
    }
    
    public synchronized void setSensorValue(Byte sensor, int newValue) {
        mAccumulatedValues.put(sensor, new Integer(newValue));
    }
    
    public synchronized int getSensorValue(Byte sensor) {
        int currentValue = 0;
        if (mAccumulatedValues.containsKey(sensor)) {
            currentValue = mAccumulatedValues.get(sensor).intValue(); 
        }
        return currentValue;
    }
    
    public void performAccumulation(Packet packet) {
        Iterator<Byte> it = mAccumulation.keySet().iterator();
        while (it.hasNext()) {
            Byte sensor = it.next();
            try {
                int value = packet.getSensorValue(sensor.byteValue());
                AccumulatorType at = mAccumulation.get(sensor);
                switch (at) {
                case Sum: // Sum up the values
                    incrementSensorValue(sensor, value);
                    break;
                case Value: // Just set the value
                    setSensorValue(sensor, value);
                    break;
                }
            } catch (InvalidPacketError e) {
                // Really should do something with this...
            }
        }
    }

}
