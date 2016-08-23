package it.polito.mad.insane.lab4.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.FirebaseException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.AddReviewSpinnerAdapter;
import it.polito.mad.insane.lab4.data.RestaurantInfo;
import it.polito.mad.insane.lab4.managers.NotificationDailyOfferService;

public class EditProfileRestaurateurActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener{


    public static Activity EditProfileRestaurateurActivity = null; // attribute used to finish() the current activity from another activity
    private static int MY_GL_MAX_TEXTURE_SIZE = 1024;
    private static final int REQUEST_IMAGE_GALLERY = 581;
    private static Bitmap tempCoverPhoto = null;
    private NavigationView navigationView;
    private String rUser;
    private String rid;

    static final String PREF_LOGIN = "loginPref";
    private SharedPreferences mPrefs = null;

    @Override
    public void finish()
    {
        super.finish();
        EditProfileRestaurateurActivity = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //getWindow().//full screen prima di set content view
        EditProfileRestaurateurActivity = this;
        setContentView(R.layout.edit_profile_restaurateur_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        if (mPrefs != null)
        {
            rid = this.mPrefs.getString("rid", null);
            rUser = this.mPrefs.getString("rUser", null);
            setTitle(rUser);
        }

        final ImageView img = (ImageView) findViewById(R.id.coverPhoto);
        if(img != null) {
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePhotoFromGallery();
                }
            });
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null){
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveData();
                }
            });
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Spinner uSpinner = (Spinner) findViewById(R.id.restaurateur_profile_universitySpinner);
        List<String> universities = new ArrayList<>();
        Resources res = getResources();
        String[] uStrings = res.getStringArray(R.array.university_array);
        Collections.addAll(universities, uStrings);
        AddReviewSpinnerAdapter uAdapter = new AddReviewSpinnerAdapter(EditProfileRestaurateurActivity.this,
                R.layout.support_simple_spinner_dropdown_item, universities);
        uAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        uSpinner.setAdapter(uAdapter);

        final Spinner cSpinner = (Spinner) findViewById(R.id.restaurateur_profile_cuisineSpinner);
        List<String> cuisines = new ArrayList<>();
        res = getResources();
        String[] cStrings = res.getStringArray(R.array.cuisine_array);
        Collections.addAll(cuisines, cStrings);
        AddReviewSpinnerAdapter cAdapter = new AddReviewSpinnerAdapter(EditProfileRestaurateurActivity.this,
                R.layout.support_simple_spinner_dropdown_item, cuisines);
        cAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        if(cSpinner != null)
            cSpinner.setAdapter(cAdapter);

        //set image if available
        loadImageFromStorage();
        loadData();

        // Fix Portrait Mode
        if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        /**********************DRAWER****************************/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_drawer);
        TextView title_drawer = (TextView) headerView.findViewById(R.id.title_drawer);
        if(mPrefs != null) {
            title_drawer.setText(mPrefs.getString("rUser", null));
        }
        navigationView.setNavigationItemSelectedListener(this);
        /**************************************************/
    }

    /********************DRAWER*****************************/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.home_restaurateur_activity:
                if(!getClass().equals(HomeRestaurateurActivity.class))
                {
                    // finish the HomeRestaurateurActivity if is not finished
                    if(HomeRestaurateurActivity.HomeRestaurateurActivity != null)
                        HomeRestaurateurActivity.HomeRestaurateurActivity.finish();

                    Intent i = new Intent(this, HomeRestaurateurActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.action_daily_menu:
                if(!getClass().equals(DailyMenuActivity.class))
                {
                    // finish the DailyMenuActivity if is not finished
                    if(DailyMenuActivity.DailyMenuActivity != null)
                        DailyMenuActivity.DailyMenuActivity.finish();

                    // Start DailyMenuActivity activity
                    Intent invokeDailyMenu = new Intent(this, DailyMenuActivity.class);
                    startActivity(invokeDailyMenu);
                    break;
                }

            case R.id.my_reviews_restaurant:
                if(!getClass().equals(MyReviewsRestaurantActivity.class))
                {
                    // finish the MyReviewsRestaurantActivity if is not finished
                    if(MyReviewsRestaurantActivity.MyReviewsRestaurantActivity != null)
                        MyReviewsRestaurantActivity.MyReviewsRestaurantActivity.finish();

                    Intent invokeMyReviewsRestaurant = new Intent(this, MyReviewsRestaurantActivity.class);
                    startActivity(invokeMyReviewsRestaurant);
                }
                break;

            case R.id.action_edit_profile:
                if(!getClass().equals(EditProfileRestaurateurActivity.class))
                {
                    // finish the EditProfileRestaurateurActivity if is not finished
                    if(EditProfileRestaurateurActivity != null)
                        EditProfileRestaurateurActivity.finish();

                    //Start EditProfileActivity
                    Intent invokeEditProfile = new Intent(this, EditProfileRestaurateurActivity.class);
                    startActivity(invokeEditProfile);
                }
                break;

            case R.id.logout_restaurateur_drawer:
                if(rid == null){
                    Toast.makeText(this, R.string.not_logged,Toast.LENGTH_SHORT).show();
                }else {
                    this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
                    if (mPrefs != null) {
                        rid = null;
                        SharedPreferences.Editor editor = this.mPrefs.edit();
                        editor.clear();
                        editor.apply();
                    }

                    //stop service and clear notifications
                    stopService(new Intent(this, NotificationDailyOfferService.class));
                    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(001);

                    Intent i = new Intent(this, HomePageActivity.class);
                    startActivity(i);
                    finish();
                }
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /*************************************************/
    @Override
    public void onBackPressed() {
        /**********************DRAWER***************************/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        /*************************************************/
    }

    @Override
    protected void onResume(){
        super.onResume();

        navigationView.getMenu().findItem(R.id.action_edit_profile).setChecked(true);
        if(tempCoverPhoto != null){
            ImageView iv = (ImageView) findViewById(R.id.coverPhoto);
            iv.setImageBitmap(tempCoverPhoto);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItemText = (String) parent.getItemAtPosition(position);
        // If user change the default selection
        // First item is disable and it is used for hint
        if(position > 0){
            // Notify the selected item text
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void takePhotoFromGallery()
    {
        Intent imageGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI); // EXTERNAL_CONTENT_URI or INTERNAL_CONTENT_URI
        // start the image gallery intent
        startActivityForResult(imageGalleryIntent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        String imgPath;

        switch(requestCode)
        {
            case REQUEST_IMAGE_GALLERY:
                if(resultCode == RESULT_OK) {
                    if (data == null)
                        break;
                    // Get the Image from data
                    Uri selectedImage = data.getData();
                    if (selectedImage == null)
                        break;

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(selectedImage, null, null, null, null);
                    if (cursor == null) {
                        imgPath = selectedImage.getPath();
                    } else {
                        // Move to first row
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        imgPath = cursor.getString(columnIndex);
                        cursor.close();
                    }

                    try {
                        tempCoverPhoto = processImg(imgPath);
                        ImageView iv = (ImageView) findViewById(R.id.coverPhoto);
                        if (iv != null) {
                            iv.setImageBitmap(tempCoverPhoto);
                        }
                    } catch (Exception e) {
                        Toast.makeText(EditProfileRestaurateurActivity.this, R.string.error_processing_img,
                                Toast.LENGTH_LONG).show();
                    }

                }
                break;
            default:
//                Toast.makeText(this, "Switch-case non trovato", Toast.LENGTH_LONG).show();
                break;
        }
    }

    /** Our Methods **/

    /**
     * Method that copy the original img in the app internal directory and compress it
     * @param imgPath
     * @return the URI of the new Img
     * @throws Exception
     */
    private Bitmap processImg(String imgPath) throws Exception
    {
        Bitmap rotatedBitmapImg = rotateImg(imgPath);

        /** scale photo **/ // In teoria non dovrebbe servire
        int imgHeight = rotatedBitmapImg.getHeight();
        int imgWidth = rotatedBitmapImg.getWidth();
        int newImgHeight = imgHeight;
        int newImgWidth = imgWidth;
        int maxValue = Math.max(imgHeight,imgWidth);
        if(maxValue > MY_GL_MAX_TEXTURE_SIZE){
            double scaleFactor = (double) maxValue / (double) MY_GL_MAX_TEXTURE_SIZE;
            newImgHeight = (int) (imgHeight / scaleFactor);
            newImgWidth = (int) (imgWidth / scaleFactor);
        }

        return Bitmap.createScaledBitmap(rotatedBitmapImg, newImgWidth ,newImgHeight, false);
    }

    /**
     * Decode the input photo in relation to the display dim
     * @param photoPath
     * @return
     */
    private Bitmap decodePhoto(String photoPath)
    {
        int ratio = 1;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x;
        int displayHeight = size.y;
        this.MY_GL_MAX_TEXTURE_SIZE = Math.max(displayWidth,displayHeight);

        // Create an object BitmapFactory.Options
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;// If set to true, the decoder will return null (no bitmap), but the outX fields will still be set, allowing the caller
        // to query the bitmap without having to allocate the memory for its pixels
        BitmapFactory.decodeFile(photoPath, options); // set outX fields
        // get the dim of the bitmap img
        int photoW = options.outWidth;
        int photoH = options.outHeight;

        if(photoW > displayWidth || photoH > displayHeight)
        {
            // Compute the scaling ratio to avoid distortion
            ratio = Math.min(photoW / displayWidth, photoH / displayHeight);
        }

        // Set the scaling ratio
        options.inSampleSize = ratio;
        options.inJustDecodeBounds = false; // The decoder will decode the whole image and return their bitmap
        // Decode  the file
        Bitmap photoBitmap = BitmapFactory.decodeFile(photoPath, options);

        // return Bitmap file
        return photoBitmap;
    }
    /**
     * Rotate the image which is located in the input imgPath
     * @param imgPath
     * @return the bitmap image rotated (if needed)
     * @throws Exception
     */
    private Bitmap rotateImg(String imgPath) throws Exception
    {
        int rotationInDegrees;
        Bitmap resultImg;

        // open image given
        //File f = new File(imgPath);
        // obtain bitmap from original file
        // Bitmap originalBitmapImg = BitmapFactory.decodeStream(new FileInputStream(f));
        Bitmap originalBitmapImg = decodePhoto(imgPath);

        // Reads Exif tags from the specified JPEG file.
        ExifInterface exif = new ExifInterface(imgPath);

        // find the current rotation
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        // Convert exif rotation to degrees:
        switch(rotation)
        {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationInDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationInDegrees = 180;
                break;
            case  ExifInterface.ORIENTATION_ROTATE_270:
                rotationInDegrees = 270;
                break;
            default:
                rotationInDegrees = 0;
                break;
        }

        // use the actual rotation of the image as a reference point to rotate the image using a Matrix
        Matrix matrix = new Matrix();
        if (rotation != 0f) // 0 float
            matrix.preRotate(rotationInDegrees);

        // create the new rotate img
        resultImg = Bitmap.createBitmap(originalBitmapImg, 0, 0, originalBitmapImg.getWidth(),originalBitmapImg.getHeight(), matrix, true);

        return resultImg;
    }

    private void loadImageFromStorage()
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReferenceFromUrl("gs://lab4-insane.appspot.com/restaurants/" + rid + "/cover.jpg");

        // Create a reference with an initial file path and name

        //start download of image
//        final long FIVE_MEGABYTE = 1024 * 1024 * 5;
//        imageRef.getBytes(FIVE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] bytes) {
//                // Data for "images/island.jpg" is returns, use this as needed
//                ImageView img = (ImageView)findViewById(R.id.coverPhoto);
//                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                bmp = Bitmap.createScaledBitmap(bmp, img.getWidth(), img.getHeight(), true);
//                img.setImageBitmap(bmp);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle any errors
//                Toast.makeText(EditProfileRestaurateurActivity, exception.toString(), Toast.LENGTH_SHORT).show();
//            }
//        });

        //test con picasso/glide

        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                // Pass it to Picasso to download, show in ImageView and caching

                ImageView img = (ImageView)findViewById(R.id.coverPhoto);

//                Picasso.with(getApplicationContext())
//                        .load(uri.toString())
//                        .into(img);

                Glide.with(getApplicationContext())
                        .load(uri.toString())
                        .placeholder(R.drawable.default_img_rest_1)
                        .into(img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveImageOnStorage(){

        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReferenceFromUrl("gs://lab4-insane.appspot.com/restaurants/" + rid + "/cover.jpg");
        // Create a reference with an initial file path and name
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tempCoverPhoto.compress(Bitmap.CompressFormat.JPEG, 100, baos); //TODO elaborare un algoritmo di compressione in base alla dimensione dell'immagine
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(EditProfileRestaurateurActivity, exception.toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri imageDownloadUrl = taskSnapshot.getDownloadUrl(); //TODO può essere utile usare questo parametro?
                tempCoverPhoto = null;
                Toast.makeText(getApplicationContext(), R.string.cover_photo_update_success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showTimePickerDialog(View view) {
        switch (view.getId()){
            case(R.id.restaurateur_profile_openingHour):
                DialogFragment openingFragment = new HomeRestaurateurActivity.TimePickerFragment();
                openingFragment.show(getSupportFragmentManager(), "openingPicker");
                break;
            case (R.id.restaurateur_profile_closingHour):
                DialogFragment closingFragment = new HomeRestaurateurActivity.TimePickerFragment();
                closingFragment.show(getSupportFragmentManager(), "closingPicker");
                break;
        }
    }

    private void loadData() {

        SharedPreferences prefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        String restaurantId = null;
        if (prefs != null) {
            restaurantId = prefs.getString("rid", null);
        }

        if (restaurantId != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference restaurantInfoRef = database.getReference("restaurants/" + restaurantId + "/info");

            restaurantInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    RestaurantInfo info = dataSnapshot.getValue(RestaurantInfo.class);
                    if(info != null){
                        EditText et;

                        et = (EditText) findViewById(R.id.restaurateur_profile_editServices);
                        if (et != null) {
                            if (info.getAdditionalServices() != null)
                                et.setText(info.getAdditionalServices());
                        }

                        et = (EditText) findViewById(R.id.restaurateur_profile_editAddress);
                        if (et != null) {
                            if (info.getAddress() != null)
                                et.setText(info.getAddress());
                        }

                        et = (EditText) findViewById(R.id.restaurateur_profile_editDescription);
                        if (et != null) {
                            if (info.getDescription() != null)
                                et.setText(info.getDescription());
                        }

                        et = (EditText) findViewById(R.id.restaurateur_profile_editPayment);
                        if (et != null) {
                            if (info.getPaymentMethod() != null)
                                et.setText(info.getPaymentMethod());
                        }

                        et = (EditText) findViewById(R.id.restaurateur_profile_editName);
                        if (et != null) {
                            if (info.getRestaurantName() != null)
                                et.setText(info.getRestaurantName());
                        }

                        et = (EditText) findViewById(R.id.restaurateur_profile_editTimeNotes);
                        if (et != null) {
                            if (info.getTimeInfo() != null)
                                et.setText(info.getTimeInfo());
                        }

                        Button button;
                        button = (Button) findViewById(R.id.restaurateur_profile_openingHour);
                        if (button != null) {
                            button.setText(info.getOpeningHour());
                        }

                        button = (Button) findViewById(R.id.restaurateur_profile_closingHour);
                        if (button != null) {
                            button.setText(info.getClosingHour());
                        }

                        Spinner spinner;
                        spinner = (Spinner) findViewById(R.id.restaurateur_profile_universitySpinner);
                        if (spinner != null) {
                            String[] universities = getResources().getStringArray(R.array.university_array);
                            for (int i = 0; i < universities.length; i++) {
                                if (universities[i].equals(info.getNearbyUniversity()))
                                    spinner.setSelection(i);
                            }
                        }
                        spinner = (Spinner) findViewById(R.id.restaurateur_profile_cuisineSpinner);
                        if (spinner != null) {
                            String[] cuisines = getResources().getStringArray(R.array.cuisine_array);
                            for (int i = 0; i < cuisines.length; i++) {
                                if (cuisines[i].equals(info.getCuisineType()))
                                    spinner.setSelection(i);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void saveData() {

        if(tempCoverPhoto != null)
            saveImageOnStorage();

        RestaurantInfo profile = new RestaurantInfo();

        EditText et;
        if ((et = (EditText) findViewById(R.id.restaurateur_profile_editName)) != null)
            profile.setRestaurantName(String.valueOf(et.getText()));

        if ((et = (EditText) findViewById(R.id.restaurateur_profile_editAddress)) != null)
                profile.setAddress(String.valueOf(et.getText()));

        if ((et = (EditText) findViewById(R.id.restaurateur_profile_editDescription)) != null)
                profile.setDescription(String.valueOf(et.getText()));

        if ((et = (EditText) findViewById(R.id.restaurateur_profile_editPayment)) != null)
            profile.setPaymentMethod(String.valueOf(et.getText()));

        if ((et = (EditText) findViewById(R.id.restaurateur_profile_editTimeNotes)) != null)
            profile.setTimeInfo(String.valueOf(et.getText()));

        if ((et = (EditText) findViewById(R.id.restaurateur_profile_editServices)) != null)
            profile.setAdditionalServices(String.valueOf(et.getText()));


        Spinner spinner;
        if((spinner = (Spinner) findViewById(R.id.restaurateur_profile_universitySpinner)) != null) {
            String[] universities = getResources().getStringArray(R.array.university_array);
            if(String.valueOf(spinner.getSelectedItem()).equals(universities[0]))
                profile.setNearbyUniversity("");
            else
                profile.setNearbyUniversity(String.valueOf(spinner.getSelectedItem()));
        }
        if((spinner = (Spinner) findViewById(R.id.restaurateur_profile_cuisineSpinner)) != null) {
            String[] cuisines = getResources().getStringArray(R.array.cuisine_array);
            if(String.valueOf(spinner.getSelectedItem()).equals(cuisines[0]))
                profile.setCuisineType("");
            else
                profile.setCuisineType(String.valueOf(spinner.getSelectedItem()));
        }

        Button b = (Button) findViewById(R.id.restaurateur_profile_openingHour);
        if (b != null)
            profile.setOpeningHour(String.valueOf(b.getText()));


        b = (Button) findViewById(R.id.restaurateur_profile_closingHour);
        if ((b != null))
            profile.setClosingHour(String.valueOf(b.getText()));


        SharedPreferences prefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        String restaurantId = null;
        if (prefs != null) {
            restaurantId = prefs.getString("rid", null);
        }
        if (restaurantId != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference restaurantInfoRef = database.getReference("/restaurants/" + restaurantId + "/info");

            restaurantInfoRef.setValue(profile, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Toast.makeText(getApplicationContext(), R.string.confirm_profile_updated, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }
}
