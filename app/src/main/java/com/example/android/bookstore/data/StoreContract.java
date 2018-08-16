package com.example.android.bookstore.data;

import android.provider.BaseColumns;

public final class StoreContract {

    //to prevent anyone from instantiating the class, give empty constructor
    private StoreContract() {}

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static final class ProductEntry implements BaseColumns {

        /** Name of database table for products */
        public final static String TABLE_NAME = "products";

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="name";

        /**
         * price of the pet.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * quantity of the product.
         *         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * name of the supplier of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_SUPPLIER_NAME= "supplier_name";

        /**
         * phone number of the supplier of the product
         *
         * type: TEXT
         */
        public final static String COLUMN_PRODUCT_SUPPLIER_PHONE="supplier_phone";

    }

}
