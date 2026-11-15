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
import java.util.Locale;
public class MainActivity extends Activity {
 static {System.loadLibrary("lightlab");}
 private static native String nativeStage(int stage);
 TextView report,header,test;
 Button next;
 int stage=0; boolean white=true;
 final String[] names={"Open DRM devices","Read display resources","Enumerate connectors","Read connector properties"};
 @Override public void onCreate(Bundle b){
  super.onCreate(b);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
  root.setPadding(14,14,14,14);root.setBackgroundColor(0xff17171b);
  header=new TextView(this);header.setTextColor(Color.WHITE);header.setTextSize(20);
  header.setText("LightLab v5 • AMOLED feasibility");root.addView(header);
  TextView info=new TextView(this);info.setTextColor(Color.LTGRAY);
  info.setText("Green ticks mean DRM access works, NOT that OLED pixels detect light. All queries are read-only. No ambient light, camera or touch sensing.");
  root.addView(info);
  Button all=new Button(this);all.setText("RUN ALL SAFE TESTS");root.addView(all);
  next=new Button(this);root.addView(next);
  Button toggle=new Button(this);toggle.setText("WHITE / BLACK TEST PATTERN");root.addView(toggle);
  test=new TextView(this);test.setGravity(Gravity.CENTER);test.setText("WHITE");test.setTextColor(Color.BLACK);test.setBackgroundColor(Color.WHITE);
  root.addView(test,new LinearLayout.LayoutParams(-1,0,1));
  ScrollView scroll=new ScrollView(this);report=new TextView(this);report.setTextColor(Color.WHITE);report.setTextSize(13);scroll.addView(report);
  root.addView(scroll,new LinearLayout.LayoutParams(-1,0,3));
  all.setOnClickListener(v->{stage=0;StringBuilder s=new StringBuilder();
   for(int i=0;i<4;i++){String result=nativeStage(i);s.append(label(i,result)).append(result).append("\n");}
   stage=4;report.setText(s.toString());updateNext();});
  next.setOnClickListener(v->{if(stage<4){String result=nativeStage(stage);report.append(label(stage,result)+result+"\n");stage++;updateNext();}});
  toggle.setOnClickListener(v->{white=!white;test.setBackgroundColor(white?Color.WHITE:Color.BLACK);test.setTextColor(white?Color.BLACK:Color.WHITE);test.setText(white?"WHITE":"BLACK");});
  report.setText("MODEL: "+Build.MODEL+"\nANDROID: "+Build.VERSION.RELEASE+"\n\nRun tests to identify accessible driver interfaces.\n");
  updateNext();setContentView(root);
 }
 String label(int i,String result){
  boolean ok=i==0?result.contains("DRM_VERSION OK"):i==1?result.contains("GETRESOURCES OK"):i==2?result.contains("connector ")&&!result.contains("GETCONNECTOR FAIL"):result.contains("  ")&&result.contains("(flags=");
  return (ok?"✓ PASS: ":"? NOT CONFIRMED: ")+names[i]+"\n";
 }
 void updateNext(){next.setEnabled(stage<4);next.setText(stage<4?"NEXT TEST: "+names[stage]:"ALL STAGES COMPLETED");}
}
