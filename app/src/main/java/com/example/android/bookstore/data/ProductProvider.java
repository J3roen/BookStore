package com.example.android.bookstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.bookstore.R;
import com.example.android.bookstore.data.StoreContract.ProductEntry;

public class ProductProvider extends ContentProvider {
    /**
     * Tag for log message
     */
    private static final String LOG_TAG = ProductProvider.class.getSimpleName();

    /**
     * Uri matcher code for content Uri for entire products table
     */
    private static final int PRODUCTS = 100;

    /**
     * Uri matcher code for content Uri for single product in products table
     */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * Input passed into constructor represents the code to return for the root URI.
     * here we use NO_MATCH as input for constructor
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //static initializer, run the first time anything is called from this class
    static {
        //calls to addURI() go here, for all content URI patterns that the provider should recognize.

        //uriMatcher for entire products table
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_PRODUCTS, PRODUCTS);

        //uriMatcher for single row from products table
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private StoreDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new StoreDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //this cursor will hold result of query
        Cursor cursor;

        //figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT_ID:
                //for PRODUCT_ID code, extract out ID from the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                //perform query to return 1 row of entire table, with given ID
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCTS:
                //for PRODUCTS code, query table directly with given params, return entire table
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.error_uri_unknown,uri));
        }

        //set notification URI on the cursor
        //if data changes, cursor gets notified!
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //return cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException(getContext().getString(R.string.error_uri_unknown_match, uri, match));
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.error_insertion_not_supported) + uri);
        }
    }

    /**
     * helper method to insert with given values
     *
     * @param uri    where to insert product
     * @param values values to insert into database
     * @return uri linked to new product
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        //check that name is not null
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_product_no_name));
        }

        //check that price >= 0
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_price_negative));
        }

        //check that quantity >= 0
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_quantity_negative));
        }

        //check that supplier name is not null
        String supplier_name = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        if (supplier_name == null || supplier_name.isEmpty()) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_no_supplier_name));
        }

        //check that supplier phone number is not null
        String supplier_phone = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
        if (supplier_phone == null || supplier_phone.isEmpty()) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_no_supplier_phone));
        }

        //get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //insert new pet with given values
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
        //if ID is -1, insertion failed, log error & return null
        if (id == -1) {
            Log.e(LOG_TAG, getContext().getString(R.string.error_failed_insert_row, uri.toString()));
            return null;
        }

        //Notify all listeners that data has changed for pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        //return new URI with ID appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        //get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                //delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                //delete a single riw given by ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.error_deletion_not_supported, uri));
        }

        //if 1 or more rows were deleted, notify all listeners that data at given URI has been changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //return number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                //extract ID from uri so we know which row to update
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.error_update_not_supported,uri));
        }
    }

    /**
     * helper method to update single product in table
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //if the @COLUMN_PRODUCT_NAME is present, check name is not null
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_product_name_empty));
            }
        }

        //check that price >= 0, if price is present
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_price_negative));
            }
        }

        //check that quantity >= 0, if quantity is present
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_quantity_negative));
            }
        }

        //check that supplier name is not null, if present
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME)) {
            String supplier_name = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            if (supplier_name == null || supplier_name.isEmpty()) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_no_supplier_name));
            }
        }

        //check that supplier phone number is not null, if present
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE)) {
            String supplier_phone = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
            if (supplier_phone == null || supplier_phone.isEmpty()) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_no_supplier_phone));
            }
        }

        //Perform update on database, get number of rows affected
        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        //if 1 or more rows were updated, notify all listeners
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //return number of rows updated
        return rowsUpdated;

    }
}
