package de.baumann.pdfcreator.helper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

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


    public static void toolbar (final Activity activity) {

        String title;

        PreferenceManager.setDefaultValues(activity, R.xml.user_settings, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPref.getString("rotateString", "portrait");

        title = sharedPref.getString("title", null);
        File pdfFile = new File(helper_pdf.actualPath(activity));

        if (pdfFile.exists()) {
            activity.setTitle(title);
        } else {
            activity.setTitle(R.string.app_name);
        }
    }

    public static void pdf_textField (final Activity activity, final View view) {

        String title;

        PreferenceManager.setDefaultValues(activity, R.xml.user_settings, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPref.getString("rotateString", "portrait");

        TextView textTitle = (TextView) view.findViewById(R.id.textTitle);

        textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
                if (sharedPref.getString ("rotateString", "portrait").equals("portrait")) {
                    sharedPref.edit()
                            .putString("rotateString", "landscape")
                            .apply();
                } else {
                    sharedPref.edit()
                            .putString("rotateString", "portrait")
                            .apply();
                }
                helper_pdf.pdf_textField(activity, view);
            }
        });

        title = sharedPref.getString("title", null);
        File pdfFile = new File(helper_pdf.actualPath(activity));
        String textRotate;

        if (sharedPref.getString ("rotateString", "portrait").equals("portrait")) {
            textRotate = activity.getString(R.string.app_portrait);
        } else {
            textRotate = activity.getString(R.string.app_landscape);
        }

        String text = title + " | " + textRotate;
        String text2 = activity.getString(R.string.toast_noPDF) + " | " + textRotate;

        if (pdfFile.exists() && helper_pdf.actualPath(activity).contains(".pdf")) {
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

    public static void pdf_mergePDF(Activity activity, View view) {

        String path2 = Environment.getExternalStorageDirectory() +  "/" + "123456.pdf";
        String path3 = Environment.getExternalStorageDirectory() +  "/" + "1234567.pdf";

        try {
            String[] files = { helper_pdf.actualPath(activity), path2 };
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(path3));
            document.open();
            PdfReader ReadInputPDF;
            int number_of_pages;
            for (String file : files) {
                ReadInputPDF = new PdfReader(file);
                number_of_pages = ReadInputPDF.getNumberOfPages();
                for (int page = 0; page < number_of_pages; ) {
                    copy.addPage(copy.getImportedPage(ReadInputPDF, ++page));
                }
            }
            document.close();
        } catch (Exception i) {
            Snackbar.make(view, activity.getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG).show();
        }
        helper_pdf.pdf_deleteTemp_1(activity);
    }

    public static void pdf_success (final Activity activity, View view) {

        Snackbar snackbar = Snackbar
                .make(view, activity.getString(R.string.toast_successfully), Snackbar.LENGTH_LONG)
                .setAction(activity.getString(R.string.toast_open), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File file = new File(helper_pdf.actualPath(activity));

                        Intent intent = new Intent(activity, MuPDFActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.fromFile(file));
                        activity.startActivity(intent);
                    }
                });
        snackbar.show();
    }
}
