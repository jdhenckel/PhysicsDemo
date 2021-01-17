package com.poorfox.physicsdemo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        MainView view = new MainView(this);
        setContentView(view);
        //gravity = new MyGravitySensorListener();
    }


}
