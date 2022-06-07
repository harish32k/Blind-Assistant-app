package com.example.blindassist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.icu.util.Output;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ControlActivity extends AppCompatActivity implements View.OnClickListener ,
        MyDialogBox.MyDialogBoxListener, View.OnFocusChangeListener {

    private static final String TAG = "btconn";
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothSocket skt;
    public BluetoothSocket socket;
    private TextToSpeech mTTS;
    private Button object, caption, face, ocr, depth, grief_button, obj_depth_button, speak_button, recent_button;
    private Button vehicle_button, obstacle_button;
    private Intent intent;
    private List<String> objectSpeech = Arrays.asList("object", "object recognition", "object detection");
    private  List<String>captionSpeech = Arrays.asList("image","image captioning","caption","captioning");
    private List<String>faceSpeech = Arrays.asList("face","face recognition","face detection");
    private List<String>ocrSpeech = Arrays.asList("read text","text recognition","ocr");
    private List<String>depthSpeech = Arrays.asList("depth","depth estimation","estimation");
    private String task = "";
    public String fb_token;
    public static HashMap<String, String> cam_pos;

    public static BluetoothSocket obstacle_socket = null;
    public static boolean obstacle_connected = false;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private void set_button_state(boolean state) {
        object.setEnabled(state);
        caption.setEnabled(state);
        face.setEnabled(state);
        ocr.setEnabled(state);
        depth.setEnabled(state);
        speak_button.setEnabled(state);
    }

    public void open_dialog() {
        MyDialogBox myDialogBox = new MyDialogBox();
        myDialogBox.show(getSupportFragmentManager(), "select direction");
    }

    public void start_grief_signal() {
        String directions = "front right back left ";
        Log.d("selected-task", task);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // get location here
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(
                        new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    double lat = location.getLatitude();
                                    double longt = location.getLongitude();

                                    String coords = lat + " " + longt;
                                    Log.d("msg", "Lol");
                                    Log.d("msg", task + " " + "Example_User" + " " + coords);
                                    new SendMessage(socket,task + " " + "Example_User" + " " + coords);

                                }
                            }
                        }
                );
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        mTTS =  new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    mTTS.setLanguage(Locale.US);
                }
            }
        });
        ConstraintLayout layout = findViewById(R.id.layout);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        /*layout.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                speak("Clicking Outside Boundaries");
                return false;
            }
        });*/


        cam_pos = new HashMap<>();
        cam_pos.put("img1", "front");
        cam_pos.put("img2", "right");
        cam_pos.put("img3", "back");
        cam_pos.put("img4", "left");

        // buttons
        object = findViewById(R.id.button1);
        object.setOnClickListener(this);

        caption = findViewById(R.id.button2);
        caption.setOnClickListener(this);

        face = findViewById(R.id.button3);
        face.setOnClickListener(this);

        ocr = findViewById(R.id.button4);
        ocr.setOnClickListener(this);

        depth = findViewById(R.id.button5);
        depth.setOnClickListener(this);

        speak_button = findViewById(R.id.speak);
        speak_button.setOnClickListener(this);

        obj_depth_button = findViewById(R.id.button6);
        obj_depth_button.setOnClickListener(this);

        grief_button = findViewById(R.id.button7);
        grief_button.setOnClickListener(this);

        recent_button = findViewById(R.id.recent_button);
        recent_button.setOnClickListener(this);

        vehicle_button = findViewById(R.id.vehicle_button);
        vehicle_button.setOnClickListener(this);

        obstacle_button = findViewById(R.id.obstacle_button);
        obstacle_button.setOnClickListener(this);

        set_button_state(false);

        //start intent
        Intent intent = getIntent();
        BluetoothDevice device = intent.getParcelableExtra("device");
        String name = "";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            name = device.getName();
        }
        String address = device.getAddress();

        SharedPreferences shrd = getSharedPreferences("firebase_token", MODE_PRIVATE);
        fb_token = shrd.getString("token", "no token found");

        //Toast.makeText(this, name + " " + address, Toast.LENGTH_SHORT).show();
        ConnectThread connectThread = new ConnectThread(device);
        connectThread.start();

        recent_button.setOnFocusChangeListener(this);
        speak_button.setOnFocusChangeListener(this);
        object.setOnFocusChangeListener(this);
        caption.setOnFocusChangeListener(this);
        face.setOnFocusChangeListener(this);
        ocr.setOnFocusChangeListener(this);
        depth.setOnFocusChangeListener(this);
        grief_button.setOnFocusChangeListener(this);
        obj_depth_button.setOnFocusChangeListener(this);
        vehicle_button.setOnFocusChangeListener(this);
        obstacle_button.setOnFocusChangeListener(this);


    }

    @Override
    public void onClick(View v) {
        speak("selected: " + ((Button) v).getText().toString());
        switch (v.getId()) {
            case R.id.button1:
                task = "object";
                open_dialog();
                break;
            case R.id.button2:
                task = "caption";
                open_dialog();
                break;
            case R.id.button3:
                task = "face";
                open_dialog();
                break;
            case R.id.button4:
                task = "ocr";
                open_dialog();
                break;
            case R.id.button5:
                task = "depth";
                open_dialog();
                break;
            case R.id.button6:
                task = "obj_depth";
                open_dialog();
                break;
            case R.id.button7:
                task = "grief";
                start_grief_signal();
                break;
            case R.id.vehicle_button:
                task = "vehicle";
            {Intent intent = new Intent(this, VehicleDetect.class);
                startActivity(intent);}
            break;
            case R.id.obstacle_button:
                task = "obstacle";
            {Intent intent = new Intent(this, ObstacleActivity.class);
                startActivity(intent);}
            break;
            case R.id.recent_button:
                Intent intent = new Intent(this, GetDataActivity.class);
                startActivity(intent);
                break;
            case R.id.speak:
                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start Speaking");
                startActivityForResult(intent, 10);
            default:
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(objectSpeech.contains(result.get(0))){
                        task = "object";
                        open_dialog();
                        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                    else if(captionSpeech.contains(result.get(0))){
                        task = "caption";
                        open_dialog();
                    }
                    else if(faceSpeech.contains(result.get(0))){
                        task = "face";
                        open_dialog();
                    }
                    else if(ocrSpeech.contains(result.get(0))){
                        task = "ocr";
                        open_dialog();
                    }
                    else if(depthSpeech.contains(result.get(0))){
                        task = "depth";
                        open_dialog();
                    }
                    else {
                        speak("Not Available");
                    }
                }
                break;
        }
    }

    private  void speak( String message){
        float pitch = 1f;
        float speed = 1.7f;
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
    public void readSelections(String directions) {
        Intent intent = new Intent();
        Log.d("selected-task", task);
        new SendMessage(socket,task + " " + fb_token + " " + directions);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b) {
            speak((String) ((Button) view).getText());
        } else {
            //Toast.makeText(getApplicationContext(), "Lost the focus", Toast.LENGTH_LONG).show();
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
            set_button_state(true);
        }

        @SuppressLint("MissingPermission")
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            socket = mmSocket;
            skt = socket;
            //Toast.makeText(ControlActivity.this, "Connected.", Toast.LENGTH_SHORT).show();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        speak("Please click BACK again to exit");
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

}



//    private class SendMessage implements View.OnClickListener {
//
//        @Override
//        public void onClick(View view) {
//            String message = String.valueOf(messageView.getText());
//            try {
//                OutputStream ostream = socket.getOutputStream();
//                ostream.write(message.getBytes(StandardCharsets.UTF_8));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }