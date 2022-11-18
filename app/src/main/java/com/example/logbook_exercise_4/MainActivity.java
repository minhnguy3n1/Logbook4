package com.example.logbook_exercise_4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.CAMERA;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int REQUEST_IMAGE_CAPTURE = 102;
    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;

    private FusedLocationProviderClient locationClient;

    ImageView imageCatured;
    Button captureButton, backwardButton, forwardButton;
    TextView txtLocation;
    String currentPhotoPath;
    int index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageCatured = findViewById(R.id.imageView);
        captureButton = findViewById(R.id.captureButton);
        backwardButton = findViewById(R.id.backward_button);
        forwardButton = findViewById(R.id.forward_button);
        txtLocation = findViewById(R.id.txtLocation);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        Glide.with(MainActivity.this).load(displayLastImage()).into(imageCatured);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCameraPermissions();
                Database myDB = new Database(MainActivity.this);
                myDB.addImage(currentPhotoPath);
                Cursor cursor = myDB.getImage();
                cursor.moveToLast();
                index = cursor.getPosition();
                Glide.with(MainActivity.this).load(displayLastImage()).into(imageCatured);
                showLocation();

            }
        });


        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(MainActivity.this).load(backwardButton()).into(imageCatured);
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(MainActivity.this).load(forwardButton()).into(imageCatured);
            }
        });
    }

    String displayLastImage(){
        Database myDB = new Database(MainActivity.this);
        Cursor cursor = myDB.getImage();
        String location;

//
        cursor.moveToLast();
        location = cursor.getString(1);
        index= cursor.getPosition();
        return location;
    }


    String forwardButton(){
        Database myDB = new Database(MainActivity.this);
        Cursor cursor = myDB.getImage();
        String url;

        cursor.moveToLast();
        int last_index = cursor.getPosition();
        if(index == last_index){

            cursor.moveToFirst();
            index = cursor.getPosition();
        } else {
            index++;
            cursor.moveToPosition(index);

        }
        url = cursor.getString(1);
        return url;
    }

    String backwardButton(){
        Database myDB = new Database(MainActivity.this);
        Cursor cursor = myDB.getImage();
        String url;

        if(index == 0){
            cursor.moveToLast();
            index = cursor.getPosition();
        } else {
            index--;
            cursor.moveToPosition(index);
        }
        url = cursor.getString(1);
        return url;
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            dispatchTakePictureIntent();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
        if(requestCode == CAMERA_PERM_CODE){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, "Permission Granted!",
                        Toast.LENGTH_SHORT
                ).show();
                dispatchTakePictureIntent();

            } else {
                Toast.makeText(MainActivity.this, "Permission Denied!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        if(requestCode == REQUEST_PERMISSION_FINE_LOCATION){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(MainActivity.this, "Permission Granted!",
                        Toast.LENGTH_SHORT
                ).show();
                showLocation();
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied!",
                        Toast.LENGTH_SHORT
                ).show();
                txtLocation.setText("Location permission not granted");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE){
            File file = new File(currentPhotoPath);
//            Bitmap image = (Bitmap) data.getExtras().get("data");
            imageCatured.setImageURI(Uri.fromFile(file));
            Log.d("tag","ABsolute Url is Image is "+Uri.fromFile(file));
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
        }
    }

    @SuppressLint("MissingPermission")
    private void showLocation() {
        locationClient.getLastLocation().addOnSuccessListener(this,
                new OnSuccessListener<Location>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null){
                            txtLocation.setText("Current location is: " +
                                    "\n Latitude: " + location.getLatitude() +
                                    "\n Longitude: " + location.getLongitude()
                            );
                        }
                    }
                }
        );
    }
}