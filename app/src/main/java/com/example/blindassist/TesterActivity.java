package com.example.blindassist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;

import java.util.ArrayList;


public class TesterActivity extends AppCompatActivity {
    private DataSnapshot snapshot;
    private JsonObject outputJson;
    private ArrayList<Point> points;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tester);
        Button tester_button = findViewById(R.id.tester_button);
        tester_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int volume = 50;
                ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, volume);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
            }
        });
    }



}