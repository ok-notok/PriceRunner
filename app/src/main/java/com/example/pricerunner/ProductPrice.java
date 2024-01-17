package com.example.pricerunner;

public class ProductPrice {
    private String storeName;
    private String price;
    private String currencySymbol;
    private String currency;
    private String url;

    public ProductPrice(String storeName, String price, String currencySymbol, String currency, String url) {
        this.storeName = storeName;
        this.price = price;
        this.currencySymbol = currencySymbol;
        this.currency = currency;
        this.url = url;
    }

    public String getStoreName() {
        return storeName;
    }


    public String getPrice() {
        return price;
    }

    public String getCurrencySymbol() { return currencySymbol; }

    public String getCurrency() { return currency; }

    public String getUrl() { return url; }
}