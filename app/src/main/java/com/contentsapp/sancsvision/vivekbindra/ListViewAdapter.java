package com.contentsapp.sancsvision.vivekbindra;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by abhinandan on 02/06/18.
 */

//https://www.101apps.co.za/articles/gridview-tutorial-using-the-universal-image-loader-library.html
//https://www.journaldev.com/10416/android-listview-with-custom-adapter-example-tutorial
public class ListViewAdapter extends ArrayAdapter {

    private ArrayList  dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView textDesc;
        ImageView imageView;
    }

    public ListViewAdapter(ArrayList data, Context context) {
        super(context, R.layout.row_item , data);
        this.dataSet = data;
        this.mContext=context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject dataModel = null;
        try {
             dataModel = new JSONObject((String) getItem(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.textDesc = (TextView) convertView.findViewById(R.id.textView);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            viewHolder.imageView.setTag(position);
            result=convertView;

            result.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

//        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//        result.startAnimation(animation);
//        lastPosition = position;

        try {
            viewHolder.textDesc.setText(dataModel.getString("title"));
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions options=new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .resetViewBeforeLoading(true)
                    .build();
            imageLoader.displayImage(dataModel.getString("cover_image"),viewHolder.imageView , options);
//            imageLoader.displayImage("https://img.youtube.com/vi/nuq-wvTdFho/0.jpg",viewHolder.imageView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}
