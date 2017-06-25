package com.google.sample.cloudvision;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MasterActivity extends AppCompatActivity {


    private static final String AWSK = "AKIAI4ZF6DHYJ6XBQ7KA";
    private static final String AWSSK = "Md4GE9WUoo8aBlBP9LHKP";
    private static final String AWSSK1 = "HTLTtDGA+9663fOaWsr";

    private static final String ENDPOINT_DE = "webservices.amazon.de";
    private static final String ENDPOINT_US = "webservices.amazon.com";

    ListView listview;

    private static final String ASSOCIATE_ID_DE = "visi05-21";
    private static final String ASSOCIATE_ID_US = "visionbuy-20";
//    //parent
//    static final String KEY_ITEM = "Item";
//    //inside item
//    static final String KEY_ITEM_ATTRIBUTES = "ItemAttributes";
//    //inside itemattributes
//    static final String KEY_LIST_PRICE = "ListPrice";
//    //inside listprice
//    static final String KEY_FORMATTED_PRICE = "FormattedPrice";
//    //inside itemattriubtes
//    static final String KEY_TITLE = "Title";
//
//    //inside item
//    static final String KEY_SMALL_IMAGE = "SmallImage";
//    //inside smallimage
//    static final String KEY_URL = "Url";
//
//    //inside item
//    static final String KEY_ASIN = "ASIN";


    //ArrayList<HashMap<String, String>> products = new ArrayList<HashMap<String, String>>();
//    ArrayList<HashMap<String, String>> products = new ArrayList<>();
//
//    public TextView tw;
//    public Parser parser;
    public String requestUrl = null;
    public SignedRequestsHelper helper;
    MaterialDialog.Builder builder;
    MaterialDialog dialog;
    TextView keywordstw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        keywordstw = (TextView) findViewById(R.id.keywordtextview);

        //gets the keywords from the MainActivity intent
        ArrayList<String> keywords = getIntent().getStringArrayListExtra("keys");
        //build a keyword search string
        StringBuilder sb = new StringBuilder();
        if (keywords != null) {

            for (String s : keywords) {
                sb.append(s);
                sb.append(" ");
            }
        }

        keywordstw.setText("Searching for Product with Keywords: "+sb.toString());

        //Setting up Language settings
        //String loc = Locale.getDefault().getISO3Language();
        String currentLocaleAssociateKey = "";
        String currentEndpoint = "";
        if (Locale.getDefault().getISO3Language().equals("deu")){
            currentLocaleAssociateKey = ASSOCIATE_ID_DE;
            currentEndpoint = ENDPOINT_DE;
        }else{
            currentLocaleAssociateKey = ASSOCIATE_ID_US;
            currentEndpoint = ENDPOINT_US;
        }
        //get the SignedRequestHelper instance with Endpoint and specified credentials
        try {
            helper = SignedRequestsHelper.getInstance(currentEndpoint, AWSK, AWSSK + AWSSK1);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }




        //build Hashmap for QueryParams
        Map<String, String> params = new HashMap<>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemSearch");
        params.put("AWSAccessKeyId", "AKIAI4ZF6DHYJ6XBQ7KA");
        params.put("AssociateTag", currentLocaleAssociateKey);
        params.put("SearchIndex", "All");
        params.put("ContentType", "text/xml");
        params.put("Keywords", sb.toString());
        params.put("ResponseGroup", "Images,ItemAttributes,Offers");

        //sign the url with params
        requestUrl = helper.sign(params);
        loadPage(requestUrl);
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
        protected void onPreExecute() {
            builder = new MaterialDialog.Builder(MasterActivity.this)
                    .title("Searching for products")
                    .content(R.string.searchProductDelay)
                    .progress(true, 0)
                    .cancelable(false);

            dialog = builder.build();
            dialog.show();
        }

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
            //setContentView(R.layout.activity_master);
            populateListView(result);
            dialog.dismiss();



//
//            String itemstring = "";
//
//            for (Item item : items) {
//                if (item.title == null) {
//                    item.title = "No title available\n";
//                    itemstring += item.title;
//                } else itemstring += item.title + "\n";
//                if (item.brand == null) {
//                    item.brand = "No brand available\n";
//                    itemstring += item.brand;
//                } else itemstring += item.brand + "\n";
//                if (item.foramattedPrice == null) {
//                    item.foramattedPrice = "No price available\n";
//                    itemstring += item.foramattedPrice;
//                } else itemstring += item.foramattedPrice + "\n";
//                if (item.imageURL == null) {
//                    item.imageURL = "No Image Url available\n";
//                    itemstring += item.imageURL;
//                } else itemstring += item.imageURL + "\n";
//
//                itemstring += "\n\n";
//            }
            Toast.makeText(MasterActivity.this, "Items successfully parsed!", Toast.LENGTH_SHORT).show();
//            TextView xmlview;
//            xmlview = (TextView) findViewById(R.id.xmlview);
//            xmlview.setText(itemstring);

        }

        void populateListView(String xmlString) {

            InputStream stream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
            listview = (ListView) findViewById(R.id.listview);

            List<Item> items = null;
            AmazonParser parser = new AmazonParser();
            try {
                items = parser.parse(stream);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }

            ArrayList<Item> arrayListItems = new ArrayList<>();
            if (items != null) {
                arrayListItems.addAll(items);
            }

            CustomArrayAdapter customArrayAdapter = new CustomArrayAdapter(MasterActivity.this, R.layout.list_row, arrayListItems);
            listview.setAdapter(customArrayAdapter);

            final List<Item> finalItems = items;
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    //TODO HANDLE ITEMCKLCIK
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Item " + (position + 1) + ": " + finalItems.get(position),
                            Toast.LENGTH_SHORT);
                    toast.show();


                    Item item = finalItems.get(position);
                    Intent i = new Intent(getApplicationContext(), DetailActivity.class);
                    i.putExtra("item", item);
                    startActivity(i);
                }
            });
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


