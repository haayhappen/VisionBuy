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

import java.util.ArrayList;
import java.util.List;

import static android.R.id.list;

/**
 * Created by Fynn on 18.06.2017.
 */

public class CustomArrayAdapter extends ArrayAdapter implements View.OnClickListener {

    //ViewHolder Pattern class
    private static class ViewHolder {
        //Represents ListRow components
        TextView titletextview;
        TextView brandtextview;
        TextView pricetextview;
        ImageView productimageview;
    }

    //Declarations---------------------------
    //Items
    List<Item> itemlist = new ArrayList<>();
    //URIs
    List<String> urllist = new ArrayList<>();
    //MasterActivity context
    Context context;
    //LastView position
    private int lastPosition = -1;
    //---------------------------------------


    public CustomArrayAdapter(Context context,int recourceId, ArrayList<Item> list) {
        super(context, recourceId, list);
        // this.itemlist = list;
        this.context = context;

        //Get Urls from Products
        for (Item Item : list) {
            urllist.add(Item.imageURL);
        }
    }

//    public AmazonParser.Item getItem(int position){
//        return itemlist.get(position);
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        Item item = (Item) getItem(position);
        final View result;

//        LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) { //ConvertView can not be reused or doesn't exist
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_row, parent, false);
            viewHolder.titletextview = (TextView) convertView.findViewById(R.id.titletextview);
            viewHolder.brandtextview = (TextView) convertView.findViewById(R.id.brandtextview);
            viewHolder.pricetextview = (TextView) convertView.findViewById(R.id.pricetextview);
            viewHolder.productimageview = (ImageView) convertView.findViewById(R.id.list_imageview);
            //myImageView = (ImageView) inflater.inflate(R.layout.list_row, parent, false);

            //ImageView fotoView = (ImageView) convertView.findViewById(R.id.list_imageview);

            result = convertView;
            convertView.setTag(viewHolder);
        }//ConvertView can be reused -->
        else {
            //myImageView = (ImageView) convertView;
            viewHolder = (ViewHolder) convertView.getTag();
            //result=convertView;
        }

        String url = urllist.get(position);
        //Item item = itemlist.get(position);

//        Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//        result.startAnimation(animation);
//        lastPosition = position;
        Glide.with(context).load(url).placeholder(R.drawable.ic_image_black_24dp).error(R.drawable.ic_image_black_24dp).into(viewHolder.productimageview);

        viewHolder.titletextview.setText(item.title);
        viewHolder.brandtextview.setText(item.brand);
        viewHolder.pricetextview.setText(item.foramattedPrice);
        //viewHolder.productimageview
        //holder.productimageview.set
        // set the name to the text;

        return convertView;

    }

    @Override
    public void onClick(View view) {
        int position = (Integer) view.getTag();
        Object object = getItem(position);
        Item item = (Item) object;

        Intent i = new Intent(context, DetailActivity.class);
        i.putExtra("item", item);
        context.startActivity(i);
        //TODO INTENT TO DETAILVIEW
    }


}

