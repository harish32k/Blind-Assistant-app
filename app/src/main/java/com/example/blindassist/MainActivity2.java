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

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Connection" ;
    private ImageView image;
    private Map<String,String> map;
    private Bitmap bitmap,pixelBitmap;
    private int index = 1;
    private TextToSpeech mTTS;
    private  FirebaseDatabase database;
    private static float area(int x1, int y1, int x2,
                              int y2, int x3, int y3)
    {
        return (float)Math.abs((x1 * (y2 - y3) +
                x2 * (y3 - y1) + x3 * (y1 - y2)) / 2.0);
    }
    private static boolean check(int x1, int y1, int x2, int y2,
                                 int x3, int y3, int x4, int y4, int x, int y)
    {

        /* Calculate area of rectangle ABCD */
        float A = area(x1, y1, x2, y2, x3, y3)+
                area(x1, y1, x4, y4, x3, y3);

        /* Calculate area of triangle PAB */
        float A1 = area(x, y, x1, y1, x2, y2);

        /* Calculate area of triangle PBC */
        float A2 = area(x, y, x2, y2, x3, y3);

        /* Calculate area of triangle PCD */
        float A3 = area(x, y, x3, y3, x4, y4);

        /* Calculate area of triangle PAD */
        float A4 = area(x, y, x1, y1, x4, y4);

        /* Check if sum of A1, A2, A3 and A4
        is same as A */
        return (A == A1 + A2 + A3 + A4);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void addTouchListener(){
        image = (ImageView) findViewById(R.id.imageView);
        TextView text = (TextView) findViewById(R.id.textView);
        image.setDrawingCacheEnabled(true);
        image.buildDrawingCache(true);
        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN ){
                    //  ||
                    //} event.getAction() == MotionEvent.ACTION_MOVE) {
//                    pixelBitmap = image.getDrawingCache();
                    int pixel = bitmap.getPixel((int) event.getX(),(int) event.getY());

                    int r = Color.red(pixel);
                    int g = Color.green(pixel);
                    int b = Color.blue(pixel);
                    text.setBackgroundColor(Color.rgb(r,g,b));
                    text.setText("R("+r+")"+"G("+g+")"+"B("+b+")\n"+"x: "+String.valueOf(event.getX())+" y: "+String.valueOf( event.getY()));
                    // text.setText(String.valueOf(bitmap.getPixel((int) event.getX(),(int) event.getY())));
                    Log.d("pixels",String.valueOf(pixel)+"red:"+String.valueOf(r)+"green:"+String.valueOf(g));
//                    float x = event.getX();
//                    float y = event.getY();
//                    @SuppressLint("DefaultLocale")
//                    String message = String.format("Coordinates : (%.2f,%.2f)", x, y);
//                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
//                    DatabaseReference myRef = database.getReference();
//                    myRef.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                for (int i = 0; i < snapshot.child("boundaries").getChildrenCount(); i++) {
//                                    String x1 = snapshot.child("boundaries").child(String.valueOf(i)).child("0").child("0").getValue().toString();
//                                    String y1 = snapshot.child("boundaries").child(String.valueOf(i)).child("0").child("1").getValue().toString();
//                                    String x2 = snapshot.child("boundaries").child(String.valueOf(i)).child("2").child("0").getValue().toString();
//                                    String y2 = snapshot.child("boundaries").child(String.valueOf(i)).child("2").child("1").getValue().toString();
//                                    if (Integer.valueOf(x1) > Math.round(x) || Integer.valueOf(x2) < Math.round(x) || Integer.valueOf(y1) > Math.round(y) || Integer.valueOf(y2) < Math.round(y)) {
//                                        Log.d(TAG, "Outside  " + i + " " + x + " " + y);
//                                    } else {
//                                        Log.d(TAG, "Indide Container " + i + " " + x + " " + y);
//                                    }
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
                };
                return true;
            }
        });
    }

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
                speak("outside");
                return false;
            }
        });
        //Real Time DataBase
//        database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("message");
        image = (ImageView) findViewById(R.id.imageView);
        map=new HashMap<String,String>();
        Button previous = (Button) findViewById(R.id.previous);
        previous.setOnClickListener(this);
        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(this);
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://api-endpoints-3d693.appspot.com/output.json");
        
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
//                    String value = snapshot.getValue().toString();
//                    Log.d(TAG, "Value is: " + value);
//                }
//
//                try{
//                    for(int i=1;i<=4;i++) {
//                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://pushnotificationtest-68d48.appspot.com/Image"+i);
//                        File localFile = File.createTempFile("tempfile", ".jpeg");
//                        Log.d(TAG, "Index is: "+i);
//                        int finalI = i;
//                        storageRef.getFile(localFile)
//                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                                    @Override
//                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                                        map.put(""+finalI,localFile.getAbsolutePath());
//                                        Log.d(TAG, "Value is: "+localFile.getAbsolutePath());
//                                        bitmap = BitmapFactory.decodeFile(map.get("1"));
//                                        //Set Image
//                                        image.setImageBitmap(bitmap);
//                                        addTouchListener();
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Toast.makeText(MainActivity2.this, "Failed To Load", Toast.LENGTH_LONG);
//                                    }
//                                });
//                    }
//                } catch (IOException e){
//                    Log.d(TAG, "Error is: " );
//                    e.printStackTrace();
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });
    }
    private  void speak( String message){
        float pitch = 1f;
        float speed = 0.2f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(message,TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    protected void onDestroy() {
        if(mTTS!=null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous:
                if(index>1) {
                    index=index-1;
                    bitmap = BitmapFactory.decodeFile(map.get(""+index));
                    image.setImageBitmap(bitmap);
                    speak("previous");
                }
                break;
            case R.id.next:
                if(index<4) {
                    index=index+1;
                    bitmap = BitmapFactory.decodeFile(map.get(""+index));
                    image.setImageBitmap(bitmap);
                speak("next");
                }
                break;
            default:
                break;
        }
    }
}