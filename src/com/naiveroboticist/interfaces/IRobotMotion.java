package com.naiveroboticist.interfaces;

import java.io.IOException;

public interface IRobotMotion {

    void initialize() throws IOException;
    void drive(int velocity, int angle) throws IOException;
    void rotate(int velocity) throws IOException;
    void stop() throws IOException;
}
