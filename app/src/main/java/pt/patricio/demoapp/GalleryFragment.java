package pt.patricio.demoapp;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.patricio.demoapp.helper.GalleriesSnapping;

import static pt.patricio.demoapp.CameraFragment.STORAGE_FILE_PATH;
import static pt.patricio.demoapp.CameraFragment.STORAGE_TEMP_PATH;

/**
 * This class will support the image gallery
 * Created by patricio on 04-02-2017.
 */

public class GalleryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LinearLayout photoGallery = (LinearLayout) view.findViewById(R.id.photo_gallery);

        final List<File> localImages = getLocalImages();
        if(localImages.size() > 0) {

            photoGallery.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        photoGallery.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    else {
                        photoGallery.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }

                    final GalleriesSnapping gn = new GalleriesSnapping(getActivity());
                    gn.setFeatureItems(localImages, photoGallery.getWidth(), photoGallery.getHeight());

                    photoGallery.addView(gn);
                }
            });
        }
    }

    private List<File> getLocalImages() {
        File myDir = new File(STORAGE_FILE_PATH + STORAGE_TEMP_PATH);
        List<File> photos = new ArrayList<>();

        if(myDir.exists() && myDir.isDirectory() && myDir.listFiles() != null) {
            Collections.addAll(photos, myDir.listFiles());
        }
        return photos;
    }
}
