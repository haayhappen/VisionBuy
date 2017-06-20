package com.google.sample.cloudvision;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Fynn on 18.06.2017.
 */

public class CustomArrayAdapter extends ArrayAdapter implements View.OnClickListener{

    List<AmazonParser.Item> itemlist;
    Context con;
    List<String> urllist;
    private int lastPosition = -1;



    public CustomArrayAdapter(Context context, List<AmazonParser.Item> list){
        super(context,0,list);
        this.itemlist = list;
        this.con = context;

        for (AmazonParser.Item item : list){
            urllist.add(item.imageURL);
        }
    }

    public AmazonParser.Item getItem(int position){
        return itemlist.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        //AmazonParser.Item item = getItem(position);
        final ImageView myImageView;
        final View result;

        LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) { //ConvertView can not be reused or doesn't exist
            LayoutInflater inflator = null;
            convertView = inflater.inflate(R.layout.list_row,parent,false);
            // inflate custom layout called list_row
            holder = new ViewHolder();
            holder.titletextview =(TextView) convertView.findViewById(R.id.titletextview);
            holder.brandtextview =(TextView) convertView.findViewById(R.id.brandtextview);
            holder.pricetextview =(TextView) convertView.findViewById(R.id.pricetextview);

            //holder.productimageview = (ImageView) convertView.findViewById(R.id.list_imageview);
            myImageView = (ImageView) inflater.inflate(R.layout.list_row, parent, false);


            result = convertView;
            convertView.setTag(holder);
        }//ConvertView can be reused -->
        else
        {
            myImageView = (ImageView) convertView;
            holder = (ViewHolder)convertView.getTag();
            result=convertView;
        }

        String url = urllist.get(position);

        Animation animation = AnimationUtils.loadAnimation(con, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        Glide.with(con).load(url).placeholder(R.drawable.ic_image_black_24dp).error(R.drawable.ic_image_black_24dp).into(myImageView);


        AmazonParser.Item item = itemlist.get(position);
        holder.titletextview.setText(item.title);
        holder.brandtextview.setText(item.brand);
        holder.pricetextview.setText(item.foramattedPrice);
        //holder.productimageview.set
        // set the name to the text;

        return convertView;

    }

    @Override
    public void onClick(View view) {
        int position=(Integer) view.getTag();
        Object object= getItem(position);
        AmazonParser.Item item=(AmazonParser.Item) object;

        Intent i =new Intent(con,DetailActivity.class);
        i.putExtra("item",item);
        con.startActivity(i);
        //TODO INTENT TO DETAILVIEW
    }

    static class ViewHolder
    {

        TextView titletextview;
        TextView brandtextview;
        TextView pricetextview;
        ImageView productimageview;
    }
}

