/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class SenderActivity extends ActionBarActivity {

    public String myId = "michael";
    public ArrayList<String> sons = new ArrayList< String> ();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        Button upload = (Button) findViewById(R.id.UploadId);
        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                selectImage();
                ParseAnalytics.trackAppOpenedInBackground(getIntent());
            }
        });
    }

    public boolean selectImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(photoPickerIntent, "Select picture to upload "), 10);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:

                if (resultCode == RESULT_OK) {
                    InputStream imageStream;
                    if (data.getData() != null) {
                        //If uploaded with Android Gallery (max 1 image)
                        Uri selectedImage = data.getData();

                        try {

                            imageStream = getContentResolver().openInputStream(selectedImage);
                            Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);

                            ImageView imView = (ImageView) findViewById(R.id.imUploadView);
                            imView.setImageURI(selectedImage);


                            uploadImage(yourSelectedImage, "sara");

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else if (Build.VERSION.SDK_INT >= 16) {
                        //If uploaded with the new Android Photos gallery
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item img = clipData.getItemAt(i);
                            Uri uriImg = img.getUri();
                            try {
                            imageStream = getContentResolver().openInputStream(uriImg);
                            Bitmap curImg = BitmapFactory.decodeStream(imageStream);
                            uploadImage(curImg, "sara");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        }
    }


    /**
     * Uploads an image to the main table in parse, with field "uploader" and "recipient"
     * @return
     */
    public boolean uploadImage(Bitmap bitmap, String receiverID) {
        Log.v("tag", "beforeImagePath");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Compress image to lower quality scale 1 - 100
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] image = stream.toByteArray();

        // Create the ParseFile
        ParseFile file = new ParseFile("image.jpeg", image);
        // Upload the image into Parse Cloud
        file.saveInBackground();

        // Create a New Class called "Image Table" in Parse
        ParseObject imgupload = new ParseObject("ImageTable");

        // Create a column named "Sender ID" and set the string
        imgupload.put("SenderID", myId);

        // Create a column named "Sender ID" and set the string
        imgupload.put("ReceiverID", receiverID);

//              // Create a column named "ImageFile" and insert the image
        imgupload.put("ImageFile", file);
//
        // Create the class and the columns
        imgupload.saveInBackground();

        // Show a simple toast message
        Toast.makeText(SenderActivity.this, "Image Uploaded",
                Toast.LENGTH_SHORT).show();
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sender, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean saveImg(String senderPath, String name, Bitmap bmp) {

        OutputStream fOut = null;
        File file = new File(senderPath, name); // the File to save to
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
        try {
            fOut.flush();
            fOut.close(); // do not forget to close the stream
            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * resize bitmap
     * @param image
     * @return
     */
    public Bitmap getResizedBitmap(Bitmap image) {
        int maxSize = 1300000;
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /**
     * create a directory

     * @param albumName - name of album
     * @return File - the new directory
     */
    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("test", "Directory not created");
        }
        Log.e("test", "Directory is at"+file.getAbsolutePath());
        return file;
    }

    public boolean retImg(String reciverID) {



        return true;
    }
}
