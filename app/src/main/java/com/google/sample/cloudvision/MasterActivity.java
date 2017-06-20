package com.google.sample.cloudvision;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterActivity extends AppCompatActivity {

    private static final String AWS_ACCESS_KEY_ID = "AKIAIOGZ47QZFXV2OYIA";

    private static final String AWS_SECRET_KEY = "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";

    private static final String ENDPOINT = "webservices.amazon.de";
    ListView listview;
    String xml;

    //parent
    static final String KEY_ITEM = "Item";
    //inside item
    static final String KEY_ITEM_ATTRIBUTES = "ItemAttributes";
    //inside itemattributes
    static final String KEY_LIST_PRICE = "ListPrice";
    //inside listprice
    static final String KEY_FORMATTED_PRICE = "FormattedPrice";
    //inside itemattriubtes
    static final String KEY_TITLE = "Title";

    //inside item
    static final String KEY_SMALL_IMAGE = "SmallImage";
    //inside smallimage
    static final String KEY_URL = "Url";

    //inside item
    static final String KEY_ASIN = "ASIN";


    ArrayList<HashMap<String, String>> products = new ArrayList<HashMap<String, String>>();

    public TextView tw;
    public Parser parser;
    public String requestUrl = null;
    public SignedRequestsHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        listview = (ListView) findViewById(R.id.listview);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO HANDLE ITEMCKLCIK
                Toast.makeText(MasterActivity.this, "Item clicked, onItemClickListener in MasterActivity", Toast.LENGTH_LONG);
            }
        });

        //gets the keywords from the MainActivity intent
        ArrayList<String> keywords = getIntent().getStringArrayListExtra("keys");

        try {
            //get the SignedRequestHelper instance with Endpoint and specified credentials
            helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //build a keyword search string
        if (keywords != null) {
            StringBuilder sb = new StringBuilder();
            for (String s : keywords) {
                sb.append(s);
                sb.append("\t");
            }
        }
        //build Hashmap for QueryParams
        Map<String, String> params = new HashMap<String, String>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemSearch");
        params.put("AWSAccessKeyId", "AKIAIOGZ47QZFXV2OYIA");
        params.put("AssociateTag", "visi05-21");
        params.put("SearchIndex", "All");
        params.put("ContentType", "text/xml");
        //TODO REMOVE HARDCODED KEYWORDs
        params.put("Keywords", "iphone");
        params.put("ResponseGroup", "Images,ItemAttributes,Offers");

        //sign the url with params
        requestUrl = helper.sign(params);

        //TODO REMOVE FOR PRODUCTION
        System.out.println("RequestUrl: " + requestUrl);
    }

    public void xmlButtonClicked(View v) {
        loadPage(requestUrl);
    }

    public void loadPage(String url) {

        new DownloadXmlTask().execute(url);
    }

    // Implementation of AsyncTask used to download XML products from amazon
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            URL url = null;
            try {
                url = new URL(urls[0]);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            InputStream stream = null;
            HttpURLConnection connection = null;
            String result = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                // Timeout for reading InputStream arbitrarily set to 3000ms.
                connection.setReadTimeout(3000);
                // Timeout for connection.connect() arbitrarily set to 3000ms.
                connection.setConnectTimeout(3000);
                // For this use case, set HTTP method to GET.
                connection.setRequestMethod("GET");
                // Already true by default but setting just in case; needs to be true since this request
                // is carrying an input (response) body.
                connection.setDoInput(true);
                // Open communications link (network traffic occurs here).
                connection.connect();
                //publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }
                // Retrieve the response body as an InputStream.
                stream = connection.getInputStream();
                //publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
                if (stream != null) {
                    // Converts Stream to String with max length of 500.
                    result = readStream(stream, 1000000);
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Close Stream and disconnect HTTPS connection.
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            setContentView(R.layout.activity_master);

            InputStream stream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));


            List<Item> items = null;
            AmazonParser parser = new AmazonParser();
            try {
                items = parser.parse(stream);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<Item> arrayListItems= new ArrayList<Item>();
            arrayListItems.addAll(items);

            CustomArrayAdapter caa = new CustomArrayAdapter(MasterActivity.this, arrayListItems);
            listview.setAdapter(caa);
            caa.addAll(items);

            //TODO POPULATE LISTVIEW
            String itemstring = "";

            for (Item item : items) {
                if (item.title == null) {
                    item.title = "No title available\n";
                    itemstring += item.title;
                } else itemstring += item.title + "\n";
                if (item.brand == null) {
                    item.brand = "No brand available\n";
                    itemstring += item.brand;
                } else itemstring += item.brand + "\n";
                if (item.foramattedPrice == null) {
                    item.foramattedPrice = "No price available\n";
                    itemstring += item.foramattedPrice;
                } else itemstring += item.foramattedPrice + "\n";
                if (item.imageURL == null) {
                    item.imageURL = "No Image Url available\n";
                    itemstring += item.imageURL;
                } else itemstring += item.imageURL + "\n";

                itemstring += "\n\n";
            }
            Toast.makeText(MasterActivity.this, "Items successfully parsed!", Toast.LENGTH_SHORT).show();
//            TextView xmlview;
//            xmlview = (TextView) findViewById(R.id.xmlview);
//            xmlview.setText(itemstring);

//            // Displays the HTML string in the UI via a WebView
//            WebView myWebView = (WebView) findViewById(R.id.webview);
//            myWebView.loadData(result, "text/html", null);
        }

        /**
         * Converts the contents of an InputStream to a String.
         */
        private String readStream(InputStream stream, int maxLength) throws IOException {
            String result = null;
            // Read InputStream using the UTF-8 charset.
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            // Create temporary buffer to hold Stream data with specified max length.
            char[] buffer = new char[maxLength];
            // Populate temporary buffer with Stream data.
            int numChars = 0;
            int readSize = 0;
            while (numChars < maxLength && readSize != -1) {
                numChars += readSize;
                int pct = (100 * numChars) / maxLength;
                //publishProgress(DownloadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS, pct);
                readSize = reader.read(buffer, numChars, buffer.length - numChars);
            }
            if (numChars != -1) {
                // The stream was not empty.
                // Create String that is actual length of response body if actual length was less than
                // max length.
                numChars = Math.min(numChars, maxLength);
                result = new String(buffer, 0, numChars);
            }
            return result;
        }
    }
}


