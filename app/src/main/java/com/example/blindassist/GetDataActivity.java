package com.example.blindassist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class GetDataActivity extends AppCompatActivity {

    public HashMap<String, byte[]> imageMap;
    public Set<String> pendingImages;
    public DataSnapshot snapshot;
    public ArrayList<String> image_names;

    public String getJsonFromSnapshot(DataSnapshot dataSnapshot) {
        Object object = dataSnapshot.getValue(Object.class);
        String json = new Gson().toJson(object);
        return json;
    }

    public void download_img(String img_name, StorageReference storageRef) {
        StorageReference imgRef = storageRef.child(img_name+".jpg");
        final long THREE_MEGABYTES = 3*1024*1024;
        imgRef.getBytes(THREE_MEGABYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                imageMap.put(img_name, bytes);
                Log.d("fb-download","Downloaded "+img_name+".jpg");
                pendingImages.remove(img_name);
                if(pendingImages.isEmpty()) {
                    Log.d("fb-download", "imageMap: " + imageMap.size() + " elements");
                    Log.d("fb-download", "pendingImages: " + pendingImages.size() + " elements");
                    Log.d("fb-download", "Successfully completed downloading");
                    Intent intent = new Intent(GetDataActivity.this, OCRDisplay.class);
                    intent.putExtra("imageMap", imageMap);
                    intent.putExtra("image_names", image_names);
                    intent.putExtra("snapshot", (Parcelable) snapshot);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_data);
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference("recent");
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase-debug", "Error getting data", task.getException());
                }
                else {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    imageMap = new HashMap<>();
                    pendingImages = new HashSet<String>();

                    snapshot = task.getResult();
                    image_names = (ArrayList<String>) snapshot.child("img_list").getValue();

                    //Log.d("firebase-debug", String.valueOf(task.getResult()));

                    //ArrayList<String> image_names = (ArrayList<String>) task.getResult().getValue();
                    for(String image_name: image_names) pendingImages.add(image_name);
                    Log.d("fb-download", "imageMap: " + imageMap.size() + " elements");
                    Log.d("fb-download", "pendingImages: " + pendingImages.size() + " elements");
                    for(String image_name: image_names) {
                        download_img(image_name, storageRef);
                        Log.d("fb-download", image_name);
                    }
                    //Log.d("firebase-debug", String.valueOf(task.getResult()));
                }
            }
        });

    }
}