package de.baumann.pdfcreator.pages;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.baumann.pdfcreator.filechooser.ChooserDialog;
import de.baumann.pdfcreator.helper.ActivityEditor;
import de.baumann.pdfcreator.R;
import de.baumann.pdfcreator.helper.helper_main;
import de.baumann.pdfcreator.helper.helper_pdf;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class add_image extends Fragment {

    @SuppressWarnings("unused")
    private String title;
    private String folder;

    private ImageView img;
    private int imgquality_int;
    private View rootView;

    private SharedPreferences sharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_image, container, false);

        helper_pdf.pdf_textField(getActivity(), rootView);
        setHasOptionsMenu(true);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.user_settings, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final String imgQuality = sharedPref.getString("imageQuality", "80");
        imgquality_int = Integer.parseInt(imgQuality);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_plus_white_48dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");

                if(imgFile.exists()){
                    
                    File pdfFile = new File(helper_pdf.actualPath(getActivity()));
                    final String fileExtension = helper_pdf.actualPath(getActivity()).substring(helper_pdf.actualPath(getActivity()).lastIndexOf("."));

                    if (pdfFile.exists() && fileExtension.equals(".pdf")) {

                        title = sharedPref.getString("title", null);

                        helper_pdf.pdf_backup(getActivity());
                        createPDF();
                        helper_pdf.pdf_mergePDF(getActivity(), img);
                        helper_pdf.pdf_success(getActivity(), img);
                        helper_pdf.pdf_deleteTemp_1(getActivity());
                        helper_pdf.pdf_deleteTemp_2(getActivity());
                        img.setImageResource(R.drawable.image);

                    } else {
                        Snackbar.make(img, getString(R.string.toast_noPDF), Snackbar.LENGTH_LONG).show();
                    }

                } else {
                    Snackbar.make(img, getString(R.string.toast_noImage), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        FloatingActionButton fab_1 = (FloatingActionButton) rootView.findViewById(R.id.fab_1);
        fab_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage_1();
            }
        });

        FloatingActionButton fab_2 = (FloatingActionButton) rootView.findViewById(R.id.fab_2);
        fab_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
                if(imgFile.exists()){
                    sharedPref.edit().putInt("startFragment", 2).putBoolean("appStarted", false).apply();

                    Intent intent = new Intent(getActivity(), com.theartofdev.edmodo.cropper.sample.MainActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0, 0);
                } else {
                    Snackbar.make(img, getString(R.string.toast_noImage), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        FloatingActionButton fab_3 = (FloatingActionButton) rootView.findViewById(R.id.fab_3);
        fab_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
                if(imgFile.exists()){
                    sharedPref.edit().putInt("startFragment", 2).putBoolean("appStarted", false).apply();
                    Intent intent = new Intent(getActivity(), ActivityEditor.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0, 0);
                } else {
                    Snackbar.make(img, getString(R.string.toast_noImage), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        FloatingActionButton fab_4 = (FloatingActionButton) rootView.findViewById(R.id.fab_4);
        fab_4.setOnClickListener(new View.OnClickListener() {
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

        img=(ImageView)rootView.findViewById(R.id.imageView);
        File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            img.setImageBitmap(myBitmap);
        }

        // Get intent, action and MIME type
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        }

        return rootView;
    }

    private void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
            img.setImageURI(imageUri);

            BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");

            // Encode the file as a JPEG image.
            FileOutputStream outStream;
            try {

                outStream = new FileOutputStream(imgFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, imgquality_int, outStream);
                outStream.flush();
                outStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createPDF() {
        // Input file
        String inputPath = Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg";

        // Output file
        String outputPath = Environment.getExternalStorageDirectory() +  "/" + "123456.pdf";

        // Run conversion
        final boolean result = convertToPdf(inputPath, outputPath);

        // Notify the UI
        if (result) {
            Snackbar snackbar = Snackbar
                    .make(img, getString(R.string.toast_successfully), Snackbar.LENGTH_LONG)
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
        } else Snackbar.make(img, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private boolean convertToPdf(String jpgFilePath, String outputPdfPath) {
        try {
            // Check if Jpg file exists or not

            File inputFile = new File(jpgFilePath);
            if (!inputFile.exists()) throw new Exception("File '" + jpgFilePath + "' doesn't exist.");

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

            Image image = Image.getInstance(jpgFilePath);
            if (sharedPref.getString ("rotateString", "portrait").equals("portrait")) {
                if (PageSize.A4.getWidth() - image.getWidth() < 0) {
                    image.scaleToFit(PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin(),
                            PageSize.A4.getHeight() - document.topMargin() - document.bottomMargin());
                } else if (PageSize.A4.getHeight() - image.getHeight() < 0) {
                    image.scaleToFit(PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin(),
                            PageSize.A4.getHeight() - document.topMargin() - document.bottomMargin());}
            } else {
                if (PageSize.A4.rotate().getWidth() - image.getWidth() < 0) {
                    image.scaleToFit(PageSize.A4.rotate().getWidth() - document.leftMargin() - document.rightMargin(),
                            PageSize.A4.rotate().getHeight() - document.topMargin() - document.bottomMargin());
                } else if (PageSize.A4.rotate().getHeight() - image.getHeight() < 0) {
                    image.scaleToFit(PageSize.A4.rotate().getWidth() - document.leftMargin() - document.rightMargin(),
                            PageSize.A4.rotate().getHeight() - document.topMargin() - document.bottomMargin());
                }
            }
            image.setAlignment(Element.ALIGN_CENTER);

            document.add(image);
            document.close();

            File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
            if(imgFile.exists()){
                imgFile.delete();
            }

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    private void selectImage_1() {

        final CharSequence[] options = {
                getString(R.string.goal_camera),
                getString(R.string.goal_gallery),
                getString(R.string.choose_chooser)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals(getString(R.string.goal_camera))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri contentUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", f);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                    } else {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    }

                    try {
                        startActivityForResult(intent, 1);
                    } catch (ActivityNotFoundException e) {
                        Snackbar.make(img, R.string.toast_install_app, Snackbar.LENGTH_LONG).show();
                    }

                } else if (options[item].equals(getString(R.string.goal_gallery))) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                } else if (options[item].equals(getString(R.string.choose_chooser))) {
                    folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");

                    new ChooserDialog().with(getActivity())
                            .withFilter(false, false, "jpg", "jpeg", "png", "pdf")
                            .withResources()
                            .withStartFile(Environment.getExternalStorageDirectory() + folder)
                            .withChosenListener(new ChooserDialog.Result() {
                                @Override
                                public void onChoosePath(String path, final File pathFile) {

                                    img.setImageURI(Uri.fromFile(pathFile));

                                    BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
                                    Bitmap bitmap = drawable.getBitmap();

                                    File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");

                                    // Encode the file as a JPEG image.
                                    FileOutputStream outStream;
                                    try {

                                        outStream = new FileOutputStream(imgFile);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, imgquality_int, outStream);
                                        outStream.flush();
                                        outStream.close();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .build()
                            .show();
                }
            }
        });
        builder.setPositiveButton(getString(R.string.dialog_cancel), null);
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {

                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
                if(imgFile.exists()){

                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    img.setImageBitmap(myBitmap);

                    BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();

                    // Encode the file as a JPEG image.
                    FileOutputStream outStream;
                    try {

                        outStream = new FileOutputStream(imgFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, imgquality_int, outStream);
                        outStream.flush();
                        outStream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    img.setImageBitmap(bitmap);
                }

            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                img.setImageURI(selectedImage);

                BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");

                // Encode the file as a JPEG image.
                FileOutputStream outStream;
                try {

                    outStream = new FileOutputStream(imgFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, imgquality_int, outStream);
                    outStream.flush();
                    outStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            int PIC_CROP = 1;
            if (requestCode == PIC_CROP) {
                if (data != null) {
                    // get the returned data
                    Bundle extras = data.getExtras();
                    // get the cropped bitmap
                    Bitmap selectedBitmap = extras.getParcelable("data");

                    img.setImageBitmap(selectedBitmap);

                    BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();

                    File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");

                    // Encode the file as a PNG image.
                    FileOutputStream outStream;
                    try {

                        outStream = new FileOutputStream(imgFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, imgquality_int, outStream);
                        outStream.flush();
                        outStream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
        helper_pdf.pdf_textField(getActivity(), rootView);
        helper_pdf.toolbar(getActivity());
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            img.setImageBitmap(myBitmap);
        } else {
            img.setImageResource(R.drawable.image);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
            helper_pdf.pdf_textField(getActivity(), rootView);
            helper_pdf.toolbar(getActivity());
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                img.setImageBitmap(myBitmap);
            } else {
                img.setImageResource(R.drawable.image);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String path = helper_pdf.actualPath(getActivity());
        File pdfFile = new File(helper_pdf.actualPath(getActivity()));

        switch (item.getItemId()) {

            case R.id.action_help:

                final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.add_image)
                        .setMessage(helper_main.textSpannable(getString(R.string.dialog_addImage)))
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
                    Snackbar.make(img, R.string.toast_noPDF, Snackbar.LENGTH_LONG).show();
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
                    Snackbar.make(img, R.string.toast_noPDF, Snackbar.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
