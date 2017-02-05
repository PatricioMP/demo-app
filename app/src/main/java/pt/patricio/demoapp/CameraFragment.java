package pt.patricio.demoapp;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import pt.patricio.demosdk.CameraManager;

/**
 * This fragment will show the preview of the photo
 * Created by patricio on 03-02-2017.
 */

@SuppressWarnings("deprecation")
public class CameraFragment extends Fragment {

    private final static String LOG_TAG = CameraFragment.class.getName();
    public static final String LOG_IO_EXCEPTION = "IO Exception: ";
    public static final String LOG_FILE_NOT_FOUND_EXCEPTION = "File not found Exception: ";

    public static final String STORAGE_FILE_PATH = Environment.getExternalStorageDirectory().toString();
    public static final String STORAGE_TEMP_PATH = "/demoapp";
    public static final String TEMP_SELFIE_NAME = "Selfie";
    public static final String SELFIE_PHOTO_EXTENSION = ".jpg";

    private CameraManager manager;
    private FrameLayout frame;
    private FloatingActionButton takePicture;
    private FloatingActionButton save;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_camera, container, false);

        frame = (FrameLayout) mainView.findViewById(R.id.camera_preview);

        manager = new CameraManager(getActivity());

        Bundle bundle = getArguments();
        if(bundle == null)
            manager.start(frame, R.drawable.mustache);
        else {
            manager.start(frame, bundle.getInt("overlay"));
        }

        takePicture = (FloatingActionButton) mainView.findViewById(R.id.take_picture);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.takePicture();
            }
        });

        save = (FloatingActionButton) mainView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmap((Bitmap) view.getTag());
                Snackbar.make(view, "Picture saved.", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
                getActivity().onBackPressed();
            }
        });

        manager.setCameraListener(new CameraManager.CameraListener() {
            @Override
            public void onImageCreate(Bitmap result) {
                manager.stop();
                frame.removeAllViews();
                ImageView view = new ImageView(getActivity());
                view.setImageBitmap(result);
                frame.addView(view);
                takePicture.setVisibility(View.GONE);
                save.setVisibility(View.VISIBLE);
                save.setTag(result);
            }

            @Override
            public void onError(String error) {
                MainActivity.ErrorDialog.newInstance(getString(R.string.no_camera))
                         .show(getFragmentManager(), MainActivity.FRAGMENT_DIALOG);
            }
        });

        return mainView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(manager != null) {
            manager.stop();
        }
    }

    /**
     * Saves the bitmap to the phone's memory.
     * */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean saveBitmap(Bitmap mergedBmp) {
        File myDir = new File(STORAGE_FILE_PATH + STORAGE_TEMP_PATH);
        String filename = TEMP_SELFIE_NAME + "_" + System.currentTimeMillis() + SELFIE_PHOTO_EXTENSION;
        File file;

        //Save Drawable
        myDir.mkdirs();
        file = new File(myDir, filename);
        if (file.exists())
            file.delete();

        MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), mergedBmp, TEMP_SELFIE_NAME, "");

        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            mergedBmp.compress(Bitmap.CompressFormat.JPEG, 90, out); //Bitmap.CompressFormat.JPEG
            out.flush();
            out.close();
            mergedBmp.recycle();
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, LOG_FILE_NOT_FOUND_EXCEPTION + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(LOG_TAG, LOG_IO_EXCEPTION + e.getMessage());
            return false;
        }
        return true;
    }
}
