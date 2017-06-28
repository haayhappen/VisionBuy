/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.cloudvision;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.WebEntity;
import com.google.api.services.vision.v1.model.WebPage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyBR1m6mwGDpaS36TDAKaEtVkpwFlOCsRII";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private TextView mImageDetails;
    private ImageView mMainImage;

    Context con;

    MaterialDialog.Builder builder;
    MaterialDialog dialog;
    private RecyclerView recyclerView;
    private CustomIntroAdapter adapter;
    private List<IntroCard> introCardList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        con = this;

        initCollapsingToolbar();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        introCardList = new ArrayList<>();
        adapter = new CustomIntroAdapter(this, introCardList);

//        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
//        recyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        prepareAlbums();

        try {
            Glide.with(this).load(R.drawable.cover).into((ImageView) findViewById(R.id.backdrop));
        } catch (Exception e) {
            e.printStackTrace();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setMessage(R.string.dialog_select_prompt)
                        .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startGalleryChooser();
                            }
                        })
                        .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                startCamera();


                            }
                        });
                builder.create().show();

            }
        });

        //mImageDetails = (TextView) findViewById(R.id.image_details);
        //mMainImage = (ImageView) findViewById(R.id.main_image);
    }


    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    /**
     * Adding few albums for testing
     */
    private void prepareAlbums() {
        int[] covers = new int[]{
                R.drawable.album1,
                R.drawable.album2,
                R.drawable.album3,
                R.drawable.album4,
                R.drawable.album5,
                R.drawable.album6,
                R.drawable.album7,
                R.drawable.album8,
                R.drawable.album9,
                R.drawable.album10,
                R.drawable.album11};

        IntroCard a = new IntroCard("True Romance", covers[0]);
        introCardList.add(a);

        a = new IntroCard("Xscpae", covers[1]);
        introCardList.add(a);

        a = new IntroCard("Maroon 5", covers[2]);
        introCardList.add(a);

        a = new IntroCard("Born to Die", covers[3]);
        introCardList.add(a);

        a = new IntroCard("Honeymoon", covers[4]);
        introCardList.add(a);

        a = new IntroCard("I Need a Doctor", covers[5]);
        introCardList.add(a);

        a = new IntroCard("Loud", covers[6]);
        introCardList.add(a);

        a = new IntroCard("Legend", covers[7]);
        introCardList.add(a);

        a = new IntroCard("Hello", covers[8]);
        introCardList.add(a);

        a = new IntroCard("Greatest Hits", covers[9]);
        introCardList.add(a);

        adapter.notifyDataSetChanged();
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    //throws only for testing AMAZON API
    public void startCamera() {

        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {

                    startCamera();

                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri), 1200);

                //TODO ONLY FOR TESTING
                callCloudVision(bitmap);

                //mMainImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        //mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected void onPreExecute() {
                builder = new MaterialDialog.Builder(con)
                        .title("Searching for products")
                        .content("This should only take a few seconds, depending on your internet connection")
                        .progress(true, 0)
                        .cancelable(false);

                dialog = builder.build();
                dialog.show();

                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Object... params) {
                try {


                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                        /**
                         * We override this so we can inject important identifying fields into the HTTP
                         * headers. This enables use of a restricted cloud platform API key.
                         */
                        @Override
                        protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                throws IOException {
                            super.initializeVisionRequest(visionRequest);

                            String packageName = getPackageName();
                            visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                            String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                            visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                        }
                    };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {
                        {
                            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                            // Add the image
                            Image base64EncodedImage = new Image();
                            // Convert the bitmap to a JPEG
                            // Just in case it's a format that Android understands but Cloud Vision
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                            byte[] imageBytes = byteArrayOutputStream.toByteArray();

                            // Base64 encode the JPEG
                            base64EncodedImage.encodeContent(imageBytes);
                            annotateImageRequest.setImage(base64EncodedImage);

                            // add the features we want
                            /*
                            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                                Feature labelDetection = new Feature();
                                labelDetection.setType("LABEL_DETECTION");
                                labelDetection.setMaxResults(20);
                                add(labelDetection);
                            }}); */
                            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                                Feature webDetection = new Feature();
                                webDetection.setType("WEB_DETECTION");
                                webDetection.setMaxResults(10);
                                add(webDetection);
                                Feature logoDetection = new Feature();
                                logoDetection.setType("LOGO_DETECTION");
                                logoDetection.setMaxResults(5);
                                add(logoDetection);
                            }});

                            // Add the list of one thing to the request
                            add(annotateImageRequest);
                        }
                    });

                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                dialog.dismiss();
                // mImageDetails.setText(result);

                List<String> keywords = new ArrayList<String>(Arrays.asList(result.split(" ")));
                Intent intent = new Intent(MainActivity.this, MasterActivity.class);
                intent.putStringArrayListExtra("keys", (ArrayList<String>) keywords);
                startActivity(intent);
                //TODO Call Amazon api here with keywords
            }
        }.execute();
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String logo = "";
        String logoMessage = "";
        //Float logoConfidence = 0f;
        Float logoScore = 0f;
        String message = "I found these things:\n\n";
        List<String> keywords = new ArrayList<String>();
        List<String> possibleSearchEntries = new ArrayList<>();
        List<Float> possibleSearchEntryScores = new ArrayList<>();

        List<EntityAnnotation> logoAnnotationList = response.getResponses().get(0).getLogoAnnotations();
        if (logoAnnotationList != null) {
            EntityAnnotation logoAnnotation = logoAnnotationList.get(0);
            if (logoAnnotation != null || !logoAnnotation.getDescription().equals("")) {
                //Logo is valid
                logo = logoAnnotation.getDescription();
                //logoConfidence = logoAnnotation.getConfidence();
                logoScore = logoAnnotation.getScore();
                logoMessage = "Found Logo of: " + logo + " with score of: " + logoScore.toString() + "\n\n";
            }
        }


        //logoMessage returns logo details


