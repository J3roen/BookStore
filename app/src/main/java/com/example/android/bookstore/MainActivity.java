package com.example.android.bookstore;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.android.bookstore.data.StoreContract;
import com.example.android.bookstore.data.StoreDbHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private StoreDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //To access database, instantiate StoreDbHelper and pass context
        mDbHelper = new StoreDbHelper(this);

        //create dummy data for testing purpose
        String name = "Scroll of Truth";
        int price = 9999;
        int quantity = 1;
        String supplier_name = "God";
        String supplier_phone ="+32 0800 123 12";

        //create Product object (because OO bisj!)
        Product writeProduct = new Product(name, price, quantity, supplier_name, supplier_phone);
        //insert newProduct in database
        insertProduct(writeProduct);

        //load data from database in cursor, returning list of product objects
        ArrayList<Product> readProduct = readProducts();
    }

    /**
     * helper method to insert product into database
     *
     * @param product
     */
    private void insertProduct(Product product) {
        //gets database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //create a ContentValues object where column names are keys, #product attributes are values
        ContentValues values = new ContentValues();
        values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME, product.getName());
        values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, product.getQuantity());
        values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, product.getSupplier_name());
        values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, product.getSupplier_phone());

        // Insert a new row for product in the database, returning the ID of that new row.
        // The first argument for db.insert() is the product table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for product.
        long newRowId = db.insert(StoreContract.ProductEntry.TABLE_NAME, null, values);
    }

    private ArrayList<Product> readProducts() {
        //create and/or open database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //define a projection that specifies which columns from the database
        // you will actually use after this query
        String[] projection = {
                StoreContract.ProductEntry._ID,
                StoreContract.ProductEntry.COLUMN_PRODUCT_NAME,
                StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE };

        //Perform query on products table
        Cursor cursor = db.query(
                StoreContract.ProductEntry.TABLE_NAME,  //table name
                projection,                             //String[] with columns to return
                null,                          // columns for WHERE clause
                null,                       // values for WHERE clause
                null,                          //don't group the rows
                null,                           // don't filter by row groups
                null);                          // don't sort by specific order

        //fill values into array of @Product objects
        ArrayList<Product> products = new ArrayList<>();
        try {
            //figure out index of each column
            int idColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

            //iterate through the returned rows in the cursor
            while(cursor.moveToNext()) {
                //use index to extract String or Int value of the word at
                //the current row the cursor is on
                int currentId = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                String currentSupplierPhone = cursor.getString(supplierPhoneColumnIndex);

                //create new Product object with those values & add to the array
                products.add(new Product(currentName, currentPrice, currentQuantity, currentSupplierName, currentSupplierPhone));
            }
        } finally {
            //always close cursor when done reading from it
            cursor.close();
            return products;
        }
    }
}
