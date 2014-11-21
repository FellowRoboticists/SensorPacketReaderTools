package com.naiveroboticist.interfaces;

import java.io.IOException;

/**
 * Primary interface to robot controller.
 * 
 * @author dsieh
 */
public interface RobotReaderWriter {
    void sendCommand(byte command) throws IOException;
    void sendCommand(byte command, byte[] payload) throws IOException;
    void sendCommand(byte[] buffer) throws IOException;
    int read(byte[] buffer, int timeoutMillis) throws IOException;
}
