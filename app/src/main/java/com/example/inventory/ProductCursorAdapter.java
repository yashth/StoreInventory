package com.example.inventory;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = "ProductCursorAdapter";


    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d(LOG_TAG,"newView called cursor: "+cursor+" context: "+context+" parent: "+parent);
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(LOG_TAG,"bindView called cursor: "+cursor+" context: "+context+" view: "+view);


        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView descTextView = (TextView) view.findViewById(R.id.desc);
        TextView countTextView = (TextView) view.findViewById(R.id.count);
        TextView priceTextView =(TextView) view.findViewById(R.id.price);
        TextView saleTextView = (TextView) view.findViewById(R.id.sale);




            Log.d(LOG_TAG,"bindView called cursor.getCount(): "+cursor.getCount());
            Log.d(LOG_TAG,"bindView called cursor.getColumnCount(): "+cursor.getColumnCount());


            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int descColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_DESCRIPTION);
            int countColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_COUNT);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int saleColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SALE);

            Log.d(LOG_TAG,"bindView called priceColumnIndex: "+priceColumnIndex);
            Log.d(LOG_TAG,"bindView called saleColumnIndex: "+saleColumnIndex);


            byte[] imageArray = cursor.getBlob(imageColumnIndex);
            Bitmap imageBm = BitmapFactory.decodeByteArray(imageArray, 0 ,imageArray.length);
            String productName = cursor.getString(nameColumnIndex);
            String productDesc = cursor.getString(descColumnIndex);
            int productCount = cursor.getInt(countColumnIndex);
            int productPrice = cursor.getInt(priceColumnIndex);
            int productSale = cursor.getInt(saleColumnIndex);
            // If the product description is empty string or null, then use some default text
            // that says "", so the TextView isn't blank.
            if (TextUtils.isEmpty(productDesc)) {
                productDesc = "";
            }


            imageView.setImageBitmap(imageBm);
            nameTextView.setText(productName);
            descTextView.setText(productDesc);
            countTextView.setText(Integer.toString(productCount));
            priceTextView.setText(Integer.toString(productPrice));
            saleTextView.setText(Integer.toString(productSale));



    }
}
