// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.theartofdev.edmodo.cropper.sample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

import de.baumann.pdfcreator.R;

/**
 * The fragment that will show the Image Cropping UI by requested preset.
 */
public final class MainFragment extends Fragment
        implements CropImageView.OnSetImageUriCompleteListener, CropImageView.OnGetCroppedImageCompleteListener {

    private CropDemoPreset mDemoPreset;
    private CropImageView mCropImageView;

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString("DEMO_PRESET", CropDemoPreset.RECT.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        switch (mDemoPreset) {
            case RECT:
                rootView = inflater.inflate(R.layout.fragment_main_rect, container, false);
                break;
            default:
                throw new IllegalStateException("Unknown preset: " + mDemoPreset);
        }

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_crop_white_48dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(mCropImageView, getString(R.string.toast_savedImage), Snackbar.LENGTH_INDEFINITE).show();
                mCropImageView.getCroppedImageAsync();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");

        mCropImageView = (CropImageView) view.findViewById(R.id.cropImageView);
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnGetCroppedImageCompleteListener(this);

        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        mCropImageView.setImageBitmap(myBitmap);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.crop_image_menu_rotate_right) {
            mCropImageView.rotateImage(90);
            return true;

        } else if (item.getItemId() == R.id.crop_image_menu_rotate_left) {
            mCropImageView.rotateImage(270);
            return true;

        } else if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDemoPreset = CropDemoPreset.valueOf(getArguments().getString("DEMO_PRESET"));
        ((MainActivity) getActivity()).setCurrentFragment(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mCropImageView != null) {
            mCropImageView.setOnSetImageUriCompleteListener(null);
            mCropImageView.setOnGetCroppedImageCompleteListener(null);
        }
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
        if (error == null) {
            Toast.makeText(getActivity(), "Image load successful", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("AIC", "Failed to load image2 by URI", error);
            Toast.makeText(getActivity(), "Image load failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGetCroppedImageComplete(Bitmap bitmap, Exception error) {
        handleCropResult(null, bitmap, error);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            handleCropResult(result.getUri(), null, result.getError());
        }
    }

    private void handleCropResult(Uri uri, Bitmap bitmap, Exception error) {
        if (error == null) {
            Intent intent = new Intent(getActivity(), CropResultActivity.class);
            if (uri != null) {
                intent.putExtra("URI", uri);
            } else {
                CropResultActivity.mImage = mCropImageView.getCropShape() == CropImageView.CropShape.OVAL
                        ? CropImage.toOvalBitmap(bitmap)
                        : bitmap;
            }
            startActivity(intent);
            getActivity().finish();
        } else {
            Log.e("AIC", "Failed to crop image2", error);
            Toast.makeText(getActivity(), "Image crop failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
