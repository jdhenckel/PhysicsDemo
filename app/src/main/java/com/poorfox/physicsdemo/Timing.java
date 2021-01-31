package com.poorfox.physicsdemo;

import androidx.annotation.NonNull;

import java.util.Iterator;

public class Timing
{
    public boolean isRunning;
    int i;
    long[] nano = new long[10];
    int sim;
    int draw;

    void start() { nano[i] = System.nanoTime(); next(); }
    void startSim() { sim = i; start(); }
    void startDraw() { draw = i; start(); }
    void next() { if (++i >= nano.length) i = 0; }

    float[] getResult()
    {
    return null;
    }
}
