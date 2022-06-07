package com.example.blindassist;

import static com.example.blindassist.ControlActivity.obstacle_connected;
import static com.example.blindassist.ControlActivity.obstacle_socket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class ObstacleActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static BluetoothSocket socket;
    private Button start_button;
    private Button stop_button;
    //private BluetoothSocket ob_socket = obstacle_socket;
    private TextToSpeech mTTS;



    private void set_button_state() {
        start_button.setVisibility(View.VISIBLE);
        stop_button.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obstacle);

        start_button = findViewById(R.id.start_obstacle);
        stop_button = findViewById(R.id.stop_obstacle);

        mTTS =  new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    mTTS.setLanguage(Locale.US);
                }
            }
        });

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d("bt-debug", "Bluetooth not present");
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.enable();
                Log.d("bt-debug", "Bluetooth present");
            }
        }

        Set<BluetoothDevice> myDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>();
        for (BluetoothDevice device : myDevices) pairedDevices.add(device);

        String device_address = "98:D3:51:FE:5B:66";
        BluetoothDevice myDevice = null;
//        if (pairedDevices.size() > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            for (BluetoothDevice device : pairedDevices) {
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//                if (deviceHardwareAddress.equals(device_address)) {
//                    myDevice = device;
//                    break;
//                }
//                Log.d("paired", deviceName + ", address: " + deviceHardwareAddress);
//            }
//        }

        myDevice = bluetoothAdapter.getRemoteDevice(device_address);

        if(!obstacle_connected)
        try {
            obstacle_socket = myDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            obstacle_socket.connect();
            obstacle_connected = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new SendMessage(obstacle_socket,"1");
                try {
                    obstacle_socket.getOutputStream().write("1".toString().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                speak("starting obstacle detection");
            }
        });
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new SendMessage(obstacle_socket,"0");
                try {
                    obstacle_socket.getOutputStream().write("0".toString().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                speak("stopping obstacle detection");
            }
        });
        set_button_state();
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
        mTTS.speak(message, TextToSpeech.QUEUE_FLUSH,null);
    }

}

/*
package com.example.blindassist;

import static com.example.blindassist.ControlActivity.obstacle_connected;
import static com.example.blindassist.ControlActivity.obstacle_socket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class ObstacleActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static BluetoothSocket socket;
    private Button start_button;
    private Button stop_button;
    //private BluetoothSocket ob_socket = obstacle_socket;
    private TextToSpeech mTTS;


    private void set_button_state() {
        start_button.setVisibility(View.VISIBLE);
        stop_button.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obstacle);

        start_button = findViewById(R.id.start_obstacle);
        stop_button = findViewById(R.id.stop_obstacle);

        mTTS =  new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    mTTS.setLanguage(Locale.US);
                }
            }
        });

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d("bt-debug", "Bluetooth not present");
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.enable();
                Log.d("bt-debug", "Bluetooth present");
            }
        }

        Set<BluetoothDevice> myDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>();
        for (BluetoothDevice device : myDevices) pairedDevices.add(device);

        String device_address = "98:D3:51:FE:5B:66";
        BluetoothDevice myDevice = null;
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (deviceHardwareAddress.equals(device_address)) {
                    myDevice = device;
                    break;
                }
                Log.d("paired", deviceName + ", address: " + deviceHardwareAddress);
            }
        }

        if(!obstacle_connected) {
            ConnectThread connectThread = new ConnectThread(myDevice);
            connectThread.start();
        }

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak("starting obstacle detection");
                new SendMessage(obstacle_socket,"1");
            }
        });
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak("stopping obstacle detection");
                new SendMessage(obstacle_socket,"0");
            }
        });
        set_button_state();
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
        mTTS.speak(message, TextToSpeech.QUEUE_FLUSH,null);
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
                Log.e("btconn", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
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
                    Log.e("btconn", "Could not close the client socket", closeException);
                }
            }
            Log.d("btconn", "run: here");

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            obstacle_socket = mmSocket;
            obstacle_connected = true;
            //Toast.makeText(ControlActivity.this, "Connected.", Toast.LENGTH_SHORT).show();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("btconn", "Could not close the client socket", e);
            }
        }
    }

}
*/
