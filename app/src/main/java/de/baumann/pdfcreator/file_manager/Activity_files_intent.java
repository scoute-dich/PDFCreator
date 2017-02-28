/*
    This file is part of the Browser webview app.

    HHS Moodle WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HHS Moodle WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Browser webview app.

    If not, see <http://www.gnu.org/licenses/>.
 */

package de.baumann.pdfcreator.file_manager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.baumann.pdfcreator.R;
import de.baumann.pdfcreator.helper.helper_main;

import static android.content.ContentValues.TAG;
import static java.lang.String.valueOf;

public class Activity_files_intent extends AppCompatActivity {

    private ListView listView = null;
    private DbAdapter_Files db;
    private SimpleCursorAdapter adapter;
    private SharedPreferences sharedPref;

    private EditText filter;
    private RelativeLayout filter_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        setContentView(R.layout.activity_file_manager);

        PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String folder = Environment.getExternalStorageDirectory() + sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

        sharedPref.edit().putString("files_startFolder",
                folder).apply();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        listView = (ListView)findViewById(R.id.listNotes);
        filter_layout = (RelativeLayout) findViewById(R.id.filter_layout);
        filter_layout.setVisibility(View.GONE);
        filter = (EditText) findViewById(R.id.myFilter);

        ImageButton ib_hideKeyboard =(ImageButton) findViewById(R.id.ib_hideKeyboard);
        ib_hideKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                filter_layout.setVisibility(View.GONE);
                setFilesList();
            }
        });

        //calling Notes_DbAdapter
        db = new DbAdapter_Files(this);
        db.open();

        setFilesList();
        onNewIntent(getIntent());
    }

    protected void onNewIntent(final Intent intent) {

        String action = intent.getAction();
        PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if ("file_chooseImage".equals(action)) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Cursor row2 = (Cursor) listView.getItemAtPosition(position);
                    final String files_icon = row2.getString(row2.getColumnIndexOrThrow("files_icon"));
                    final String files_attachment = row2.getString(row2.getColumnIndexOrThrow("files_attachment"));
                    final File pathFile = new File(files_attachment);

                    if(pathFile.isDirectory()) {
                        try {
                            sharedPref.edit().putString("files_startFolder", files_attachment).apply();
                            setFilesList();
                        } catch (Exception e) {
                            Snackbar.make(listView, R.string.toast_directory, Snackbar.LENGTH_LONG).show();
                        }
                    } else if(files_attachment.equals("")) {
                        try {
                            final File pathActual = new File(sharedPref.getString("files_startFolder",
                                    Environment.getExternalStorageDirectory().getPath()));
                            sharedPref.edit().putString("files_startFolder", pathActual.getParent()).apply();
                            setFilesList();
                        } catch (Exception e) {
                            Snackbar.make(listView, R.string.toast_directory, Snackbar.LENGTH_LONG).show();
                        }
                    } else if (files_icon.equals(".jpg") ||
                            files_icon.equals(".JPG") ||
                            files_icon.equals(".png") ||
                            files_icon.equals(".jpeg")){
                        Bitmap bitmap = BitmapFactory.decodeFile(files_attachment);
                        File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
                        // Encode the file as a JPEG image.
                        FileOutputStream outStream;
                        try {

                            int imgquality_int = Integer.parseInt(sharedPref.getString("imageQuality", "80"));
                            outStream = new FileOutputStream(imgFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, imgquality_int, outStream);
                            outStream.flush();
                            outStream.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        finish();
                    } else {
                        Snackbar.make(listView, R.string.toast_picFile, Snackbar.LENGTH_LONG).show();
                    }
                }
            });

        } else if ("file_choosePDF".equals(action)) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Cursor row2 = (Cursor) listView.getItemAtPosition(position);
                    final String files_attachment = row2.getString(row2.getColumnIndexOrThrow("files_attachment"));
                    final String files_icon = row2.getString(row2.getColumnIndexOrThrow("files_icon"));
                    final File pathFile = new File(files_attachment);

                    if(pathFile.isDirectory()) {
                        try {
                            sharedPref.edit().putString("files_startFolder", files_attachment).apply();
                            setFilesList();
                        } catch (Exception e) {
                            Snackbar.make(listView, R.string.toast_directory, Snackbar.LENGTH_LONG).show();
                        }
                    } else if(files_attachment.equals("")) {
                        try {
                            final File pathActual = new File(sharedPref.getString("files_startFolder",
                                    Environment.getExternalStorageDirectory().getPath()));
                            sharedPref.edit().putString("files_startFolder", pathActual.getParent()).apply();
                            setFilesList();
                        } catch (Exception e) {
                            Snackbar.make(listView, R.string.toast_directory, Snackbar.LENGTH_LONG).show();
                        }
                    } else if (files_icon.equals(".pdf")){
                        final String fileName = pathFile.getAbsolutePath().substring(pathFile.getAbsolutePath().lastIndexOf("/")+1);
                        sharedPref.edit().putString("pathPDF", pathFile.getAbsolutePath()).apply();
                        sharedPref.edit().putString("title", fileName).apply();
                        finish();
                    } else {
                        Snackbar.make(listView, R.string.toast_pdfFile, Snackbar.LENGTH_LONG).show();
                    }
                }
            });

        }  else if ("file_chooseSecondPDF".equals(action)) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Cursor row2 = (Cursor) listView.getItemAtPosition(position);
                    final String files_attachment = row2.getString(row2.getColumnIndexOrThrow("files_attachment"));
                    final String files_icon = row2.getString(row2.getColumnIndexOrThrow("files_icon"));
                    final File pathFile = new File(files_attachment);

                    if(pathFile.isDirectory()) {
                        try {
                            sharedPref.edit().putString("files_startFolder", files_attachment).apply();
                            setFilesList();
                        } catch (Exception e) {
                            Snackbar.make(listView, R.string.toast_directory, Snackbar.LENGTH_LONG).show();
                        }
                    } else if(files_attachment.equals("")) {
                        try {
                            final File pathActual = new File(sharedPref.getString("files_startFolder",
                                    Environment.getExternalStorageDirectory().getPath()));
                            sharedPref.edit().putString("files_startFolder", pathActual.getParent()).apply();
                            setFilesList();
                        } catch (Exception e) {
                            Snackbar.make(listView, R.string.toast_directory, Snackbar.LENGTH_LONG).show();
                        }
                    } else if (files_icon.equals(".pdf")){
                        sharedPref.edit().putString("file_chooseSecondPDF", pathFile.getAbsolutePath()).apply();
                        finish();
                    } else {
                        Snackbar.make(listView, R.string.toast_pdfFile, Snackbar.LENGTH_LONG).show();
                    }
                }
            });

        }
    }

    private void setFilesList() {

        deleteDatabase("files_DB_v01.db");

        String folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

        File f = new File(sharedPref.getString("files_startFolder",
                folder));
        final File[] files = f.listFiles();

        if (files.length == 0) {
            Snackbar.make(listView, R.string.toast_files, Snackbar.LENGTH_LONG).show();
        }

        // looping through all items <item>
        for (File file : files) {

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            String file_Name = file.getName().substring(0,1).toUpperCase() + file.getName().substring(1).toLowerCase();
            String file_Size = getReadableFileSize(file.length());
            String file_date = formatter.format(new Date(file.lastModified()));
            String file_path = file.getAbsolutePath();

            String file_ext;
            if (file.isDirectory()) {
                file_ext = ".";
            } else {
                file_ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
            }

            db.open();

            if (file_ext.equals(".") ||
                    file_ext.equals(".pdf") ||
                    file_ext.equals(".") ||
                    file_ext.equals(".jpg") ||
                    file_ext.equals(".JPG") ||
                    file_ext.equals(".jpeg") ||
                    file_ext.equals(".png")) {
                if(db.isExist(file_Name)) {
                    Log.i(TAG, "Entry exists" + file_Name);
                } else {
                    db.insert(file_Name, file_Size, file_ext, file_path, file_date);
                }
            }
        }

        try {
            db.insert("...", "", "", "", "");
        } catch (Exception e) {
            Snackbar.make(listView, R.string.toast_directory, Snackbar.LENGTH_LONG).show();
        }

        //display data
        final int layoutstyle=R.layout.list_item;
        int[] xml_id = new int[] {
                R.id.textView_title_notes,
                R.id.textView_des_notes,
                R.id.textView_create_notes
        };
        String[] column = new String[] {
                "files_title",
                "files_content",
                "files_creation"
        };
        final Cursor row = db.fetchAllData(this);
        adapter = new SimpleCursorAdapter(this, layoutstyle,row,column, xml_id, 0) {
            @Override
            public View getView (final int position, View convertView, ViewGroup parent) {

                Cursor row2 = (Cursor) listView.getItemAtPosition(position);
                final String files_icon = row2.getString(row2.getColumnIndexOrThrow("files_icon"));
                final String files_attachment = row2.getString(row2.getColumnIndexOrThrow("files_attachment"));
                final String files_title = row2.getString(row2.getColumnIndexOrThrow("files_title"));

                final File pathFile = new File(files_attachment);

                View v = super.getView(position, convertView, parent);
                final ImageView iv = (ImageView) v.findViewById(R.id.icon_notes);

                iv.setVisibility(View.VISIBLE);

                if (pathFile.isDirectory()) {
                    iv.setImageResource(R.drawable.folder);
                } else {
                    switch (files_icon) {
                        case ".gif":case ".bmp":case ".tiff":case ".svg":
                        case ".png":case ".jpg":case ".JPG":case ".jpeg":
                            try {
                                Uri uri = Uri.fromFile(pathFile);
                                Picasso.with(Activity_files_intent.this).load(uri).resize(76, 76).centerCrop().into(iv);
                            } catch (Exception e) {
                                Log.w("HHS_Moodle", "Error Load image", e);
                            }
                            break;
                        case ".pdf":
                            iv.setImageResource(R.drawable.file_pdf);
                            break;
                        default:
                            iv.setImageResource(R.drawable.arrow_up_dark);
                            break;
                    }
                }

                if (files_title.equals("...")) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            iv.setImageResource(R.drawable.arrow_up_dark);
                        }
                    }, 200);
                }
                return v;
            }
        };

        //display data by filter
        final String note_search = sharedPref.getString("filter_filesBY", "files_title");
        sharedPref.edit().putString("filter_filesBY", "files_title").apply();
        filter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }
        });
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                return db.fetchDataByFilter(constraint.toString(),note_search);
            }
        });

        listView.setAdapter(adapter);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    private static String getReadableFileSize(long size) {
        final int BYTES_IN_KILOBYTES = 1024;
        final DecimalFormat dec = new DecimalFormat("###.#");
        final String KILOBYTES = " KB";
        final String MEGABYTES = " MB";
        final String GIGABYTES = " GB";
        float fileSize = 0;
        String suffix = KILOBYTES;

        if (size > BYTES_IN_KILOBYTES) {
            fileSize = size / BYTES_IN_KILOBYTES;
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES;
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES;
                    suffix = GIGABYTES;
                } else {
                    suffix = MEGABYTES;
                }
            }
        }
        return valueOf(dec.format(fileSize) + suffix);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.filter_title:
                sharedPref.edit().putString("filter_filesBY", "files_title").apply();
                setFilesList();
                filter_layout.setVisibility(View.VISIBLE);
                filter.setText("");
                filter.setHint(R.string.action_filter_title);
                filter.requestFocus();
                helper_main.showKeyboard(Activity_files_intent.this, filter);
                return true;
            case R.id.filter_ext:
                sharedPref.edit().putString("filter_filesBY", "files_icon").apply();
                setFilesList();
                filter_layout.setVisibility(View.VISIBLE);
                filter.setText("");
                filter.setHint(R.string.action_filter_ext);
                filter.requestFocus();
                helper_main.showKeyboard(Activity_files_intent.this, filter);
                return true;

            case R.id.filter_today:
                setTitle(getString(R.string.choose_titleMain) + " | " + getString(R.string.filter_today));
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                final String search = dateFormat.format(cal.getTime());
                sharedPref.edit().putString("filter_filesBY", "files_creation").apply();
                setFilesList();
                filter.setText(search);
                return true;
            case R.id.filter_yesterday:
                setTitle(getString(R.string.choose_titleMain) + " | " + getString(R.string.filter_yesterday));
                DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal2 = Calendar.getInstance();
                cal2.add(Calendar.DATE, -1);
                final String search2 = dateFormat2.format(cal2.getTime());
                sharedPref.edit().putString("filter_filesBY", "files_creation").apply();
                setFilesList();
                filter.setText(search2);
                return true;
            case R.id.filter_before:
                setTitle(getString(R.string.choose_titleMain) + " | " + getString(R.string.filter_before));
                DateFormat dateFormat3 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal3 = Calendar.getInstance();
                cal3.add(Calendar.DATE, -2);
                final String search3 = dateFormat3.format(cal3.getTime());
                sharedPref.edit().putString("filter_filesBY", "files_creation").apply();
                setFilesList();
                filter.setText(search3);
                return true;
            case R.id.filter_month:
                setTitle(getString(R.string.choose_titleMain) + " | " + getString(R.string.filter_month));
                DateFormat dateFormat4 = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                Calendar cal4 = Calendar.getInstance();
                final String search4 = dateFormat4.format(cal4.getTime());
                sharedPref.edit().putString("filter_filesBY", "files_creation").apply();
                setFilesList();
                filter.setText(search4);
                return true;
            case R.id.filter_own:
                setTitle(getString(R.string.choose_titleMain) + " | " + getString(R.string.filter_own));
                sharedPref.edit().putString("filter_filesBY", "files_creation").apply();
                setFilesList();
                filter_layout.setVisibility(View.VISIBLE);
                filter.setText("");
                filter.setHint(R.string.action_filter_create);
                filter.requestFocus();
                helper_main.showKeyboard(Activity_files_intent.this, filter);
                return true;
            case R.id.filter_clear:
                filter.setText("");
                setFilesList();
                return true;

            case R.id.sort_title:
                sharedPref.edit().putString("sortDBF", "title").apply();
                setFilesList();
                return true;
            case R.id.sort_ext:
                sharedPref.edit().putString("sortDBF", "file_ext").apply();
                setFilesList();
                return true;
            case R.id.sort_creation:
                sharedPref.edit().putString("sortDBF", "file_date").apply();
                setFilesList();
                return true;

            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}