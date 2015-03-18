package com.kanditag.kanditag;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Jim on 2/7/15.
 */
public class GifView extends View {

    private InputStream gifInputStream;
    private Movie myMovie;
    private long movieStart;
    private int mWidth, mHeight;
    private long movieDuration;

    public GifView (Context context, byte[] array) {
        super(context);
        init(array);
    }

    public GifView (Context context, AttributeSet set, byte[] array) {
        super(context, set);
        init(array);
    }

    public GifView (Context context, AttributeSet set, int defStyle, byte[] array) {
        super(context, set, defStyle);
        init(array);
    }

    public void init (byte[] array) {
        setFocusable(true);
        gifInputStream = new ByteArrayInputStream(array);
        myMovie = Movie.decodeStream(gifInputStream);
        mWidth = myMovie.width();
        mHeight = myMovie.height();
        movieDuration = myMovie.duration();
    }

    @Override
    protected void onMeasure(int width, int height) {
        setMeasuredDimension(mWidth, mHeight);
    }

    public int getMovieWidth() {
        return mWidth;
    }

    public int getMovieHeight() {
        return mHeight;
    }

    public long getMovieDuration() {
        return movieDuration;
    }

    @Override
    protected void onDraw (Canvas canvas) {
        long now = android.os.SystemClock.uptimeMillis();
        if (movieStart == 0) {
            movieStart = now;
        }

        if (myMovie != null) {
            int dur = myMovie.duration();
            if (dur == 0) {
                dur = 1000;
            }
            int relTime = (int)((now - movieStart) % dur);
            myMovie.setTime(relTime);

            myMovie.draw(canvas, 0, 0);
            invalidate();
        }
    }
}
