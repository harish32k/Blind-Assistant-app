package com.example.blindassist;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.Build;
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
import java.util.Locale;

public class ObjDepthDisplay extends AppCompatActivity implements View.OnClickListener {

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
    private SoundPool soundPool;
    private int sound1;

    public JsonObject getJsonFromString(String jsonString) {
        JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
        return obj;
    }

    public void vibrate_color(int strength) {
        float volume = ((float)strength/255);
        soundPool.play(sound1, volume, volume, 0, 0, 1);
    }

    private void check_obj_and_depth(int x, int y, int r) {
        x = (int) (1.2 * x);
        y = (int) (1.2 * y);
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
        Log.d("detected", "here");
        if(found_items != ""){
            vibrate_color(r);
            speak(found_items.substring(0, found_items.length()));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            vibrate_color(r);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_obj_depth_display);


        /*Soundpool setup below*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(4)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        }
        sound1 = soundPool.load(this, R.raw.beep, 1);
        /*Soundpool setup above*/

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
        modelOutput = getJsonFromString(intent.getStringExtra("json_data"));

        image_dir = getExternalCacheDir()+"/blind_assistant_images/";

        imageView = findViewById(R.id.objdepthImage);
        nextButton = findViewById(R.id.objdepth_next);
        prevButton = findViewById(R.id.objdepth_prev);
        textView = findViewById(R.id.objdepthTextView);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);

        Bitmap bmp = BitmapFactory.decodeFile(image_dir+image_names.get(currentIndex)+".jpg");
        Bitmap sbmp = Bitmap.createScaledBitmap(bmp, 1600, 900, false);
        imageView.setImageBitmap(sbmp);

        try {
            currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
            if(currentList == null) currentList = new JsonArray();
        } catch (Exception e) {
            currentList = new JsonArray();
        }
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
                            Log.d("obj_depth", "here: "+ image_names.get(currentIndex));
                            Log.d("obj_depth", "x: " + x + ", y: " + y + ", r: " + r);
                            check_obj_and_depth(x, y, r);
                        }
                    }
                    catch (Exception e) {
                        //do nothing
                    }

                }
                return true;
            }
        });


        int test = 150;
        int volume = (int) ((double) test/255) * 100;
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, volume);
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
    }

    public void refresh_image_view() {
        imageView.setDrawingCacheEnabled(false);
        imageView.buildDrawingCache(false);
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache(true);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.objdepth_prev:
                Log.d("test", "reached in previous");
                if (currentIndex > 0) {
                    currentIndex = currentIndex - 1;
                    String filename = image_dir + image_names.get(currentIndex) + ".jpg";
                    Bitmap bmp = BitmapFactory.decodeFile(filename);
                    Bitmap sbmp = Bitmap.createScaledBitmap(bmp, 1600, 900, false);
                    imageView.setImageBitmap(sbmp);
                    refresh_image_view();
                    try {
                        currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
                        if(currentList == null) currentList = new JsonArray();
                    } catch (Exception e) {
                        currentList = new JsonArray();
                    }
                    //Log.d("current-list", currentList.toString());

                }
                break;
            case R.id.objdepth_next:
                Log.d("test", "reached in next");
                if (currentIndex < image_names.size() - 1) {
                    currentIndex = currentIndex + 1;
                    //byte[] myImage = imageMap.get(image_names.get(currentIndex));
                    String filename = image_dir + image_names.get(currentIndex) + ".jpg";
                    Bitmap bmp = BitmapFactory.decodeFile(filename);
                    Bitmap sbmp = Bitmap.createScaledBitmap(bmp, 1600, 900, false);
                    imageView.setImageBitmap(sbmp);
                    refresh_image_view();
                    Log.d("test", imageView.getWidth() + " " + imageView.getHeight());
                    try {
                        currentList = modelOutput.getAsJsonObject("prediction").getAsJsonArray(image_names.get(currentIndex));
                        if(currentList == null) currentList = new JsonArray();
                    } catch (Exception e) {
                        currentList = new JsonArray();
                    }
                    //Log.d("current-list", currentList.toString());

                    //speak("next");
                }
                break;
            default:
                Log.d("test", "reached in onclick");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        soundPool.release();
        super.onDestroy();
    }

    private  void speak(String message){
        float pitch = 1f;
        float speed = 1f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(message,TextToSpeech.QUEUE_FLUSH,null);
    }
}