package com.parking.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;



import java.util.ArrayList;


public class CustomSwipeAdapter extends PagerAdapter {
    private ArrayList<Bitmap> image_resources = new ArrayList<Bitmap>();
    private Context ctx;
    private LayoutInflater layoutInflater;
    Boolean noImageSelected = true;

    public CustomSwipeAdapter(Context ctx) {
        this.ctx = ctx;
        image_resources.add(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.no_image_selected));
    }

    @Override
    public int getCount() {
        return image_resources.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == (LinearLayout)object);
    }

    @Override
    public int getItemPosition(Object object) {
        int index = image_resources.indexOf(object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.swipe_layout, container, false);
        ImageView imageView = (ImageView) item_view.findViewById(R.id.image_view);
        imageView.setImageBitmap(image_resources.get(position));
        container.addView(item_view);

        return item_view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)object);
    }

    public int addImageResource(ViewPager pager, Bitmap bitmap){

        if (noImageSelected) {
            image_resources.set(0, bitmap);
            notifyDataSetChanged();
            noImageSelected = false;
            return image_resources.size();
        } else {
            image_resources.add(image_resources.size(), bitmap);
            notifyDataSetChanged();
            return image_resources.size();
        }
    }
}
