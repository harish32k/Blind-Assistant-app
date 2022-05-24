package com.example.blindassist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;

public class MyDialogBox extends AppCompatDialogFragment {

    private CheckBox frontBox;
    private CheckBox rightBox;
    private CheckBox backBox;
    private CheckBox leftBox;
    private MyDialogBoxListener listener;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater= getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        builder.setView(view)
                .setTitle("direction")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

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
                    }
                });

        frontBox = view.findViewById(R.id.frontBox);
        rightBox = view.findViewById(R.id.rightBox);
        backBox = view.findViewById(R.id.backBox);
        leftBox = view.findViewById(R.id.leftBox);

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

    public interface MyDialogBoxListener {
        void readSelections(String directions);
    }

}
