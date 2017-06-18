package de.baumann.pdfcreator;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.baumann.pdfcreator.helper.Activity_Settings;
import de.baumann.pdfcreator.helper.Activity_intro;
import de.baumann.pdfcreator.helper.helper_main;
import de.baumann.pdfcreator.fragments.add_text;
import de.baumann.pdfcreator.fragments.create_image;
import de.baumann.pdfcreator.fragments.add_image;
import de.baumann.pdfcreator.fragments.create_text;
import de.baumann.pdfcreator.fragments.file_manager;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Activity_Main extends AppCompatActivity {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    private SharedPreferences sharedPref;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Get intent, action and MIME type
        Intent intent = this.getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean appStarted = sharedPref.getBoolean("appStarted", true);

        if (appStarted) {
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if (type.startsWith("image/")) {
                    sharedPref.edit().putInt("startFragment", 1).apply();
                } else if (type.startsWith("text/")) {
                    sharedPref.edit().putInt("startFragment", 2).apply();
                } else if (type.startsWith("application/pdf")) {
                    sharedPref.edit().putInt("startFragment", 4).apply();
                }
            } else if ("pdf_openFolder".equals(action)) {

                String path = intent.getStringExtra("path");
                String name = intent.getStringExtra("name");

                if (path == null || name == null) {
                    sharedPref.edit().putString("menu", "no").apply();
                } else {
                    setTitle(name);
                    sharedPref.edit().putString("pathPDF", path).apply();
                    sharedPref.edit().putString("title", name).apply();
                }
            }
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (position == 0){
                    file_manager file_manager = (file_manager) viewPager.getAdapter().instantiateItem(viewPager, viewPager.getCurrentItem());
                    file_manager.setTitle();
                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                        int hasWRITE_EXTERNAL_STORAGE = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (hasWRITE_EXTERNAL_STORAGE == PackageManager.PERMISSION_GRANTED) {
                            file_manager.setFilesList();
                        } else {
                            Snackbar snackbar = Snackbar
                                    .make(viewPager, R.string.toast_permission, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.toast_yes, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                                            intent.setData(uri);
                                            startActivity(intent);
                                        }
                                    });
                            snackbar.show();
                        }
                    } else {
                        file_manager.setFilesList();
                    }
                } else if (position == 1){
                    create_image create_image = (create_image) viewPager.getAdapter().instantiateItem(viewPager, viewPager.getCurrentItem());
                    create_image.onResume();
                } else if (position == 2){
                    create_text create_text = (create_text) viewPager.getAdapter().instantiateItem(viewPager, viewPager.getCurrentItem());
                    create_text.onResume();
                } else if (position == 3){
                    add_image add_image = (add_image) viewPager.getAdapter().instantiateItem(viewPager, viewPager.getCurrentItem());
                    add_image.onResume();
                } else if (position == 4){
                    add_text add_text = (add_text) viewPager.getAdapter().instantiateItem(viewPager, viewPager.getCurrentItem());
                    add_text.onResume();
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean show = sharedPref.getBoolean("introShowDo_notShow", true);

        if (show){
            Intent intent2 = new Intent(Activity_Main.this, Activity_intro.class);
            startActivity(intent2);
            overridePendingTransition(0, 0);
        }

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int hasWRITE_EXTERNAL_STORAGE = Activity_Main.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                if (!Activity_Main.this.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    new AlertDialog.Builder(Activity_Main.this)
                            .setTitle(R.string.app_permissions_title)
                            .setMessage(helper_main.textSpannable(Activity_Main.this.getString(R.string.app_permissions)))
                            .setNeutralButton(R.string.toast_notAgain, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    sharedPref.edit().putBoolean("perm_notShow", false).apply();
                                }
                            })
                            .setPositiveButton(Activity_Main.this.getString(R.string.toast_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (android.os.Build.VERSION.SDK_INT >= 23)
                                        Activity_Main.this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            })
                            .setNegativeButton(Activity_Main.this.getString(R.string.toast_cancel), null)
                            .show();
                    return;
                }
                Activity_Main.this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }

        if (sharedPref.getBoolean ("folderDef", false)){
            sharedPref.edit().putString("folder", "/Android/data/de.baumann.pdf/").apply();
        }

        String folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
        File directory = new File(Environment.getExternalStorageDirectory() + folder + "/pdf_backups/");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File imgFolder = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/");
        if (!imgFolder.exists()) {
            imgFolder.mkdirs();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        int startFragment = sharedPref.getInt("startFragment", 0);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new file_manager(), String.valueOf(getString(R.string.choose_titleMain)));
        adapter.addFragment(new create_image(), String.valueOf(getString(R.string.create_image)));
        adapter.addFragment(new create_text(), String.valueOf(getString(R.string.create_text)));
        adapter.addFragment(new add_image(), String.valueOf(getString(R.string.add_image)));
        adapter.addFragment(new add_text(), String.valueOf(getString(R.string.add_text)));

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startFragment);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);// add return null; to display only icons
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(Activity_Main.this, Activity_Settings.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/");

        if (file.exists()) {
            String deleteCmd = "rm -r " + file;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sharedPref.edit().putInt("startFragment", 0).putBoolean("appStarted", true).apply();
        super.onBackPressed();
    }
}