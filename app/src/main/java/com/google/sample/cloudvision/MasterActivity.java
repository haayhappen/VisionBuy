package com.google.sample.cloudvision;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static android.R.attr.entries;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.google.sample.cloudvision.R.id.xmlview;

public class MasterActivity extends AppCompatActivity {

    private static final String AWS_ACCESS_KEY_ID = "AKIAIOGZ47QZFXV2OYIA";

    private static final String AWS_SECRET_KEY = "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";

    private static final String ENDPOINT = "webservices.amazon.de";
    ListView xmlView;
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

    TextView tw;
    Parser parser;
    String requestUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        xmlView = (ListView) findViewById(R.id.list);

        ArrayList<String> keywords = getIntent().getStringArrayListExtra("keys");

        //tw = (TextView) findViewById(R.id.textView);

        SignedRequestsHelper helper;

        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        if (keywords != null) {
            StringBuilder sb = new StringBuilder();
            for (String s : keywords) {
                sb.append(s);
                sb.append("\t");
            }
        }
        Map<String, String> params = new HashMap<String, String>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemSearch");
        params.put("AWSAccessKeyId", "AKIAIOGZ47QZFXV2OYIA");
        params.put("AssociateTag", "visi05-21");
        params.put("SearchIndex", "All");
        params.put("ContentType", "text/xml");
        params.put("Keywords", "iphone");
        params.put("ResponseGroup", "Images,ItemAttributes,Offers");

        requestUrl = helper.sign(params);

        System.out.println("RequestUrl: " + requestUrl);
    }

    public void xmlButtonClicked(View v) {
        loadPage(requestUrl);
    }

    public void loadPage(String url) {

        new DownloadXmlTask().execute(url);
    }

    // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
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
            setContentView(R.layout.content_xml);
            ListView lv = (ListView) findViewById(R.id.listview);

            InputStream stream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));

            List<AmazonParser.Item> items = null;
            AmazonParser parser = new AmazonParser();
            try {
                items = parser.parse(stream);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            CustomArrayAdapter caa = new CustomArrayAdapter(MasterActivity.this,items);
            lv.setAdapter(caa);

            //TODO POPULATE LISTVIEW
            String itemstring = "";

            for (AmazonParser.Item item : items) {
                if (item.title == null){
                    item.title = "No title available\n";
                    itemstring += item.title;
                } else itemstring += item.title+"\n";
                if (item.brand == null){
                    item.brand = "No brand available\n";
                    itemstring += item.brand;
                } else itemstring += item.brand+"\n";
                if (item.foramattedPrice == null){
                    item.foramattedPrice = "No price available\n";
                    itemstring += item.foramattedPrice;
                }else itemstring += item.foramattedPrice+"\n";
                if (item.imageURL == null){
                    item.imageURL = "No Image Url available\n";
                    itemstring += item.imageURL;
                }else itemstring += item.imageURL+"\n";

                itemstring += "\n\n";
            }
            Toast.makeText(MasterActivity.this, "Items successfully parsed!", Toast.LENGTH_SHORT).show();
            TextView xmlview;
            xmlview = (TextView) findViewById(R.id.xmlview);
             xmlview.setText(itemstring);

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

//    private AmazonParser.Item loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
//        InputStream stream = null;
//        // Instantiate the parser
//        AmazonParser amazonParser = new AmazonParser();
//        List<AmazonParser.Item> items = null;
//        String title = null;
//        String url = null;
//        String summary = null;
////        Calendar rightNow = Calendar.getInstance();
////        DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");
//
//        // Checks whether the user set the preference to include summary text
////        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
////        boolean pref = sharedPrefs.getBoolean("summaryPref", false);
//
////        StringBuilder htmlString = new StringBuilder();
////        htmlString.append("<h3>" + getResources().getString(R.string.page_title) + "</h3>");
////        htmlString.append("<em>" + getResources().getString(R.string.updated) + " " +
////                formatter.format(rightNow.getTime()) + "</em>");
//
//        try {
//            stream = downloadUrl(urlString);
//            items = amazonParser.parse(stream);
//            // Makes sure that the InputStream is closed after the app is
//            // finished using it.
//        } finally {
//            if (stream != null) {
//                stream.close();
//            }
//        }
//
//        // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
//        // Each Entry object represents a single post in the XML feed.
//        // This section processes the entries list to combine each entry with HTML markup.
//        // Each entry is displayed in the UI as a link that optionally includes
//        // a text summary.
//        for (AmazonParser.Item item : items) {
//            //TODO FILL LISTVIEW WITH ITEMS
//        }
//        return items;
//    }

    // Given a string representation of a URL, sets up a connection and gets
// an input stream.
//    private InputStream downloadUrl(String urlString) throws IOException {
//        URL url = new URL(urlString);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setReadTimeout(10000 /* milliseconds */);
//        conn.setConnectTimeout(15000 /* milliseconds */);
//        conn.setRequestMethod("GET");
//        conn.setDoInput(true);
//        // Starts the query
//        conn.connect();
//        return conn.getInputStream();
//    }
}


