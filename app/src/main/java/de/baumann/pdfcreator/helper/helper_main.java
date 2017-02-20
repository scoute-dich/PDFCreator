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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;


import com.artifex.mupdfdemo.MuPDFActivity;

import java.io.File;

import de.baumann.pdfcreator.R;
import de.baumann.pdfcreator.filechooser.ChooserDialog;

public class helper_main {

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
                .withFilter(false, false, "jpg", "jpeg", "png", "pdf")
                .withResources()
                .withStartFile(startDir)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(final String path, final File pathFile) {
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
                                        case ".png":
                                        case ".jpg":
                                        case ".jpeg":
                                            helper_main.openFile(activity, pathFile, "image/*", view);
                                            break;
                                        case ".pdf":
                                            Uri uri = Uri.parse(pathFile.getAbsolutePath());
                                            Intent intent = new Intent(activity, MuPDFActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.setData(uri);
                                            activity.startActivity(intent);
                                            break;

                                        default:
                                            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                                            break;
                                    }

                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                    helper_main.openFilePicker(activity, view, dir);
                                }
                                if (options[item].equals(activity.getString(R.string.choose_menu_2))) {

                                    if (pathFile.exists()) {
                                        String text = activity.getString(R.string.action_share_Text);

                                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                        sharingIntent.setType("image/png");
                                        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, fileName);
                                        sharingIntent.putExtra(Intent.EXTRA_TEXT, text + " " + fileName);
                                        Uri bmpUri = Uri.fromFile(pathFile);
                                        sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                        activity.startActivity(Intent.createChooser(sharingIntent, (activity.getString(R.string.app_share_file))));
                                    }
                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                    helper_main.openFilePicker(activity, view, dir);
                                }
                                if (options[item].equals(activity.getString(R.string.choose_menu_4))) {
                                    final AlertDialog.Builder dialog2 = new AlertDialog.Builder(activity);

                                    dialog2.setTitle(R.string.confirm);
                                    dialog2.setMessage(activity.getString(R.string.choose_delete));
                                    dialog2.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            pathFile.delete();
                                            helper_pdf.toolbar(activity);
                                            helper_pdf.pdf_textField(activity, view);
                                            new Handler().postDelayed(new Runnable() {
                                                public void run() {
                                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                                    helper_main.openFilePicker(activity, view, dir);
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
                                            helper_main.openFilePicker(activity, view, dir);
                                        }
                                    });
                                    dialog2.show();
                                }
                                if (options[item].equals(activity.getString(R.string.choose_menu_3))) {

                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
                                    View dialogView = View.inflate(activity, R.layout.dialog_edit_file, null);

                                    final EditText edit_title = (EditText) dialogView.findViewById(R.id.pass_title);
                                    edit_title.setText(fileNameWE);

                                    builder.setView(dialogView);
                                    builder.setTitle(R.string.choose_title);
                                    builder.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {

                                            String inputTag = edit_title.getText().toString().trim();

                                            File dir = pathFile.getParentFile();
                                            File to = new File(dir,inputTag + fileExtension);

                                            pathFile.renameTo(to);
                                            pathFile.delete();
                                            helper_pdf.toolbar(activity);
                                            helper_pdf.pdf_textField(activity, view);

                                            new Handler().postDelayed(new Runnable() {
                                                public void run() {
                                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                                    helper_main.openFilePicker(activity, view, dir);
                                                }
                                            }, 500);
                                        }
                                    });
                                    builder.setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            // dialog dismiss without button press
                                            String dir = pathFile.getParentFile().getAbsolutePath();
                                            helper_main.openFilePicker(activity, view, dir);
                                        }
                                    });

                                    final android.app.AlertDialog dialog2 = builder.create();
                                    // Display the custom alert dialog on interface
                                    dialog2.show();

                                    new Handler().postDelayed(new Runnable() {
                                        public void run() {
                                            helper_main.showKeyboard(activity,edit_title);
                                        }
                                    }, 200);
                                }
                            }
                        });
                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                // dialog dismiss without button press
                                String dir = pathFile.getParentFile().getAbsolutePath();
                                helper_main.openFilePicker(activity, view, dir);
                            }
                        });
                        dialog.setPositiveButton(activity.getString(R.string.dialog_cancel), null);
                        dialog.show();
                    }
                })
                .build()
                .show();
    }

    public static void openFilePickerPDF (final Activity activity, final View view, final String startDir) {

        new ChooserDialog().with(activity)
                .withFilter(false, false, "jpg", "jpeg", "png", "pdf")
                .withResources()
                .withStartFile(startDir)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(final String path, final File pathFile) {
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
                                        case ".png":
                                        case ".jpg":
                                        case ".jpeg":
                                            helper_main.openFile(activity, pathFile, "image/*", view);
                                            break;
                                        case ".pdf":
                                            Uri uri = Uri.parse(pathFile.getAbsolutePath());
                                            Intent intent = new Intent(activity, MuPDFActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.setData(uri);
                                            activity.startActivity(intent);
                                            break;

                                        default:
                                            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                                            break;
                                    }

                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                    helper_main.openFilePicker(activity, view, dir);
                                }
                                if (options[item].equals(activity.getString(R.string.choose_menu_2))) {

                                    if (pathFile.exists()) {
                                        String text = activity.getString(R.string.action_share_Text);

                                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                        sharingIntent.setType("image/png");
                                        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, fileName);
                                        sharingIntent.putExtra(Intent.EXTRA_TEXT, text + " " + fileName);
                                        Uri bmpUri = Uri.fromFile(pathFile);
                                        sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                        activity.startActivity(Intent.createChooser(sharingIntent, (activity.getString(R.string.app_share_file))));
                                    }
                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                    helper_main.openFilePicker(activity, view, dir);
                                }
                                if (options[item].equals(activity.getString(R.string.choose_menu_4))) {
                                    final AlertDialog.Builder dialog2 = new AlertDialog.Builder(activity);

                                    dialog2.setTitle(R.string.confirm);
                                    dialog2.setMessage(activity.getString(R.string.choose_delete));
                                    dialog2.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            pathFile.delete();
                                            helper_pdf.toolbar(activity);
                                            new Handler().postDelayed(new Runnable() {
                                                public void run() {
                                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                                    helper_main.openFilePicker(activity, view, dir);
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
                                            helper_main.openFilePicker(activity, view, dir);
                                        }
                                    });
                                    dialog2.show();
                                }
                                if (options[item].equals(activity.getString(R.string.choose_menu_3))) {

                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
                                    View dialogView = View.inflate(activity, R.layout.dialog_edit_file, null);

                                    final EditText edit_title = (EditText) dialogView.findViewById(R.id.pass_title);
                                    edit_title.setText(fileNameWE);

                                    builder.setView(dialogView);
                                    builder.setTitle(R.string.choose_title);
                                    builder.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {

                                            String inputTag = edit_title.getText().toString().trim();

                                            File dir = pathFile.getParentFile();
                                            File to = new File(dir,inputTag + fileExtension);

                                            pathFile.renameTo(to);
                                            pathFile.delete();
                                            helper_pdf.toolbar(activity);

                                            new Handler().postDelayed(new Runnable() {
                                                public void run() {
                                                    String dir = pathFile.getParentFile().getAbsolutePath();
                                                    helper_main.openFilePicker(activity, view, dir);
                                                }
                                            }, 500);
                                        }
                                    });
                                    builder.setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            // dialog dismiss without button press
                                            String dir = pathFile.getParentFile().getAbsolutePath();
                                            helper_main.openFilePicker(activity, view, dir);
                                        }
                                    });

                                    final android.app.AlertDialog dialog2 = builder.create();
                                    // Display the custom alert dialog on interface
                                    dialog2.show();

                                    new Handler().postDelayed(new Runnable() {
                                        public void run() {
                                            helper_main.showKeyboard(activity,edit_title);
                                        }
                                    }, 200);
                                }
                            }
                        });
                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                // dialog dismiss without button press
                                String dir = pathFile.getParentFile().getAbsolutePath();
                                helper_main.openFilePicker(activity, view, dir);
                            }
                        });
                        dialog.setPositiveButton(activity.getString(R.string.dialog_cancel), null);
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

    public static void showKeyboard(Activity from, EditText editText) {
        InputMethodManager imm = (InputMethodManager) from.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }
}
