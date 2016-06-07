package it.polito.mad.insane.lab4.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.data.Dish;

        // TODO: integrare la logica di firebase (Michele)
public class EditDishActivity extends AppCompatActivity
{
    private static int MY_GL_MAX_TEXTURE_SIZE = 1024; // compatible with almost all devices. To obtain the right value for each device use:   int[] maxSize = new int[1];
                                                      // (this needs an OpenGL context)                                                       GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxSize, 0);

    private static final int REQUEST_IMAGE_GALLERY = 157;
    private Dish currentDish = null;
    private EditText dishID;
    private EditText dishName;
    private EditText dishDesc;
    private EditText dishQty;
    private EditText dishPrice;
    private ImageView dishPhoto;

    View.OnClickListener saveDishFabListener = new View.OnClickListener()
    {

        @Override
        public void onClick(View view)
        {
            // check if all the required info are filled
            if(!isAllDataFilled()) {
                Toast.makeText(EditDishActivity.this, R.string.error_some_empty_fill, Toast.LENGTH_SHORT).show();
                return;
            }

            // save data in manager
            if(currentDish != null)
            {
//                for (Dish d : EditDishActivity.manager.getDishes())
//                {
//                    try
//                    {
//                        if (d.getID().equals(currentDish.getID()))
//                        {
//                            // edit existing dish
//                            d.setName(EditDishActivity.this.dishName.getText().toString());
//                            d.setDescription(EditDishActivity.this.dishDesc.getText().toString());
//                            d.setAvailability_qty(Integer.parseInt(EditDishActivity.this.dishQty.getText().toString()));
//                            d.setPrice(Double.parseDouble(EditDishActivity.this.dishPrice.getText().toString()));
//                            d.setPhotoPath(EditDishActivity.this.currentDish.getPhotoPath());
//                            //String photoPath = (String) EditDish.this.dishPhoto.getTag();
//                            //if(photoPath != null)
//                            //d.setPhotoPath(photoPath);
//                            EditDishActivity.manager.saveDbApp();
//                            Toast.makeText(EditDishActivity.this, R.string.confirm_save_dish, Toast.LENGTH_SHORT).show();
//                            finish();
//                            return;
//                        }
//                    } catch (NumberFormatException e) {
//                        e.printStackTrace();
//                        Toast.makeText(EditDishActivity.this, R.string.error_input_number, Toast.LENGTH_SHORT).show();
//                    }
//                }

                // Dish not found: user is adding a new dish
                try
                {
//                    currentDish.setName(dishName.getText().toString());
//                    currentDish.setDescription(dishDesc.getText().toString());
//                    currentDish.setAvailabilityQty(Integer.parseInt(dishQty.getText().toString()));
//                    currentDish.setPrice(Double.parseDouble(dishPrice.getText().toString()));
                    //photoPath already set

                    //String photoPath = (String) EditDish.this.dishPhoto.getTag();
                    //if(photoPath != null)
                    //EditDish.this.currentDish.setPhotoPath(photoPath);

                    //EditDish.manager.getDishes().add(EditDish.this.currentDish);
                    //EditDish.manager.saveDbApp();
                    Toast.makeText(EditDishActivity.this, R.string.confirm_add_dish, Toast.LENGTH_SHORT).show();
                    finish();
                }catch( NumberFormatException e)
                {
                    e.printStackTrace();
                    Toast.makeText(EditDishActivity.this, R.string.error_input_number, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    /** Standard Methods **/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // initialize manager
//        EditDishActivity.manager = RestaurateurJsonManager.getInstance(this);

        // load layout form XML
        setContentView(R.layout.edit_dish_activity);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.save_edit_dish);
        if (fab != null)
            fab.setOnClickListener(saveDishFabListener);


        this.dishID = (EditText) EditDishActivity.this.findViewById(R.id.edit_dish_ID);
        this.dishName = (EditText) EditDishActivity.this.findViewById(R.id.edit_dish_name);
        this.dishDesc = (EditText) EditDishActivity.this.findViewById(R.id.edit_dish_description);
        this.dishQty = (EditText) EditDishActivity.this.findViewById(R.id.edit_dish_availab_qty);
        this.dishPrice = (EditText) EditDishActivity.this.findViewById(R.id.edit_dish_price);
        this.dishPhoto = (ImageView) EditDishActivity.this.findViewById(R.id.dishPhoto);
        if(dishPhoto != null) {
            dishPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
//                    displayChooseDialog();
//                    if(suppportDynamicPermissions() == true)
//                        checkAndRequestPermissions(PERMS_REQUEST_CODE_CAMERA);
//                    else
                    takePhotoFromGallery();
                }
            });
        }
        this.currentDish = (Dish)getIntent().getSerializableExtra("Dish");
        if(this.currentDish != null)
        {
            // Edit existing dish
            this.dishID.setText(this.currentDish.getID());
            this.dishName.setText(this.currentDish.getName());
            //setTitle(this.currentDish.getName()); // set Activity Title
            this.dishDesc.setText(this.currentDish.getDescription());
            this.dishQty.setText(Integer.toString(this.currentDish.getAvailabilityQty()));
            this.dishPrice.setText(Double.toString(this.currentDish.getPrice()));
            String imgPath = this.currentDish.getPhotoPath();
            if(imgPath != null)
                this.dishPhoto.setImageURI(Uri.parse(imgPath));

        }else
        {
            try
            {
                // Crete new dish
                // TODO: usare firebase (Michele)

//               //setTitle(R.string.new_dish);
//                this.currentDish = new Dish();
//                // set ID
//                this.currentDish.setID(getNextDishID(EditDish.manager.getDishes()));
            } catch (Exception e)
            {
                Toast.makeText(EditDishActivity.this, R.string.error_create_newDish, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        // Fix Portrait Mode
        if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_dish_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.delete_dish:
                deleteDish(this.currentDish.getID());
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        String imgPath;

        switch(requestCode)
        {
            case REQUEST_IMAGE_GALLERY:
//                if(resultCode == RESULT_OK)
//                {
//                    if (data == null)
//                        break;
//                    // Get the Image from data
//                    Uri selectedImage = data.getData();
//                    if (selectedImage == null)
//                        break;
//
//                    // Get the cursor
//                    Cursor cursor = getContentResolver().query(selectedImage, null, null, null, null);
//                    if (cursor == null) {
//                        imgPath = selectedImage.getPath();
//                    } else {
//                        // Move to first row
//                        cursor.moveToFirst();
//                        int columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
//                        imgPath = cursor.getString(columnIndex); // FIXME: errore su YotaPhone: "Invalid input parameters". Probabilmente è perché serve la richiesta dei permessi essendo Android 6.0
//                        cursor.close();
//                    }
//
//
//                    try
//                    {
//                        String processedImgPath = processImg(imgPath);
//                        this.dishPhoto.setImageURI(Uri.parse(processedImgPath));
//                        //set tag in photo
//                        //this.dishPhoto.setTag(processedImgPath);
//                        //update info in activity
//                        this.currentDish.setPhotoPath(processedImgPath);
//                        finish();
//                        startActivity(getIntent());
//                    }
//                    catch (Exception e)
//                    {
//                        Toast.makeText(EditDishActivity.this, R.string.error_processing_img, Toast.LENGTH_SHORT).show();
//                        e.printStackTrace();
//                    }
//                }
                break;
            default:
                break;
        }
    }

    /**
    * Method that delete the dish with the input ID
    * @param dishID
    */
    private void deleteDish(String dishID)
    {
        //TODO Implementare con firebase (Michele)
//        for (Dish d : EditDishActivity.manager.getDishes())
//            if (d.getID().equals(dishID))
//            {
//                EditDishActivity.manager.getDishes().remove(d);
//                EditDishActivity.manager.saveDbApp();
//                Toast.makeText(EditDishActivity.this, R.string.confirm_delete_dish, Toast.LENGTH_SHORT).show();
//                finish();
//                return;
//            }
    }

    /**
     * Method that copy the original img in the app internal directory and compress it
     * @param imgPath
     * @return the path of the new Img
     * @throws Exception
     */
//    private String processImg(String imgPath) throws Exception
//    {
    //TODO rivedere gestione immagini (Michele)
//        String resultString;
//        String imgName1;
//        String imgName2;
//        FileOutputStream fos;
//
//        // Take the original img and rotate it (if needed)
//        Bitmap rotatedBitmapImg = rotateImg(imgPath);
//
//        /** save bitmap into App Internal directory creating a compressed copy of it **/
//        ContextWrapper cw = new ContextWrapper(getApplicationContext());
//
//        // path: /data/data/<my_app>/app_imageDir
//        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
//        // name: dishPhoto_<dishID>
//        if(this.currentDish.getID() != null) {
//            imgName1 = PREFIX_IMAGE_NAME1 + this.currentDish.getID();
//            imgName2 = PREFIX_IMAGE_NAME2 + this.currentDish.getID();
//        }
//        else {
//            imgName1 = PREFIX_IMAGE_NAME1 + this.getNextDishID(EditDishActivity.manager.getDishes());
//            imgName2 = PREFIX_IMAGE_NAME2 + this.getNextDishID(EditDishActivity.manager.getDishes());
//        }
//
//
//        // Compress, scale and create img in: /data/data/<my_app>/app_data/imageDir/<imgName>
//        /** scale photo **/ // In teoria non dovrebbe servire
//        int imgHeight = rotatedBitmapImg.getHeight();
//        int imgWidth = rotatedBitmapImg.getWidth();
//        int newImgHeight = imgHeight;
//        int newImgWidth = imgWidth;
//        int maxValue = Math.max(imgHeight,imgWidth);
//        if(maxValue > MY_GL_MAX_TEXTURE_SIZE)
//        {
//            double scaleFactor = (double) maxValue / (double) MY_GL_MAX_TEXTURE_SIZE;
//            newImgHeight = (int) (imgHeight / scaleFactor);
//            newImgWidth = (int) (imgWidth / scaleFactor);
//        }
//
//        Bitmap bitmapImgScaled = Bitmap.createScaledBitmap(rotatedBitmapImg,newImgWidth,newImgHeight,false);
//
//
//        File myImg1 = new File(directory, imgName1);
//        File myImg2 = new File(directory, imgName2);
//        if(this.currentDish.getPhotoPath()!= null && this.currentDish.getPhotoPath().equals(myImg1.getPath())) {
//            resultString = myImg2.getPath();
//            fos = new FileOutputStream(myImg2);
//        }
//        else {
//            resultString = myImg1.getPath();
//            fos = new FileOutputStream(myImg1);
//        }
//
//        bitmapImgScaled.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//        fos.close();
//        //return myImg.getPath();
//        return resultString;
//    }

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
        //Bitmap originalBitmapImg = BitmapFactory.decodeStream(new FileInputStream(f));
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
//    public void displayChooseDialog() { // not used in this version
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(EditDishActivity.this);
//
//        //Create dialog entries
//        final String [] items = new String[] {EditDishActivity.this.getResources().getString(R.string.take_photo),
//                EditDishActivity.this.getResources().getString(R.string.gallery_image)};
//        final Integer[] icons = new Integer[] {R.drawable.ic_camera_alt_black_24dp, R.drawable.ic_collections_black_24dp,};
//        ListAdapter adapter = new DialogArrayAdapter(EditDishActivity.this, items, icons);
//
//        builder.setTitle(EditDishActivity.this.getResources().getString(R.string.alert_title))
//                .setAdapter(adapter, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int item) {
//                        switch (item) {
//                            case (0):
//                                takePhotoFromCamera();
//                                break;
//                            case (1):
//                                takePhotoFromGallery();
//                                break;
//                        }
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.dismiss();
//                    }
//                });
//
//        Dialog dialog = builder.create();
//        dialog.show();
//    }

    private void takePhotoFromCamera() { // not implemented yet
        Toast.makeText(EditDishActivity.this, "Camera", Toast.LENGTH_SHORT).show();
    }

    private void takePhotoFromGallery() {
        Intent imageGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // EXTERNAL_CONTENT_URI or INTERNAL_CONTENT_URI
        // start the image gallery intent
        startActivityForResult(imageGalleryIntent, REQUEST_IMAGE_GALLERY);
    }

//    /**
//     * Method that get the max ID used in the input list and return the next ID to use (maxID +1)
//     * @param dishes
//     * @return next ID to use
//     * @throws NumberFormatException
//     */
    // TODO da aggiustare in base alla nuova logica di firebase (Michele)
//    private String getNextDishID(List<Dish> dishes) throws Exception
//    {
//        int maxID = 0;
//
//        if(!dishes.isEmpty())
//            for(Dish d: dishes)
//            {
//                int tempID = Integer.parseInt(d.getID());
//                if(tempID > maxID)
//                    maxID = tempID;
//            }
//
//        maxID ++;
//        return Integer.toString(maxID);
//
//    }

    /**
     * Method that check if all the field of the activity are filled
     * @return
     */
    private boolean isAllDataFilled()
    {
        //this.dishID.getText().toString().trim().length() > 0
        if(this.dishName.getText().toString().trim().length() > 0 &&
                this.dishDesc.getText().toString().trim().length() > 0 &&
                this.dishPrice.getText().toString().trim().length() > 0 &&
                this.dishQty.getText().toString().trim().length() > 0)
            return  true;
        else
            return false;

    }

}
