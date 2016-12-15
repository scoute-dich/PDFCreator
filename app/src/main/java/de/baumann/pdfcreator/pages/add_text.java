package de.baumann.pdfcreator.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import de.baumann.pdfcreator.R;
import de.baumann.pdfcreator.filechooser.ChooserDialog;
import de.baumann.pdfcreator.helper.helper_dialogs;
import de.baumann.pdfcreator.helper.helper_main;
import de.baumann.pdfcreator.helper.helper_pdf;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class add_text extends Fragment {

    private EditText edit;
    private String title;
    private String folder;
    private String pages;
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
        fab.setImageResource(R.drawable.ic_plus_white_48dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String paragraph = edit.getText().toString().trim();

                if (paragraph.isEmpty()) {
                    Snackbar.make(edit, getString(R.string.toast_noText), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {

                    File pdfFile = new File(helper_pdf.actualPath(getActivity()));

                    if (pdfFile.exists()) {
                        title = sharedPref.getString("title", null);

                        helper_pdf.pdf_backup(getActivity());
                        createPDF();
                        mergePDF();
                        success();
                        helper_pdf.pdf_deleteTemp_1(getActivity());
                        helper_pdf.pdf_deleteTemp_2(getActivity());
                        edit.setText("");
                    } else {
                        Snackbar.make(edit, getString(R.string.toast_noPDF), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            }
        });

        FloatingActionButton fab_1 = (FloatingActionButton) rootView.findViewById(R.id.fab_1);
        fab_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File pdfFile = new File(helper_pdf.actualPath(getActivity()));

                if (pdfFile.exists()) {
                    helper_pdf.pdf_backup(getActivity());
                    pages = sharedPref.getString("deletePages", null);

                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                    View dialogView = View.inflate(getActivity(), R.layout.dialog_delete, null);

                    final EditText pagesDelete = (EditText) dialogView.findViewById(R.id.delete);

                    builder.setView(dialogView);
                    builder.setTitle(R.string.add_text_delete);
                    builder.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {

                            pages = pagesDelete.getText().toString().trim();
                            sharedPref.edit()
                                    .putString("deletePages", pages)
                                    .apply();
                            deletePage();
                            helper_pdf.pdf_deleteTemp_1(getActivity());
                        }
                    });
                    builder.setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });

                    final android.support.v7.app.AlertDialog dialog2 = builder.create();
                    // Display the custom alert dialog on interface

                    dialog2.show();

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            helper_main.showKeyboard(getActivity(), pagesDelete);
                        }
                    }, 200);

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

                File pdfFile = new File(helper_pdf.actualPath(getActivity()));

                if (pdfFile.exists()) {

                    folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

                    new ChooserDialog().with(getActivity())
                            .withFilter(false, false, "pdf")
                            .withResources()
                            .withStartFile(Environment.getExternalStorageDirectory() + folder)
                            .withChosenListener(new ChooserDialog.Result() {
                                @Override
                                public void onChoosePath(String path, final File pathFile) {

                                    helper_pdf.pdf_backup(getActivity());

                                    // Load existing PDF
                                    title = sharedPref.getString("title2", null);
                                    folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

                                    String existingPDF = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                                            folder + title + ".pdf");

                                    // Resulting pdf
                                    String path3 = Environment.getExternalStorageDirectory() +  "/" + "1234567.pdf";

                                    try {
                                        String[] files = { existingPDF, pathFile.getAbsolutePath() };
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
                                    helper_pdf.pdf_deleteTemp_2(getActivity());
                                    success();
                                }
                            })
                            .build()
                            .show();

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

                File pdfFile = new File(helper_pdf.actualPath(getActivity()));

                if (pdfFile.exists()) {
                    helper_pdf.pdf_backup(getActivity());
                    encryptPDF();
                    helper_pdf.pdf_deleteTemp_1(getActivity());
                } else {
                    Snackbar.make(edit, getString(R.string.toast_noPDF), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        fab_3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                File pdfFile = new File(helper_pdf.actualPath(getActivity()));

                if (pdfFile.exists()) {
                    helper_dialogs.dialog_encryption(getActivity());
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

                File pdfFile = new File(helper_pdf.actualPath(getActivity()));

                if (pdfFile.exists()) {
                    helper_pdf.pdf_backup(getActivity());
                    addMeta();
                    helper_pdf.pdf_deleteTemp_2(getActivity());
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

                File pdfFile = new File(helper_pdf.actualPath(getActivity()));

                if (pdfFile.exists()) {
                    helper_dialogs.dialog_metaTags(getActivity());
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

                File pdfFile = new File(helper_pdf.actualPath(getActivity()));

                if (pdfFile.exists()) {
                    Snackbar.make(edit, getString(R.string.toast_wait), Snackbar.LENGTH_INDEFINITE)
                            .setAction("Action", null).show();
                    helper_pdf.pdf_backup(getActivity());
                    title = sharedPref.getString("title", null);
                    folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

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

                        // do a fit center to A4 Size image2 2480x3508
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
                                    String folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                                    helper_main.openFilePicker(getActivity(), edit, Environment.getExternalStorageDirectory() + folder);
                                }
                            });
                    snackbar.show();
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

                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

                new ChooserDialog().with(getActivity())
                        .withFilter(false, false, "pdf")
                        .withResources()
                        .withStartFile(Environment.getExternalStorageDirectory() + folder)
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, final File pathFile) {

                                final String fileName = pathFile.getAbsolutePath().substring(pathFile.getAbsolutePath().lastIndexOf("/")+1);
                                sharedPref.edit().putString("pathPDF", pathFile.getAbsolutePath()).apply();
                                sharedPref.edit().putString("title", fileName).apply();
                                helper_pdf.pdf_textField(getActivity(), rootView);
                            }
                        })
                        .build()
                        .show();
            }
        });

        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                handleSendText(intent); // Handle single image2 being sent
            } else if (type.startsWith("application/pdf")) {
                handleSendPDF(intent); // Handle single image2 being sent
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
        sharedPref.edit().putString("pathPDF", FilePath).apply();
        sharedPref.edit().putString("title", FileTitle).apply();
        helper_pdf.pdf_textField(getActivity(), rootView);
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
        helper_pdf.pdf_deleteTemp_1(getActivity());
    }

    private void deletePage() {
        try {

            // Load existing PDF
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
                helper_pdf.pdf_deleteTemp_2(getActivity());
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

    private void success(){

        Snackbar snackbar = Snackbar
                .make(edit, getString(R.string.toast_successfully), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.toast_open), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File file = new File(helper_pdf.actualPath(getActivity()));
                        helper_main.openFile(getActivity(), file, "application/pdf", edit);
                    }
                });
        snackbar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        title = sharedPref.getString("title", null);
        folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
        String path = helper_pdf.actualPath(getActivity());

        File pdfFile = new File(helper_pdf.actualPath(getActivity()));

        switch (item.getItemId()) {
            case R.id.action_help:

                final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.add_text)
                        .setMessage(helper_main.textSpannable(getString(R.string.dialog_addText)))
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
                    helper_main.openFile(getActivity(), pdfFile, "application/pdf", edit);
                } else {
                    Snackbar.make(edit, R.string.toast_noPDF, Snackbar.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
