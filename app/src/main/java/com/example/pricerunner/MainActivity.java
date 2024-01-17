package com.example.pricerunner;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnScan;
    private TextView tvScanResult;
    private ListView lvPrices;
    private ArrayAdapter<String> adapter;
    private List<String> priceList;
    List<ProductPrice> prices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = findViewById(R.id.btnScan);
        tvScanResult = findViewById(R.id.tvScanResult);
        lvPrices = findViewById(R.id.lvPrices);

        priceList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, priceList);
        lvPrices.setAdapter(adapter);

        lvPrices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProductPrice productPrice = prices.get(position);
                String url = productPrice.getUrl();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setOrientationLocked(false);
                new IntentIntegrator(MainActivity.this).initiateScan();
            }
        });
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                tvScanResult.setText("Scan was Cancelled");
            } else {
                String scannedBarcode = result.getContents();
                tvScanResult.setText("Scanned: " + scannedBarcode);
                fetchPrices(scannedBarcode);
            }
        }
    }

    private void fetchPrices(String barcode) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://barcode-lookup.p.rapidapi.com/v3/products?barcode=" + barcode;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-RapidAPI-Key", "YOUR_API_KEY_HERE")
                .addHeader("X-RapidAPI-Host", "barcode-lookup.p.rapidapi.com")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Log.d("API Response", responseData);
                    List<ProductPrice> prices = parsePrices(responseData);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            priceList.clear();
                            for (ProductPrice price : prices) {
                                String s = price.getStoreName() + ": " + price.getPrice() + price.getCurrencySymbol() + " " + price.getCurrency();
                                priceList.add(s);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    private List<ProductPrice> parsePrices(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray products = jsonObject.getJSONArray("products");
            if (products.length() > 0) {
                JSONObject firstProduct = products.getJSONObject(0);
                JSONArray stores = firstProduct.getJSONArray("stores");
                for (int i = 0; i < stores.length(); i++) {
                    JSONObject store = stores.getJSONObject(i);
                    String storeName = store.getString("name");
                    String price = store.getString("price");
                    String currencySymbol = store.getString("currency_symbol");
                    String currency = store.getString("currency");
                    String url = store.getString("link");
                    prices.add(new ProductPrice(storeName, price, currencySymbol, currency, url));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return prices;
    }

}