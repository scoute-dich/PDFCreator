package de.baumann.pdfcreator.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.artifex.mupdfdemo.MuPDFActivity;
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
import de.baumann.pdfcreator.helper.helper_main;
import de.baumann.pdfcreator.helper.helper_pdf;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class create_text extends Fragment {

    private String title;
    private String folder;
    private EditText edit;
    private SharedPreferences sharedPref;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_text, container, false);

        edit = (EditText) rootView.findViewById(R.id.editText);
        helper_pdf.pdf_textField(getActivity(), rootView);
        setHasOptionsMenu(true);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.user_settings, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String paragraph = edit.getText().toString().trim();

                if (paragraph.isEmpty()) {
                    Snackbar.make(edit, getString(R.string.toast_noText), Snackbar.LENGTH_LONG).show();
                } else {
                    folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                    View dialogView = View.inflate(getActivity(), R.layout.dialog_title, null);

                    final EditText editTitle = (EditText) dialogView.findViewById(R.id.title);

                    builder.setView(dialogView);
                    builder.setTitle(R.string.app_title);
                    builder.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {

                            title = editTitle.getText().toString().trim();
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
                            helper_pdf.pdf_textField(getActivity(), rootView);
                        }
                    });
                    builder.setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
                    builder.setNeutralButton(R.string.app_title_date, null);

                    final android.support.v7.app.AlertDialog dialog2 = builder.create();
                    // Display the custom alert dialog on interface
                    dialog2.setOnShowListener(new DialogInterface.OnShowListener() {

                        @Override
                        public void onShow(DialogInterface dialog) {

                            Button b = dialog2.getButton(AlertDialog.BUTTON_NEUTRAL);
                            b.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    Date date = new Date();
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    String dateNow = format.format(date);
                                    editTitle.append(String.valueOf(dateNow));

                                }
                            });
                        }
                    });
                    dialog2.show();

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            helper_main.showKeyboard(getActivity(), editTitle);
                        }
                    }, 200);
                }
            }
        });

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
                handleSendText(intent); // Handle single image2 being sent
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

    @Override
    public void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        helper_pdf.pdf_textField(getActivity(), rootView);
        helper_pdf.toolbar(getActivity());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            helper_pdf.toolbar(getActivity());
            helper_pdf.pdf_textField(getActivity(), rootView);
        }
    }

    private void createPDF() {

        // Output file
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
                            File file = new File(helper_pdf.actualPath(getActivity()));

                            Intent intent = new Intent(getActivity(), MuPDFActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setData(Uri.fromFile(file));
                            getActivity().startActivity(intent);
                        }
                    });
            snackbar.show();
        } else Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG).show();
    }

    private boolean convertToPdf(String outputPdfPath) {
        try {

            String paragraph = edit.getText().toString().trim();

            // Create output file if needed
            File outputFile = new File(outputPdfPath);
            if (!outputFile.exists()) outputFile.createNewFile();

            Document document;
            if (sharedPref.getString ("rotateString", "portrait").equals("portrait")) {
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

        String path = helper_pdf.actualPath(getActivity());
        File pdfFile = new File(helper_pdf.actualPath(getActivity()));

        switch (item.getItemId()) {
            case R.id.action_help:

                final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.create_text)
                        .setMessage(helper_main.textSpannable(getString(R.string.dialog_createText)))
                        .setPositiveButton(getString(R.string.toast_yes), null);
                dialog.show();
                return true;

            case R.id.action_share:

                if (pdfFile.exists()) {

                    String FileTitle = path.substring(path.lastIndexOf("/")+1);
                    String text = getString(R.string.action_share_Text);

                    Uri myUri= Uri.fromFile(pdfFile);
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("application/pdf");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, myUri);
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, FileTitle);
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, text + " " + FileTitle);
                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.action_share_with)));
                } else {
                    Snackbar.make(edit, R.string.toast_noPDF, Snackbar.LENGTH_LONG).show();
                }
                return true;

            case R.id.action_open:

                if (pdfFile.exists()) {
                    Intent intent = new Intent(getActivity(), MuPDFActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.fromFile(pdfFile));
                    getActivity().startActivity(intent);
                } else {
                    Snackbar.make(edit, R.string.toast_noPDF, Snackbar.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
