package com.example.blindassist;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionBarPolicy;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ImageDisplayBase extends AppCompatActivity { //implements View.OnClickListener {

    private static final String TAG = "Connection" ;
    private ImageView image;
    private Map<String,String> map;
    private Bitmap bitmap,pixelBitmap;
    private int index = 1;
    private TextToSpeech mTTS;
    private FirebaseDatabase database;

//    @SuppressLint("ClickableViewAccessibility")
//    private void addTouchListener(){
//        image = (ImageView) findViewById(R.id.imageView);
//        TextView text = (TextView) findViewById(R.id.textView);
//        image.setDrawingCacheEnabled(true);
//        image.buildDrawingCache(true);
//        image.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                if(event.getAction()==MotionEvent.ACTION_DOWN ){
//
//                    int pixel = bitmap.getPixel((int) event.getX(),(int) event.getY());
//
//                    int r = Color.red(pixel);
//                    int g = Color.green(pixel);
//                    int b = Color.blue(pixel);
//                    text.setBackgroundColor(Color.rgb(r,g,b));
//                    text.setText("R("+r+")"+"G("+g+")"+"B("+b+")\n"+"x: "+String.valueOf(event.getX())+" y: "+String.valueOf( event.getY()));
//                    // text.setText(String.valueOf(bitmap.getPixel((int) event.getX(),(int) event.getY())));
//                    Log.d("pixels",String.valueOf(pixel)+"red:"+String.valueOf(r)+"green:"+String.valueOf(g));
//                };
//                return true;
//            }
//        });
//    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main2);
        mTTS =  new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    mTTS.setLanguage(Locale.US);
                }
            }
        });
        ConstraintLayout layout = findViewById(R.id.layout);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                TextView text = (TextView) findViewById(R.id.textView);
                text.setText("Outside");
                //speak("outside");
                return false;
            }
        });
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("recent");

        myRef.setValue("Hello, World!");

        image = (ImageView) findViewById(R.id.imageView);
        map=new HashMap<String,String>();
        Button previous = (Button) findViewById(R.id.previous);
        //previous.setOnClickListener(this);
        Button next = (Button) findViewById(R.id.next);
        //next.setOnClickListener(this);
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://api-endpoints-3d693.appspot.com/output.json");

    }
//    private  void speak( String message){
//        float pitch = 1f;
//        float speed = 0.2f;
//        mTTS.setPitch(pitch);
//        mTTS.setSpeechRate(speed);
//        mTTS.speak(message,TextToSpeech.QUEUE_FLUSH,null);
//    }
//
//    @Override
//    protected void onDestroy() {
//        if(mTTS!=null){
//            mTTS.stop();
//            mTTS.shutdown();
//        }
//        super.onDestroy();
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.previous:
//                if(index>1) {
//                    index=index-1;
//                    bitmap = BitmapFactory.decodeFile(map.get(""+index));
//                    image.setImageBitmap(bitmap);
//                    speak("previous");
//                }
//                break;
//            case R.id.next:
//                if(index<4) {
//                    index=index+1;
//                    bitmap = BitmapFactory.decodeFile(map.get(""+index));
//                    image.setImageBitmap(bitmap);
//                    speak("next");
//                }
//                break;
//            default:
//                break;
//        }
//    }
}