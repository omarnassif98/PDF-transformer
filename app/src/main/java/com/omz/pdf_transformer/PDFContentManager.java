package com.omz.pdf_transformer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.IOException;
import java.io.InputStream;


public class PDFContentManager implements Runnable {
    public static  PDFContentManager singleton;
    Context context;
    int pagenum, x = 0;
    TextView pageDisplay = null;
    PDDocument pdfDoc;
    PDFTextStripper pdfTextStripper;
    public PDFContentManager(Context context) throws IOException {
        //Only pass in application context, no activity contexts please
        this.context = context;
        pdfTextStripper = new PDFTextStripper();
        singleton = this;
    }

    @Override
    public void run() {
        PDFBoxResourceLoader.init(context);
        x++;
    }

    public void InitializeView(final TextView pageDisplay, Uri PDFUri, ContentResolver androidContentResolver) throws IOException {

        InputStream resolvedPDFMedia = androidContentResolver.openInputStream(PDFUri);
        pdfDoc = PDDocument.load(resolvedPDFMedia);

        resolvedPDFMedia.close();

        this.pageDisplay = pageDisplay;

        pagenum = 0;
        pdfTextStripper.setStartPage(pagenum);
        pdfTextStripper.setEndPage(pagenum+1);
        final String scrapedText = pdfTextStripper.getText(pdfDoc);
        Log.d("SCRAPE", scrapedText);
        pdfDoc.close();
        /*
        I just need a better way to communicate with the UI thread
        pageDisplay.post(new Runnable() {
            @Override
            public void run() {
                pageDisplay.setText(scrapedText);
            }
        });
        */
    }

    public void Check(){
        Log.d("Check run", "Check " + x);
    }
}