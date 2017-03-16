/*
    This file is part of the HHS Moodle WebApp.

    HHS Moodle WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HHS Moodle WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora Native WebApp.

    If not, see <http://www.gnu.org/licenses/>.
 */

package de.baumann.pdfcreator.helper;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import de.baumann.pdfcreator.R;

public class Activity_images extends AppCompatActivity {

    private final ArrayList<String> f = new ArrayList<>();// list of file paths

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(R.string.action_imageLoad);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getFromSdcard(Environment.getExternalStorageDirectory());
        GridView imageGrid = (GridView) findViewById(R.id.PhoneImageGrid);
        ImageAdapter imageAdapter = new ImageAdapter();
        imageGrid.setAdapter(imageAdapter);
    }

    private void getFromSdcard(File dir) {

        File[] listFile;
        listFile = dir.listFiles();

        if (listFile != null) {
            for (File aListFile : listFile) {
                if (aListFile.isDirectory()
                        && !aListFile.getName().contains("thumb")
                        && !aListFile.getName().contains("cache")) {
                    getFromSdcard(aListFile);
                } else {
                    if (aListFile.getName().toLowerCase().endsWith(".jpg") ||
                            aListFile.getName().toLowerCase().endsWith(".JPG") ||
                            aListFile.getName().toLowerCase().endsWith(".jpeg") ||
                            aListFile.getName().toLowerCase().endsWith(".png") ||
                            aListFile.getName().toLowerCase().endsWith(".jpg") ||
                            aListFile.getName().toLowerCase().endsWith(".bmp") ||
                            aListFile.getName().toLowerCase().endsWith(".gif") ||
                            aListFile.getName().toLowerCase().endsWith(".tiff")) {
                        f.add(aListFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    private class ImageAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;

        private ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return f.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.item_grid, parent, false);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            } try {
                Glide.with(Activity_images.this)
                        .load(f.get(position)) // or URI/path
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .override(150, 150)
                        .centerCrop()
                        .into(holder.imageview); //imageView to set thumbnail to

                holder.imageview.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {

                        try {

                            //create output directory if it doesn't exist
                            File dir = new File (Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/");
                            if (!dir.exists()) {
                                //noinspection ResultOfMethodCallIgnored
                                dir.mkdirs();
                            }

                            InputStream in = new FileInputStream(f.get(position));
                            OutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");

                            byte[] buffer = new byte[1024];
                            int read;
                            while ((read = in.read(buffer)) != -1) {
                                out.write(buffer, 0, read);
                            }
                            in.close();

                            // write the output file
                            out.flush();
                            out.close();

                        } catch (Exception e) {
                            Log.e("PDF Creator: error", e.getMessage());
                        }

                        finish();
                    }
                });


            } catch (Exception e) {
                Log.w("HHS_Moodle", "Error load thumbnail", e);
                holder.imageview.setImageResource(R.drawable.file_image);
            }
            return convertView;
        }
    }

    private class ViewHolder {
        ImageView imageview;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}