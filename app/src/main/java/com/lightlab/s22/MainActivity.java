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
import android.hardware.display.DisplayManager;
import android.view.Display;
import java.io.File;
import java.util.Locale;

public class MainActivity extends Activity {
 TextView report, test;
 boolean white=true;
 final StringBuilder log=new StringBuilder();
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  LinearLayout root=new LinearLayout(this);
  root.setOrientation(LinearLayout.VERTICAL);
  root.setPadding(16,16,16,16);
  root.setBackgroundColor(Color.rgb(24,24,28));
  TextView title=new TextView(this);
  title.setText("AMOLED Electrical Sensing Probe");
  title.setTextColor(Color.WHITE); title.setTextSize(21); root.addView(title);
  TextView note=new TextView(this);
  note.setText("No touch, camera or ambient light sensor is used. This app checks whether the display exposes a readable electrical sensing interface. A white area alone cannot detect light.");
  note.setTextColor(Color.LTGRAY); root.addView(note);
  Button scan=new Button(this); scan.setText("SCAN DISPLAY INTERFACES"); root.addView(scan);
  Button toggle=new Button(this); toggle.setText("SWITCH WHITE / BLACK"); root.addView(toggle);
  test=new TextView(this); test.setText("DISPLAY TEST AREA"); test.setTextColor(Color.BLACK);
  test.setGravity(Gravity.CENTER); test.setBackgroundColor(Color.WHITE);
  root.addView(test,new LinearLayout.LayoutParams(-1,0,1));
  ScrollView scroll=new ScrollView(this);
  report=new TextView(this);report.setTextColor(Color.WHITE);report.setTextSize(13);
  scroll.addView(report);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,2));
  scan.setOnClickListener(v->scan());
  toggle.setOnClickListener(v->{white=!white;test.setBackgroundColor(white?Color.WHITE:Color.BLACK);test.setTextColor(white?Color.BLACK:Color.WHITE);});
  setContentView(root);scan();
 }
 void append(String s){log.append(s).append("\n");}
 void probe(String path){
  File f=new File(path);
  if(!f.exists())return;
  append(path+" exists; readable="+f.canRead()+"; writable="+f.canWrite());
 }
 void scan(){
  log.setLength(0);
  append("DEVICE: "+Build.MANUFACTURER+" "+Build.MODEL);
  append("BOARD: "+Build.BOARD+" / "+Build.HARDWARE);
  append("ANDROID: "+Build.VERSION.RELEASE+" API "+Build.VERSION.SDK_INT);
  append("KERNEL: "+System.getProperty("os.version"));
  Display d=getWindowManager().getDefaultDisplay();
  android.util.DisplayMetrics m=new android.util.DisplayMetrics();d.getRealMetrics(m);
  append(String.format(Locale.US,"DISPLAY: %d x %d, %.1f Hz",m.widthPixels,m.heightPixels,d.getRefreshRate()));
  append("Display ID: "+d.getDisplayId()+" name: "+d.getName());
  append("\nPROBING EXISTENCE AND ACCESS (NO WRITES):");
  String[] dirs={"/sys/class/drm","/sys/class/graphics","/sys/class/backlight","/sys/class/lcd","/sys/devices/platform","/dev/dri","/dev/graphics"};
  for(String dir:dirs){
   File f=new File(dir);
   append(dir+": "+(f.exists()?"exists, readable="+f.canRead():"not accessible"));
   if(f.isDirectory() && f.canRead()){
    File[] children=f.listFiles();
    if(children!=null)for(int i=0;i<Math.min(children.length,30);i++){
     String name=children[i].getName();
     if(name.contains("panel")||name.contains("lcd")||name.contains("drm")||name.contains("backlight")||name.contains("fb")||name.contains("card")||name.contains("dsi"))probe(children[i].getAbsolutePath());
    }
   }
  }
  append("\nRESULT: No public Android API provides AMOLED pixel photocurrent readings.");
  append("An accessible driver path does NOT prove it provides sensing data.");
  append("This scan cannot establish that no private kernel interface exists.");
  append("No camera, touchscreen, or ambient-light sensor data collected.");
  report.setText(log.toString());
 }
}
