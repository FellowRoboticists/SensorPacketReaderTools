package com.naiveroboticist.interfaces;

import java.io.IOException;

/**
 * Primary interface to robot controller.
 * 
 * @author dsieh
 */
public interface IRobotReader {
    int read(byte[] buffer, int timeoutMillis) throws IOException;
}
