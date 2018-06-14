package com.google.firebase.codelab.mlkit;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by Abin.Thomas on 23-05-2018.
 */

public class LoginActivity extends Activity {

    ImageView imageView = null;
    Button btn_gpn= null;

    Uri imageUri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*imageView = (ImageView) findViewById(R.id.imageView1);
        btn_gpn = (Button)findViewById(R.id.btn_gpn);

        btn_gpn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                    startActivityForResult(intent, 0);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = null;
        switch(requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Bitmap bm = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), imageUri);
                        Bitmap imageOne = Bitmap.createScaledBitmap(bm, 640, 640, false);
                        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                        imageView.setImageBitmap(imageOne);
                        //String imageurl = getRealPathFromURI(imageUri);
                        //detectAndFrame(imageOne);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }*/
    }
}
