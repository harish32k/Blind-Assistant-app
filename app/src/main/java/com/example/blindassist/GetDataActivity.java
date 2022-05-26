package com.example.blindassist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class GetDataActivity extends AppCompatActivity {

    public HashMap<String, byte[]> imageMap;
    public Set<String> pendingImages;
    public DataSnapshot snapshot;
    public ArrayList<String> image_names;
    public String image_dir;
    public JsonObject outputJson;
    public ArrayList<JsonArray> boxes;
    public TextView statusTextView;
    public ProgressBar progressBar;

    public String getJsonFromSnapshot(DataSnapshot dataSnapshot) {
        Object object = dataSnapshot.getValue(Object.class);
        String json = new Gson().toJson(object);
        return json;
    }

    public JsonObject getJsonFromString(String jsonString) {
        jsonString = getJsonFromSnapshot(snapshot);
        JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
        return obj;
    }

    public void process_poly() {
        Intent intent = new Intent(GetDataActivity.this, OCRDisplay.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("image_names", image_names);
        intent.putExtra("json_data", getJsonFromSnapshot(snapshot));
        startActivity(intent);
    }

    public void process_boxes() {
        boxes = new ArrayList<>();
        JsonObject predictions = outputJson.getAsJsonObject("prediction");
        for(String image_name: image_names) {
            JsonArray jsonArray;
            try {
                jsonArray = predictions.getAsJsonArray(image_name);
                if(jsonArray == null) jsonArray = new JsonArray();
            } catch (NullPointerException e) {
                jsonArray = new JsonArray();
            }
            boxes.add(jsonArray);
            //Log.d("json-test", jsonArray.toString());
            Intent intent = new Intent(GetDataActivity.this, ObjectDisplay.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("image_names", image_names);
            intent.putExtra("json_data", getJsonFromSnapshot(snapshot));
            startActivity(intent);
        }
    }

    public void process_depth() {
        Log.d("depth-display", "checked");
        Intent intent = new Intent(GetDataActivity.this, DepthDisplay.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("image_names", image_names);
        startActivity(intent);
    }

    public void caption_starter() {
        outputJson = outputJson.get("prediction").getAsJsonObject();
        Intent intent = new Intent(GetDataActivity.this, CaptionDisplay.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("image_names", image_names);
        intent.putExtra("json_data", outputJson.toString());
        startActivity(intent);
    }

    public void process_obj_depth_boxes() {
        boxes = new ArrayList<>();
        JsonObject predictions = outputJson.getAsJsonObject("prediction");
        for(String image_name: image_names) {
            JsonArray jsonArray;
            try {
                jsonArray = predictions.getAsJsonArray(image_name);
                if(jsonArray == null) jsonArray = new JsonArray();
            } catch (NullPointerException e) {
                jsonArray = new JsonArray();
            }
            boxes.add(jsonArray);
            Log.d("json-test", jsonArray.toString());
            Intent intent = new Intent(GetDataActivity.this, ObjDepthDisplay.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("image_names", image_names);
            intent.putExtra("json_data", getJsonFromSnapshot(snapshot));
            startActivity(intent);
        }
    }


    public void post_download() {
        statusTextView.setText("Processing data, please wait...");
        String task = outputJson.get("task").getAsString();
        switch (task) {
            case "object":
                process_boxes();
                statusTextView.setText("Success!");
                progressBar.setVisibility(View.GONE);
                break;
            case "ocr":
                process_poly();
                statusTextView.setText("Success!");
                progressBar.setVisibility(View.GONE);
                break;
            case "depth":
                process_depth();
                statusTextView.setText("Success!");
                progressBar.setVisibility(View.GONE);
                break;
            case "caption":
                caption_starter();
                statusTextView.setText("Success!");
                progressBar.setVisibility(View.GONE);
                break;
            case "obj_depth":
                process_obj_depth_boxes();
                statusTextView.setText("Success!");
                progressBar.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }

    public void download_img(String img_name, StorageReference storageRef) throws IOException {
        statusTextView.setText("Downloading "+img_name+"...");
        StorageReference imgRef = storageRef.child(img_name+".jpg");
        File localFile = new File(image_dir, img_name+".jpg");
        imgRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("fb-download","Downloaded "+img_name+".jpg");
                statusTextView.setText("Downloaded "+img_name+"...");
                pendingImages.remove(img_name);
                if(pendingImages.isEmpty()) {
                    statusTextView.setText("Downloaded all images...");
                    Log.d("fb-download", "pendingImages: " + pendingImages.size() + " elements");
                    Log.d("fb-download", "Successfully completed downloading");
                    post_download();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_get_data);
        statusTextView = findViewById(R.id.statusTextView);
        progressBar = findViewById(R.id.getDataProgress);

        DatabaseReference mDatabase;
        image_dir = getExternalCacheDir()+"/blind_assistant_images";
        File dir = new File(image_dir);
        boolean dirCreated = dir.mkdir();

        Log.d("fb-download", "created dir: " + image_dir +", " + dirCreated);
        //delete all existing image files
        if (dir.isDirectory())
        {
            statusTextView.setText("deleting old images...");
            Log.d("fb-download", "dir exists, "+image_dir);
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
                Log.d("fb-download", "Deleted: "+children[i]);
            }
        }


        mDatabase = FirebaseDatabase.getInstance().getReference("recent");
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase-debug", "Error getting data", task.getException());
                }
                else {

                    statusTextView.setText("obtained model data...");
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    pendingImages = new HashSet<String>();
                    snapshot = task.getResult();
                    outputJson = getJsonFromString(getJsonFromSnapshot(snapshot));
                    image_names = (ArrayList<String>) snapshot.child("img_list").getValue();
                    for(String image_name: image_names) pendingImages.add(image_name);

                    Log.d("fb-download", "pendingImages: " + pendingImages.size() + " elements");
                    for(String image_name: image_names) {
                        try {
                            download_img(image_name, storageRef);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("fb-download", image_name);
                    }
                }
            }
        });
    }
}
