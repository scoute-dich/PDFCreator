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

package com.theartofdev.edmodo.cropper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import de.baumann.pdfcreator.R;

public class CropImageActivity extends AppCompatActivity implements CropImageView.OnSetImageUriCompleteListener, CropImageView.OnSaveCroppedImageCompleteListener {

    /**
     * The crop image2 view library widget used in the activity
     */
    private CropImageView mCropImageView;

    /**
     * the options that were set for the crop image2
     */
    private CropImageOptions mOptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image_activity);

        mCropImageView = (CropImageView) findViewById(R.id.cropImageView);

        Intent intent = getIntent();
        Uri source = intent.getParcelableExtra(CropImage.CROP_IMAGE_EXTRA_SOURCE);
        mOptions = intent.getParcelableExtra(CropImage.CROP_IMAGE_EXTRA_OPTIONS);

        if (savedInstanceState == null) {
            mCropImageView.setImageUriAsync(source);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String title = mOptions.activityTitle != null && !mOptions.activityTitle.isEmpty()
                    ? mOptions.activityTitle
                    : getResources().getString(R.string.app_title_crop);
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnSaveCroppedImageCompleteListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCropImageView.setOnSetImageUriCompleteListener(null);
        mCropImageView.setOnSaveCroppedImageCompleteListener(null);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResultCancel();
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
        if (error == null) {
            if (mOptions.initialCropWindowRectangle != null) {
                mCropImageView.setCropRect(mOptions.initialCropWindowRectangle);
            }
            if (mOptions.initialRotation > -1) {
                mCropImageView.setRotatedDegrees(mOptions.initialRotation);
            }
        } else {
            setResult(null, error);
        }
    }

    @Override
    public void onSaveCroppedImageComplete(Uri uri, Exception error) {
        setResult(uri, error);
    }

    //region: Private methods


    /**
     * Result with cropped image2 data or error if failed.
     */
    private void setResult(Uri uri, Exception error) {
        int resultCode = error == null ? RESULT_OK : CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
        setResult(resultCode, getResultIntent(uri, error));
        finish();
    }

    /**
     * Cancel of cropping activity.
     */
    private void setResultCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Get intent instance to be used for the result of this activity.
     */
    private Intent getResultIntent(Uri uri, Exception error) {
        CropImage.ActivityResult result = new CropImage.ActivityResult(uri,
                error,
                mCropImageView.getCropPoints(),
                mCropImageView.getCropRect(),
                mCropImageView.getRotatedDegrees());
        Intent intent = new Intent();
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result);
        return intent;
    }
    //endregion
}

