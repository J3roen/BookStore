//TODO fix "call supplier" button
package com.example.android.bookstore;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.android.bookstore.data.StoreContract;

/**
 * Display list of products that were entered & stored in the app
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Adapter for the ListView
     **/
    ProductCursorAdapter mCursorAdapter;

    /**
     * identifier for the product data loader
     **/
    private static final int PRODUCT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find the ListView which will be populated with product data
        ListView productListView = findViewById(R.id.list);

        //Find and set empty view on the ListView, so it only shows when list has no items
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        //setup adapter to create list item for each row of data in Cursor
        //no pet data yet (until loader finishes) so loader = null
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        //Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(StoreContract.ProductEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, getString(R.string.logger_rowsdeleted, rowsDeleted));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from res/menu/menu_main_activity.xml file
        // This menu adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //user clicked on a menu optoin in app bar overflow menu
        switch (item.getItemId()) {
            //when clicked on 'add new product' ->start editorActivity
            case R.id.action_add_new_product:
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_delete_all_entries:
                //respond to 'delete all entries' clicked
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //define a projection that specifies the comuns from the table we care about, see project rubric
        String[] projection = {
                StoreContract.ProductEntry._ID,     //we always want the ID
                StoreContract.ProductEntry.COLUMN_PRODUCT_NAME,     //we also want the product name
                StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE,    //we also want the product price
                StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY}; //we also want the product quantity

        //this loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, //parent activity context,
                StoreContract.ProductEntry.CONTENT_URI, //provider content URI to query
                projection,                              //columns to include in resulting Cursor
                null,                           //no selection clause
                null,                       // no selection arguments
                null                            // default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //update @ProductCursorAdapter with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);

    }
}
