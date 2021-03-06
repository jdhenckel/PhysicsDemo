package com.poorfox.physicsdemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import androidx.appcompat.app.AppCompatActivity;

/*
Implementation notes:

TODO:   x = do it now

- Single tap mode
    - open details
    - select stuff (toggle) in preparation for drag or copy

- Modes:
    - Play simulation y/n
    - toggle mode:
        - Edit mode (Grab and drag or open details)
        - Select stuff
        - View mode
- Buttons
    play/pause (single Step?)
    zoom in/out (short side scrub)
    mode - edit, view, select
    reset view (fit all, toggle)
- File
    save/restore (named, unnamed)
    merge files into single world
- Navigate
    - Zoom Pan Spin
    - preset zoom (fit all, km, m, cm, mm)
    - pan to center
    - zero spin
- Detail Editor
    - To open each editor, must be in edit mode and single tap on the object.
    - World
        global gravity: mag, dir (use device)
        global magnetism : mag, dir (use device compass)
        mutual gravitation
        mutual magnetism
        reset global linear momentum
        crop outlier bodies
        set global boundaries ? always set to +/- 1000 ?

    - Body
        type: static, dynamic, kinematic (animated)
        density
        shape: circle, box, polygon (premium)
        velocity lin/ang
        damping lin.ang
        fiction
        restitution
        collision category (default 1)
        magnetism axis and strength
        thruster (use device?)

    - Joint
        bodyA, bodyB (or world)
        type: revolute, sliding
        limits:
        drive:



 */


public class MainActivity extends AppCompatActivity
{

    MainWorld mainWorld;
    GravitySensor gravitySensor;
    SensorManager sensorManager;
    MainView view;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        view = new MainView(this);
        mainWorld = new MainWorld();
        mainWorld.initialize(10, 7);

        gravitySensor = new GravitySensor();
        setContentView(view);
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        if (sensorManager == null)
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (sensor != null)
            sensorManager.registerListener(gravitySensor, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (sensorManager != null)
            sensorManager.unregisterListener(gravitySensor);
    }

}
