package de.baumann.pdfcreator.helper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import de.baumann.pdfcreator.R;


public class helper_dialogs {

    public static void dialog_path (final Activity activity) {

        PreferenceManager.setDefaultValues(activity, R.xml.user_settings, false);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = View.inflate(activity, R.layout.dialog_path, null);

        final EditText path = (EditText) dialogView.findViewById(R.id.path);
        path.setText(sharedPref.getString("folder", ""));

        builder.setView(dialogView);
        builder.setTitle(R.string.settings_prefDir);
        builder.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                String inputTag = path.getText().toString().trim();
                sharedPref.edit().putString("folder", inputTag).apply();

                if (inputTag.length() > 0) {
                    sharedPref.edit().putBoolean("folderDef", false).apply();
                } else {
                    sharedPref.edit().putBoolean("folderDef", true).apply();
                }
            }
        });
        builder.setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog2 = builder.create();
        // Display the custom alert dialog on interface
        dialog2.show();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                helper_main.showKeyboard(activity, path);
            }
        }, 200);
    }

    public static void dialog_encryption (final Activity activity) {

        PreferenceManager.setDefaultValues(activity, R.xml.user_settings, false);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = View.inflate(activity, R.layout.dialog_encrypt, null);

        final EditText pass_ownerPW = (EditText) dialogView.findViewById(R.id.pass_ownerPW);
        pass_ownerPW.setText(sharedPref.getString("pwOWNER", "OWNER"));
        final EditText pass_userPW = (EditText) dialogView.findViewById(R.id.pass_userPW);
        pass_userPW.setText(sharedPref.getString("pwUSER", "USER"));

        builder.setView(dialogView);
        builder.setTitle(R.string.settings_prefEnc);
        builder.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                String inputTag = pass_ownerPW.getText().toString().trim();
                sharedPref.edit().putString("pwOWNER", inputTag).apply();

                String inputTag2 = pass_userPW.getText().toString().trim();
                sharedPref.edit().putString("pwUSER", inputTag2).apply();
            }
        });
        builder.setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog2 = builder.create();
        // Display the custom alert dialog on interface
        dialog2.show();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                helper_main.showKeyboard(activity, pass_ownerPW);
            }
        }, 200);
    }

    public static void dialog_metaTags (final Activity activity) {

        PreferenceManager.setDefaultValues(activity, R.xml.user_settings, false);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = View.inflate(activity, R.layout.dialog_meta, null);

        final EditText metaAuthor = (EditText) dialogView.findViewById(R.id.metaAuthor);
        metaAuthor.setText(sharedPref.getString("metaAuthor", ""));
        final EditText metaCreator = (EditText) dialogView.findViewById(R.id.metaCreator);
        metaCreator.setText(sharedPref.getString("metaCreator", "PDF Creator using iText"));
        final EditText metaSubject = (EditText) dialogView.findViewById(R.id.metaSubject);
        metaSubject.setText(sharedPref.getString("metaSubject", ""));
        final EditText metaKeywords = (EditText) dialogView.findViewById(R.id.metaKeywords);
        metaKeywords.setText(sharedPref.getString("metaKeywords", ""));

        builder.setView(dialogView);
        builder.setTitle(R.string.settings_prefMeta);
        builder.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                String inputTag = metaAuthor.getText().toString().trim();
                sharedPref.edit().putString("metaAuthor", inputTag).apply();

                String inputTag2 = metaCreator.getText().toString().trim();
                sharedPref.edit().putString("metaCreator", inputTag2).apply();

                String inputTag3 = metaSubject.getText().toString().trim();
                sharedPref.edit().putString("metaSubject", inputTag3).apply();

                String inputTag4 = metaKeywords.getText().toString().trim();
                sharedPref.edit().putString("metaKeywords", inputTag4).apply();
            }
        });
        builder.setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog2 = builder.create();
        // Display the custom alert dialog on interface
        dialog2.show();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                helper_main.showKeyboard(activity, metaAuthor);
            }
        }, 200);
    }
}
