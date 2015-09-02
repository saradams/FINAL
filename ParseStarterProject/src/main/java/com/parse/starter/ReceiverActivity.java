/**
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class ReceiverActivity extends Activity {


    public String myId;
    public ArrayList<String> sons = new ArrayList< String> ();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        sons.add("sara");

        Button download = (Button) findViewById(R.id.DownloadId);
        download.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                for ( int i = 0; i < sons.size(); i++) {
                    Log.d("test", "start son: " +  sons.get(i));
                    downloadImages(sons.get(i));

                }
                Log.d("test", "FINITO LAPITITO :-)");
            }

        });

    }


    /**
     * Uploads an image to the main table in parse, with field "uploader" and "recipient"
     *
     * @return
     */
    public boolean downloadImages(String receiverID) {

        // Locate the class table named "ImageUpload" in Parse.com
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("ImageTable");
        query.whereEqualTo("ReceiverID", "sara");
        // Locate the objectId from the class

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objectList, ParseException e) {

                for (int i = 0; i < objectList.size(); i++){

                    ParseObject object = objectList.get(i);
                    ParseFile fileObject = object.getParseFile("ImageFile");

                    String senderID = object.getString("SenderID");
                    String fileName =  fileObject.getName();

                    object.deleteInBackground();

                    fileObject.getDataInBackground(new myGetDataCallback(senderID, fileName));
                    Log.d("test", "finished pic numer :" + i);
                }

            }

        });


        return true;
    }

    /**
     * create a directory
     *
     * @param albumName - name of album
     * @return File - the new directory
     */
    public static File getAlbumStorageDir(String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);

        return file;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_receiver, menu);
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
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * resize bitmap
     *
     * @param image
     * @return
     */
    public Bitmap getResizedBitmap(Bitmap image) {
        int maxSize = 1300000;
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public class myGetDataCallback implements GetDataCallback {

        private String senderId;
        private String fileName;


        public myGetDataCallback(String senderID, String fileName) {

            this.senderId = senderID;
            this.fileName = fileName;

        }


        @Override
        public void done(byte[] data, ParseException e) {

            if (e == null) {

                // Decode the Byte[] into // Bitmap
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                //TODO: resize!!!
                //bmp = getResizedBitmap(bmp);
                File relativePath = getAlbumStorageDir("KISS/" + senderId);
                saveImg(relativePath.getAbsolutePath(), fileName, bmp);


                TextView filename = (TextView) findViewById(R.id.imName);
                filename.setText(fileName);

                TextView fileAddress = (TextView) findViewById(R.id.imAddress);
                fileAddress.setText(relativePath.toString());


                // Get the ImageView from
                // main.xml
                ImageView image = (ImageView) findViewById(R.id.imDownloadView);

                // Set the Bitmap into the
                // ImageView
                image.setImageBitmap(bmp);


            } else {
                Log.d("test", "There was a problem downloading the data.");
            }
        }
    }
}
