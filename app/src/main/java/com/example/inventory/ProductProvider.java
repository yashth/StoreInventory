package com.example.inventory;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.inventory.ProductContract.ProductEntry;


public class ProductProvider extends ContentProvider {

    private static final String LOG_TAG = "ProductProvider";

    public ProductDbHelper mDbHelper;
    /** URI matcher code for the content URI for the pets table */
    private static final int PRODUCTS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PRODUCT_ID = 101;

    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG,"onCreate called");
        mDbHelper = new ProductDbHelper(getContext());

        return true;
    }

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{
        Log.d(LOG_TAG,"sUriMatcher called");
        sUriMatcher.addURI(ProductEntry.CONTENT_AUTHORITY, ProductEntry.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductEntry.CONTENT_AUTHORITY, ProductEntry.PATH_PRODUCTS+"/#",PRODUCT_ID);

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        Log.d(LOG_TAG,"query called");
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        Log.d(LOG_TAG,"query match: "+match);
        switch(match){
            case PRODUCTS:

                Log.d(LOG_TAG,"match PRODUCTS");

                cursor = database.query(ProductEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);
                break;
            case PRODUCT_ID:

                Log.d(LOG_TAG,"match PRODUCT_ID");
                selection = ProductEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(ProductEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder,null);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown Uri "+uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        Log.d(LOG_TAG,"getType");
        final int match = sUriMatcher.match(uri);
        Log.d(LOG_TAG,"getType match: "+match);
        switch (match) {
            case PRODUCTS:
                Log.d(LOG_TAG,"return ProductEntry.CONTENT_LIST_TYPE");
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                Log.d(LOG_TAG,"return ProductEntry.CONTENT_ITEM_TYPE");
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.d(LOG_TAG,"insert uri "+ uri +" values: "+values);

        final int match = sUriMatcher.match(uri);

        Log.d(LOG_TAG,"insert match: "+match);

        switch(match){
            case PRODUCTS:
                Log.d(LOG_TAG,"insertProduct");
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for "+uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        Log.d(LOG_TAG,"delete uri:"+ uri+" selection: "+selection+"selectionArgs: "+selectionArgs);
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        Log.d(LOG_TAG,"delete match:"+ match);
        switch (match) {
            case PRODUCTS:
                Log.d(LOG_TAG,"delete PRODUCTS");
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                Log.d(LOG_TAG,"delete PRODUCT_ID");
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            Log.d(LOG_TAG,"delete rowsDeleted != 0");
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(LOG_TAG,"update uri: "+uri+" values: "+ values+" selection: "+ selection+" selectionArgs: "+selectionArgs);
        final int match = sUriMatcher.match(uri);
        Log.d(LOG_TAG,"update match: "+match);
        switch (match){
            case PRODUCTS:
                Log.d(LOG_TAG,"update PETS");
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                Log.d(LOG_TAG,"update PET_ID");
                selection = ProductEntry._ID+"=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for "+uri);

        }
    }


    private Uri insertProduct(Uri uri, ContentValues values){

        Log.d(LOG_TAG,"insertProduct uri: "+uri+" values: "+values);




        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        Log.d(LOG_TAG,"insertProduct name: "+name);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        String desc = values.getAsString(ProductEntry.COLUMN_PRODUCT_DESCRIPTION);

        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer count = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_COUNT);
        Log.d(LOG_TAG,"insertProduct count: "+count);
        if (count != null && count < 0) {
            throw new IllegalArgumentException("Product requires valid count");
        }

        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        Log.d(LOG_TAG,"insertProduct price: "+price);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        Integer sale = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_SALE);
        Log.d(LOG_TAG,"insertProduct sale: "+sale);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(ProductEntry.TABLE_NAME, null, values);

        Log.e(LOG_TAG,"insertProduct() id "+id);

        if(id==-1){
            Log.e(LOG_TAG,"Failed to insert row for "+uri);

            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);
        Log.d(LOG_TAG,"return ContentUris.withAppendedId(uri,id): "+ContentUris.withAppendedId(uri,id));
        return ContentUris.withAppendedId(uri,id);
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs){



        // If the {@link PetEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }



        // If the {@link PetEntry#COLUMN_PRODUCT_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            // Check that the price is greater than or equal to 0 Rs
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }


        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_COUNT)) {
            // Check that the count is greater than or equal to 0
            Integer count = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_COUNT);
            if (count != null && count < 0) {
                throw new IllegalArgumentException("Product requires valid count");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        Log.d(LOG_TAG,"updateProduct rowsUpdated: "+rowsUpdated);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);

        }

        return rowsUpdated;
    }
}
