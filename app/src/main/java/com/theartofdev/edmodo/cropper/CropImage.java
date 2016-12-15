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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * helper_main to simplify crop image2 work like starting pick-image2 activity and handling camera/gallery intents.<br>
 * The goal of the helper is to simplify the starting and most-common usage of image2 cropping and not
 * all purpose all possible scenario one-to-rule-them-all code base. So feel free to use it as is and as
 * a wiki to make your own.<br>
 * Added value you get out-of-the-box is some edge case handling that you may miss otherwise, like the
 * stupid-ass Android camera result URI that may differ from version to version and from device to device.
 */
public final class CropImage {

    //region: Fields and Consts

    /**
     * The key used to pass crop image2 source URI to {@link CropImageActivity}.
     */
    public static final String CROP_IMAGE_EXTRA_SOURCE = "CROP_IMAGE_EXTRA_SOURCE";

    /**
     * The key used to pass crop image2 options to {@link CropImageActivity}.
     */
    public static final String CROP_IMAGE_EXTRA_OPTIONS = "CROP_IMAGE_EXTRA_OPTIONS";

    /**
     * The key used to pass crop image2 result data back from {@link CropImageActivity}.
     */
    public static final String CROP_IMAGE_EXTRA_RESULT = "CROP_IMAGE_EXTRA_RESULT";

    /**
     * The request code used to start {@link CropImageActivity} to be used on result to identify the this specific
     * request.
     */
    public static final int CROP_IMAGE_ACTIVITY_REQUEST_CODE = 203;

    /**
     * The result code used to return error from {@link CropImageActivity}.
     */
    public static final int CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE = 204;
    //endregion

    private CropImage() {
    }

    /**
     * Create a new bitmap that has all pixels beyond the oval shape transparent.
     * Old bitmap is recycled.
     */
    public static Bitmap toOvalBitmap(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        int color = 0xff424242;
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        RectF rect = new RectF(0, 0, width, height);
        canvas.drawOval(rect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        bitmap.recycle();

        return output;
    }



    /**
     * Get {@link CropImageActivity} result data object for crop image2 activity started using {@link #(Uri)}.
     *
     * @param data result data intent as received in {@link Activity#onActivityResult(int, int, Intent)}.
     * @return Crop Image Activity Result object or null if none exists
     */
    public static ActivityResult getActivityResult(@Nullable Intent data) {
        return data != null ? (ActivityResult) data.getParcelableExtra(CROP_IMAGE_EXTRA_RESULT) : null;
    }

    /**
     * Result data of Crop Image Activity.
     */
    public static final class ActivityResult implements Parcelable {

        public static final Creator<ActivityResult> CREATOR = new Creator<ActivityResult>() {
            @Override
            public ActivityResult createFromParcel(Parcel in) {
                return new ActivityResult(in);
            }

            @Override
            public ActivityResult[] newArray(int size) {
                return new ActivityResult[size];
            }
        };

        /**
         * The Android uri of the saved cropped image2 result
         */
        private final Uri mUri;

        /**
         * The error that failed the loading/cropping (null if successful)
         */
        private final Exception mError;

        /**
         * The 4 points of the cropping window in the source image2
         */
        private final float[] mCropPoints;

        /**
         * The rectangle of the cropping window in the source image2
         */
        private final Rect mCropRect;

        /**
         * The final rotation of the cropped image2 relative to source
         */
        private final int mRotation;

        ActivityResult(Uri uri, Exception error, float[] cropPoints, Rect cropRect, int rotation) {
            mUri = uri;
            mError = error;
            mCropPoints = cropPoints;
            mCropRect = cropRect;
            mRotation = rotation;
        }

        ActivityResult(Parcel in) {
            mUri = in.readParcelable(Uri.class.getClassLoader());
            mError = (Exception) in.readSerializable();
            mCropPoints = in.createFloatArray();
            mCropRect = in.readParcelable(Rect.class.getClassLoader());
            mRotation = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mUri, flags);
            dest.writeSerializable(mError);
            dest.writeFloatArray(mCropPoints);
            dest.writeParcelable(mCropRect, flags);
            dest.writeInt(mRotation);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * The Android uri of the saved cropped image2 result
         */
        public Uri getUri() {
            return mUri;
        }

        /**
         * The error that failed the loading/cropping (null if successful)
         */
        public Exception getError() {
            return mError;
        }
    }
    //endregion
}