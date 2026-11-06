package com.lightlab.s22;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends Activity {
 TextView report, test;
 boolean white=true;
 String lastWhite="",lastBlack="";
 final StringBuilder log=new StringBuilder();
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  LinearLayout root=new LinearLayout(this);
  root.setOrientation(LinearLayout.VERTICAL);
  root.setPadding(16,16,16,16);
  root.setBackgroundColor(Color.rgb(24,24,28));
  TextView title=new TextView(this);
  title.setText("AMOLED Driver Access Probe v3"); title.setTextSize(21);title.setTextColor(Color.WHITE);root.addView(title);
  TextView note=new TextView(this);
  note.setText("No ambient light, touch, or camera measurements. Read-only driver access tests; no hardware writes. White/black differences are NOT evidence of optical sensing.");
  note.setTextColor(Color.LTGRAY);root.addView(note);
  Button scan=new Button(this);scan.setText("TEST ACTUAL DEVICE ACCESS");root.addView(scan);
  Button toggle=new Button(this);toggle.setText("SWITCH WHITE / BLACK");root.addView(toggle);
  test=new TextView(this);test.setText("WHITE TEST AREA");test.setGravity(Gravity.CENTER);
  test.setBackgroundColor(Color.WHITE);test.setTextColor(Color.BLACK);
  root.addView(test,new LinearLayout.LayoutParams(-1,0,1));
  ScrollView scroll=new ScrollView(this);report=new TextView(this);
  report.setTextColor(Color.WHITE);report.setTextSize(13);scroll.addView(report);
  root.addView(scroll,new LinearLayout.LayoutParams(-1,0,2));
  scan.setOnClickListener(v->scan());
  toggle.setOnClickListener(v->{white=!white;test.setBackgroundColor(white?Color.WHITE:Color.BLACK);test.setTextColor(white?Color.BLACK:Color.WHITE);test.setText(white?"WHITE TEST AREA":"BLACK TEST AREA");});
  setContentView(root);scan();
 }
 void append(String s){log.append(s).append("\n");}
 String errno(IOException e){return e.getClass().getSimpleName()+": "+e.getMessage();}
 void openDevice(String path){
  File f=new File(path);
  if(!f.exists()){append(path+" MISSING");return;}
  try(FileInputStream stream=new FileInputStream(f)){
   append(path+" OPEN READ-ONLY: SUCCESS");
   append("  Descriptor opened; no DRM ioctl attempted. This does not establish usable display diagnostics.");
  }catch(IOException | SecurityException e){append(path+" OPEN READ-ONLY: FAILED: "+e.getClass().getSimpleName()+": "+e.getMessage());}
 }
 void listDirectory(String path){
  File f=new File(path);append("\n"+path+":");
  if(!f.exists()){append("  Missing");return;}
  File[] entries=f.listFiles();
  if(entries==null){append("  Directory listing denied or unavailable");return;}
  int count=0;
  for(File entry:entries){
   if(count++>=60){append("  ...truncated");break;}
   append("  "+entry.getName()+(entry.isDirectory()?"/":""));
  }
 }
 void scan(){
  log.setLength(0);
  append("MODEL: "+Build.MANUFACTURER+" "+Build.MODEL);
  append("ANDROID: "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")");
  append("KERNEL: "+System.getProperty("os.version"));
  android.util.DisplayMetrics metrics=new android.util.DisplayMetrics();
  getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
  append(String.format(Locale.US,"DISPLAY: %d x %d",metrics.widthPixels,metrics.heightPixels));
  append("\nACTUAL READ-ONLY OPEN TESTS:");
  openDevice("/dev/dri/card0");openDevice("/dev/dri/card1");
  listDirectory("/dev/dri");listDirectory("/sys/class/drm");
  listDirectory("/sys/class/lcd");listDirectory("/sys/class/backlight");
  append("\nCOMPARISON STATUS:");
  append("No validated electrical telemetry endpoint identified. White/black comparison is unavailable until such an endpoint exists.");
  append("RESULT: No AMOLED pixel photocurrent measurement available to this app.");
  append("A successful open is NOT proof of a DRM ioctl or optical sensing capability.");
  report.setText(log.toString());
 }
}
