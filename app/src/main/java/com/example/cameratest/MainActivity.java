package com.example.cameratest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Intent.ACTION_CREATE_DOCUMENT;

public class MainActivity extends AppCompatActivity {

    static final int CAM_REQUEST = 1;
    static final int ADD_TO_GALLERY_REQUEST = 2;
    private ImageView image;
    private String imageFilePath;
    private String imageFileName;
    private String imageFileDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        image = findViewById(R.id.image);
        setupButtons();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAM_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED)
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAM_REQUEST);

                } else {
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAM_REQUEST && resultCode == RESULT_OK) {
            File imgFile = new  File(imageFilePath);
            if(imgFile.exists()) {
                image.setImageURI(Uri.fromFile(imgFile));
            }
        }
        else if (requestCode == ADD_TO_GALLERY_REQUEST && resultCode == RESULT_OK) {
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(), imageFilePath, imageFileName, imageFileDesc);
                Toast toast =
                        Toast.makeText(getApplicationContext(),
                                "Photo file successfuly created",
                                Toast.LENGTH_SHORT);
                toast.show();
            } catch (Exception e){
                Toast toast =
                        Toast.makeText(getApplicationContext(),
                                "Photo file can't be created, please try again",
                                Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private void setupButtons () {
        findViewById(R.id.btn_show_camera).setOnClickListener(
                new showCamera());

        findViewById(R.id.btn_save_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToGallery();
            }
        });

        findViewById(R.id.btn_go_to_configuration).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogToGoToConfiguration();
            }
        });

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        imageFileDesc = imageFileName;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private void addToGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            startActivityForResult(intent, ADD_TO_GALLERY_REQUEST);
        } catch (ActivityNotFoundException e) {
            Log.d("AAAAAAA", e.getMessage());
        }

    }

    private void showDialogToGoToConfiguration () {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setView(R.layout.error_dialog);
        adb.setTitle("Permissions manager");
        adb.setMessage("Do you want yo go to Android configuration to grant access to this application?");
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent conf = new Intent(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS));
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                conf.setData(uri);
                startActivity(conf);
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        adb.show();
    }

    class showCamera implements  Button.OnClickListener {
        public void onClick(View view) {
            if ((ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
            || (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
            || (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED))
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)
                        || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showDialogToGoToConfiguration();
                    //ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},0);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            CAM_REQUEST);
                }
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra( MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File pictureFile;
                    try {
                        pictureFile = createImageFile();
                        if (pictureFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                    "com.example.android.fileprovider",
                                    pictureFile);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(cameraIntent, CAM_REQUEST);
                        }
                    } catch (IOException ex) {
                        Toast toast =
                                Toast.makeText(getApplicationContext(),
                                        "Photo file can't be created, please try again",
                                        Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

            }
        }
    }

}
