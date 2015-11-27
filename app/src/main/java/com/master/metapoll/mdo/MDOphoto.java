package com.master.metapoll.mdo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Enables the user to take a photo
 * Created by ich on 26.11.15.
 */
public class MDOphoto extends MDobject{
    private static final String TAG = "MDOphoto";
    private LinearLayout photoLayout;
    private Button addPhotoButton;
    static final int REQUEST_TAKE_PHOTO = 1;
    private ViewGroup vg;
    String mCurrentPhotoPath;
    File photoFile = null;

    public MDOphoto(Context context, Node node, int pageNo) {
        super(context, node, pageNo);



    }

    /**
     * Has to be implemented by the subclass. returns the View for the GUI
     *
     * @return View with (input) elements encapsulated
     */
    @Override
    public View getView() {
        photoLayout = new LinearLayout(context);
        addPhotoButton = new Button(context);
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        addPhotoButton.setText("Take a photo");
        photoLayout.addView(addPhotoButton);
//        photoLayout.
        return photoLayout;
    }

    /**
     * Has to be implemented by the subclass. Pulls the data from the generated input elements
     * and saves them to variable(s)
     */
    @Override
    public void save() {

    }

    /**
     * Has to be implemented by the subclass.
     * Should clear all graphical objects.
     */
    @Override
    protected void clear() {

    }

    /**
     * Has to be implemented by the subclass. adds the user input data from the variables to
     * a ContentValues object and returns it.
     *
     * @param values Holds the values of all MDobjects in the same poll.
     * @return Same Object as came in, but with additional data.
     */
    @Override
    public ContentValues db_save(ContentValues values) {
        return null;
    }

    /**
     * Has to be implemented by the subclass.
     * Creates a database suitable name from the name variable. In the database it's used as
     * column name.
     * This function should call clear() in the end when implemented.
     *
     * @return
     */
    @Override
    public String getDbString() {
        return null;
    }


    /**
     * Dispatches a take picture Intent.
     *
     * from: http://developer.android.com/intl/vi/training/camera/photobasics.html
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
               Log.e(TAG, "error.");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
        else {
            Toast toast = Toast.makeText(context, "Sorry, no camera app found.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * from: http://developer.android.com/intl/vi/training/camera/photobasics.html
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    /**
     * Callback function. The embedded activity calls the callback function
     * callBack(int requestCode, int resultCode, Intent data, int page)
     * in MDPoll. The function dispatches a call to all MDobject subclasses that
     * are on the corresponding page. The functions who implemented the callback
     * the call and can decide by request code if it's relevant or not.
     *
     *  from: http://developer.android.com/intl/vi/training/camera/photobasics.html
     *
     * @param requestCode given request code in call dispatchTakePictureEvent()
     * @param resultCode negative if success, positive if no success.
     * @param data Contains a Thumbnail of the taken picture.
     */
    public void activityCallback(int requestCode, int resultCode, Intent data) {

        Log.d(TAG,"callback called. request code: " + requestCode + " result code: " + resultCode);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(photoFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
//            if(data.getData()==null){
//                imageBitmap = (Bitmap)data.getExtras().get("data");
//            }else{
//                try {
//                    imageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                ImageView mImageView = new ImageView(context);
                mImageView.setImageBitmap(imageBitmap);
                photoLayout.addView(mImageView);
                photoLayout.invalidate();
            photoLayout.refreshDrawableState();
            }



    }

    public static String getXmlName() {
        return "photoElement";
    }
}
