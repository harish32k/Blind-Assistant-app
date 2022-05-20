package com.example.blindassist;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import java.awt.*;

public class OCRDisplay extends AppCompatActivity implements View.OnClickListener{
    private int currentIndex = 0;
    private HashMap<String, byte[]> imageMap;
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



    public JsonObject getJsonFromString(String jsonString) {
        JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
        return obj;
    }

    public void checkBoxes(int x, int y) {

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrdisplay);
        mTTS =  new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    mTTS.setLanguage(Locale.US);
                }
            }
        });

        ////////


        Intent intent = getIntent();
        image_dir = getExternalCacheDir()+"/blind_assistant_images/";
        image_names = intent.getStringArrayListExtra("image_names");
        modelOutput = getJsonFromString(intent.getStringExtra("json_data"));
        Log.d("model-json-test", modelOutput.toString());

        imageView = findViewById(R.id.ocrImage);
        nextButton = findViewById(R.id.ocr_next);
        prevButton = findViewById(R.id.ocr_prev);
        textView = findViewById(R.id.ocrTextView);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);

        Bitmap bmp = BitmapFactory.decodeFile(image_dir+image_names.get(currentIndex)+".jpg");
        Bitmap sbmp = Bitmap.createScaledBitmap(bmp, 1600, 900, false);
        imageView.setImageBitmap(sbmp);
        currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
        Log.d("current-list", currentList.toString());

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache(true);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int x = (int) (1.2 * motionEvent.getX());
                    int y = (int) (1.2 * motionEvent.getY());
                    checkBoxes(x, y);
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous_button:
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
                    currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
                    Log.d("current-list", currentList.toString());

                    //speak("previous");
                }
                break;
            case R.id.next_button:
                Log.d("test", "reached in next");
                if(currentIndex<image_names.size()-1) {
                    currentIndex = currentIndex+1;
                    //byte[] myImage = imageMap.get(image_names.get(currentIndex));
                    String filename = image_dir+image_names.get(currentIndex)+".jpg";
                    Bitmap bmp = BitmapFactory.decodeFile(filename);
                    Bitmap sbmp = Bitmap.createScaledBitmap(bmp, 1600, 900, false);
                    imageView.setImageBitmap(sbmp);
                    Log.d("test", imageView.getWidth() + " " + imageView.getHeight());
                    currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
                    Log.d("current-list", currentList.toString());

                    //speak("next");
                }
                break;
            default:
                Log.d("test", "reached in onclick");
                break;
        }
    }
}