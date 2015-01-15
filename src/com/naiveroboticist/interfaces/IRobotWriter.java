package com.naiveroboticist.interfaces;

import java.io.IOException;

/**
 * Primary interface to robot controller.
 * 
 * @author dsieh
 */
public interface IRobotWriter {
    void sendCommand(byte command) throws IOException;
    void sendCommand(byte command, byte[] payload) throws IOException;
    void sendCommand(byte[] buffer) throws IOException;
}
