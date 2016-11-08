package de.baumann.pdfcreator.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.baumann.pdfcreator.R;
import de.baumann.pdfcreator.helper.Helper;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class create_text extends Fragment {

    private String title;
    private String folder;
    private EditText edit;
    private TextView textTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_text, container, false);

        setHasOptionsMenu(true);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String paragraph = edit.getText().toString().trim();

                if (paragraph.isEmpty()) {
                    Snackbar.make(edit, getString(R.string.toast_noText), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

                    LinearLayout layout = new LinearLayout(getActivity());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setGravity(Gravity.CENTER_HORIZONTAL);
                    final EditText input = new EditText(getActivity());
                    input.setSingleLine(true);
                    layout.setPadding(25, 0, 50, 0);
                    input.setHint(R.string.app_hint);
                    layout.addView(input);

                    final AlertDialog d = new AlertDialog.Builder(getActivity())
                            .setView(layout)
                            .setTitle(R.string.app_title)
                            .setCancelable(true)
                            .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    title = input.getText().toString().trim();
                                    sharedPref.edit()
                                            .putString("title", title)
                                            .putString("pathPDF", Environment.getExternalStorageDirectory() +  folder + title + ".pdf")
                                            .apply();
                                    createPDF();

                                    InputStream in;
                                    OutputStream out;

                                    try {

                                        title = sharedPref.getString("title", null);
                                        folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                                        String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                                                folder + title + ".pdf");

                                        in = new FileInputStream(Environment.getExternalStorageDirectory() +  "/" + title + ".pdf");
                                        out = new FileOutputStream(path);

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

                                    File pdfFile = new File(Environment.getExternalStorageDirectory() +  "/" + title + ".pdf");
                                    if(pdfFile.exists()){
                                        pdfFile.delete();
                                    }
                                    edit.setText("");
                                    setTextField();
                                }
                            })
                            .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            })
                            .setNeutralButton(R.string.app_title_date, null)
                            .create();

                    d.setOnShowListener(new DialogInterface.OnShowListener() {

                        @Override
                        public void onShow(DialogInterface dialog) {

                            Button b = d.getButton(AlertDialog.BUTTON_NEUTRAL);
                            b.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    Date date = new Date();
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    String dateNow = format.format(date);
                                    input.append(String.valueOf(dateNow));

                                }
                            });
                        }
                    });
                    d.show();
                }
            }
        });

        edit = (EditText) rootView.findViewById(R.id.editText);
        textTitle = (TextView) rootView.findViewById(R.id.textTitle);
        textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (sharedPref.getBoolean ("rotate", false)) {
                    sharedPref.edit()
                            .putBoolean("rotate", false)
                            .apply();
                } else {
                    sharedPref.edit()
                            .putBoolean("rotate", true)
                            .apply();
                }
                setTextField();
            }
        });
        setTextField();

        FloatingActionButton fab_1 = (FloatingActionButton) rootView.findViewById(R.id.fab_1);
        fab_1.setVisibility(View.INVISIBLE);

        FloatingActionButton fab_2 = (FloatingActionButton) rootView.findViewById(R.id.fab_2);
        fab_2.setVisibility(View.INVISIBLE);

        FloatingActionButton fab_3 = (FloatingActionButton) rootView.findViewById(R.id.fab_3);
        fab_3.setVisibility(View.INVISIBLE);

        FloatingActionButton fab_4 = (FloatingActionButton) rootView.findViewById(R.id.fab_4);
        fab_4.setVisibility(View.INVISIBLE);

        FloatingActionButton fab_5 = (FloatingActionButton) rootView.findViewById(R.id.fab_5);
        fab_5.setVisibility(View.INVISIBLE);

        FloatingActionButton fab_6 = (FloatingActionButton) rootView.findViewById(R.id.fab_6);
        fab_6.setVisibility(View.INVISIBLE);

        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                handleSendText(intent); // Handle single image being sent
            } else if (type.startsWith("application/pdf")) {
                handleSendPDF(intent); // Handle single image being sent
            }
        }

        return rootView;
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            edit.setText(sharedText);
        }
    }

    private void handleSendPDF(Intent intent) {
        Uri pdfUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        String FilePath = pdfUri.getPath();
        String FileTitle = FilePath.substring(FilePath.lastIndexOf("/")+1);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPref.edit().putString("pathPDF", FilePath).apply();
        sharedPref.edit().putString("title", FileTitle).apply();
        setTextField();
    }

    private void setTextField() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        title = sharedPref.getString("title", null);
        folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
        String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                folder + title + ".pdf");

        File pdfFile = new File(path);
        String textRotate;

        if (sharedPref.getBoolean ("rotate", false)) {
            textRotate = getString(R.string.app_portrait);
        } else {
            textRotate = getString(R.string.app_landscape);
        }

        String text = title + " | " + textRotate;
        String text2 = getString(R.string.toast_noPDF) + " | " + textRotate;

        if (pdfFile.exists()) {
            textTitle.setText(text);
        } else {
            textTitle.setText(text2);
        }
    }

    private void createPDF() {

        // Output file
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        title = sharedPref.getString("title", null);
        folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
        String outputPath = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                folder + title + ".pdf");

        // Run conversion
        final boolean result = convertToPdf(outputPath);

        // Notify the UI
        if (result) {
            Snackbar snackbar = Snackbar
                    .make(edit, getString(R.string.toast_successfully), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.toast_open), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                            title = sharedPref.getString("title", null);
                            String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                                    folder + title + ".pdf");

                            File file = new File(path);
                            Helper.openFile(getActivity(), file, "application/pdf", edit);
                        }
                    });
            snackbar.show();
        } else Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private boolean convertToPdf(String outputPdfPath) {
        try {

            String paragraph = edit.getText().toString().trim();

            // Create output file if needed
            File outputFile = new File(outputPdfPath);
            if (!outputFile.exists()) outputFile.createNewFile();

            Document document;
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPref.getBoolean ("rotate", false)) {
                document = new Document(PageSize.A4);
            } else {
                document = new Document(PageSize.A4.rotate());
            }

            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();
            document.add (new Paragraph(paragraph));

            document.close();

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_help:

                final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.create_text)
                        .setMessage(Helper.textSpannable(getString(R.string.dialog_createText)))
                        .setPositiveButton(getString(R.string.toast_yes), null);
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