//        parser = new Parser();
//        parser.setContext(this);
//        //xml = parser.getUrlContents(requestUrl);
//
//
//        // Instantiate the RequestQueue.
//        RequestQueue queue = Volley.newRequestQueue(this);
//
//// Request a string response from the provided URL.
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        //Handle Response
////                        xml = response;
////                        tw.setText(response);
//
//                        Document doc = parser.getDomElement(response);
//                        //get the item tags content
////                        System.out.println(getStringFromDoc(doc));
//
//                        NodeList itemNodeList = doc.getElementsByTagName(KEY_ITEM);
//                        System.out.println("itemNodeListLength: "+itemNodeList.getLength());
//                        //loop for item attributes
//                        for (int i = 0; i < itemNodeList.getLength(); i++) {
//                            HashMap<String, String> map = new HashMap<String, String>();
//                            Element e = (Element) itemNodeList.item(i);
//
//
////                            NodeList itemAttributeNodeList = doc.getElementsByTagName(KEY_ITEM_ATTRIBUTES);
////                            System.out.println("itemAttributesNodeListLength: "+itemAttributeNodeList.getLength());
//
////                            for (int j = 0; j < itemNodeList.getLength(); j++) {
////                                Element f = (Element) itemNodeList.item(i);
////                                map.put(KEY_TITLE,parser.getValue(f,KEY_TITLE));
////
////                                for (int r = 0; r < itemNodeList.getLength(); r++) {
////                                    Element g = (Element) itemNodeList.item(i);
////                                    map.put(KEY_LIST_PRICE, parser.getValue(g, KEY_TITLE));
////
////                                    for (int k = 0; k < itemNodeList.getLength(); k++) {
////                                        Element l = (Element) itemNodeList.item(i);
////                                        map.put(KEY_LIST_PRICE, parser.getValue(l, KEY_LIST_PRICE));
////                                    }
////                                }
////                            }
////                            System.out.println("Nodename: "+itemNodeList.item(i).getNodeName());
//                            map.put(KEY_ASIN,parser.getValue(e,KEY_ASIN));
//                            map.put(KEY_TITLE,parser.getValue(e,KEY_TITLE));
//                            map.put(KEY_FORMATTED_PRICE,parser.getValue(e,KEY_FORMATTED_PRICE));
//
//
//                            //map.put("ParentASIN",parser.getValue(e,"ParentASIN"));
//                            products.add(map);
//                        }
//
//                        StringBuilder sb = new StringBuilder();
//                        for (HashMap s : products)
//                        {
//                            sb.append(s.toString());
//                            sb.append("\t");
//                        }
//                        tw.setText(sb.toString());
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                //Handle ERROR
//                Log.d("RequestError", "Error while getting xml: " + error.networkResponse.toString());
//            }
//        });
//// Add the request to the RequestQueue.
//        queue.add(stringRequest);
//    }
//    public static String getStringFromDoc(Document doc) {
//        try {
//            StringWriter sw = new StringWriter();
//            TransformerFactory tf = TransformerFactory.newInstance();
//            Transformer transformer = tf.newTransformer();
//            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
//            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//
//            transformer.transform(new DOMSource(doc), new StreamResult(sw));
//            return sw.toString();
//        } catch (Exception ex) {
//            throw new RuntimeException("Error converting to String", ex);
//        }


