package com.lightlab.s22;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
 static { System.loadLibrary("lightlab"); }
 private static native String nativeProbe();
 TextView report, test;
 boolean white=true;
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  LinearLayout root=new LinearLayout(this);
  root.setOrientation(LinearLayout.VERTICAL);
  root.setPadding(16,16,16,16);
  root.setBackgroundColor(Color.rgb(24,24,28));
  TextView title=new TextView(this);
  title.setText("AMOLED DRM Probe v4");title.setTextSize(22);title.setTextColor(Color.WHITE);root.addView(title);
  TextView note=new TextView(this);
  note.setText("Native read-only DRM ioctl queries. No camera, touch or light sensor used. These queries do NOT detect light through OLED pixels.");
  note.setTextColor(Color.LTGRAY);root.addView(note);
  Button scan=new Button(this);scan.setText("RUN NATIVE DRM QUERIES");root.addView(scan);
  Button toggle=new Button(this);toggle.setText("SWITCH WHITE / BLACK");root.addView(toggle);
  test=new TextView(this);test.setText("WHITE DISPLAY TEST");test.setGravity(Gravity.CENTER);
  test.setBackgroundColor(Color.WHITE);test.setTextColor(Color.BLACK);
  root.addView(test,new LinearLayout.LayoutParams(-1,0,1));
  ScrollView scroll=new ScrollView(this);report=new TextView(this);
  report.setTextColor(Color.WHITE);report.setTextSize(13);scroll.addView(report);
  root.addView(scroll,new LinearLayout.LayoutParams(-1,0,2));
  scan.setOnClickListener(v->scan());
  toggle.setOnClickListener(v->{white=!white;test.setBackgroundColor(white?Color.WHITE:Color.BLACK);test.setTextColor(white?Color.BLACK:Color.WHITE);test.setText(white?"WHITE DISPLAY TEST":"BLACK DISPLAY TEST");});
  setContentView(root);scan();
 }
 void scan(){
  report.setText("MODEL: "+Build.MODEL+"\nANDROID: "+Build.VERSION.RELEASE+"\n\n"+nativeProbe());
 }
}
