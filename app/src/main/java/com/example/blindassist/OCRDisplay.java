package com.example.blindassist;

import static com.example.blindassist.ControlActivity.cam_pos;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
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
import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class OCRDisplay extends AppCompatActivity implements View.OnClickListener{
    private int currentIndex = 0;
    private HashMap<String, byte[]> imageMap;
    private ArrayList<String> image_names;
    private ImageView imageView;
    private Button nextButton;
    private Button prevButton;
    public String image_dir;
    public JsonObject modelOutput;
    public ArrayList<Box> currentList;
    public TextView textView;
    private TextToSpeech mTTS;
    private Bitmap bitmap;
    public JsonObject outputJson;
    public ArrayList<ArrayList<Box>> boxes_tray;
    public BluetoothSocket socket = ControlActivity.skt;


    private int dstWidth = 1200;
    private int dstHeight = 900;
    double scaleFactor = 1/1.875;

    public JsonObject getJsonFromString(String jsonString) {
        JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
        return obj;
    }

    public void createBoxes() {
        //String image_names[] = {"img1", "img2", "img3", "img4"};

        JsonObject predictions = null;
        try {
            predictions = outputJson.get("prediction").getAsJsonObject();
        } catch (Exception e) {
            predictions = new JsonObject();
        }
        boxes_tray = new ArrayList<>();

        for(String image_name: image_names) {
            Log.d("polytest", image_name);
            JsonArray image_preds;
            try {
                image_preds = predictions.get(image_name).getAsJsonArray();
            } catch (Exception e) {
                image_preds = new JsonArray();
            }
            boolean first = true;
            ArrayList<Box> boxes = new ArrayList<>();
            for(JsonElement jsonElement: image_preds) {
                if(first) {first = false; continue;}
                JsonObject pred = jsonElement.getAsJsonObject();
                JsonArray coordinates = pred.get("coordinates").getAsJsonArray();

                Polygon.Builder polyBuilder = Polygon.Builder();
                for(JsonElement tempObject: coordinates) {
                    JsonArray coordinate = tempObject.getAsJsonArray();
                    int x = coordinate.get(0).getAsInt();
                    int y = coordinate.get(1).getAsInt();
                    polyBuilder.addVertex(new Point(x, y));
                }
                Polygon poly = polyBuilder.build();

                String desc = pred.get("description").getAsString();
                String locale = pred.get("locale").getAsString();

                Box b = new Box(poly, desc, locale);
                boxes.add(b);
            }
            Log.d("polytest", String.valueOf(boxes.size()));
            boxes_tray.add(boxes);
        }
    }

    public void checkBoxes(int x, int y) {
        String to_read = "";
        for(Box b: currentList) {
            if (b.polygon.contains(new Point(x, y)))
            to_read += b.desc;
        }
        if(to_read != "")
        speak(to_read.toLowerCase(Locale.ROOT)
        );
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Intent intent = getIntent();
        image_dir = getExternalCacheDir()+"/blind_assistant_images/";
        image_names = intent.getStringArrayListExtra("image_names");
        outputJson = getJsonFromString(intent.getStringExtra("json_data"));

        createBoxes();
        setContentView(R.layout.activity_ocrdisplay);
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



        imageView = findViewById(R.id.ocrImage);
        nextButton = findViewById(R.id.ocr_next);
        prevButton = findViewById(R.id.ocr_prev);
        textView = findViewById(R.id.ocrTextView);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);

        Bitmap bmp = BitmapFactory.decodeFile(image_dir+image_names.get(currentIndex)+".jpg");
        Bitmap sbmp = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, false);
        imageView.setImageBitmap(sbmp);
        currentList = boxes_tray.get(currentIndex);
        Log.d("current-list", currentList.toString());

        //message
        //new SendMessage(socket, image_names.get(currentIndex));

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache(true);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int x = (int) (motionEvent.getX()*scaleFactor);
                    int y = (int) (motionEvent.getY()*scaleFactor);
                    checkBoxes(x, y);
                }
                return false;
            }
        });
    }
    private void speak( String message){
        float pitch = 1f;
        float speed = 1f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(message,TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ocr_prev:
                Log.d("test", "reached in previous");
                if(currentIndex>0) {
                    currentIndex = currentIndex-1;
                    String filename = image_dir+image_names.get(currentIndex)+".jpg";
                    //byte[] myImage = imageMap.get(image_names.get(currentIndex));
                    //Bitmap bitmap = BitmapFactory.decodeByteArray(myImage, 0, myImage.length);
                    Bitmap bmp = BitmapFactory.decodeFile(filename);
                    Bitmap sbmp = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, false);
                    //bitmap = BitmapFactory.decodeFile(map.get(""+index));
                    imageView.setImageBitmap(sbmp);
                    currentList = boxes_tray.get(currentIndex);
                    Log.d("current-list", String.valueOf(currentList.size()));
                    //new SendMessage(socket, image_names.get(currentIndex));
                    speak(cam_pos.get(image_names.get(currentIndex)));
                    //speak("previous");
                }
                break;
            case R.id.ocr_next:
                Log.d("test", "reached in next");
                if(currentIndex<image_names.size()-1) {
                    currentIndex = currentIndex+1;
                    //byte[] myImage = imageMap.get(image_names.get(currentIndex));
                    String filename = image_dir+image_names.get(currentIndex)+".jpg";
                    Bitmap bmp = BitmapFactory.decodeFile(filename);
                    Bitmap sbmp = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, false);
                    imageView.setImageBitmap(sbmp);
                    Log.d("test", imageView.getWidth() + " " + imageView.getHeight());
                    currentList = boxes_tray.get(currentIndex);
                    Log.d("current-list", String.valueOf(currentList.size()));
                    //new SendMessage(socket, image_names.get(currentIndex));
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