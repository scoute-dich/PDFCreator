package de.baumann.pdfcreator.helper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.baumann.pdfcreator.R;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class helper_pdf {

    public static String actualPath (Activity activity) {

        String title;
        String folder;
        String path;

        PreferenceManager.setDefaultValues(activity, R.xml.user_settings, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        title = sharedPref.getString("title", null);
        folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
        path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                folder + title + ".pdf");

        return path;
    }


    public static void pdf_backup (final Activity activity) {

        String title;
        String folder;

        PreferenceManager.setDefaultValues(activity, R.xml.user_settings, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        if (sharedPref.getBoolean ("backup", false)){

            InputStream in;
            OutputStream out;

            try {
                title = sharedPref.getString("title", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

                in = new FileInputStream(helper_pdf.actualPath(activity));
                out = new FileOutputStream(Environment.getExternalStorageDirectory() +
                        folder + "pdf_backups/" + title + ".pdf");

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
                Log.e("tag", e.getMessage());
            }
        }
    }

    public static void pdf_textField (final Activity activity, final View view) {

        String title;

        PreferenceManager.setDefaultValues(activity, R.xml.user_settings, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        TextView textTitle = (TextView) view.findViewById(R.id.textTitle);

        textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
                if (sharedPref.getBoolean ("rotate", false)) {
                    sharedPref.edit()
                            .putBoolean("rotate", false)
                            .apply();
                } else {
                    sharedPref.edit()
                            .putBoolean("rotate", true)
                            .apply();
                }
                helper_pdf.pdf_textField(activity, view);
            }
        });

        title = sharedPref.getString("title", null);
        File pdfFile = new File(helper_pdf.actualPath(activity));
        String textRotate;

        if (sharedPref.getBoolean ("rotate", false)) {
            textRotate = activity.getString(R.string.app_portrait);
        } else {
            textRotate = activity.getString(R.string.app_landscape);
        }

        String text = title + " | " + textRotate;
        String text2 = activity.getString(R.string.toast_noPDF) + " | " + textRotate;

        if (pdfFile.exists()) {
            textTitle.setText(text);
        } else {
            textTitle.setText(text2);
        }
    }

    public static void pdf_deleteTemp_1 (final Activity activity) {

        InputStream in;
        OutputStream out;

        try {

            in = new FileInputStream(Environment.getExternalStorageDirectory() +  "/" + "123456.pdf");
            out = new FileOutputStream(helper_pdf.actualPath(activity));

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
            Log.e("tag", e.getMessage());
        }

        File pdfFile = new File(Environment.getExternalStorageDirectory() +  "/" + "123456.pdf");
        if(pdfFile.exists()){
            pdfFile.delete();
        }
    }

    public static void pdf_deleteTemp_2 (final Activity activity) {

        InputStream in;
        OutputStream out;

        try {

            in = new FileInputStream(Environment.getExternalStorageDirectory() +  "/" + "1234567.pdf");
            out = new FileOutputStream(helper_pdf.actualPath(activity));

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
            Log.e("tag", e.getMessage());
        }

        File pdfFile = new File(Environment.getExternalStorageDirectory() +  "/" + "1234567.pdf");
        if(pdfFile.exists()){
            pdfFile.delete();
        }

    }
}
