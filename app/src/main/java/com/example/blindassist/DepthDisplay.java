package com.example.blindassist;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Locale;

public class DepthDisplay extends AppCompatActivity implements View.OnClickListener {
    private int currentIndex = 0;
    private ArrayList<String> image_names;
    private ImageView imageView;
    private Button nextButton;
    private Button prevButton;
    public String image_dir;
    public JsonObject modelOutput;
    public JsonArray currentList;
    public TextView textView;
    private TextToSpeech mTTS;
    private Bitmap bitmap;
    private Vibrator vibrator;

    public void vibrate_color(int strength) {

        //if(vibrator != null) vibrator.cancel();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // vibrator.cancel();

        int dot; //one millisecond of vibration
        int short_gap; //one millisecond of break - could be more to weaken the vibration

        //strength = ((strength/10)*4)/10;
        Log.d("vibrate", "vibrate_color: " + strength);

        if (strength >= 0 && strength <= 20) {
            dot = 0; short_gap = 500;
        }
        else if (strength > 20 && strength <= 30) {
            dot = 70; short_gap = 700;
        }
        else if (strength > 30 && strength <= 40) {
            dot = 70; short_gap = 600;
        }
        else if (strength > 40 && strength <= 50) {
            dot = 80; short_gap = 500;
        }
        else if (strength > 50 && strength <= 60) {
            dot = 100; short_gap = 400;
        }
        else if (strength > 60 && strength <= 75) {
            dot = 100; short_gap = 350;
        }
        else if (strength > 75 && strength <= 90) {
            dot = 150; short_gap = 300;
        }
        else if (strength > 90 && strength <= 120) {
            dot = 150; short_gap = 200;
        }
        else if (strength > 120 && strength <= 150) {
            dot = 200; short_gap = 200;
        }
        else if (strength > 150 && strength <= 180) {
            dot = 200; short_gap = 150;
        }
        else if (strength > 180 && strength <= 210) {
            dot = 300; short_gap = 100;
        }
        else if (strength > 210 && strength <= 240) {
            dot = 400; short_gap = 50;
        }
        else if (strength > 240 && strength <= 255) {
            dot = 500; short_gap = 10;
        }
        else {
            dot = 0; short_gap = 0;
        }

        long[] pattern = {
                0, dot, short_gap, dot, short_gap, dot, short_gap, dot //, short_gap, dot, short_gap, dot, short_gap
        };

        if(dot != 0)
        vibrator.vibrate(pattern, -1);



        //v.vibrate(Math.min(1000, strength*4));
        //Log.d("vibrate", String.valueOf(Math.min(1000, strength*4)));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_depth_display);
        mTTS =  new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    mTTS.setLanguage(Locale.US);
                }
            }
        });



        Intent intent = getIntent();
        image_names = intent.getStringArrayListExtra("image_names");

        image_dir = getExternalCacheDir()+"/blind_assistant_images/";

        imageView = findViewById(R.id.depthImage);
        nextButton = findViewById(R.id.depth_next);
        prevButton = findViewById(R.id.depth_prev);
        textView = findViewById(R.id.depthTextView);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);

        Bitmap bmp = BitmapFactory.decodeFile(image_dir+image_names.get(currentIndex)+".jpg");
        Bitmap sbmp = Bitmap.createScaledBitmap(bmp, 1600, 900, false);
        imageView.setImageBitmap(sbmp);
        //currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
        //Log.d("current-list", currentList.toString());

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache(true);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int x = (int) (motionEvent.getX());
                    int y = (int) (motionEvent.getY());
                    bitmap = imageView.getDrawingCache();
                    try{
                        if(x >= 0 && x <= 1600 && y >= 0 && y <= 900) {
                            if(y>900) y = 900-1; if(y < 0) y = 1;
                            if(x>1600) x = 1600-1; if(x < 0) x = 1;
                            int pixel = bitmap.getPixel(x, y);
                            int r = Color.red(pixel);
                            int g = Color.green(pixel);
                            int b = Color.blue(pixel);
                            textView.setBackgroundColor(Color.rgb(r,g,b));
                            String temp = "R: "+r+"G: "+g+"B: "+b;
                            textView.setText(temp);
                            vibrate_color(r);
                        }
                    }
                    catch (Exception e) {
                        //do nothing
                    }

                }
                return false;
            }
        });
        //bitmap = imageView.getDrawingCache();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.depth_prev:
                Log.d("test", "reached in previous");
                if(currentIndex>0) {
                    currentIndex = currentIndex-1;
                    String filename = image_dir+image_names.get(currentIndex)+".jpg";
                    //byte[] myImage = imageMap.get(image_names.get(currentIndex));
                    //Bitmap bitmap = BitmapFactory.decodeByteArray(myImage, 0, myImage.length);
                    Bitmap bmp = BitmapFactory.decodeFile(filename);
                    Bitmap sbmp = Bitmap.createScaledBitmap(bmp, 1600, 900, false);
                    //bitmap = BitmapFactory.decodeFile(map.get(""+index));
                    imageView.setImageBitmap(sbmp);
                    //currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
                    //Log.d("current-list", currentList.toString());

                    //speak("previous");
                }
                break;
            case R.id.depth_next:
                Log.d("test", "reached in next");
                if(currentIndex<image_names.size()-1) {
                    currentIndex = currentIndex+1;
                    //byte[] myImage = imageMap.get(image_names.get(currentIndex));
                    String filename = image_dir+image_names.get(currentIndex)+".jpg";
                    Bitmap bmp = BitmapFactory.decodeFile(filename);
                    Bitmap sbmp = Bitmap.createScaledBitmap(bmp, 1600, 900, false);
                    imageView.setImageBitmap(sbmp);
                    Log.d("test", imageView.getWidth() + " " + imageView.getHeight());
                    //currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
                    //Log.d("current-list", currentList.toString());

                    //speak("next");
                }
                break;
            default:
                Log.d("test", "reached in onclick");
                break;
        }
    }
}