package com.example.blindassist;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

public class VehicleDetect extends AppCompatActivity implements View.OnFocusChangeListener {

    private Button start_button;
    private Button stop_button;
    private BluetoothSocket socket = ControlActivity.skt;
    private TextToSpeech mTTS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detect);
        start_button = findViewById(R.id.start_vehicle);
        stop_button = findViewById(R.id.stop_vehicle);
        //new SendMessage(socket,task + " " + fb_token + " " + directions);

        mTTS =  new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    mTTS.setLanguage(Locale.US);
                }
            }
        });

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak("starting vehicle detection");
                new SendMessage(socket, "vehicle_start" + " " + "no_token" + " " + "front");
            }
        });
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak("stopping vehicle detection");
                new SendMessage(socket, "vehicle_stop" + " " + "no_token" + " " + "front");
            }
        });

        start_button.setOnFocusChangeListener(this);
        stop_button.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b) {
            speak((String) ((Button) view).getText());
        } else {
            //Toast.makeText(getApplicationContext(), "Lost the focus", Toast.LENGTH_LONG).show();
        }
    }


    private  void speak( String message){
        float pitch = 1f;
        float speed = 1.7f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(message,TextToSpeech.QUEUE_FLUSH,null);
    }

}