///////////////////////////////////////////////////////////////////////////////////////
        /**
         * Web pages containing the matching images from the Internet.
         * The value may be {@code null}.
         */
        List<WebPage> pages = response.getResponses().get(0).getWebDetection().getPagesWithMatchingImages();
        if (pages != null) {
            message += "Pages with matching images: \n\n\n";
            for (WebPage page : pages) {
                message += String.format(Locale.US, "%.3f: %s", page.getScore(), page.getUrl());
                message += "\n";
            }
            message += "\n\n";
        } else {
            message += "NO Pages with matching images!\n\n\n";
        }
///////////////////////////////////////////////////////////////////////////////////////

        /**
         * Partial matching images from the Internet. Those images are similar enough to share some key-
         * point features. For example an original image will likely have partial matching for its crops.
         * The value may be {@code null}.
        List<WebImage> partialMatchingImages = response.getResponses().get(0).getWebDetection().getPartialMatchingImages();
        if (partialMatchingImages != null) {
            message += "Pages with partial matching images: \n\n\n";
            for (WebImage image : partialMatchingImages) {
                message += String.format(Locale.US, "%.3f: %s", image.getScore(), image.getUrl());
                message += "\n";
            }
            message += "\n\n\n";
        } else {
            message += "No Pages with partially matching images!\n\n\n";
        }
*/
        ///////////////////////////////////////////////////////////////////////////////////////
        /**
         * The visually similar image results.
         * The value may be {@code null}.

        List<WebImage> visuallySimilarImages = response.getResponses().get(0).getWebDetection().getVisuallySimilarImages();
        if (visuallySimilarImages != null) {
            message += "Pages with similar images: \n\n\n";
            for (WebImage image : visuallySimilarImages) {
                message += String.format(Locale.US, "%.3f: %s", image.getScore(), image.getUrl());
                message += "\n";
            }
            message += "\n\n\n";
        } else {
            message += "No Pages with similar images!\n\n\n";
        }*/
        ///////////////////////////////////////////////////////////////////////////////////////
        /**
         * Deduced entities from similar images on the Internet.
         * The value may be {@code null}.
         */
        List<WebEntity> webEntities = response.getResponses().get(0).getWebDetection().getWebEntities();
        if (webEntities != null) {
            message += "Deduced entities from similar images on the Internet: \n\n\n";
            for (WebEntity entity : webEntities) {
                message += String.format(Locale.US, "%.3f: %s", entity.getScore(), entity.getDescription());
                message += "\n";
                possibleSearchEntries.add(entity.getDescription());
                possibleSearchEntryScores.add(entity.getScore());
            }
            message += "\n\n\n";
        } else {
            message += "No Deduced entities!\n\n\n";
        }
        ///////////////////////////////////////////////////////////////////////////////////////

        /*List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (pagesWithImage != null) {
            for (WebDetection web : pagesWithImage) {
                message += String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription());
                message += "\n";
            }
        } else {
            message += "nothing";
        }
*/

        //Adding the first web entity plus second if score is over 0.7
        if (possibleSearchEntries.size() >= 3) {
            //keywords.subList(5, keywords.size()).clear();
            keywords.add(possibleSearchEntries.get(0));
            if (possibleSearchEntryScores.get(1) > 0.700f)
                keywords.add(possibleSearchEntries.get(1));
            //if (possibleSearchEntryScores.get(2) > 0.700f)
            //  keywords.add(possibleSearchEntries.get(2));
        }

        //Intent intent = new Intent(this, MasterActivity.class);
        //intent.putStringArrayListExtra("keys", (ArrayList<String>) keywords);
        //startActivity(intent);

        return keywords.toString();
        //return logoMessage + message;
    }
}
