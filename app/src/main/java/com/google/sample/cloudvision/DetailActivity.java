package com.google.sample.cloudvision;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {

    TextView pricetw;
    TextView titletw;
    TextView brandtw;
    ImageView productimgview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        titletw = (TextView) findViewById(R.id.titletextview);
        brandtw = (TextView) findViewById(R.id.brandtextview);
        pricetw = (TextView) findViewById(R.id.pricetextview);
        productimgview = (ImageView) findViewById(R.id.productimageview);

        Intent i = getIntent();
        Item item = (Item)i.getSerializableExtra("item");

        titletw.setText(item.title);
        brandtw.setText(item.brand);
        pricetw.setText(item.foramattedPrice);
        Glide.with(this).load(item.imageURL).placeholder(R.drawable.ic_image_black_24dp).fitCenter().into(productimgview);
//

        //TODO FILL LAYOUT WITH ITEM PROPERTIES
    }
}
