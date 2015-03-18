package com.kanditag.kanditag;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Jim on 1/28/15.
 */
public class LoginImageAdapter extends PagerAdapter{
    Context context;
    private int[] arrayOfImages = new int[] {
            R.drawable.nexus_mock_dark_kt,
            R.drawable.nexus_mock_white_kt
    };
    LoginImageAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrayOfImages.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ImageView) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        //int padding = context.getResources().getDimensionPixelSize(R.dimen.padding_medium);
        //imageView.setPadding(padding, padding, padding, padding);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(arrayOfImages[position]);
        ((ViewPager) container).addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
    }
}
