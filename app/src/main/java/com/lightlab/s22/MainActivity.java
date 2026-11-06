package com.lightlab.s22;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener {
 SensorManager manager;
 Sensor light;
 TextView status, touchStatus, white;
 float lux = Float.NaN, baseline = Float.NaN;
 boolean touchEnabled = true;
 @Override public void onCreate(Bundle b) {
  super.onCreate(b);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  manager = (SensorManager)getSystemService(SENSOR_SERVICE);
  light = manager.getDefaultSensor(Sensor.TYPE_LIGHT);
  LinearLayout root = new LinearLayout(this);
  root.setOrientation(LinearLayout.VERTICAL);
  root.setBackgroundColor(Color.rgb(22,22,26));
  root.setPadding(18,18,18,18);
  status = new TextView(this); status.setTextColor(Color.WHITE); status.setTextSize(19);
  root.addView(status);
  touchStatus = new TextView(this); touchStatus.setTextColor(Color.WHITE); touchStatus.setTextSize(16);
  touchStatus.setText("Touch: none"); root.addView(touchStatus);
  Button calibrate = new Button(this); calibrate.setText("Set current lux as baseline");
  calibrate.setOnClickListener(v -> {baseline=lux; updateStatus();}); root.addView(calibrate);
  Button toggle = new Button(this); toggle.setText("Touch detection: ON");
  toggle.setOnClickListener(v -> {touchEnabled=!touchEnabled;toggle.setText("Touch detection: "+(touchEnabled?"ON":"OFF"));touchStatus.setText("Touch: "+(touchEnabled?"enabled":"disabled"));}); root.addView(toggle);
  white = new TextView(this); white.setBackgroundColor(Color.WHITE); white.setTextColor(Color.BLACK);
  white.setGravity(Gravity.CENTER); white.setTextSize(22);
  white.setText("WHITE TEST AREA\n\nPlace finger here\n\nAmbient light is measured by phone sensor, NOT OLED pixels");
  root.addView(white,new LinearLayout.LayoutParams(-1,0,1));
  white.setOnTouchListener((v,event)->{
   if(touchEnabled) {
    if(event.getActionMasked()==MotionEvent.ACTION_DOWN || event.getActionMasked()==MotionEvent.ACTION_MOVE)
     touchStatus.setText(String.format(Locale.US,"Touch: x %.0f, y %.0f",event.getX(),event.getY()));
    else if(event.getActionMasked()==MotionEvent.ACTION_UP || event.getActionMasked()==MotionEvent.ACTION_CANCEL)
     touchStatus.setText("Touch: released");
   }
   return true;
  });
  updateStatus();
  setContentView(root);
 }
 void updateStatus(){
  String value = Float.isNaN(lux)?"waiting":String.format(Locale.US,"%.1f lux",lux);
  String delta = Float.isNaN(baseline)?"not calibrated":String.format(Locale.US,"%+.1f lux",lux-baseline);
  status.setText("Ambient light: "+(light==null?"Sensor unavailable":value)+"\nChange from baseline: "+delta);
 }
 @Override protected void onResume(){super.onResume();if(light!=null)manager.registerListener(this,light,SensorManager.SENSOR_DELAY_UI);}
 @Override protected void onPause(){manager.unregisterListener(this);super.onPause();}
 @Override public void onSensorChanged(SensorEvent event){if(event.sensor.getType()==Sensor.TYPE_LIGHT){lux=event.values[0];updateStatus();}}
 @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
}
