package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import androidx.annotation.NonNull;

import java.util.Iterator;

public class Timing
{
    long p1,s1,s2,d1,d2;
    
    int totalUs,simUs,gapUs,drawUs;  // micro seconds

    void startSim() { s1 = System.nanoTime(); if (p1>0) totalUs = (int)((s1 - p1)/1000); p1 = s1; }
    void stopSim() { s2 = System.nanoTime(); simUs = (int)((s2 - s1)/1000); }
    void startDraw() { d1 = System.nanoTime(); gapUs = (int)((d1 - s2)/1000); }
    void stopDraw() { d2 = System.nanoTime(); drawUs = (int)((d2 - d1)/1000); }

}
