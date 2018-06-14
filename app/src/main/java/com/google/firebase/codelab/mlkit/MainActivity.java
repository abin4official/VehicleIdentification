// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.codelab.mlkit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.codelab.mlkit.GraphicOverlay.Graphic;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudDocumentTextDetector;
import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudText;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.vistrav.ask.Ask;
import com.vistrav.ask.annotations.AskDenied;
import com.vistrav.ask.annotations.AskGranted;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import android.Manifest;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    private ImageView mImageView;
    private Button mButton;
    private Button mCloudButton;
    private Bitmap mSelectedImage;
    private GraphicOverlay mGraphicOverlay;
    // Max width (portrait mode)
    private Integer mImageMaxWidth;
    // Max height (portrait mode)
    private Integer mImageMaxHeight;

    private String searchNumber = "";
    private EditText txtvw;
    private Button button_get_details;
    private ProgressBar progressBar;

    private String vehicle_number = null;

    ImageView imageView = null;
    Button btn_gpn= null;

    Uri imageUri=null;

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 123;

    private TextView txtvw_descp,txtvw_vin,txtvw_engineno,txtvw_regdate,txtvw_location,txtvw_carmake,txtvw_carmodel,txtvw_enginesize,txtv_fueltype;
    private LinearLayout llouter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }

        mButton = findViewById(R.id.button_text);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runCloudTextRecognition();
            }
        });

        mImageView = findViewById(R.id.image_view);

        mButton = findViewById(R.id.button_text);
        //mCloudButton = findViewById(R.id.button_cloud_text);

        txtvw = findViewById(R.id.txtvw);
        button_get_details = findViewById(R.id.button_get_details);
        progressBar = findViewById(R.id.progressbar);

        llouter = findViewById(R.id.llouter);
        llouter.setVisibility(View.GONE);
        txtvw_descp = findViewById(R.id.txtvw_descp);
        txtvw_vin = findViewById(R.id.txtvw_vin);
        txtvw_engineno = findViewById(R.id.txtvw_engineno);
        txtvw_regdate = findViewById(R.id.txtvw_regdate);
        txtvw_location = findViewById(R.id.txtvw_location);
        txtvw_carmake = findViewById(R.id.txtvw_carmake);
        txtvw_carmodel = findViewById(R.id.txtvw_carmodel);
        txtvw_enginesize = findViewById(R.id.txtvw_enginesize);
        txtv_fueltype = findViewById(R.id.txtv_fueltype);

        mGraphicOverlay = findViewById(R.id.graphic_overlay);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTextRecognition();
            }
        });

        button_get_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(txtvw.getText().toString().length()!=0) {
                    GetVehicleDetails getDetails = new GetVehicleDetails();
                    getDetails.execute();
                }
                else {
                    Toast.makeText(MainActivity.this,"Enter a vehicle number",Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView1);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePicker();
            }
        });

    }


    public void imagePicker(){
        CharSequence colors[] = new CharSequence[] {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choose Image");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                Log.d("###CLICK",""+which);
                if(which==0) {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photo));
                    //imageUri = Uri.fromFile(photo);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    imageUri = FileProvider.getUriForFile(MainActivity.this, MainActivity.this.getApplicationContext().getPackageName() + BuildConfig.APPLICATION_ID + ".provider", photo);
                    startActivityForResult(intent, 0);
                }

                else if(which==1){
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, 1);
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(MainActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = null;
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    //Uri imageUri;
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    //ImageView imageView = (ImageView) findViewById(R.id.IMAGE);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);

                        imageView.setImageBitmap(bitmap);
                        Toast.makeText(this, "This file: "+selectedImage.toString(),
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                                .show();
                        Log.e("Camera", e.toString());
                    }
                }


            case 1:
                if(resultCode == RESULT_OK){
                    if(data.getData()!=null){
                        uri = data.getData();
                    }
                    try {
                        Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        int finalHeight, finalWidth;
                        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                        finalHeight = imageView.getMeasuredHeight();
                        finalWidth = imageView.getMeasuredWidth();

                        //Bitmap firstImage = Bitmap.createScaledBitmap(bm, 1200, 640, false);
                        Bitmap firstImage = Bitmap.createScaledBitmap(bm, finalWidth, finalHeight, false);


                        imageView.setImageBitmap(firstImage);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        //detectAndFrame(firstImage);
                        mSelectedImage = firstImage;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void runTextRecognition() {
        if(mSelectedImage!= null){
            progressBar.setVisibility(View.VISIBLE);
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mSelectedImage);
            FirebaseVisionTextDetector detector = FirebaseVision.getInstance()
                    .getVisionTextDetector();
            mButton.setEnabled(false);
            detector.detectInImage(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText texts) {
                                    mButton.setEnabled(true);
                                    processTextRecognitionResult(texts);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    mButton.setEnabled(true);
                                    e.printStackTrace();
                                }
                            });

        }
        else {
            Toast.makeText(MainActivity.this,"Choose an image",Toast.LENGTH_LONG).show();
        }
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.Block> blocks = texts.getBlocks();
        if (blocks.size() == 0) {
            showToast("No text found");
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);
                    Log.e("###TEXT22",""+blocks.get(0).getText());
                    searchNumber = blocks.get(0).getText();
                    txtvw.setText(searchNumber);
                    vehicle_number = txtvw.getText().toString();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void runCloudTextRecognition() {
        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                        .setMaxResults(15)
                        .build();
        mCloudButton.setEnabled(false);
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mSelectedImage);
        FirebaseVisionCloudDocumentTextDetector detector = FirebaseVision.getInstance()
                .getVisionCloudDocumentTextDetector(options);
        detector.detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionCloudText>() {
                            @Override
                            public void onSuccess(FirebaseVisionCloudText texts) {
                                mCloudButton.setEnabled(true);
                                processCloudTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                mCloudButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    private void processCloudTextRecognitionResult(FirebaseVisionCloudText text) {
        // Task completed successfully
        if (text == null) {
            showToast("No text found");
            return;
        }
        mGraphicOverlay.clear();
        List<FirebaseVisionCloudText.Page> pages = text.getPages();
        for (int i = 0; i < pages.size(); i++) {
            FirebaseVisionCloudText.Page page = pages.get(i);
            List<FirebaseVisionCloudText.Block> blocks = page.getBlocks();
            for (int j = 0; j < blocks.size(); j++) {
                List<FirebaseVisionCloudText.Paragraph> paragraphs = blocks.get(j).getParagraphs();
                for (int k = 0; k < paragraphs.size(); k++) {
                    FirebaseVisionCloudText.Paragraph paragraph = paragraphs.get(k);
                    List<FirebaseVisionCloudText.Word> words = paragraph.getWords();
                    for (int l = 0; l < words.size(); l++) {
                        Graphic cloudTextGraphic = new CloudTextGraphic(mGraphicOverlay, words
                                .get(l));
                        mGraphicOverlay.add(cloudTextGraphic);
                    }
                }
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Functions for loading images from app assets.

    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxWidth() {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = mImageView.getWidth();
        }

        return mImageMaxWidth;
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxHeight() {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight =
                    mImageView.getHeight();
        }

        return mImageMaxHeight;
    }

    // Gets the targeted width / height.
    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        mGraphicOverlay.clear();
        switch (position) {
            case 0:
                mSelectedImage = getBitmapFromAsset(this, "Please_walk_on_the_grass.jpg");

                break;
            case 1:
                // Whatever you want to happen when the second item gets selected
                mSelectedImage = getBitmapFromAsset(this, "non-latin.jpg");
                break;
            case 2:
                // Whatever you want to happen when the thrid item gets selected
                mSelectedImage = getBitmapFromAsset(this, "nl2.jpg");
                break;
            case 3:
                // Whatever you want to happen when the thrid item gets selected
                mSelectedImage = getBitmapFromAsset(this, "car.jpg");
                break;
            case 4:
                // Whatever you want to happen when the thrid item gets selected
                mSelectedImage = getBitmapFromAsset(this, "car2.jpg");
                break;
        }
        if (mSelectedImage != null) {
            // Get the dimensions of the View
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            // Determine how much to scale down the image
            float scaleFactor =
                    Math.max(
                            (float) mSelectedImage.getWidth() / (float) targetWidth,
                            (float) mSelectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            mSelectedImage,
                            (int) (mSelectedImage.getWidth() / scaleFactor),
                            (int) (mSelectedImage.getHeight() / scaleFactor),
                            true);

            mImageView.setImageBitmap(resizedBitmap);
            mSelectedImage = resizedBitmap;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream is;
        Bitmap bitmap = null;
        try {
            is = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public class GetVehicleDetails extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            txtvw_descp.setText("");
            txtvw_vin.setText("");
            txtvw_engineno.setText("");
            txtvw_regdate.setText("");
            txtvw_location.setText("");
            txtvw_carmake.setText("");
            txtvw_carmodel.setText("");
            txtvw_enginesize.setText("");
            txtv_fueltype.setText("");
        }

        @Override
        protected String doInBackground(String... strings) {

            try {

            vehicle_number = txtvw.getText().toString();
            String query = URLEncoder.encode(vehicle_number, "utf-8");
            String urlStr = "https://www.regcheck.org.uk/api/reg.asmx/CheckIndia?RegistrationNumber="+query+"&username=abin05" ;

            //String urlStr = "www.regcheck.org.uk/api/reg.asmx/CheckIndia?RegistrationNumber=KL 35 6148&username=AbinThomas";
            String content = null, line;


                URL mUrl = new URL(urlStr);
                HttpURLConnection httpConnection = (HttpURLConnection) mUrl.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Content-length", "0");
                httpConnection.setUseCaches(false);
                httpConnection.setAllowUserInteraction(false);
                httpConnection.setConnectTimeout(100000);
                httpConnection.setReadTimeout(100000);


                httpConnection.connect();

                int responseCode = httpConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();

                    Log.e("###RESP1",""+sb.toString());

                    return sb.toString();

                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("###EXC1",""+e.getMessage().toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("###EXC2",""+ ex.getCause().toString());
            }
            return null;
        }

        protected void onPostExecute(String getResponse) {
            progressBar.setVisibility(View.INVISIBLE);
            String jsonData = null;
            if(getResponse!=null){
                Log.e("###RES",""+getResponse.toString());

                XmlPullParserFactory factory = null;
                try {
                    factory = XmlPullParserFactory.newInstance();

                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(getResponse));
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                        //System.out.println("Start document");
                    } else if (eventType == XmlPullParser.START_TAG) {
                        //System.out.println("Start tag " + xpp.getName());
                    } else if (eventType == XmlPullParser.END_TAG) {
                        //System.out.println("End tag " + xpp.getName());
                    } else if ((eventType == XmlPullParser.TEXT)&&(xpp.getText().contains("Description"))) {
                        jsonData = xpp.getText();
                        System.out.println("Text " + xpp.getText());
                    }
                    eventType = xpp.next();
                }
                //System.out.println("End document");
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.e("###RES",""+jsonData.toString());



                try {

                    llouter.setVisibility(View.VISIBLE);

                    String carDetails= null;
                    StringBuilder stringBuilder = new StringBuilder();

                    JSONObject jsonObj = new JSONObject(jsonData);

                    if(jsonObj.has("Description")) {
                        String descp = jsonObj.getString("Description");
                        Log.e("### DESC",""+descp.toString());
                        stringBuilder.append("Car Details are ; "+ System.getProperty("line.separator"));
                        stringBuilder.append("Description : "+descp.toString()+ System.getProperty("line.separator"));
                    txtvw_descp.setText(descp.toString());
                    } else {
                        Log.e("### EXCP Description", "No Description");
                    }

                    if(jsonObj.has("VechileIdentificationNumber")) {
                        String VechileIdentificationNumber = jsonObj.getString("VechileIdentificationNumber");
                        Log.e("### VIN",""+VechileIdentificationNumber.toString());
                        stringBuilder.append("VIN : "+VechileIdentificationNumber.toString());
                        txtvw_vin.setText(VechileIdentificationNumber.toString());
                    } else {
                        Log.e("### EXCP VIN", "No VIN");
                    }

                    if(jsonObj.has("Colour")) {
                        String Colour = jsonObj.getString("Colour");
                        Log.e("### Colour", "" + Colour.toString());
                        stringBuilder.append("Colour : " + Colour.toString() + System.getProperty("line.separator"));
                    } else {
                        Log.e("### EXCP Colour", "No Color");
                    }

                    if(jsonObj.has("EngineNumber")) {
                        String EngineNumber = jsonObj.getString("EngineNumber");
                        Log.e("### EngineNumber", "" + EngineNumber.toString());
                        stringBuilder.append("EngineNumber : " + EngineNumber.toString() + System.getProperty("line.separator"));
                        txtvw_engineno.setText(EngineNumber.toString());
                    } else {
                        Log.e("### EXCP EngineNumber", "No EngineNumber");
                    }

                    if(jsonObj.has("RegistrationDate")) {
                        String RegistrationDate = jsonObj.getString("RegistrationDate");
                        Log.e("### RegistrationDate",""+RegistrationDate.toString());
                        stringBuilder.append("RegistrationDate : "+RegistrationDate.toString()+ System.getProperty("line.separator"));
                        txtvw_regdate.setText(RegistrationDate.toString());
                    } else {
                        Log.e("### EXCP RegtnDate", "No RegistrationDate");
                    }

                    if(jsonObj.has("Location")) {
                    String Location = jsonObj.getString("Location");
                    Log.e("### Location",""+Location.toString());
                    stringBuilder.append("Location : "+Location.toString()+ System.getProperty("line.separator"));
                    txtvw_location.setText(Location.toString());
                    } else {
                        Log.e("### EXCP Location", "No Location");
                    }

                    if(jsonObj.has("CarMake")) {
                        JSONObject carMakeObj = jsonObj.getJSONObject("CarMake");
                        if(carMakeObj.has("CurrentTextValue")) {
                            String carMakeName = carMakeObj.getString("CurrentTextValue");
                            Log.e("### carMakeName", "" + carMakeName.toString());
                            stringBuilder.append("carMakeName : " + carMakeName.toString() + System.getProperty("line.separator"));
                            txtvw_carmake.setText(carMakeName.toString());
                        } else {
                            Log.e("### EXCP carMakeName", "No carMakeName");
                        }
                    }else{
                        Log.e("### EXCP carMakeName", "No carMakeName");
                    }

                    if(jsonObj.has("CarModel")) {
                        JSONObject carModelObj = jsonObj.getJSONObject("CarModel");
                        if(carModelObj.has("CurrentTextValue")) {
                            String carModelName = carModelObj.getString("CurrentTextValue");
                            Log.e("### carModelName", "" + carModelName.toString());
                            stringBuilder.append("carModelName : " + carModelName.toString() + System.getProperty("line.separator"));
                            if(carModelName.toString()!="null"){
                                txtvw_carmodel.setText(carModelName.toString());
                            }
                        }else {
                            Log.e("### EXCP carModelObj", "No carModelObj");
                            txtvw_carmodel.setText("");
                        }
                    }else{
                        Log.e("### EXCP carModelObj", "No carModelObj");
                        txtvw_carmodel.setText("");
                    }

                    JSONObject engineSize = jsonObj.getJSONObject("EngineSize");
                    String carEngineSize = engineSize.getString("CurrentTextValue");
                    Log.e("### carEngineSize",""+carEngineSize.toString());
                    stringBuilder.append("carEngineSize : "+carEngineSize.toString()+ System.getProperty("line.separator"));
                    if(carEngineSize.toString()!="null") {
                        txtvw_enginesize.setText(carEngineSize.toString());
                    }

                    JSONObject makeDescription = jsonObj.getJSONObject("MakeDescription");
                    String carMakeDescription = makeDescription.getString("CurrentTextValue");
                    Log.e("### carMakeDescription",""+carMakeDescription.toString());
                    stringBuilder.append("carMakeDescription : "+carMakeDescription.toString()+ System.getProperty("line.separator"));

                    JSONObject modelDescription = jsonObj.getJSONObject("ModelDescription");
                    String carmodelDescription = modelDescription.getString("CurrentTextValue");
                    Log.e("### carmodelDescription",""+carmodelDescription.toString());
                    //stringBuilder.append("carmodelDescription : "+carmodelDescription.toString()+ System.getProperty("line.separator"));

                    if(jsonObj.has("FuelType")) {
                    JSONObject fuelType = jsonObj.getJSONObject("FuelType");
                    String carFuelType = fuelType.getString("CurrentTextValue");
                    Log.e("### carFuelType",""+carFuelType.toString());
                    stringBuilder.append("carFuelType : "+carFuelType.toString()+ System.getProperty("line.separator"));
                    txtv_fueltype.setText(carFuelType.toString());
                    } else {
                        Log.e("### EXCP FuelType", "No FuelType");
                    }

                    carDetails = stringBuilder.toString();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
