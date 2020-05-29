package com.example.inventory;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductContract {

    private ProductContract(){}

    public static final class ProductEntry implements BaseColumns {

        /** Name of database table for inventory of products */
        public final static String TABLE_NAME = "productinventory1";

        public static final String CONTENT_AUTHORITY = "com.example.inventory";

        public static  final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

        public static final String PATH_PRODUCTS = "productinventory1";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);


        /**
         * Unique ID number for the products (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Product Image.
         *
         * Type: BLOB
         */
        public final static String COLUMN_PRODUCT_IMAGE = "productimage";

        /**
         * Product Name
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="productname";

        /**
         * Product Description.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_DESCRIPTION = "productdescription";

        /**
         * Product Count.
         * Provide count of product available in inventory
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_COUNT = "productcount";

        /**
         * Product price.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_PRICE = "productprice";

        /**
         * Product Sale.
         *
         * Type: Integer
         */
        public final static String COLUMN_PRODUCT_SALE = "productsale";





        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;


    }
}
