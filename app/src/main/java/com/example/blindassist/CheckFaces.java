package com.example.blindassist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CheckFaces extends AppCompatActivity {

    private TextToSpeech mTTS;
    private Button faces_button;
    private JsonObject faces_data;

    public JsonObject getJsonFromString(String jsonString) {
        JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
        return obj;
    }

    public ArrayList<String> getArrayListFromJsonArray(JsonArray jArray) {
        ArrayList<String> listdata = new ArrayList<String>();
        if (jArray != null) {
            for (int i=0;i<jArray.size();i++){
                listdata.add(jArray.get(i).getAsString());
            }
        }
        return listdata;
    }
    public void read_faces() {
        String to_speak = "";
        Set<Map.Entry<String, JsonElement>> entries = faces_data.entrySet();
        Log.d("faces", "onClick: "+entries.toString());
        if(entries.isEmpty()) {
            speak("No faces detected");
            return;
        }
        for (Map.Entry<String, JsonElement> entry: entries) {
            String direction = entry.getKey();
            JsonArray json_detections = faces_data.getAsJsonArray(direction);
            ArrayList<String> detections = getArrayListFromJsonArray(json_detections);

            to_speak += direction + ". ";
            for(String face: detections) {
                to_speak += face + ", ";
            }

            to_speak += ". ";
            Log.d("faces", detections.toString());
        }
        Log.d("faces", to_speak);
        speak(to_speak);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_faces);

        Intent intent = getIntent();
        String faces = intent.getStringExtra("faces");
        JsonElement json_data = getJsonFromString(faces).get("prediction");
        if(json_data == null) {
            faces_data = new JsonObject();
        }
        else {
            faces_data = json_data.getAsJsonObject();
        }
        Log.d("faces", faces_data.toString());

        faces_button = findViewById(R.id.speak_faces_button);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    mTTS.setLanguage(Locale.US);
                    faces_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            read_faces();
                        }
                    });
                    faces_button.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    private void speak(String message){
        float pitch = 1f;
        float speed = 1f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(message,TextToSpeech.QUEUE_FLUSH,null);
    }
}