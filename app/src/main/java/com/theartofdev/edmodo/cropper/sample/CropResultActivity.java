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

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.baumann.pdfcreator.*;

@SuppressWarnings("UnusedParameters")
public final class CropResultActivity extends AppCompatActivity {

    /**
     * The image2 to show in the activity.
     */
    static Bitmap mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_result);

        setTitle(R.string.app_title_2);

        ImageView imageView;
        imageView = ((ImageView) findViewById(R.id.resultImageView));

        assert imageView != null;
        imageView.setImageBitmap(mImage);

        Snackbar.make(imageView, getString(R.string.toast_savedImage), Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null).show();

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");

        // Encode the file as a JPEG image2.
        FileOutputStream outStream;
        try {

            outStream = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);
            outStream.flush();
            outStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mImage != null) {
            mImage.recycle();
            mImage = null;
        }
        finish();
    }
}
