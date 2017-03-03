package de.baumann.pdfcreator.helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import java.io.File;

import de.baumann.pdfcreator.R;

public class helper_main {

    public static void openFile (Activity activity, File file, String string, View view) {

        Intent intent = new Intent();
        intent.putExtra("path", file.getAbsolutePath());
        intent.putExtra("name", file.getName());
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

    public static void open (String extension, Activity activity, File pathFile, View view) {
        File file = new File(pathFile.getAbsolutePath());
        final String fileExtension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
        String text = activity.getString(R.string.toast_extension) + ": " + fileExtension;
        switch (extension) {
            case ".gif":
            case ".bmp":
            case ".tiff":
            case ".svg":
            case ".png":
            case ".jpg":
            case ".JPG":
            case ".jpeg":
                helper_main.openFile(activity, pathFile, "image/*", view);
                break;
            case ".m3u8":
            case ".mp3":
            case ".wma":
            case ".midi":
            case ".wav":
            case ".aac":
            case ".aif":
            case ".amp3":
            case ".weba":
                helper_main.openFile(activity, pathFile, "audio/*", view);
                break;
            case ".mpeg":
            case ".mp4":
            case ".ogg":
            case ".webm":
            case ".qt":
            case ".3gp":
            case ".3g2":
            case ".avi":
            case ".f4v":
            case ".flv":
            case ".h261":
            case ".h263":
            case ".h264":
            case ".asf":
            case ".wmv":
                helper_main.openFile(activity, pathFile, "video/*", view);
                break;
            case ".rtx":
            case ".csv":
            case ".txt":
            case ".vcs":
            case ".vcf":
            case ".css":
            case ".ics":
            case ".conf":
            case ".config":
            case ".java":
                helper_main.openFile(activity, pathFile, "text/*", view);
                break;
            case ".html":
                helper_main.openFile(activity, pathFile, "text/html", view);
                break;
            case ".apk":
                helper_main.openFile(activity, pathFile, "application/vnd.android.package-archive", view);
                break;
            case ".pdf":
                helper_main.openFile(activity, pathFile, "application/pdf", view);
                break;
            case ".doc":
                helper_main.openFile(activity, pathFile, "application/msword", view);
                break;
            case ".xls":
                helper_main.openFile(activity, pathFile, "application/vnd.ms-excel", view);
                break;
            case ".ppt":
                helper_main.openFile(activity, pathFile, "application/vnd.ms-powerpoint", view);
                break;
            case ".docx":
                helper_main.openFile(activity, pathFile, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", view);
                break;
            case ".pptx":
                helper_main.openFile(activity, pathFile, "application/vnd.openxmlformats-officedocument.presentationml.presentation", view);
                break;
            case ".xlsx":
                helper_main.openFile(activity, pathFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", view);
                break;
            case ".odt":
                helper_main.openFile(activity, pathFile, "application/vnd.oasis.opendocument.text", view);
                break;
            case ".ods":
                helper_main.openFile(activity, pathFile, "application/vnd.oasis.opendocument.spreadsheet", view);
                break;
            case ".odp":
                helper_main.openFile(activity, pathFile, "application/vnd.oasis.opendocument.presentation", view);
                break;
            case ".zip":
                helper_main.openFile(activity, pathFile, "application/zip", view);
                break;
            case ".rar":
                helper_main.openFile(activity, pathFile, "application/x-rar-compressed", view);
                break;
            case ".epub":
                helper_main.openFile(activity, pathFile, "application/epub+zip", view);
                break;
            case ".cbz":
                helper_main.openFile(activity, pathFile, "application/x-cbz", view);
                break;
            case ".cbr":
                helper_main.openFile(activity, pathFile, "application/x-cbr", view);
                break;
            case ".fb2":
                helper_main.openFile(activity, pathFile, "application/x-fb2", view);
                break;
            case ".rtf":
                helper_main.openFile(activity, pathFile, "application/rtf", view);
                break;
            case ".opml":
                helper_main.openFile(activity, pathFile, "application/opml", view);
                break;

            default:
                Snackbar snackbar = Snackbar
                        .make(view, text, Snackbar.LENGTH_LONG);
                snackbar.show();

                break;
        }
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

    public static void showKeyboard(Activity from, EditText editText) {
        InputMethodManager imm = (InputMethodManager) from.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }
}
