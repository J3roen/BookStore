package com.example.android.bookstore.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper for the Store app. Manages database creation and version management
 */

public class StoreDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = StoreDbHelper.class.getSimpleName();

    /** name of the database file */
    private static final String DATABASE_NAME ="store.db";

    /**
     * Database version. When changing database schema, increment version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constuct new instance of {@link StoreDbHelper}
     *
     * @param context of the app
     */
    public StoreDbHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when database is created for the first time
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //create String that contains SQL statement to create the products table
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + StoreContract.ProductEntry.TABLE_NAME +" ("
                + StoreContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StoreContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER DEFAULT 0, "
                + StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT NOT NULL, "
                + StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE + " TEXT NOT NULL);";

        //execute SQL statement
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    /**
     * this is called when database needs to be upgraded, leave empty for now
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

}
