package de.baumann.pdfcreator.helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.io.File;

import de.baumann.pdfcreator.R;
import filechooser.ChooserDialog;

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

    public static void openFilePicker (final Activity activity, final View view, final String startDir) {

        new ChooserDialog().with(activity)
                .withStartFile(startDir)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(final File pathFile) {

                        final String fileExtension = pathFile.getAbsolutePath().substring(pathFile.getAbsolutePath().lastIndexOf("."));
                        final String fileName = pathFile.getAbsolutePath().substring(pathFile.getAbsolutePath().lastIndexOf("/")+1);
                        final String  fileNameWE = fileName.substring(0, fileName.lastIndexOf("."));

                        final CharSequence[] options = {
                                activity.getString(R.string.choose_menu_1),
                                activity.getString(R.string.choose_menu_2),
                                activity.getString(R.string.choose_menu_3),
                                activity.getString(R.string.choose_menu_4)};

                        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);

                        dialog.setItems(options, new DialogInterface.OnClickListener() {
                            @SuppressWarnings("ResultOfMethodCallIgnored")
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (options[item].equals(activity.getString(R.string.choose_menu_1))) {

                                    String text = (activity.getString(R.string.toast_extension) + ": " + fileExtension);

                                    switch (fileExtension) {
                                        case ".gif":
                                        case ".bmp":
                                        case ".tiff":
                                        case ".svg":
                                        case ".png":
                                        case ".jpg":
                                        case ".jpeg":
                                            Helper.openFile(activity, pathFile, "image/*", view);
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
                                            Helper.openFile(activity, pathFile, "audio/*", view);
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
                                            Helper.openFile(activity, pathFile, "video/*", view);
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
                                            Helper.openFile(activity, pathFile, "text/*", view);
                                            break;
                                        case ".html":
                                            Helper.openFile(activity, pathFile, "text/html", view);
                                            break;
                                        case ".apk":
                                            Helper.openFile(activity, pathFile, "application/vnd.android.package-archive", view);
                                            break;
                                        case ".pdf":
                                            Helper.openFile(activity, pathFile, "application/pdf", view);
                                            break;
                                        case ".doc":
                                            Helper.openFile(activity, pathFile, "application/msword", view);
                                            break;
                                        case ".xls":
                                            Helper.openFile(activity, pathFile, "application/vnd.ms-excel", view);
                                            break;
                                        case ".ppt":
                                            Helper.openFile(activity, pathFile, "application/vnd.ms-powerpoint", view);
                                            break;
                                        case ".docx":
                                            Helper.openFile(activity, pathFile, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", view);
                                            break;
                                        case ".pptx":
                                            Helper.openFile(activity, pathFile, "application/vnd.openxmlformats-officedocument.presentationml.presentation", view);
                                            break;
                                        case ".xlsx":
                                            Helper.openFile(activity, pathFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", view);
                                            break;
                                        case ".odt":
                                            Helper.openFile(activity, pathFile, "application/vnd.oasis.opendocument.text", view);
                                            break;
                                        case ".ods":
                                            Helper.openFile(activity, pathFile, "application/vnd.oasis.opendocument.spreadsheet", view);
                                            break;
                                        case ".odp":
                                            Helper.openFile(activity, pathFile, "application/vnd.oasis.opendocument.presentation", view);
                                            break;
                                        case ".zip":
                                            Helper.openFile(activity, pathFile, "application/zip", view);
                                            break;
                                        case ".rar":
                                            Helper.openFile(activity, pathFile, "application/x-rar-compressed", view);
                                            break;
                                        case ".epub":
                                            Helper.openFile(activity, pathFile, "application/epub+zip", view);
                                            break;
                                        case ".cbz":
                                            Helper.openFile(activity, pathFile, "application/x-cbz", view);
                                            break;
                                        case ".cbr":
                                            Helper.openFile(activity, pathFile, "application/x-cbr", view);
                                            break;
                                        case ".fb2":
                                            Helper.openFile(activity, pathFile, "application/x-fb2", view);
                                            break;
                                        case ".rtf":
                                            Helper.openFile(activity, pathFile, "application/rtf", view);
                                            break;
                                        case ".opml":
                                            Helper.openFile(activity, pathFile, "application/opml", view);
                                            break;

                                        default:
                                            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                                            break;
                                    }

                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                    Helper.openFilePicker(activity, view, dir);
                                }
                                if (options[item].equals(activity.getString(R.string.choose_menu_2))) {

                                    if (pathFile.exists()) {
                                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                        sharingIntent.setType("image/png");
                                        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, fileName);
                                        sharingIntent.putExtra(Intent.EXTRA_TEXT, fileName);
                                        Uri bmpUri = Uri.fromFile(pathFile);
                                        sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                        activity.startActivity(Intent.createChooser(sharingIntent, (activity.getString(R.string.app_share_file))));
                                    }
                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                    Helper.openFilePicker(activity, view, dir);
                                }
                                if (options[item].equals(activity.getString(R.string.choose_menu_4))) {
                                    final AlertDialog.Builder dialog2 = new AlertDialog.Builder(activity);

                                    dialog2.setMessage(activity.getString(R.string.choose_delete));
                                    dialog2.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            pathFile.delete();
                                            new Handler().postDelayed(new Runnable() {
                                                public void run() {
                                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                                    Helper.openFilePicker(activity, view, dir);
                                                }
                                            }, 500);
                                        }
                                    });
                                    dialog2.setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.cancel();
                                        }
                                    });
                                    dialog2.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            // dialog dismiss without button press
                                            String dir = pathFile.getParentFile().getAbsolutePath();
                                            Helper.openFilePicker(activity, view, dir);
                                        }
                                    });
                                    dialog2.show();
                                }
                                if (options[item].equals(activity.getString(R.string.choose_menu_3))) {

                                    final LinearLayout layout = new LinearLayout(activity);
                                    layout.setOrientation(LinearLayout.VERTICAL);
                                    layout.setGravity(Gravity.CENTER_HORIZONTAL);
                                    final EditText input = new EditText(activity);
                                    input.setSingleLine(true);
                                    input.setHint(activity.getString(R.string.choose_hint));
                                    input.setText(fileNameWE);
                                    layout.setPadding(30, 0, 50, 0);
                                    layout.addView(input);

                                    new Handler().postDelayed(new Runnable() {
                                        public void run() {
                                            Helper.showKeyboard(activity,input);
                                        }
                                    }, 200);

                                    final AlertDialog.Builder dialog2 = new AlertDialog.Builder(activity);

                                    dialog2.setView(layout);
                                    dialog2.setMessage(activity.getString(R.string.choose_hint));
                                    dialog2.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {

                                            String inputTag = input.getText().toString().trim();

                                            File dir = pathFile.getParentFile();
                                            File to = new File(dir,inputTag + fileExtension);

                                            pathFile.renameTo(to);
                                            pathFile.delete();

                                            new Handler().postDelayed(new Runnable() {
                                                public void run() {
                                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                                    Helper.openFilePicker(activity, view, dir);
                                                }
                                            }, 500);
                                        }
                                    });
                                    dialog2.setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.cancel();
                                        }
                                    });
                                    dialog2.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            // dialog dismiss without button press
                                            String dir = pathFile.getParentFile().getAbsolutePath();
                                            Helper.openFilePicker(activity, view, dir);
                                        }
                                    });
                                    dialog2.show();
                                }
                            }
                        });
                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                // dialog dismiss without button press
                                String dir = pathFile.getParentFile().getAbsolutePath();
                                Helper.openFilePicker(activity, view, dir);
                            }
                        });
                        dialog.show();
                    }
                })
                .build()
                .show();
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

    private static void showKeyboard(Activity from, EditText editText) {
        InputMethodManager imm = (InputMethodManager) from.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }
}
