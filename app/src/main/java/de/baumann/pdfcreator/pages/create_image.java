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
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.baumann.pdfcreator.ActivityEditor;
import de.baumann.pdfcreator.R;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class create_image extends Fragment {

    private String title;
    private String folder;

    private ImageView img;
    private TextView textTitle;
    private int imgquality_int;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_image, container, false);

        setHasOptionsMenu(true);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String imgQuality = sharedPref.getString("imageQuality", "80");
        imgquality_int = Integer.parseInt(imgQuality);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
                if(imgFile.exists()){

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

                                    img.setImageResource(R.drawable.image);
                                    setTextField();

                                    File pdfFile = new File(Environment.getExternalStorageDirectory() +  "/" + title + ".pdf");
                                    if(pdfFile.exists()){
                                        pdfFile.delete();
                                    }
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

                } else {
                    Snackbar.make(img, getString(R.string.toast_noImage), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
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
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sharedPref.edit()
                            .putInt("startFragment", 0)
                            .putBoolean("appStarted", false)
                            .apply();

                    Intent intent = new Intent(getActivity(), com.theartofdev.edmodo.cropper.sample.MainActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0, 0);
                    getActivity().finish();
                } else {
                    Snackbar.make(img, getString(R.string.toast_noImage), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        FloatingActionButton fab_3 = (FloatingActionButton) rootView.findViewById(R.id.fab_3);
        fab_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
                if(imgFile.exists()){
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sharedPref.edit()
                            .putInt("startFragment", 0)
                            .putBoolean("appStarted", false)
                            .apply();

                    Intent intent = new Intent(getActivity(), ActivityEditor.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0, 0);
                    getActivity().finish();
                } else {
                    Snackbar.make(img, getString(R.string.toast_noImage), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        FloatingActionButton fab_4 = (FloatingActionButton) rootView.findViewById(R.id.fab_4);
        fab_4.setVisibility(View.GONE);

        img=(ImageView)rootView.findViewById(R.id.imageView);
        File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            img.setImageBitmap(myBitmap);
        }

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

        // Get intent, action and MIME type
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            } else if (type.startsWith("application/pdf")) {
                handleSendPDF(intent); // Handle single image being sent
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
        // Input file
        String inputPath = Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg";

        // Output file
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        title = sharedPref.getString("title", null);
        folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
        String outputPath = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                folder + title + ".pdf");

        // Run conversion
        final boolean result = convertToPdf(inputPath, outputPath);

        // Notify the UI
        if (result) {
            Snackbar snackbar = Snackbar
                    .make(img, getString(R.string.toast_successfully), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.toast_open), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            title = sharedPref.getString("title", null);
                            folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                            String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                                    folder + title + ".pdf");

                            File file = new File(path);
                            Intent target = new Intent(Intent.ACTION_VIEW);
                            target.setDataAndType(Uri.fromFile(file),"application/pdf");
                            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                            try {
                                startActivity(target);
                            } catch (ActivityNotFoundException e) {
                                // Instruct the user to install a PDF reader here, or something
                                Snackbar.make(img, getString(R.string.toast_install_pdf), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
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
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPref.getBoolean ("rotate", false)) {
                document = new Document(PageSize.A4);
            } else {
                document = new Document(PageSize.A4.rotate());
            }

            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();

            Image image = Image.getInstance(jpgFilePath);
            if (sharedPref.getBoolean ("rotate", false)) {
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

        final CharSequence[] options = {getString(R.string.goal_camera),getString(R.string.goal_gallery), getString(R.string.goal_cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals(getString(R.string.goal_camera))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);

                } else if (options[item].equals(getString(R.string.goal_gallery))) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                } else if (options[item].equals(getString(R.string.goal_cancel))) {
                    dialog.dismiss();
                }
            }
        });
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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_help:

                final SpannableString s = new SpannableString(Html.fromHtml(getString(R.string.dialog_createImage)));
                Linkify.addLinks(s, Linkify.WEB_URLS);

                final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.create_image)
                        .setMessage(s)
                        .setPositiveButton(getString(R.string.toast_yes), null);
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
