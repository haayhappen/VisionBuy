package com.google.sample.cloudvision;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MasterActivity extends AppCompatActivity {

    private static final String AWS_ACCESS_KEY_ID = "AKIAIAPTMNSTWRMA2UUA";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        xmlView = (ListView) findViewById(R.id.list);

        ArrayList<String> keywords = getIntent().getStringArrayListExtra("keys");

        tw = (TextView) findViewById(R.id.textView);

        SignedRequestsHelper helper;

        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String requestUrl = null;

        StringBuilder sb = new StringBuilder();
        for (String s : keywords) {
            sb.append(s);
            sb.append("\t");
        }

        Map<String, String> params = new HashMap<String, String>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemSearch");
        params.put("AWSAccessKeyId", "AKIAIAPTMNSTWRMA2UUA");
        params.put("AssociateTag", "visi05-21");
        params.put("SearchIndex", "All");
        params.put("ContentType", "text/xml");
        params.put("Keywords", "iphone");
        params.put("ResponseGroup", "Images,ItemAttributes,Offers");

        requestUrl = helper.sign(params);

        System.out.println("RequestUrl: " + requestUrl);


        parser = new Parser();
        parser.setContext(this);
        //xml = parser.getUrlContents(requestUrl);


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Handle Response
//                        xml = response;
//                        tw.setText(response);

                        Document doc = parser.getDomElement(response);
                        //get the item tags content
//                        System.out.println(getStringFromDoc(doc));

                        NodeList itemNodeList = doc.getElementsByTagName(KEY_ITEM);
                        System.out.println("NodeListLength: "+itemNodeList.getLength());
                        //loop for item attributes
                        for (int i = 0; i < itemNodeList.getLength(); i++) {

                            NodeList itemAttributeNodeList = doc.getElementsByTagName(KEY_ITEM_ATTRIBUTES);
                            System.out.println("NodeListLength: "+itemAttributeNodeList.getLength());

                            System.out.println("Nodename: "+itemNodeList.item(i).getNodeName());
                            HashMap<String, String> map = new HashMap<String, String>();
                            Element e = (Element) itemNodeList.item(i);

                            map.put(KEY_ASIN,parser.getValue(e,KEY_ASIN));
                            //map.put("ParentASIN",parser.getValue(e,"ParentASIN"));
                            products.add(map);
                        }

                        StringBuilder sb = new StringBuilder();
                        for (HashMap s : products)
                        {
                            sb.append(s.toString());
                            sb.append("\t");
                        }
                        tw.setText(sb.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Handle ERROR
                Log.d("RequestError", "Error while getting xml: " + error.getMessage());
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);




    }
    public static String getStringFromDoc(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }
}
