package com.google.sample.cloudvision;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * Created by Fynn on 17.06.2017.
 */


public class AmazonParser {



    private static final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List items = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "ItemSearchResponse");
        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("Items")) {
                parser.require(XmlPullParser.START_TAG, ns, "Items");
//            Log.i("Parser","Parser.getName ="+parser.getName());
                while (parser.next() != XmlPullParser.END_TAG) {

                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String namee = parser.getName();
                    if (namee.equals("Item")) {
                        Log.i("Parser", "New Item found! --> Adding to items list");
                        items.add(readEntry(parser));
                    } else {
                        skip(parser);
                    }
                }
            } else {
                skip(parser);
            }
        }
        return items;
    }


    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private Item readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.i("Parser", "ReadEntry Method: " + parser.getText() + parser.getName());
        parser.require(XmlPullParser.START_TAG, ns, "Item");
        String title = null;
        String brand = null;
        String formattedPrice = null;
        String imageURL = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            Log.i("Parser", parser.getName());
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("SmallImage")) {
                imageURL = readImageUrl(parser);
            } else if (name.equals("ItemAttributes")) {
                //dig into -->
                parser.require(XmlPullParser.START_TAG, ns, "ItemAttributes");
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String namee = parser.getName();
                    if (namee.equals("Title")) {
                        title = readTitle(parser);
                    } else if (namee.equals("Brand")) {
                        brand = readBrand(parser);
//                    } else if (namee.equals("ListPrice")) {
//                        parser.require(XmlPullParser.START_TAG, ns, "ListPrice");
//                        while (parser.next() != XmlPullParser.END_TAG) {
//                            if (parser.getEventType() != XmlPullParser.START_TAG) {
//                                continue;
//                            }
//                            String nameee = parser.getName();
//                            if (nameee.equals("FormattedPrice")) {
//                                formattedPrice = readPrice(parser);
//                            } else {
//                                skip(parser);
//                            }
//                        }
                    } else {
                        skip(parser);
                    }
                }
            }else if (name.equals("OfferSummary")) {
                while (parser.next() != XmlPullParser.END_TAG) {

                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String namex = parser.getName();
                    if (namex.equals("LowestNewPrice")) {
                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.getEventType() != XmlPullParser.START_TAG) {
                                continue;
                            }
                            String namew = parser.getName();

                            if (namew.equals("FormattedPrice")) {
                                formattedPrice = readPrice(parser);
                            }else skip(parser);
                        }

                    } else {
                        skip(parser);
                    }
                }
            }
            else {
                skip(parser);
            }

//            if (name.equals("Title")) {
//                title = readTitle(parser);
//            } else if (name.equals("SmallImage")) {
//                imageURL = readImageUrl(parser);
//            } else if (name.equals("Brand")) {
//                brand = readBrand(parser);
//            } else if (name.equals("FormattedPrice")) {
//                formattedPrice = readPrice(parser);
//            } else {
//                skip(parser);
//            }
        }
        return new Item(title, brand, formattedPrice, imageURL);
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "Title");
        return title;
    }

    // Processes smallimage tags in the feed.
    private String readImageUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
        String url = "";
        parser.require(XmlPullParser.START_TAG, ns, "SmallImage");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("URL")) {
                url = readURL(parser);
            } else {
                skip(parser);
            }
        }

        return url;


//        parser.nextTag();
//        parser.require(XmlPullParser.START_TAG,ns,"URL");
//        url = readText(parser);
//        parser.require(XmlPullParser.START_TAG,ns,"URL");
        //String tag = parser.getName();
//        parser.require(XmlPullParser.END_TAG, ns, "SmallImage");
    }


    private String readURL(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "URL");
        String url = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "URL");
        return url;
    }

    // Processes brand tags in the feed.
    private String readBrand(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Brand");
        String brand = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "Brand");
        return brand;
    }

    // For the tags title and summary, extracts their text values.
    private String readPrice(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "FormattedPrice");
        String price = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "FormattedPrice");
        return price;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }


    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
