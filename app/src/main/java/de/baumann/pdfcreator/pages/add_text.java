package de.baumann.pdfcreator.pages;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import org.vudroid.core.DecodeServiceBase;
import org.vudroid.core.codec.CodecPage;
import org.vudroid.pdfdroid.codec.PdfContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import de.baumann.pdfcreator.R;
import de.baumann.pdfcreator.helper.Helper;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class add_text extends Fragment {

    private EditText edit;
    private TextView textTitle;
    private String title;
    private String folder;
    private String pages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_text, container, false);

        setHasOptionsMenu(true);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_plus_white_48dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String paragraph = edit.getText().toString().trim();

                if (paragraph.isEmpty()) {
                    Snackbar.make(edit, getString(R.string.toast_noText), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    title = sharedPref.getString("title", null);
                    folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                    String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                            folder + title + ".pdf");

                    File pdfFile = new File(path);

                    if (pdfFile.exists()) {
                        title = sharedPref.getString("title", null);

                        backup();
                        createPDF();
                        mergePDF();
                        success();
                        deleteTemp();
                        deleteTemp2();
                        edit.setText("");
                    } else {
                        Snackbar.make(edit, getString(R.string.toast_noPDF), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
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
        fab_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                title = sharedPref.getString("title", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                        folder + title + ".pdf");

                File pdfFile = new File(path);

                if (pdfFile.exists()) {
                    backup();
                    pages = sharedPref.getString("deletePages", null);

                    LinearLayout layout = new LinearLayout(getActivity());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setGravity(Gravity.CENTER_HORIZONTAL);
                    final EditText input = new EditText(getActivity());
                    input.setSingleLine(true);
                    layout.setPadding(25, 0, 50, 0);
                    input.setHint("1-5,7,15-20");
                    input.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    layout.addView(input);

                    final AlertDialog d = new AlertDialog.Builder(getActivity())
                            .setView(layout)
                            .setTitle(R.string.add_text_delete)
                            .setMessage(R.string.add_text_delete_hint)
                            .setCancelable(true)
                            .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    pages = input.getText().toString().trim();
                                    sharedPref.edit()
                                            .putString("deletePages", pages)
                                            .apply();
                                    deletePage();
                                    deleteTemp();
                                }
                            })
                            .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            })
                            .create();

                    d.show();
                } else {
                    Snackbar.make(edit, getString(R.string.toast_noPDF), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        FloatingActionButton fab_2 = (FloatingActionButton) rootView.findViewById(R.id.fab_2);
        fab_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                title = sharedPref.getString("title", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                        folder + title + ".pdf");

                File pdfFile = new File(path);

                if (pdfFile.exists()) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent, 2);
                } else {
                    Snackbar.make(edit, getString(R.string.toast_noPDF), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        FloatingActionButton fab_3 = (FloatingActionButton) rootView.findViewById(R.id.fab_3);
        fab_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                title = sharedPref.getString("title", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                        folder + title + ".pdf");

                File pdfFile = new File(path);

                if (pdfFile.exists()) {
                    backup();
                    encryptPDF();
                    deleteTemp();
                } else {
                    Snackbar.make(edit, getString(R.string.toast_noPDF), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        fab_3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                title = sharedPref.getString("title", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                        folder + title + ".pdf");

                File pdfFile = new File(path);

                if (pdfFile.exists()) {
                    LinearLayout layout = new LinearLayout(getActivity());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setGravity(Gravity.CENTER_HORIZONTAL);
                    final EditText input = new EditText(getActivity());
                    input.setSingleLine(true);
                    layout.setPadding(25, 0, 50, 0);
                    layout.addView(input);

                    final AlertDialog d = new AlertDialog.Builder(getActivity())
                            .setView(layout)
                            .setTitle(R.string.add_text_PW)
                            .setMessage(R.string.add_text_PW_hint)
                            .setCancelable(true)
                            .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String pw = input.getText().toString().trim();
                                    sharedPref.edit()
                                            .putString("pwUSER", pw)
                                            .apply();
                                }
                            })
                            .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            })
                            .create();

                    d.show();
                } else {
                    Snackbar.make(edit, getString(R.string.toast_noPDF), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                return false;
            }
        });

        FloatingActionButton fab_4 = (FloatingActionButton) rootView.findViewById(R.id.fab_4);
        fab_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                title = sharedPref.getString("title", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                        folder + title + ".pdf");

                File pdfFile = new File(path);

                if (pdfFile.exists()) {
                    backup();
                    addMeta();
                    deleteTemp2();
                    success();
                } else {
                    Snackbar.make(edit, getString(R.string.toast_noPDF), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        fab_4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                title = sharedPref.getString("title", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                        folder + title + ".pdf");

                File pdfFile = new File(path);

                if (pdfFile.exists()) {

                    String metaAuthor = sharedPref.getString("metaAuthor", "");
                    String metaCreator = sharedPref.getString("metaCreator", "");
                    String metaSubject = sharedPref.getString("metaSubject", "");
                    String metaKeywords = sharedPref.getString("metaKeywords", "");

                    LinearLayout layout = new LinearLayout(getActivity());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setGravity(Gravity.CENTER_HORIZONTAL);
                    layout.setPadding(50, 0, 50, 0);

                    final TextView author = new TextView(getContext());
                    author.setText(R.string.pref_metaAuthor);
                    author.setPadding(5,50,0,0);
                    layout.addView(author);

                    final EditText authorEdit = new EditText(getContext());
                    authorEdit.setText(metaAuthor);
                    layout.addView(authorEdit);

                    final TextView creator = new TextView(getContext());
                    creator.setText(R.string.pref_metaCreator);
                    creator.setPadding(5,25,0,0);
                    layout.addView(creator);

                    final EditText creatorEdit = new EditText(getContext());
                    creatorEdit.setText(metaCreator);
                    layout.addView(creatorEdit);

                    final TextView subject = new TextView(getContext());
                    subject.setText(R.string.pref_metaSubject);
                    subject.setPadding(5,25,0,0);
                    layout.addView(subject);

                    final EditText subjectEdit = new EditText(getContext());
                    subjectEdit.setText(metaSubject);
                    layout.addView(subjectEdit);

                    final TextView keywords = new TextView(getContext());
                    keywords.setText(R.string.pref_metaKeywords);
                    keywords.setPadding(5,25,0,0);
                    layout.addView(keywords);

                    final EditText keywordsEdit = new EditText(getContext());
                    keywordsEdit.setText(metaKeywords);
                    layout.addView(keywordsEdit);

                    final AlertDialog d = new AlertDialog.Builder(getActivity())
                            .setView(layout)
                            .setCancelable(true)
                            .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String au = authorEdit.getText().toString().trim();
                                    String cr = creatorEdit.getText().toString().trim();
                                    String su = subjectEdit.getText().toString().trim();
                                    String kw = keywordsEdit.getText().toString().trim();
                                    sharedPref.edit()
                                            .putString("metaAuthor", au)
                                            .putString("metaCreator", cr)
                                            .putString("metaSubject", su)
                                            .putString("metaKeywords", kw)
                                            .apply();
                                }
                            })
                            .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            })
                            .create();

                    d.show();
                } else {
                    Snackbar.make(edit, getString(R.string.toast_noPDF), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                return false;
            }
        });

        FloatingActionButton fab_5 = (FloatingActionButton) rootView.findViewById(R.id.fab_5);
        fab_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                title = sharedPref.getString("title", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                        folder + title + ".pdf");

                File pdfFile = new File(path);

                if (pdfFile.exists()) {
                    backup();
                    pdtToImg();
                } else {
                    Snackbar.make(edit, getString(R.string.toast_noPDF), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        FloatingActionButton fab_6 = (FloatingActionButton) rootView.findViewById(R.id.fab_6);
        fab_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, 1);
            }
        });

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {

                String FilePath = data.getData().getPath();
                String FileTitle = FilePath.substring(FilePath.lastIndexOf("/")+1);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                sharedPref.edit().putString("pathPDF", FilePath).apply();
                sharedPref.edit().putString("title", FileTitle).apply();
                setTextField();

            }

            if (requestCode == 2) {

                backup();

                String FilePath = data.getData().getPath();
                String FileTitle = FilePath.substring(FilePath.lastIndexOf("/")+1);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                sharedPref.edit().putString("pathPDF2", FilePath).apply();
                sharedPref.edit().putString("title2", FileTitle).apply();

                // Load existing PDF
                title = sharedPref.getString("title2", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                        folder + title + ".pdf");

                String path2 = sharedPref.getString("pathPDF2", null);

                // Resulting pdf
                String path3 = Environment.getExternalStorageDirectory() +  "/" + "1234567.pdf";

                try {
                    String[] files = { path, path2 };
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
                }
                catch (Exception i)
                {
                    Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                deleteTemp2();
                success();
            }
        }
    }

    private void pdtToImg () {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        title = sharedPref.getString("title", null);
        folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
        String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                folder + title + ".pdf");

        File pdfFile = new File(path);
        String name = pdfFile.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        String FileTitleWithOutExt = name.replaceFirst("[.][^.]+$", "");
        String folderOut = folder + FileTitleWithOutExt + "/";

        DecodeServiceBase decodeService = new DecodeServiceBase(new PdfContext());
        decodeService.setContentResolver(getActivity().getContentResolver());

        // a bit long running
        decodeService.open(Uri.fromFile(pdfFile));

        int pageCount = decodeService.getPageCount();
        for (int i = 0; i < pageCount; i++) {
            CodecPage page = decodeService.getPage(i);
            RectF rectF = new RectF(0, 0, 1, 1);

            // do a fit center to A4 Size image 2480x3508
            int with = (page.getWidth()) * 2;
            int height = (page.getHeight()) * 2;

            // Long running
            Bitmap bitmap = page.renderBitmap(with, height, rectF);

            try {
                new File(Environment.getExternalStorageDirectory() + folderOut ).mkdirs();
                File outputFile = new File(Environment.getExternalStorageDirectory() + folderOut, FileTitleWithOutExt + "_" + String.format(Locale.GERMAN, "%03d", i + 1) + ".jpg");
                FileOutputStream outputStream = new FileOutputStream(outputFile);

                // a bit long running
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                outputStream.close();
            } catch (IOException e) {
                Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }

        Snackbar snackbar = Snackbar
                .make(edit, getString(R.string.toast_wait), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.toast_yes), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Helper.openFilePicker(getActivity(), edit);
                    }
                });
        snackbar.show();
    }

    private void createPDF() {
        // Output file
        String outputPath = Environment.getExternalStorageDirectory() +  "/" + "123456.pdf";

        // Run conversion
        final boolean result = convertToPdf(outputPath);

        // Notify the UI
        if (result) {
            success();
        } else Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void mergePDF() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Load existing PDF
        title = sharedPref.getString("title2", null);
        folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
        String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                folder + title + ".pdf");

        String path2 = Environment.getExternalStorageDirectory() +  "/" + "123456.pdf";

        // Resulting pdf
        String path3 = Environment.getExternalStorageDirectory() +  "/" + "1234567.pdf";

        try {
            String[] files = { path, path2 };
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
        }
        catch (Exception i)
        {
            Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        deleteTemp();
    }

    private void deletePage() {
        try {

            // Load existing PDF
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            title = sharedPref.getString("title", null);
            folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
            pages = sharedPref.getString("deletePages", null);
            String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                    folder + title + ".pdf");
            PdfReader reader = new PdfReader(path);
            reader.selectPages(pages);

            PdfStamper pdfStamper = new PdfStamper(reader, new FileOutputStream(Environment.getExternalStorageDirectory() +  "/" + "123456.pdf"));
            pdfStamper.close();
            success();
        }
        catch (Exception e)
        {
            Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    private void encryptPDF() {
        try {

            // Load existing PDF
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            title = sharedPref.getString("title", null);
            folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

            String user = sharedPref.getString("pwUSER", "USER");
            String owner = sharedPref.getString("pwOWNER", "OWNER");

            String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                    folder + title + ".pdf");

            PdfReader reader = new PdfReader(path);

            PdfStamper pdfStamper = new PdfStamper(reader, new FileOutputStream(Environment.getExternalStorageDirectory() +  "/" + "123456.pdf"));
            pdfStamper.setEncryption(user.getBytes(), owner.getBytes(),
                    ~(PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING), PdfWriter.STANDARD_ENCRYPTION_128);
            pdfStamper.close();
            reader.close();
            success();

        }
        catch (Exception e)
        {
            Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    private void addMeta() {
        try {

            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String metaAuthor = sharedPref.getString("metaAuthor", "");
                String metaCreator = sharedPref.getString("metaCreator", "");
                String metaSubject = sharedPref.getString("metaSubject", "");
                String metaKeywords = sharedPref.getString("metaKeywords", "");

                // Load existing PDF
                title = sharedPref.getString("title2", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                        folder + title + ".pdf");

                // Resulting pdf
                String path3 = Environment.getExternalStorageDirectory() +  "/" + "1234567.pdf";

                try {
                    Document document = new Document();
                    PdfCopy copy = new PdfCopy(document, new FileOutputStream(path3));
                    document.open();
                    PdfReader ReadInputPDF;
                    int number_of_pages;
                    ReadInputPDF = new PdfReader(path);
                    number_of_pages = ReadInputPDF.getNumberOfPages();
                    for (int page = 0; page < number_of_pages; ) {
                        copy.addPage(copy.getImportedPage(ReadInputPDF, ++page));
                    }
                    document.addTitle(title);
                    document.addAuthor(metaAuthor);
                    document.addSubject(metaSubject);
                    document.addKeywords(metaKeywords);
                    document.addCreator(metaCreator);
                    document.close();
                }
                catch (Exception i)
                {
                    Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                deleteTemp2();
                success();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        catch (Exception e)
        {
            Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

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

    private void deleteTemp(){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        title = sharedPref.getString("title", null);

        InputStream in;
        OutputStream out;

        try {

            title = sharedPref.getString("title", null);
            folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
            String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                    folder + title + ".pdf");

            in = new FileInputStream(Environment.getExternalStorageDirectory() +  "/" + "123456.pdf");
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

        File pdfFile = new File(Environment.getExternalStorageDirectory() +  "/" + "123456.pdf");
        if(pdfFile.exists()){
            pdfFile.delete();
        }
    }

    private void deleteTemp2(){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        title = sharedPref.getString("title", null);

        InputStream in;
        OutputStream out;

        try {

            title = sharedPref.getString("title", null);
            folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
            String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                    folder + title + ".pdf");

            in = new FileInputStream(Environment.getExternalStorageDirectory() +  "/" + "1234567.pdf");
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

        File pdfFile = new File(Environment.getExternalStorageDirectory() +  "/" + "1234567.pdf");
        if(pdfFile.exists()){
            pdfFile.delete();
        }
    }

    private void success(){

        Snackbar snackbar = Snackbar
                .make(edit, getString(R.string.toast_successfully), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.toast_open), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        title = sharedPref.getString("title", null);
                        folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                        String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                                folder + title + ".pdf");

                        File file = new File(path);
                        Helper.openFile(getActivity(), file, "application/pdf", edit);
                    }
                });
        snackbar.show();
    }

    private void backup(){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (sharedPref.getBoolean ("backup", false)){

            InputStream in;
            OutputStream out;

            try {
                title = sharedPref.getString("title", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                        folder + title + ".pdf");

                in = new FileInputStream(path);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_help:

                final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.add_text)
                        .setMessage(Helper.textSpannable(getString(R.string.dialog_addText)))
                        .setPositiveButton(getString(R.string.toast_yes), null);
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
