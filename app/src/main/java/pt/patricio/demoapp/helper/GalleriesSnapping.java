package pt.patricio.demoapp.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pt.patricio.demoapp.R;

/**
 * The Galleries for view the photos
 * Created by patricio on 9/25/13.
 */
public class GalleriesSnapping extends HorizontalScrollView {

    private static final String LOG_TAG = GalleriesSnapping.class.getName();
    private ArrayList<View> mItems = null;
    private final GestureDetector mGestureDetector;
    private int mActivePhoto = 0;
    private final Context mContext;
    private List<File> localPhotos;

    private static final int MAX_NUM_IMAGES = 2;
    private int mWidth = 0;
    private int mHeight = 0;

    public GalleriesSnapping(Context context) {
        super(context);
        mContext = context;
        mGestureDetector = new GestureDetector(context, new MyGestureDetector());
    }

    public void setFeatureItems(List<File> files, int width, int height) {

        localPhotos = files;
        mWidth = width;
        mHeight = height;

        ArrayList<View> viewList = new ArrayList<>();
        Log.d(LOG_TAG, "Gallery width:" + width + " Gallery height: " + height);

        LinearLayout internalWrapper = new LinearLayout(getContext());
        internalWrapper.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        internalWrapper.setOrientation(LinearLayout.HORIZONTAL);
        addView(internalWrapper);

        int i = 0;
        while(i < files.size()) {
            @SuppressLint("InflateParams") //don't remove this, will afect design of galley background
                    View view = LayoutInflater.from(mContext).inflate(R.layout.gallery_image, null);
            if (view != null) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
                view.setLayoutParams(params);
                viewList.add(view);
                ScrollView scroll = new ScrollView(mContext);
                scroll.setVerticalScrollBarEnabled(false);
                scroll.addView(view);
                internalWrapper.addView(scroll);
            }
            i++;
        }
        mItems = viewList;
        //load first image
        loadImage();


        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //If the user swipes
                if (mGestureDetector.onTouchEvent(event)) {
                    return true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL ){
                    int scrollX = getScrollX();
                    int featureWidth = v.getMeasuredWidth();
                    mActivePhoto = ((scrollX + (featureWidth/2))/featureWidth);
                    int scrollTo =  mActivePhoto * featureWidth;

                    smoothScrollTo(scrollTo, 0);
                    loadImage();
                    removeLeftImage();
                    Log.d("APP", "onTouch - active: " + mActivePhoto);
                    return true;
                }
                else{
                    return false;
                }
            }
        });
    }

    private void loadImage() {
        File location = localPhotos.get(mActivePhoto);
        View view = mItems.get(mActivePhoto);
        ImageView imageView = (ImageView) view.findViewById(R.id.gallery_image);
        if(imageView.getDrawable() == null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mWidth, mHeight);
            imageView.setLayoutParams(params);
            Picasso.with(mContext).load(location).into(imageView);
        }
    }

    private void removeLeftImage() {
        if(mActivePhoto >= MAX_NUM_IMAGES) {
            int index = mActivePhoto - MAX_NUM_IMAGES;
            new Handler().postDelayed(new ImageRemover(index), 200);
        }
    }

    private void removeRightImage() {
        int index = mActivePhoto + MAX_NUM_IMAGES;
        if(mItems.size() > index) {
           new Handler().postDelayed(new ImageRemover(index), 200);
        }
    }

    public ImageView getCurrentImageView() {
        View view = mItems.get(mActivePhoto);
        return (ImageView) view.findViewById(R.id.gallery_image);
    }

    public File getCurrentFileName() {
        return localPhotos.get(mActivePhoto);
    }

    private class ImageRemover implements Runnable {

        private final int mIndex;

        ImageRemover(int index) {
            mIndex = index;
        }

        @Override
        public void run() {
            View view = mItems.get(mIndex);
            ImageView imageView = (ImageView) view.findViewById(R.id.gallery_image);
            imageView.setImageDrawable(null);
            imageView.setTag(null);
            Log.i("APP", "Image removed Index: " + mIndex + " is image null->" + (imageView.getDrawable() == null));
        }
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                //to left
                if(velocityX < 0) {
                    int featureWidth = getMeasuredWidth();
                    mActivePhoto = (mActivePhoto < (mItems.size() - 1))? mActivePhoto + 1:mItems.size() -1;
                    int scrollTo =  mActivePhoto *featureWidth;

                    smoothScrollTo(scrollTo, 0);
                    loadImage();
                    removeLeftImage();
                    Log.i("APP", "onFling Negative - active: " + mActivePhoto);

                    return true;
                }
                //to right
                else if (velocityX > 0) {
                    int featureWidth = getMeasuredWidth();
                    mActivePhoto = (mActivePhoto > 0)? mActivePhoto - 1:0;
                    int scrollTo = mActivePhoto *featureWidth;

                    smoothScrollTo(scrollTo, 0);
                    loadImage();
                    removeRightImage();
                    Log.i("APP", "onFling Positive - active: " + mActivePhoto);
                    return true;
                }
            } catch (Exception e) {
                Log.e("Fling", "There was an error processing the Fling event:" + e.getMessage());
            }
            return false;
        }
    }
}
