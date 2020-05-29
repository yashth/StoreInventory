package com.example.inventory;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventory.ProductContract.ProductEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = "EditorActivity";

    /** ImageView to enter the product's image*/
    private ImageView mImageView;

    /** EditText field to enter the product's name */
    private EditText mNameEditText;

    /** EditText field to enter the product's description */
    private EditText mDescEditText;

    /** EditText field to enter the product's count */
    private EditText mCountEditText;

    /** EditText field to enter the product's price */
    private EditText mPriceEditText;

    /** TextView field of the sales*/
    private TextView mSaleText;


    private static final int EXISTING_PRODUCT_LOADER = 0;

    ProductDbHelper mDbHelper;
    /** Content URI for the existing pet (null if it's a new pet) */
    private Uri mCurrentProductUri;

    /** Boolean flag that keeps track of whether the pet has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

    /** Integer value for Image upload request*/
    private static final int PICK_IMAGE_REQUEST = 100;


    /** URI value for the Image path*/
    private Uri imageFilePath;

    /** Bitmap value of the selected image*/
    private Bitmap imageToStore;


    private ByteArrayOutputStream objectByteArrayOutputStream;

    private byte[] imageInByte;

    private boolean imageChanged = false;


    FloatingActionButton fabPlus;

    FloatingActionButton fabMinus;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.d(LOG_TAG,"onTouch() mProductHasChanged: "+mProductHasChanged);
            mProductHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        fabPlus = (FloatingActionButton) findViewById(R.id.saleplus);
        fabPlus.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                salePlus();
            }
        });

        fabMinus = (FloatingActionButton) findViewById(R.id.saleminus);
        fabMinus.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                saleMinus();
            }
        });

        Intent intent = getIntent();
        Log.d(LOG_TAG,"onCreate intent: "+intent);
        mCurrentProductUri = intent.getData();

        Log.d(LOG_TAG,"onCreate mCurrentProductUri: "+mCurrentProductUri);

        if(mCurrentProductUri==null){
            Log.d(LOG_TAG,"onCreate setTitle(Add a product)");
            setTitle("Add a product");
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        }else{
            Log.d(LOG_TAG,"onCreate setTitle(Edit a product)");
            setTitle("Edit a product");
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);



        }


        // Find all relevant views that we will need to read user input from
        mImageView = (ImageView) findViewById(R.id.edit_product_image);
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mDescEditText = (EditText) findViewById(R.id.edit_product_description);
        mCountEditText = (EditText) findViewById(R.id.edit_product_count);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mSaleText = (TextView) findViewById(R.id.edit_total_sale);

        mDbHelper = new ProductDbHelper(this);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mImageView.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescEditText.setOnTouchListener(mTouchListener);
        mCountEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        fabPlus.setOnTouchListener(mTouchListener);
        fabMinus.setOnTouchListener(mTouchListener);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        Log.d(LOG_TAG,"onCreateOptionsMenu() menu: ");
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG,"onOptionsItemSelected() item: "+item);
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                Log.d(LOG_TAG,"onOptionsItemSelected() action_save saveProduct() called");
                // Do nothing for now
                saveProduct();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                Log.d(LOG_TAG,"onOptionsItemSelected() home mProductHasChanged: "+mProductHasChanged);
                // If the pet hasn't changed, continue with navigating up to parent activity
               NavUtils.navigateUpFromSameTask(this);	                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                Log.d(LOG_TAG,"onOptionsItemSelected() home onClick");
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                Log.d(LOG_TAG,"onOptionsItemSelected() home showUnsavedChangesDialog");
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        Log.d(LOG_TAG,"onBackPressed() called");
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    public void chooseImage(View view){

        Log.d(LOG_TAG,"chooseImage() inside view: "+view);
        try{

            Intent imageIntent = new Intent();
            imageIntent.setType("image/*");
            imageIntent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(imageIntent,PICK_IMAGE_REQUEST);

            Log.d(LOG_TAG,"chooseImage() inside imageIntent sent");

        }catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(LOG_TAG,"onActivityResult() inside requestCode: "+requestCode+" resultCode: "+resultCode);
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){

                Log.d(LOG_TAG,"onActivityResult() inside resultCode: "+resultCode);
                imageFilePath = data.getData();
                imageToStore = MediaStore.Images.Media.getBitmap(getContentResolver(),imageFilePath);
                mImageView.setImageBitmap(imageToStore);
                imageChanged = true;
            }


        }catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }


    private void saveProduct(){
        Log.d(LOG_TAG,"saveProduct() inside");

        ContentValues values = new ContentValues();

        String productName = mNameEditText.getText().toString().trim();
        String productDesc = mDescEditText.getText().toString().trim();
        Integer productCount = Integer.parseInt(mCountEditText.getText().toString().trim());
        Integer productPrice = Integer.parseInt(mPriceEditText.getText().toString().trim());

        if(mCurrentProductUri!=null){

            Integer productSale = Integer.parseInt(mSaleText.getText().toString().trim());
            values.put(ProductEntry.COLUMN_PRODUCT_SALE, productSale);

        }else{
            Log.d(LOG_TAG,"saveProduct() add sales as 0");
            values.put(ProductEntry.COLUMN_PRODUCT_SALE, 0);
        }







        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, productDesc);
        values.put(ProductEntry.COLUMN_PRODUCT_COUNT, productCount);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);


        if(mImageView.getDrawable()!=null && imageToStore!=null){

            Log.d(LOG_TAG,"saveProduct() inside  image present");

            Bitmap imageToStoreBitmap = imageToStore;
            objectByteArrayOutputStream = new ByteArrayOutputStream();
            imageToStoreBitmap.compress(Bitmap.CompressFormat.JPEG,100,objectByteArrayOutputStream);

            imageInByte = objectByteArrayOutputStream.toByteArray();

            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE,imageInByte);

        }

        if(mCurrentProductUri==null) {

            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            Log.d(LOG_TAG, "saveProduct() inside newUri: " + newUri);


            if (newUri == null) {
                Toast.makeText(EditorActivity.this, "Product insert error ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditorActivity.this, "Product insert Success", Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.d(LOG_TAG, "saveProduct() inside update");
            int rowsAffected = getContentResolver().update(mCurrentProductUri,values,null,null);

            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(LOG_TAG,"onCreateLoader called id: "+id);

        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductEntry.COLUMN_PRODUCT_COUNT,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SALE
        };
        return new CursorLoader(this,mCurrentProductUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        Log.d(LOG_TAG,"onLoadFinished called");

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int descColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DESCRIPTION);
            int countColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_COUNT);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int saleColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALE);

            // Extract out the value from the Cursor for the given column index
            byte[] imageArray = cursor.getBlob(imageColumnIndex);
            Bitmap imageBm = BitmapFactory.decodeByteArray(imageArray, 0 ,imageArray.length);
            String name = cursor.getString(nameColumnIndex);
            String desc = cursor.getString(descColumnIndex);
            int count = cursor.getInt(countColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int sale = cursor.getInt(saleColumnIndex);

            if (count>0 && (sale==0 || sale<0)){
                fabMinus.setEnabled(false);
                fabPlus.setEnabled(true);
            }else if ((count == 0 || count<0) && sale>0){
                fabMinus.setEnabled(true);
                fabPlus.setEnabled(false);
            } else if(count>0 && sale>0){
                fabMinus.setEnabled(true);
                fabPlus.setEnabled(true);
            }

            // Update the views on the screen with the values from the database

            //Update the image view if the image is changed or set the existing image
            if(mCurrentProductUri!=null && imageChanged){

                mImageView.setImageBitmap(imageToStore);
                imageChanged = false;

            }else{
                mImageView.setImageBitmap(imageBm);
            }

            mNameEditText.setText(name);
            mDescEditText.setText(desc);
            mCountEditText.setText(Integer.toString(count));
            mPriceEditText.setText(Integer.toString(price));
            mSaleText.setText(Integer.toString(sale));


        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        Log.d(LOG_TAG,"onLoaderReset called");
        // If the loader is invalidated, clear out all the data from the input fields.
        mImageView.setImageBitmap(null);
        mNameEditText.setText("");
        mDescEditText.setText("");
        mCountEditText.setText("");
        mPriceEditText.setText("");
        mSaleText.setText("");

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        Log.d(LOG_TAG,"showUnsavedChangesDialog() inside");
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                Log.d(LOG_TAG,"showUnsavedChangesDialog() dialog: "+dialog);
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        Log.d(LOG_TAG,"showUnsavedChangesDialog() alertDialog build");
        AlertDialog alertDialog = builder.create();
        Log.d(LOG_TAG,"showUnsavedChangesDialog() alertDialog.show");
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
            // Close the activity
            finish();
        }
    }

    private void salePlus(){

        int productCount = Integer.parseInt(mCountEditText.getText().toString().trim());
        int productPrice = Integer.parseInt(mPriceEditText.getText().toString().trim());
        int productSale = Integer.parseInt(mSaleText.getText().toString().trim());

        productCount = productCount - 1;
        productSale = productSale + productPrice;

        fabMinus.setEnabled(true);

        if(productCount==0 || productCount<0){

            fabPlus.setEnabled(false);
        }

        mCountEditText.setText(Integer.toString(productCount));
        mSaleText.setText(Integer.toString(productSale));




    }

    private void saleMinus(){

        int productCount = Integer.parseInt(mCountEditText.getText().toString().trim());
        int productPrice = Integer.parseInt(mPriceEditText.getText().toString().trim());
        int productSale = Integer.parseInt(mSaleText.getText().toString().trim());

        productCount = productCount + 1;
        productSale = productSale - productPrice;

        fabPlus.setEnabled(true);

        if (productSale==0 || productSale<0){
            fabMinus.setEnabled(false);
        }

        mCountEditText.setText(Integer.toString(productCount));
        mSaleText.setText(Integer.toString(productSale));
    }
}
