package com.example.logbook4;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    ImageView imageView;
    Button buttonBack, buttonNext, buttonAdd, cameraBtn;
    String currentPhotoPath;
    EditText inputUrl;
    List<URLImage> imageList;
    int index = 0;
    ImageDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        buttonBack = findViewById(R.id.backward);
        buttonNext = findViewById(R.id.forward);
        inputUrl = findViewById(R.id.etURL);
        buttonAdd = findViewById(R.id.addLink);
        cameraBtn = findViewById(R.id.cameraBtn);
        imageList = new ArrayList<>();

        database = new ImageDatabase(MainActivity.this);
        Cursor cursor = database.readImgUrl();
        while (cursor.moveToNext()){
            imageList.add(new URLImage(cursor.getString(1)));
        }

        load();
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index--;
                inputUrl.getText().clear();
                if(index <= 0)
                    index = 0;
                load();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index++;
                inputUrl.getText().clear();
                if(index == imageList.size()) {
                    index = 0;
                    Toast.makeText(MainActivity.this, "Back to the first image", Toast.LENGTH_SHORT).show();
                }
                load();
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String linkWebUrl = "(https:?\\/\\/.*\\.(?:png|jpg))";
                String urlImage = inputUrl.getText().toString();
                if (inputUrl.getText() != null && inputUrl.getText().toString().matches(linkWebUrl)) {
                    database = new ImageDatabase(MainActivity.this);
                    database.addImageURL(urlImage);
                    imageList.add(new URLImage(urlImage));
                    Toast.makeText(MainActivity.this, "Add success", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "The path has not been entered or the path is syntactically incorrect.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissions();
            }
        });
    }



    public void load(){
        Glide.with(MainActivity.this)
                .load(imageList.get(index).urlImage)
                .centerCrop()
                .into(imageView);
    }
    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(MainActivity.this, "Required to use Camera", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                imageView.setImageURI(Uri.fromFile(f));
                Log.d("tag", "ABsolute Url of Image is" + Uri.fromFile(f));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMđ_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        database = new ImageDatabase(MainActivity.this);
        database.addImageURL(currentPhotoPath);
        Cursor cursor = database.readImgUrl();
        imageList = new ArrayList<>();
        while (cursor.moveToNext()){
            imageList.add(new URLImage(cursor.getString(1)));
        }
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException ex) {
                // Error occurred while creating this File

            }
            // Continue only if the File was successfully created
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }
}