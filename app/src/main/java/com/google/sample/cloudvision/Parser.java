//package com.google.sample.cloudvision;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.afollestad.materialdialogs.MaterialDialog;
//import com.loopj.android.http.*;
//
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//
//import java.io.BufferedReader;
//import java.io.Console;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.StringReader;
//import java.io.UnsupportedEncodingException;
//import java.net.URL;
//import java.net.URLConnection;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//
//import cz.msebera.android.httpclient.Header;
//
//import static java.lang.System.out;
//
//
//public class Parser {
//    /** ---------------------  Search TAG --------------------- */
//    private static final String KEY_ROOT="Items";
//    private static final String KEY_REQUEST_ROOT="Request";
//    private static final String KEY_REQUEST_CONTAINER="IsValid";
//    private static final String KEY_ITEM="Item";
//    private static final String KEY_ID="ASIN";
//    private static final String KEY_ITEM_URL="DetailPageURL";
//    private static final String KEY_IMAGE_ROOT="MediumImage";
//    private static final String KEY_IMAGE_CONTAINER="URL";
//    private static final String KEY_ITEM_ATTR_CONTAINER="ItemAttributes";
//    private static final String KEY_ITEM_ATTR_TITLE="Title";
//
//    private static final String VALUE_VALID_RESPONCE="True";
//
//    Context mainContext;
//    String responseXML = null;
//    final StringBuilder strBuilder = new StringBuilder();
//
//    //Tags
//    //Items,Request,IsValid,Item,ASIN,DetailPageURL,MediumImage,URL,ItemAttributes,Title
//
//    public void setContext(Context c){
//        this.mainContext = c;
//    }
//
//    public NodeList getResponseNodeList(String service_url) {
//        String searchResponse = this.getUrlContents(service_url);
//        Log.i("url",""+service_url);
//        Log.i("response",""+searchResponse);
//        Document doc;
//        NodeList items = null;
//        if (searchResponse != null) {
//            try {
//                doc = this.getDomElement(searchResponse);
//                items = doc.getElementsByTagName(KEY_ROOT);
//                Element element=(Element)items.item(0);
//                if(isResponceValid(element)){
//                    items=doc.getElementsByTagName(KEY_ITEM);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return items;
//    }
///*
//    public SearchObject getSearchObject(NodeList list,int position){
//        SearchObject object = new SearchObject();
//        Element e=(Element)list.item(position);
//        object.setUrl(this.getValue(e, KEY_ITEM_URL));
//        object.setId(this.getValue(e, KEY_ID));
//        object.setImageUrl(this.getValue((Element)(e.getElementsByTagName(KEY_IMAGE_ROOT).item(0))
//                , KEY_IMAGE_CONTAINER));
//        object.setTitle(this.getValue((Element)(e.getElementsByTagName(KEY_ITEM_ATTR_CONTAINER).item(0))
//                , KEY_ITEM_ATTR_TITLE));
//        return object;
//    }
//*/
//    public boolean isResponceValid(Element element){
//        NodeList nList=element.getElementsByTagName(KEY_REQUEST_ROOT);
//        Element e=(Element)nList.item(0);
//        if(getValue(e, KEY_REQUEST_CONTAINER).equals(VALUE_VALID_RESPONCE)){
//            return true;
//        }
//        return false;
//    }
//
//    /** In app reused functions */
//
//    public String getUrlContents(String theUrl) {
//        StringBuilder content = new StringBuilder();
//        MaterialDialog.Builder builder = new MaterialDialog.Builder(mainContext)
//                .title("Dialog")
//                .content("Please wait while fetching data")
//                .progress(true, 0);
//
//        MaterialDialog dialog = builder.build();
//        dialog.show();
//
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.get(theUrl, new AsyncHttpResponseHandler() {
//
//            @Override
//            public void onStart() {
//                // called before request is started
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//
//                String body = null;
//                try {
//                    body = new String(responseBody, "UTF-8");
//                    Log.d("OnSuccess","StatusCode: "+statusCode+" ResponseBody: "+ body);
//                    strBuilder.append(body);
//
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//
//                //Parser.this.responseXML = body;
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//
//                String body = null;
//                try {
//                    body = new String(responseBody, "UTF-8");
//                    Log.d("OnFailure","StatusCode: "+statusCode+" ResponseBody: "+ body);
//                    strBuilder.append(body);
//
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//
//                //Parser.this.responseXML = body;
//            }
//
//            @Override
//            public void onRetry(int retryNo) {
//                // called when request is retried
//            }
//        });
//
////        try {
////            URL url = new URL(theUrl);
////            URLConnection urlConnection = url.openConnection();
////            BufferedReader bufferedReader = new BufferedReader(
////                    new InputStreamReader(urlConnection.getInputStream()), 8);
////            String line;
////            while ((line = bufferedReader.readLine()) != null) {
////                content.append(line + "");
////            }
////            bufferedReader.close();
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//        dialog.dismiss();
////        return content.toString();
//        return strBuilder.toString();
//    }
//
//    public Document getDomElement(String xml) {
//        Document doc = null;
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        try {
//
//            DocumentBuilder db = dbf.newDocumentBuilder();
//
//            InputSource is = new InputSource();
//            is.setCharacterStream(new StringReader(xml));
//            doc = (Document) db.parse(is);
//
//        } catch (ParserConfigurationException e) {
//            Log.e("Error: ", e.getMessage());
//            return null;
//        } catch (SAXException e) {
//            Log.e("Error: ", e.getMessage());
//            return null;
//        } catch (IOException e) {
//            Log.e("Error: ", e.getMessage());
//            return null;
//        }
//
//        return doc;
//    }
//
//    public final String getElementValue(Node elem) {
//        Node child;
//        if (elem != null) {
//            if (elem.hasChildNodes()) {
//                for (child = elem.getFirstChild(); child != null; child = child
//                        .getNextSibling()) {
//                    if (child.getNodeType() == Node.TEXT_NODE
//                            || (child.getNodeType() == Node.CDATA_SECTION_NODE)) {
//                        return child.getNodeValue();
//                    }
//                }
//            }
//        }
//        return "";
//    }
//
//    public String getValue(Element item, String str) {
//        NodeList n = item.getElementsByTagName(str);
//        return this.getElementValue(n.item(0));
//    }
//}
