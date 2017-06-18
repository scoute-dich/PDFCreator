package de.baumann.pdfcreator.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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
import de.baumann.pdfcreator.helper.Class_DbAdapter_Files;
import de.baumann.pdfcreator.helper.helper_main;

import static android.content.ContentValues.TAG;
import static java.lang.String.valueOf;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class file_manager extends Fragment {

    @SuppressWarnings("unused")
    private String title;

    private ListView listView = null;
    private Class_DbAdapter_Files db;
    private SimpleCursorAdapter adapter;

    private EditText filter;
    private RelativeLayout filter_layout;

    private SharedPreferences sharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_file_manager, container, false);

        setHasOptionsMenu(true);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.user_settings, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String folder = Environment.getExternalStorageDirectory() + sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

        sharedPref.edit().putString("files_startFolder", folder).apply();


        listView = (ListView) rootView.findViewById(R.id.listNotes);
        filter_layout = (RelativeLayout) rootView.findViewById(R.id.filter_layout);
        filter_layout.setVisibility(View.GONE);
        filter = (EditText) rootView.findViewById(R.id.myFilter);

        ImageButton ib_hideKeyboard =(ImageButton) rootView.findViewById(R.id.ib_hideKeyboard);
        ib_hideKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                filter_layout.setVisibility(View.GONE);
                setFilesList();
            }
        });

        //calling Notes_DbAdapter
        db = new Class_DbAdapter_Files(getActivity());
        db.open();

        setTitle();

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int hasWRITE_EXTERNAL_STORAGE = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWRITE_EXTERNAL_STORAGE == PackageManager.PERMISSION_GRANTED) {
                setFilesList();
            } else {
                Snackbar snackbar = Snackbar
                        .make(listView, R.string.toast_permission, Snackbar.LENGTH_LONG)
                        .setAction(R.string.toast_yes, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        });
                snackbar.show();
            }
        } else {
            setFilesList();
        }
        
        return rootView;
    }

    public void setTitle() {
        if (sharedPref.getString("sortDBF", "title").equals("title")) {
            getActivity().setTitle(getActivity().getString(R.string.choose_titleMain) + " | " + getString(R.string.sort_title));
        } else if (sharedPref.getString("sortDBF", "title").equals("file_ext")) {
            getActivity().setTitle(getActivity().getString(R.string.choose_titleMain) + " | " + getString(R.string.sort_extension));
        } else {
            getActivity().setTitle(getActivity().getString(R.string.choose_titleMain) + " | " + getString(R.string.sort_date));
        }
    }

    public void setFilesList() {

        getActivity().deleteDatabase("files_DB_v01.db");

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

            String file_Name = file.getName();
            String file_Size = getReadableFileSize(file.length());
            String file_date = formatter.format(new Date(file.lastModified()));
            String file_path = file.getAbsolutePath();

            String file_ext;
            if (file.isDirectory()) {
                file_ext = ".";
            } else {
                try {
                    file_ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                } catch (Exception e) {
                    file_ext = ".";
                }
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
        final Cursor row = db.fetchAllData(getActivity());
        adapter = new SimpleCursorAdapter(getActivity(), layoutstyle,row,column, xml_id, 0) {
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
                                Glide.with(getActivity())
                                        .load(pathFile) // or URI/path
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .override(76, 76)
                                        .centerCrop()
                                        .into(iv); //imageView to set thumbnail to
                            } catch (Exception e) {
                                Log.w("HHS_Moodle", "Error Load image", e);
                            }
                            break;
                        case ".pdf":
                            iv.setImageResource(R.drawable.file_pdf);
                            break;
                        default:
                            iv.setImageResource(R.drawable.file);
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
        //onClick function
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View view, int position, long id) {

                Cursor row2 = (Cursor) listView.getItemAtPosition(position);
                final String files_icon = row2.getString(row2.getColumnIndexOrThrow("files_icon"));
                final String files_attachment = row2.getString(row2.getColumnIndexOrThrow("files_attachment"));
                final String files_title = row2.getString(row2.getColumnIndexOrThrow("files_title"));

                final File pathFile = new File(files_attachment);
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);

                if (sharedPref.getInt("fileManager", 0) == 0) {
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
                    } else if (files_icon.equals(".pdf")) {
                        sharedPref.edit().putString("pathPDF", files_attachment).apply();
                        sharedPref.edit().putString("title", files_title).apply();
                        helper_main.open(files_icon, getActivity(), pathFile, listView);
                    }else {
                        helper_main.open(files_icon, getActivity(), pathFile, listView);
                    }

                }  else if(sharedPref.getInt("fileManager", 0) == 1) {
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
                        viewPager.setCurrentItem(sharedPref.getInt("startFragment", 0));
                        sharedPref.edit().putInt("fileManager", 0).apply();
                        sharedPref.edit().putInt("startFragment", 0).apply();
                    } else {
                        Snackbar.make(listView, R.string.toast_picFile, Snackbar.LENGTH_LONG).show();
                    }

                } else if(sharedPref.getInt("fileManager", 0) == 2) {
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
                        viewPager.setCurrentItem(sharedPref.getInt("startFragment", 0));
                        final String fileName = pathFile.getAbsolutePath().substring(pathFile.getAbsolutePath().lastIndexOf("/")+1);
                        sharedPref.edit().putString("pathPDF", pathFile.getAbsolutePath()).apply();
                        sharedPref.edit().putString("title", fileName).apply();
                        sharedPref.edit().putInt("fileManager", 0).apply();
                        sharedPref.edit().putInt("startFragment", 0).apply();
                    } else {
                        Snackbar.make(listView, R.string.toast_pdfFile, Snackbar.LENGTH_LONG).show();
                    }

                } else if(sharedPref.getInt("fileManager", 0) == 3) {
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
                        viewPager.setCurrentItem(sharedPref.getInt("startFragment", 0));
                        sharedPref.edit().putString("file_chooseSecondPDF", pathFile.getAbsolutePath()).apply();
                        sharedPref.edit().putInt("fileManager", 0).apply();
                        sharedPref.edit().putInt("startFragment", 0).apply();
                    } else {
                        Snackbar.make(listView, R.string.toast_pdfFile, Snackbar.LENGTH_LONG).show();
                    }

                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor row2 = (Cursor) listView.getItemAtPosition(position);
                final String files_title = row2.getString(row2.getColumnIndexOrThrow("files_title"));
                final String files_attachment = row2.getString(row2.getColumnIndexOrThrow("files_attachment"));

                final File pathFile = new File(files_attachment);

                if (pathFile.isDirectory()) {
                    Snackbar snackbar = Snackbar
                            .make(listView, R.string.toast_remove_confirmation, Snackbar.LENGTH_LONG)
                            .setAction(R.string.toast_yes, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    sharedPref.edit().putString("files_startFolder", pathFile.getParent()).apply();
                                    deleteRecursive(pathFile);
                                    setFilesList();
                                }
                            });
                    snackbar.show();

                } else {
                    final CharSequence[] options = {
                            getString(R.string.choose_menu_2),
                            getString(R.string.choose_menu_3),
                            getString(R.string.choose_menu_4)};

                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setPositiveButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
                    dialog.setItems(options, new DialogInterface.OnClickListener() {
                        @SuppressWarnings("ResultOfMethodCallIgnored")
                        @Override
                        public void onClick(DialogInterface dialog, int item) {

                            if (options[item].equals(getString(R.string.choose_menu_2))) {

                                if (pathFile.exists()) {
                                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                    sharingIntent.setType("image/png");
                                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, files_title);
                                    sharingIntent.putExtra(Intent.EXTRA_TEXT, files_title);
                                    Uri bmpUri = Uri.fromFile(pathFile);
                                    sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                    startActivity(Intent.createChooser(sharingIntent, (getString(R.string.app_share_file))));
                                }
                            }
                            if (options[item].equals(getString(R.string.choose_menu_4))) {

                                Snackbar snackbar = Snackbar
                                        .make(listView, R.string.toast_remove_confirmation, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.toast_yes, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                pathFile.delete();
                                                setFilesList();
                                            }
                                        });
                                snackbar.show();
                            }
                            if (options[item].equals(getString(R.string.choose_menu_3))) {
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                                View dialogView = View.inflate(getActivity(), R.layout.dialog_edit_file, null);

                                final EditText edit_title = (EditText) dialogView.findViewById(R.id.pass_title);
                                edit_title.setText(files_title);

                                builder.setView(dialogView);
                                builder.setTitle(R.string.choose_title);
                                builder.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        String inputTag = edit_title.getText().toString().trim();

                                        File dir = pathFile.getParentFile();
                                        File to = new File(dir,inputTag);

                                        pathFile.renameTo(to);
                                        pathFile.delete();
                                        setFilesList();
                                    }
                                });
                                builder.setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                    }
                                });
                                AlertDialog dialog2 = builder.create();
                                // Display the custom alert dialog on interface
                                dialog2.show();
                                helper_main.showKeyboard(getActivity(),edit_title);
                            }
                        }
                    });
                    dialog.show();
                }

                return true;
            }
        });
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
    public void onPrepareOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_help).setVisible(false);
        menu.findItem(R.id.action_share).setVisible(false);
        menu.findItem(R.id.action_open).setVisible(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_file, menu);
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
                helper_main.showKeyboard(getActivity(), filter);
                return true;

            case R.id.filter_today:
                getActivity().setTitle(getString(R.string.choose_titleMain) + " | " + getString(R.string.filter_today));
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                final String search = dateFormat.format(cal.getTime());
                sharedPref.edit().putString("filter_filesBY", "files_creation").apply();
                setFilesList();
                filter.setText(search);
                return true;
            case R.id.filter_yesterday:
                getActivity().setTitle(getString(R.string.choose_titleMain) + " | " + getString(R.string.filter_yesterday));
                DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal2 = Calendar.getInstance();
                cal2.add(Calendar.DATE, -1);
                final String search2 = dateFormat2.format(cal2.getTime());
                sharedPref.edit().putString("filter_filesBY", "files_creation").apply();
                setFilesList();
                filter.setText(search2);
                return true;
            case R.id.filter_before:
                getActivity().setTitle(getString(R.string.choose_titleMain) + " | " + getString(R.string.filter_before));
                DateFormat dateFormat3 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal3 = Calendar.getInstance();
                cal3.add(Calendar.DATE, -2);
                final String search3 = dateFormat3.format(cal3.getTime());
                sharedPref.edit().putString("filter_filesBY", "files_creation").apply();
                setFilesList();
                filter.setText(search3);
                return true;
            case R.id.filter_month:
                getActivity().setTitle(getString(R.string.choose_titleMain) + " | " + getString(R.string.filter_month));
                DateFormat dateFormat4 = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                Calendar cal4 = Calendar.getInstance();
                final String search4 = dateFormat4.format(cal4.getTime());
                sharedPref.edit().putString("filter_filesBY", "files_creation").apply();
                setFilesList();
                filter.setText(search4);
                return true;
            case R.id.filter_own:
                getActivity().setTitle(getString(R.string.choose_titleMain) + " | " + getString(R.string.filter_own));
                sharedPref.edit().putString("filter_filesBY", "files_creation").apply();
                setFilesList();
                filter_layout.setVisibility(View.VISIBLE);
                filter.setText("");
                filter.setHint(R.string.action_filter_create);
                filter.requestFocus();
                helper_main.showKeyboard(getActivity(), filter);
                return true;
            case R.id.filter_clear:
                filter.setText("");
                setFilesList();
                return true;

            case R.id.sort_title:
                sharedPref.edit().putString("sortDBF", "title").apply();
                setTitle();
                setFilesList();
                return true;
            case R.id.sort_ext:
                sharedPref.edit().putString("sortDBF", "file_ext").apply();
                setTitle();
                setFilesList();
                return true;
            case R.id.sort_creation:
                sharedPref.edit().putString("sortDBF", "file_date").apply();
                setTitle();
                setFilesList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
