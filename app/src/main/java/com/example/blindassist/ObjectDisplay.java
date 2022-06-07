package com.example.blindassist;

import static com.example.blindassist.ControlActivity.cam_pos;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

public class ObjectDisplay extends AppCompatActivity implements View.OnClickListener {

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

    private int dstWidth = 1200;
    private int dstHeight = 900;
    double scaleFactor = 1/1.875;



    public JsonObject getJsonFromString(String jsonString) {
        JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
        return obj;
    }

    void checkBoxes(int x, int y) {
        ArrayList<String> detected = new ArrayList<>();
        String found_items = "";
        for (JsonElement jelem: currentList) {
            JsonObject box = jelem.getAsJsonObject();
            float xmin = box.get("xmin").getAsFloat();
            float ymin = box.get("ymin").getAsFloat();
            float xmax = box.get("xmax").getAsFloat();
            float ymax = box.get("ymax").getAsFloat();

            if((x>=xmin && x<=xmax) && (y>=ymin && y<=ymax)) {
                String item = box.get("name").getAsString();
                detected.add(item);
                found_items += (item + ". ");
                Log.d("detected", item);
            }
            Log.d("jsonElement", box.toString());
        }
        if(found_items != "")
        speak(found_items.substring(0, found_items.length()));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_objectdisplay);

        //////// setting TTS
        mTTS =  new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    mTTS.setLanguage(Locale.US);
                    speak(cam_pos.get(image_names.get(currentIndex)));
                }
            }
        });

        ////////


        Intent intent = getIntent();
        image_dir = getExternalCacheDir()+"/blind_assistant_images/";
        image_names = intent.getStringArrayListExtra("image_names");
        modelOutput = getJsonFromString(intent.getStringExtra("json_data"));
        Log.d("model-json-test", modelOutput.toString());

        imageView = findViewById(R.id.myImage);
        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.previous_button);
        textView = findViewById(R.id.textView);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);

        Bitmap bmp = BitmapFactory.decodeFile(image_dir+image_names.get(currentIndex)+".jpg");
        Bitmap sbmp = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, false);
        imageView.setImageBitmap(sbmp);

        JsonObject pred_obj = modelOutput.getAsJsonObject("prediction");

        try {
            currentList = pred_obj.getAsJsonArray(image_names.get(currentIndex));
            if(currentList == null) currentList = new JsonArray();
        } catch (Exception e) {
            currentList = new JsonArray();
        }

        Log.d("current-list", "here");
        Log.d("current-list", currentList.toString());

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache(true);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                try {
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                        int x = (int) (motionEvent.getX()*scaleFactor);
                        int y = (int) (motionEvent.getY()*scaleFactor);
                        checkBoxes(x, y);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                return false;
            }
        });

    }

    private  void speak( String message){
        float pitch = 1f;
        float speed = 1f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(message,TextToSpeech.QUEUE_FLUSH,null);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous_button:
                Log.d("test", "reached in previous");
                if(currentIndex>0) {
                    currentIndex = currentIndex-1;
                    String filename = image_dir+image_names.get(currentIndex)+".jpg";
                    Bitmap bmp = BitmapFactory.decodeFile(filename);
                    Bitmap sbmp = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, false);
                    imageView.setImageBitmap(sbmp);
                    try {
                        currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
                        if(currentList == null) currentList = new JsonArray();
                    } catch (Exception e) {
                        currentList = new JsonArray();
                    }
                    Log.d("current-list", currentList.toString());
                    speak(cam_pos.get(image_names.get(currentIndex)));
                    //speak("previous");
                }
                break;
            case R.id.next_button:
                Log.d("test", "reached in next");
                if(currentIndex<image_names.size()-1) {
                    currentIndex = currentIndex+1;
                    String filename = image_dir+image_names.get(currentIndex)+".jpg";
                    Bitmap bmp = BitmapFactory.decodeFile(filename);
                    Bitmap sbmp = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, false);
                    imageView.setImageBitmap(sbmp);
                    Log.d("test", imageView.getWidth() + " " + imageView.getHeight());
                    try {
                        currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
                        if(currentList == null) currentList = new JsonArray();
                    } catch (Exception e) {
                        currentList = new JsonArray();
                    }
                    Log.d("current-list", currentList.toString());
                    speak(cam_pos.get(image_names.get(currentIndex)));
                    //speak("next");
                }
                break;
            default:
                Log.d("test", "reached in onclick");
                break;
        }
    }
}