package de.baumann.pdfcreator.helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.keenfin.sfcdialog.SimpleFileChooser;

import java.io.File;

import de.baumann.pdfcreator.R;

public class Helper {

    public static void openFile (Activity activity, File file, String string, View view) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(contentUri,string);

        } else {
            intent.setDataAndType(Uri.fromFile(file),string);
        }

        try {
            activity.startActivity (intent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(view, R.string.toast_install_app, Snackbar.LENGTH_LONG).show();
        }
    }

    public static void openFilePicker (final Activity activity, final View view) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

        SimpleFileChooser sfcDialog = new SimpleFileChooser();
        sfcDialog.setRootPath(Environment.getExternalStorageDirectory() + folder);

        sfcDialog.setOnChosenListener(new SimpleFileChooser.SimpleFileChooserListener() {
            @Override
            public void onFileChosen(File file) {
                // File is chosen

                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(file.toString().replace(" ", ""));

                switch (fileExtension) {
                    case "pdf":
                        Helper.openFile(activity, file, "application/pdf", view);
                        break;
                    case "jpeg":
                    case "jpg":
                    case "png":
                        Helper.openFile(activity, file, "image/*", view);
                        break;
                    case "odt":
                    case "txt":
                    case "html":
                    case "docx":
                    case "doc":
                        Helper.openFile(activity, file, "text/*", view);
                        break;
                    case "ogg":
                    case "wav":
                    case "mp3":
                        Helper.openFile(activity, file, "audio/*", view);
                        break;
                    default:
                        Snackbar.make(view, R.string.toast_extension, Snackbar.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onDirectoryChosen(File directory) {
                // Directory is chosen
                Snackbar.make(view, R.string.toast_file, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                // onCancel
            }
        });

        sfcDialog.show(activity.getFragmentManager(), "SimpleFileChooserDialog");
    }

    public static SpannableString textSpannable (String text) {
        SpannableString s;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            s = new SpannableString(Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY));
        } else {
            //noinspection deprecation
            s = new SpannableString(Html.fromHtml(text));
        }
        Linkify.addLinks(s, Linkify.WEB_URLS);
        return s;
    }
}
