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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


public class PDFContentManager {
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
        PDFBoxInitializer pdfBoxInitializerWorker = new PDFBoxInitializer(context);
        new Thread(pdfBoxInitializerWorker).start();
    }

    public void ScrapePDF(final TextView pageDisplay, Uri PDFUri, ContentResolver androidContentResolver){
        this.pageDisplay = pageDisplay;
        PDFScraper pdfScraperWorker = new PDFScraper(PDFUri,androidContentResolver);
        new Thread(pdfScraperWorker).start();
    }

    public class PDFBoxInitializer implements Runnable {
        Context context;
        public PDFBoxInitializer(Context context){
            this.context = context;
        }

        @Override
        public void run() {
            PDFBoxResourceLoader.init(context);
        }
    }

    public class PDFScraper implements Runnable {

        private Uri PDFUri;
        ContentResolver androidContentResolver;
        public PDFScraper(Uri PDFUri, ContentResolver androidContentResolver){
            this.PDFUri = PDFUri;
            this.androidContentResolver = androidContentResolver;
        }
        public void run() {
            try {
                InputStream resolvedPDFMedia = androidContentResolver.openInputStream(PDFUri);
                pdfDoc = PDDocument.load(resolvedPDFMedia);
                resolvedPDFMedia.close();
                pagenum = 0;
                pdfTextStripper.setStartPage(pagenum);
                pdfTextStripper.setEndPage(pagenum + 1);
                final String scrapedText = pdfTextStripper.getText(pdfDoc);
                Log.d("SCRAPE", scrapedText);
                pdfDoc.close();
                pageDisplay.post(new Runnable() {
                    @Override
                    public void run() {
                        pageDisplay.setText(scrapedText);
                    }
                });
            } catch (FileNotFoundException e) {
                Log.e("Error", "File was not found (Runnable)");
            }catch (IOException e){
                Log.e("Error", "IO Exception (Runnable)");
            }
        }
    }

    public void Check(){
        Log.d("Check run", "Check " + PDFBoxResourceLoader.isReady());
    }
}