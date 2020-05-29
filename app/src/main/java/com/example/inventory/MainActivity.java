package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = "MainAcitivty";

    private static final int PRODUCT_LOADER = 0;

    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(LOG_TAG,"FloatingActionButton onClick called EditorActivity");
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        // Find the ListView which will be populated with the pet data
        ListView productListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);
        Log.d(LOG_TAG,"productListView.setEmptyView(emptyView)");

        mCursorAdapter = new ProductCursorAdapter(this,null);
        productListView.setAdapter(mCursorAdapter);
        Log.d(LOG_TAG,"productListView.setAdapter(mCursorAdapter)");

        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null,this);


        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentClick = new Intent(MainActivity.this, EditorActivity.class);
                Log.d(LOG_TAG,"productListView onClick called EditorActivity");
                Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI,id);

                intentClick.setData(currentProductUri);

                startActivity(intentClick);
            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        Log.d(LOG_TAG,"onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        Log.d(LOG_TAG,"onCreateLoader called");

        String[] projection = {ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductContract.ProductEntry.COLUMN_PRODUCT_COUNT,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SALE};

        return new CursorLoader(this, ProductContract.ProductEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        Log.d(LOG_TAG,"onLoadFinished called data: "+data);

        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(LOG_TAG,"onLoaderReset called");
        mCursorAdapter.swapCursor(null);

    }
}
