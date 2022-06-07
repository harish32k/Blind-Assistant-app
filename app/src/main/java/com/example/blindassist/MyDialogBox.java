package com.example.blindassist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.Locale;


public class MyDialogBox extends AppCompatDialogFragment implements CompoundButton.OnCheckedChangeListener {

    private CheckBox frontBox;
    private CheckBox rightBox;
    private CheckBox backBox;
    private CheckBox leftBox;
    private MyDialogBoxListener listener;

    private TextToSpeech mTTS;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater= getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);
        mTTS =  new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    mTTS.setLanguage(Locale.US);
                }
            }
        });
        builder.setView(view)
                .setTitle("direction")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        speak("cancelling task");
                    }
                })
                .setPositiveButton("accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String directions = "";
                        if(frontBox.isChecked()) directions += "front ";
                        if(rightBox.isChecked()) directions += "right ";
                        if(backBox.isChecked()) directions += "back ";
                        if(leftBox.isChecked()) directions += "left ";
                        listener.readSelections(directions);
                        speak("starting task");
                    }
                });

        frontBox = view.findViewById(R.id.frontBox);
        rightBox = view.findViewById(R.id.rightBox);
        backBox = view.findViewById(R.id.backBox);
        leftBox = view.findViewById(R.id.leftBox);

        frontBox.setOnCheckedChangeListener(this);
        rightBox.setOnCheckedChangeListener(this);
        backBox.setOnCheckedChangeListener(this);
        leftBox.setOnCheckedChangeListener(this);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (MyDialogBoxListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement MyDialogBoxListener");
            //e.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        String text = compoundButton.getText().toString();
        if(b) text += " enabled";
        else text += " disabled";
        speak(text);
    }

    public interface MyDialogBoxListener {
        void readSelections(String directions);
    }

    private void speak( String message){
        float pitch = 1f;
        float speed = 1.7f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(message,TextToSpeech.QUEUE_FLUSH,null);
    }

}
