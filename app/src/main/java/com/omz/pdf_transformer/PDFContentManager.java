package com.omz.pdf_transformer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineHeightSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


public class PDFContentManager {
    public static  PDFContentManager singleton;
    Context context;
    int pagenum, x = 0;
    TextView pageDisplay = null;
    PDDocument pdfDoc;
    PDFTextStripper pdfTextStripper;
    ArrayList<FormatterObject> activeSpanTemplates = new ArrayList<FormatterObject>(), passiveSpanTemplates = new ArrayList<FormatterObject>();
    ArrayList<ParcelableSpan> instantiatedActiveSpans = new ArrayList<ParcelableSpan>(), instantiatedPassiveSpans = new ArrayList<ParcelableSpan>();
    HashMap<String, ArrayList> formatEndings = new HashMap<String, ArrayList>();
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

    public void UpdateTextView(final CharSequence updatedText){
        pageDisplay.post(new Runnable() {
            @Override
            public void run() {
                pageDisplay.setText(updatedText);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void LoadSpanPreferences(JSONObject savedConfig){
        //Update with actual file
        try {
            JSONObject activeTemplates = savedConfig.getJSONObject("active_span_templates");
            Iterator<String> it;
            it = activeTemplates.keys();
            while (it.hasNext()) {
                String key = it.next();
                Log.d("JSON", "LoadSpanPreferences found active key " + key);
                AddTemplate(key, activeTemplates.get(key), activeSpanTemplates,0);
            }
            Log.d("JSON", "Active span count = " + activeSpanTemplates.size());
            JSONObject passiveTemplates = savedConfig.getJSONObject("passive_span_templates");
            it = passiveTemplates.keys();
            while (it.hasNext()){
                String key = it.next();
                Log.d("JSON", "LoadSpanPreferences found passive key " + key);
                AddTemplate(key, passiveTemplates.get(key), passiveSpanTemplates,0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    void AddTemplate(String key, Object value, ArrayList<FormatterObject> list, int rule) throws JSONException {
        switch (key){
            case "text_color":
                list.add(new ColorFormatter(Color.parseColor((String)value), rule));
                Log.d("TEMPLATE", "Color added, RULE " + rule);
                break;
            case  "line_spacing":
                list.add(new LineSpaceFormatter((int)value, rule));
                break;
            case "text_size":
                list.add(new TextSizeFormatter((int)value, rule));
                break;
            case "sentence_first_word":
                for (Iterator<String> it = ((JSONObject) value).keys(); it.hasNext(); ) {
                    String k = it.next();

                    AddTemplate(k,((JSONObject)value).get(k),list, 2);
                }
                break;
            default:
                Log.d("TEMPLATE", "Defaulted, unrecognized key: " + key);
        }
    }


    public ArrayList GetFormatSpan(String key){
        return formatEndings.get(key);
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
                InfoScraper pdfExtractor = new InfoScraper(resolvedPDFMedia);
                pagenum = 0;
                pdfExtractor.setSortByPosition(true);
                String scrapedText = pdfExtractor.ScrapePage(0);
                UpdateFormatInfo(scrapedText);
                final SpannableString formattedText = new SpannableString(scrapedText);
                int paragraphBeginIdx = 0;
                //Active templates are applied "under" clickable spans
                //Clickable spans dont count as either passive or active because they don't actually apply transformations
                if (activeSpanTemplates.size() > 0 && formatEndings.containsKey("paragraphSpans")) {
                    int i = 0;
                    for (int[] paragraphBounds : (ArrayList<int[]>)formatEndings.get("paragraphSpans")) {
                        final int finalI = i;
                        ClickableSpan focusEvent = new ClickableSpan() {
                            @Override
                            public void onClick(@NonNull View widget) {
                                for (ContentFormater span : activeSpanTemplates){
                                    span.ClearTransformations(formattedText);
                                    span.ApplyTransformation(formattedText, finalI);
                                }
                                UpdateTextView(formattedText);
                            }
                            @Override
                            public void updateDrawState(TextPaint ds) {
                                ds.setUnderlineText(false);
                                ds.setColor(Color.BLACK);
                            }
                        };
                        formattedText.setSpan(focusEvent,paragraphBounds[0],paragraphBounds[1],Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        i++;
                    }
                }
                resolvedPDFMedia.close();

                pageDisplay.post(new Runnable() {
                    @Override
                    public void run() {
                        pageDisplay.setText(formattedText);
                        pageDisplay.setMovementMethod(new ContentScrollMovementMethod());
                    }
                });
            } catch (FileNotFoundException e) {
                Log.e("Error", "File was not found (Runnable)");
            } catch (IOException e) {
                Log.e("Error", "IO Exception (Runnable)");
            }
        }

        void UpdateFormatInfo(String extractedText){
            ArrayList<int[]> paragraphEndings = new ArrayList<int[]>();
            ArrayList<ArrayList<int[]>> sentenceSpans = new ArrayList<ArrayList<int[]>>();
            ArrayList<ArrayList<int[]>> leadingWordSpans = new ArrayList<ArrayList<int[]>>();
            int paragraphStartIdx = 0;
            for (String paragraphObj : extractedText.split("\n")){
                ArrayList<int[]> localSentenceSpans = new ArrayList<int[]>();
                ArrayList<int[]> localLeadingWordSpans = new ArrayList<int[]>();
                int sentenceStartIdx = 0;
                int sentenceEndIdx = paragraphObj.indexOf('.');
                while (sentenceEndIdx >= 0){
                    localSentenceSpans.add(new int[] {paragraphStartIdx + sentenceStartIdx,paragraphStartIdx + sentenceEndIdx});
                    localLeadingWordSpans.add(new int[] {paragraphStartIdx + sentenceStartIdx,paragraphStartIdx + paragraphObj.indexOf(' ', sentenceStartIdx + 3)});
                    sentenceStartIdx = sentenceEndIdx;
                    sentenceEndIdx = paragraphObj.indexOf('.', sentenceEndIdx +1);
                }
                sentenceSpans.add(localSentenceSpans);
                leadingWordSpans.add(localLeadingWordSpans);
                paragraphEndings.add(new int[] {paragraphStartIdx, paragraphStartIdx + paragraphObj.length() - 1});
                paragraphStartIdx += (paragraphObj.length() + 1);
            }
            formatEndings.put("paragraphSpans", paragraphEndings);
            formatEndings.put("sentenceSpans", sentenceSpans);
            formatEndings.put("leadingWordSpans", leadingWordSpans);
        }
    }
}