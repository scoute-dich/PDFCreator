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

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Task to crop bitmap asynchronously from the UI thread.
 */
final class BitmapCroppingWorkerTask extends AsyncTask<Void, Void, BitmapCroppingWorkerTask.Result> {

    //region: Fields and Consts

    /**
     * Use a WeakReference to ensure the ImageView can be garbage collected
     */
    private final WeakReference<CropImageView> mCropImageViewReference;

    /**
     * the bitmap to crop
     */
    private final Bitmap mBitmap;

    /**
     * The Android URI of the image2 to load
     */
    private final Uri mUri;

    /**
     * The context of the crop image2 view widget used for loading of bitmap by Android URI
     */
    private final Context mContext;

    /**
     * Required cropping 4 points (x0,y0,x1,y1,x2,y2,x3,y3)
     */
    private final float[] mCropPoints;

    /**
     * Degrees the image2 was rotated after loading
     */
    private final int mDegreesRotated;

    /**
     * the original width of the image2 to be cropped (for image2 loaded from URI)
     */
    private final int mOrgWidth;

    /**
     * the original height of the image2 to be cropped (for image2 loaded from URI)
     */
    private final int mOrgHeight;

    /**
     * is there is fixed aspect ratio for the crop rectangle
     */
    private final boolean mFixAspectRatio;

    /**
     * the X aspect ration of the crop rectangle
     */
    private final int mAspectRatioX;

    /**
     * the Y aspect ration of the crop rectangle
     */
    private final int mAspectRatioY;

    /**
     * required width of the cropping image2
     */
    private final int mReqWidth;

    /**
     * required height of the cropping image2
     */
    private final int mReqHeight;

    /**
     * the Android Uri to save the cropped image2 to
     */
    private final Uri mSaveUri;

    /**
     * the compression format to use when writing the image2
     */
    private final Bitmap.CompressFormat mSaveCompressFormat;

    /**
     * the quality (if applicable) to use when writing the image2 (0 - 100)
     */
    private final int mSaveCompressQuality;
    //endregion

    public BitmapCroppingWorkerTask(CropImageView cropImageView, Bitmap bitmap, float[] cropPoints,
                                    int degreesRotated, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY) {

        mCropImageViewReference = new WeakReference<>(cropImageView);
        mContext = cropImageView.getContext();
        mBitmap = bitmap;
        mCropPoints = cropPoints;
        mUri = null;
        mDegreesRotated = degreesRotated;
        mFixAspectRatio = fixAspectRatio;
        mAspectRatioX = aspectRatioX;
        mAspectRatioY = aspectRatioY;
        mSaveUri = null;
        mSaveCompressFormat = null;
        mSaveCompressQuality = 0;
        mOrgWidth = 0;
        mOrgHeight = 0;
        mReqWidth = 0;
        mReqHeight = 0;
    }

    public BitmapCroppingWorkerTask(CropImageView cropImageView, Uri uri, float[] cropPoints,
                                    int degreesRotated, int orgWidth, int orgHeight,
                                    boolean fixAspectRatio, int aspectRatioX, int aspectRatioY,
                                    int reqWidth, int reqHeight) {

        mCropImageViewReference = new WeakReference<>(cropImageView);
        mContext = cropImageView.getContext();
        mUri = uri;
        mCropPoints = cropPoints;
        mDegreesRotated = degreesRotated;
        mFixAspectRatio = fixAspectRatio;
        mAspectRatioX = aspectRatioX;
        mAspectRatioY = aspectRatioY;
        mOrgWidth = orgWidth;
        mOrgHeight = orgHeight;
        mReqWidth = reqWidth;
        mReqHeight = reqHeight;
        mSaveUri = null;
        mSaveCompressFormat = null;
        mSaveCompressQuality = 0;
        mBitmap = null;
    }

    /**
     * Crop image2 in background.
     *
     * @param params ignored
     * @return the decoded bitmap data
     */
    @Override
    protected BitmapCroppingWorkerTask.Result doInBackground(Void... params) {
        try {
            if (!isCancelled()) {

                Bitmap bitmap = null;
                if (mUri != null) {
                    bitmap = BitmapUtils.cropBitmap(mContext, mUri, mCropPoints, mDegreesRotated, mOrgWidth, mOrgHeight,
                            mFixAspectRatio, mAspectRatioX, mAspectRatioY, mReqWidth, mReqHeight);
                } else if (mBitmap != null) {
                    bitmap = BitmapUtils.cropBitmap(mBitmap, mCropPoints, mDegreesRotated, mFixAspectRatio, mAspectRatioX, mAspectRatioY);
                }

                if (mSaveUri == null) {
                    return new Result(bitmap);
                } else {
                    BitmapUtils.writeBitmapToUri(mContext, bitmap, mSaveUri, mSaveCompressFormat, mSaveCompressQuality);
                    assert bitmap != null;
                    bitmap.recycle();
                    return new Result(mSaveUri);
                }
            }
            return null;
        } catch (Exception e) {
            return new Result(e, mSaveUri != null);
        }
    }

    /**
     * Once complete, see if ImageView is still around and set bitmap.
     *
     * @param result the result of bitmap cropping
     */
    @Override
    protected void onPostExecute(Result result) {
        if (result != null) {
            boolean completeCalled = false;
            if (!isCancelled()) {
                CropImageView cropImageView = mCropImageViewReference.get();
                if (cropImageView != null) {
                    completeCalled = true;
                    cropImageView.onImageCroppingAsyncComplete(result);
                }
            }
            if (!completeCalled && result.bitmap != null) {
                // fast release of unused bitmap
                result.bitmap.recycle();
            }
        }
    }

    //region: Inner class: Result

    /**
     * The result of BitmapCroppingWorkerTask async loading.
     */
    public static final class Result {

        /**
         * The cropped bitmap
         */
        public final Bitmap bitmap;

        /**
         * The saved cropped bitmap uri
         */
        public final Uri uri;

        /**
         * The error that occurred during async bitmap cropping.
         */
        public final Exception error;

        /**
         * is the cropping request was to get a bitmap or to save it to uri
         */
        public final boolean isSave;

        Result(Bitmap bitmap) {
            this.bitmap = bitmap;
            this.uri = null;
            this.error = null;
            this.isSave = false;
        }

        Result(Uri uri) {
            this.bitmap = null;
            this.uri = uri;
            this.error = null;
            this.isSave = true;
        }

        Result(Exception error, boolean isSave) {
            this.bitmap = null;
            this.uri = null;
            this.error = error;
            this.isSave = isSave;
        }
    }
    //endregion
}
