package com.google.sample.cloudvision;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class MasterActivity extends AppCompatActivity {

    SignedRequestsHelperOLD helper;
    UrlParameterHandler parameterHandler;
    TextView xmlView;
    String xml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        xmlView = (TextView) findViewById(R.id.xmltextview);

        ArrayList<String> keywords = getIntent().getStringArrayListExtra("keys");

        String requestUrl = null;
        parameterHandler = UrlParameterHandler.getInstance();
        try {
            helper = new SignedRequestsHelperOLD();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        requestUrl = helper.sign(parameterHandler.buildMapForItemSearch(keywords));
        System.out.println("Signed URL:"+requestUrl);

        Parser parser = new Parser();
        parser.setContext(this);
        xml = parser.getUrlContents(requestUrl);

        if (xml.toString() != null) {
            xmlView.setText(xml.toString());
        }

    }
}
