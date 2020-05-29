package com.example.inventory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.inventory.ProductContract.ProductEntry;

public class ProductDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG,"onCreate called");
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_INVENTORY_TABLE =  "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +ProductEntry.COLUMN_PRODUCT_IMAGE + " BLOB,"
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_DESCRIPTION + " TEXT, "
                + ProductEntry.COLUMN_PRODUCT_COUNT + " INTEGER NOT NULL DEFAULT 0, "
                + ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + ProductEntry.COLUMN_PRODUCT_SALE + " INTEGER NOT NULL DEFAULT 0);";


        Log.d(LOG_TAG,"onCreate SQL_CREATE_INVENTORY_TABLE: "+SQL_CREATE_INVENTORY_TABLE);

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG,"onUpgrade called");
    }
}
