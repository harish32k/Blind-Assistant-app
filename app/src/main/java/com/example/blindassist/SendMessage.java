package com.example.blindassist;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SendMessage {
    public SendMessage(BluetoothSocket socket, String message) {
            try{
                OutputStream streamos = socket.getOutputStream();
                streamos.write(message.getBytes(StandardCharsets.UTF_8));
                Log.d("Socket","Correct Info");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Error","Error Occurred");
            }
    }
}
