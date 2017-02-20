package de.baumann.pdfcreator;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.baumann.pdfcreator.helper.helper_main;
import de.baumann.pdfcreator.helper.UserSettingsActivity;
import de.baumann.pdfcreator.helper.helper_pdf;
import de.baumann.pdfcreator.pages.add_text;
import de.baumann.pdfcreator.pages.create_image;
import de.baumann.pdfcreator.pages.add_image;
import de.baumann.pdfcreator.pages.create_text;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MainActivity extends AppCompatActivity {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    private ViewPager viewPager;
    private SharedPreferences sharedPref;

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
                if (type.startsWith("image2/")) {
                    sharedPref.edit().putInt("startFragment", 0).apply();
                } if (type.startsWith("text/")) {
                    sharedPref.edit().putInt("startFragment", 1).apply();
                }
            }
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper_pdf.toolbar(MainActivity.this);

        boolean show = sharedPref.getBoolean("help_notShow", true);
        if (show){
            final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.app_name)
                    .setMessage(helper_main.textSpannable(getString(R.string.dialog_help)))
                    .setPositiveButton(getString(R.string.toast_yes), null)
                    .setNegativeButton(getString(R.string.toast_notAgain), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            sharedPref.edit().putBoolean("help_notShow", false).apply();
                        }
                    });
            dialog.show();
        }

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int hasWRITE_EXTERNAL_STORAGE = MainActivity.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                if (!MainActivity.this.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.app_permissions_title)
                            .setMessage(helper_main.textSpannable(MainActivity.this.getString(R.string.app_permissions)))
                            .setNeutralButton(R.string.toast_notAgain, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    sharedPref.edit().putBoolean("perm_notShow", false).apply();
                                }
                            })
                            .setPositiveButton(MainActivity.this.getString(R.string.toast_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (android.os.Build.VERSION.SDK_INT >= 23)
                                        MainActivity.this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            })
                            .setNegativeButton(MainActivity.this.getString(R.string.toast_cancel), null)
                            .show();
                    return;
                }
                MainActivity.this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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

        adapter.addFragment(new create_image(), String.valueOf(getString(R.string.create_image)));
        adapter.addFragment(new create_text(), String.valueOf(getString(R.string.create_text)));
        adapter.addFragment(new add_image(), String.valueOf(getString(R.string.add_image)));
        adapter.addFragment(new add_text(), String.valueOf(getString(R.string.add_text)));

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startFragment);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private ViewPagerAdapter(FragmentManager manager) {
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

        private void addFragment(Fragment fragment, String title) {
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

        if (id == R.id.action_folder) {
            String folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
            helper_main.openFilePicker(MainActivity.this, viewPager, Environment.getExternalStorageDirectory() + folder);
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, UserSettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
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