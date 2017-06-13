package com.google.sample.cloudvision;

import java.util.HashMap;
import java.util.Map;

public class UrlParameterHandler {

    public static UrlParameterHandler paramHandler;
    private UrlParameterHandler() {}


    public static synchronized UrlParameterHandler getInstance(){
        if(paramHandler==null){
            paramHandler=new UrlParameterHandler();
            return paramHandler;
        }
        return paramHandler;
    }
//TODO keywords is the word to search for
    public  Map<String,String> buildMapForItemSearch(){
        Map<String, String> myparams = new HashMap<String, String>();
        myparams.put("Service", "AWSECommerceService");
        myparams.put("Operation", "ItemSearch");
        myparams.put("Version", "2009-10-01");
        myparams.put("ContentType", "text/xml");
        //myparams.put("SearchIndex", "MobileApps");//for searching mobile apps
        myparams.put("Keywords", "iphone");
        myparams.put("AssociateTag", "visi05-21");
        myparams.put("MaximumPrice","1000");
        myparams.put("Sort","price");
        myparams.put("ResponseGroup", "Images,Small");
        return myparams;
    }

}
