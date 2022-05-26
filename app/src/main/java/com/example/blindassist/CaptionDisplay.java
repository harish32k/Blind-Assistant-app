package com.example.blindassist;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class CaptionDisplay extends AppCompatActivity implements View.OnClickListener {
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
    private JsonObject outputJson;
    private HashMap<String, String> cam_pos = new HashMap<>();



    public JsonObject getJsonFromString(String jsonString) {
        JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
        return obj;
    }

    public void speak_caption() {
        Log.d("caption", image_names.get(currentIndex));
        Log.d("caption", outputJson.toString());
        JsonArray caption_obj = outputJson.get(image_names.get(currentIndex)).getAsJsonArray();
        String caption = caption_obj.get(0).getAsString();
        Log.d("caption", caption);
        speak(caption);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_caption_display);

        mTTS =  new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    mTTS.setLanguage(Locale.US);
                }
            }
        });

        cam_pos.put("img1", "front");
        cam_pos.put("img2", "right");
        cam_pos.put("img3", "back");
        cam_pos.put("img4", "left");


        Intent intent = getIntent();
        image_dir = getExternalCacheDir()+"/blind_assistant_images/";
        image_names = intent.getStringArrayListExtra("image_names");
        outputJson = getJsonFromString(intent.getStringExtra("json_data"));

        Log.d("caption", "image_names: " + image_names.toString());

        image_dir = getExternalCacheDir()+"/blind_assistant_images/";
        imageView = findViewById(R.id.captionImage);
        nextButton = findViewById(R.id.caption_next);
        prevButton = findViewById(R.id.caption_prev);
        textView = findViewById(R.id.captionTextView);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);

        Log.d("caption", image_dir+image_names.get(currentIndex)+".jpg");
        textView.setText(cam_pos.get(image_names.get(currentIndex)));
        Bitmap bmp = BitmapFactory.decodeFile(image_dir+image_names.get(currentIndex)+".jpg");
        Bitmap sbmp = Bitmap.createScaledBitmap(bmp, 1600, 900, false);
        imageView.setImageBitmap(sbmp);
        //currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
        //Log.d("current-list", currentList.toString());

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache(true);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int x = (int) (motionEvent.getX());
                    int y = (int) (motionEvent.getY());
                    bitmap = imageView.getDrawingCache();
                    try{
                        if(x >= 0 && x <= 1600 && y >= 0 && y <= 900) {
                            speak_caption();
                        }
                    }
                    catch (Exception e) {
                        throw e;
                        //do nothing
                    }

                }
                return false;
            }
        });

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.caption_prev:
                Log.d("test", "reached in previous");
                if (currentIndex > 0) {
                    currentIndex = currentIndex - 1;
                    String filename = image_dir + image_names.get(currentIndex) + ".jpg";
                    //byte[] myImage = imageMap.get(image_names.get(currentIndex));
                    //Bitmap bitmap = BitmapFactory.decodeByteArray(myImage, 0, myImage.length);
                    Bitmap bmp = BitmapFactory.decodeFile(filename);
                    Bitmap sbmp = Bitmap.createScaledBitmap(bmp, 1600, 900, false);
                    //bitmap = BitmapFactory.decodeFile(map.get(""+index));
                    imageView.setImageBitmap(sbmp);
                    textView.setText(cam_pos.get(image_names.get(currentIndex)));
                    //currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
                    //Log.d("current-list", currentList.toString());

                    //speak("previous");
                }
                break;
            case R.id.caption_next:
                Log.d("test", "reached in next");
                if (currentIndex < image_names.size() - 1) {
                    currentIndex = currentIndex + 1;
                    //byte[] myImage = imageMap.get(image_names.get(currentIndex));
                    String filename = image_dir + image_names.get(currentIndex) + ".jpg";
                    Bitmap bmp = BitmapFactory.decodeFile(filename);
                    Bitmap sbmp = Bitmap.createScaledBitmap(bmp, 1600, 900, false);
                    imageView.setImageBitmap(sbmp);
                    textView.setText(cam_pos.get(image_names.get(currentIndex)));
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


    private  void speak( String message){
        float pitch = 1f;
        float speed = 1f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(message,TextToSpeech.QUEUE_FLUSH,null);
    }
}