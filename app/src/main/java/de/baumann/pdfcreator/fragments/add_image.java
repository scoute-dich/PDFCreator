package de.baumann.pdfcreator.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.mvc.imagepicker.ImagePicker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.baumann.pdfcreator.helper.Activity_Editor;
import de.baumann.pdfcreator.R;
import de.baumann.pdfcreator.helper.helper_main;
import de.baumann.pdfcreator.helper.helper_pdf;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class add_image extends Fragment {

    @SuppressWarnings("unused")
    private String title;

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

                    if (pdfFile.exists()) {

                        title = sharedPref.getString("title", null);

                        helper_pdf.pdf_backup(getActivity());
                        createPDF();
                        helper_pdf.pdf_mergePDF(getActivity(), img);
                        helper_pdf.pdf_success(getActivity(), img);
                        helper_pdf.pdf_deleteTemp_1(getActivity());
                        helper_pdf.pdf_deleteTemp_2(getActivity());
                        img.setImageResource(R.drawable.image);
                        helper_pdf.pdf_textField(getActivity(), rootView);
                        helper_pdf.toolbar(getActivity());

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
                onPickImage();
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
                    Intent intent = new Intent(getActivity(), Activity_Editor.class);
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
                sharedPref.edit().putInt("fileManager", 2).apply();
                sharedPref.edit().putInt("startFragment", 3).apply();
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                viewPager.setCurrentItem(0);
            }
        });

        img=(ImageView)rootView.findViewById(R.id.imageView);
        File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
        if(imgFile.exists()){
            Glide.with(getActivity())
                    .load(imgFile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .fitCenter()
                    .into(img); //imageView to set thumbnail to
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
                            helper_main.openFile(getActivity(), file, "application/pdf", img);
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        InputStream inputStream = ImagePicker.getInputStreamFromResult(getActivity(), requestCode, resultCode, data);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);

        if (bitmap != null) {
            try {
                //create a file to write bitmap data
                File f = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
                f.createNewFile();

                OutputStream outStream = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.JPEG, imgquality_int, outStream);
                outStream.flush();
                outStream.close();

                Glide.with(getActivity())
                        .load(f)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .fitCenter()
                        .into(img); //imageView to set thumbnail to

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onPickImage() {
        // Click on image button
        ImagePicker.pickImage(this, "Select your image:");
    }

    @Override
    public void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");
        helper_pdf.pdf_textField(getActivity(), rootView);
        helper_pdf.toolbar(getActivity());
        if(imgFile.exists()){
            Glide.with(getActivity())
                    .load(imgFile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .fitCenter()
                    .into(img); //imageView to set thumbnail to
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
                Glide.with(getActivity())
                        .load(imgFile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .fitCenter()
                        .into(img); //imageView to set thumbnail to
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
                    helper_main.openFile(getActivity(), pdfFile, "application/pdf", img);
                } else {
                    Snackbar.make(img, R.string.toast_noPDF, Snackbar.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
