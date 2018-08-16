package com.example.android.bookstore;

public class Product {
    private String name;
    private int price;
    private int quantity;
    private String supplier_name;
    private String supplier_phone;

    public Product( String name, int price, int quantity, String supplier_name, String supplier_phone) {
        setName(name);
        setPrice(price);
        setQuantity(quantity);
        setSupplier_name(supplier_name);
        setSupplier_phone(supplier_phone);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSupplier_name() {
        return supplier_name;
    }

    public void setSupplier_name(String supplier_name) {
        this.supplier_name = supplier_name;
    }

    public String getSupplier_phone() {
        return supplier_phone;
    }

    public void setSupplier_phone(String supplier_phone) {
        this.supplier_phone = supplier_phone;
    }
}
