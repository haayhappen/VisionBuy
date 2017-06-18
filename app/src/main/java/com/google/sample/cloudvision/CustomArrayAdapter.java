package com.google.sample.cloudvision;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Fynn on 18.06.2017.
 */

public class CustomArrayAdapter extends ArrayAdapter {

    List<AmazonParser.Item> itemlist;
    Context con;


    public CustomArrayAdapter(Context context, List<AmazonParser.Item> list){
        super(context,0,list);
        itemlist = list;
        con = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            LayoutInflater inflator = null;
            convertView = inflater.inflate(R.layout.list_row,parent,false);
// inflate custom layout called row
            holder = new ViewHolder();
            holder.titletextview =(TextView) convertView.findViewById(R.id.titletextview);
            holder.brandtextview =(TextView) convertView.findViewById(R.id.brandtextview);
            holder.pricetextview =(TextView) convertView.findViewById(R.id.pricetextview);


// initialize textview
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        AmazonParser.Item item = itemlist.get(position);
        holder.titletextview.setText(item.title);
        holder.brandtextview.setText(item.brand);
        holder.pricetextview.setText(item.foramattedPrice);

        // set the name to the text;

        return convertView;

    }

    static class ViewHolder
    {

        TextView titletextview;
        TextView brandtextview;
        TextView pricetextview;
        ImageView productimageview;
    }
}

