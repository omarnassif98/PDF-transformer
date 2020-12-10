package com.omz.pdf_transformer;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class PDFContentManager {
    public static  PDFContentManager singleton;
    Context context;
    int pagenum, x = 0;
    TextView pageDisplay = null;
    PDDocument pdfDoc;
    PDFTextStripper pdfTextStripper;
    ArrayList<ContentFormater> activeSpanTemplates = new ArrayList<ContentFormater>(), passiveSpanTemplates = new ArrayList<ContentFormater>();
    ArrayList<ParcelableSpan> instantiatedActiveSpans = new ArrayList<ParcelableSpan>(), instantiatedPassiveSpans = new ArrayList<ParcelableSpan>();
    HashMap<String, Object> formatInfo = new HashMap<String, Object>();
    boolean cascadeFlag = false;
    public PDFContentManager(Context context) throws IOException {
        //Only pass in application context, no activity contexts please
        this.context = context;
        pdfTextStripper = new PDFTextStripper();
        singleton = this;
        PDFBoxInitializer pdfBoxInitializerWorker = new PDFBoxInitializer(context);
        new Thread(pdfBoxInitializerWorker).start();
    }

    public void ScrapePDF(final TextView pageDisplay, Uri PDFUri, ContentResolver androidContentResolver, float screenDensity){
        this.pageDisplay = pageDisplay;
        PDFScraper pdfScraperWorker = new PDFScraper(PDFUri,androidContentResolver, screenDensity);
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
            JSONObject passiveTemplates = savedConfig.getJSONObject("passive_span_templates");
            it = passiveTemplates.keys();
            while (it.hasNext()){
                String key = it.next();
                Log.d("JSON", "LoadSpanPreferences found passive key " + key);
                UnpackFormatRule(key, passiveTemplates.getJSONObject(key), passiveSpanTemplates);
            }

            it = activeTemplates.keys();
            while (it.hasNext()) {
                String key = it.next();
                Log.d("JSON", "LoadSpanPreferences found active key " + key);
                UnpackFormatRule(key, activeTemplates.getJSONObject(key), activeSpanTemplates);
            }
            Log.d("JSON", "Active span count = " + activeSpanTemplates.size());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    void UnpackFormatRule(String formattingRule, JSONObject templates, ArrayList<ContentFormater> list) throws JSONException {
        short formatRule = -1;
        switch (formattingRule){
            case "universal":
                formatRule = 0;
                break;
            case "leading_words":
                formatRule = 2;
                break;
            case "cascade":
                if((Boolean) templates.get("enabled")){
                    cascadeFlag = true;
                }
            default:
                Log.d("RULE", "Defaulted, unrecognized rule: " + formattingRule);
                return;
        }
        Log.d("UNPACK", "UnpackFormatRule: added " + formattingRule);
        for (Iterator<String> it = templates.keys(); it.hasNext(); ) {
            String key = it.next();
            AddTemplate(key, templates.get(key), list, formatRule);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    void AddTemplate(String key, Object value, ArrayList<ContentFormater> list, int rule) throws JSONException {
        switch (key){
            case "text_color":
                list.add(new ColorFormatter((String)value, rule));

                break;
            case  "line_spacing":
                list.add(new LineSpaceFormatter((int)value, rule));

                break;
            case "text_size":
                list.add(new TextSizeFormatter((int)value, rule));
                if (rule == 0){
                    UpdateFormatInfo("universal_size", (int)value);
                }else {
                    UpdateFormatInfo("leading_word_size", (int)value);
                }
                break;
            default:
                Log.d("TEMPLATE", "Defaulted, unrecognized key: " + key);
        }
    }


    void UpdateFormatInfo(String key, int val){
        formatInfo.put(key, val);
    }

    public Object GetFormatSpan(String key){
        return formatInfo.get(key);
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
        float res;
        public PDFScraper(Uri PDFUri, ContentResolver androidContentResolver, float res){
            this.PDFUri = PDFUri;
            this.androidContentResolver = androidContentResolver;
            this.res = res;
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        public void run() {
            try {
                InputStream resolvedPDFMedia = androidContentResolver.openInputStream(PDFUri);
                InfoScraper pdfExtractor = new InfoScraper(resolvedPDFMedia);
                resolvedPDFMedia.close();
                pagenum = 0;
                pdfExtractor.setSortByPosition(true);
                String scrapedText = pdfExtractor.ScrapePage(0);
                UpdateFormatInfo(scrapedText);
                final SpannableStringBuilder formattedText = new SpannableStringBuilder(scrapedText);
                int paragraphBeginIdx = 0;
                //Active templates are applied "under" clickable spans
                //Clickable spans dont count as either passive or active because they don't actually apply transformations

                final AppendingFormatterObject cpl = new AppendingFormatterObject(pageDisplay, res);
                if(cascadeFlag){
                    activeSpanTemplates.add(new AppendingFormatterObject(pageDisplay, res));
                }
                if (activeSpanTemplates.size() > 0 && formatInfo.containsKey("paragraphSpans")) {
                    int i = 0;
                    for (int[] paragraphBounds : (ArrayList<int[]>) formatInfo.get("paragraphSpans")) {
                        final int finalI = i;
                        ClickableSpan focusEvent = new ClickableSpan() {
                            @Override
                            public void onClick(@NonNull View widget) {
                                for (ContentFormater span : activeSpanTemplates){
                                    span.ClearTransformations(formattedText);
                                }
                                for (ContentFormater span : activeSpanTemplates){
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

                if (passiveSpanTemplates.size() > 0) {
                    int i = 0;
                    for (int[] paragraphBounds : (ArrayList<int[]>) formatInfo.get("paragraphSpans")) {
                        final int finalI = i;
                        for (ContentFormater span : passiveSpanTemplates){
                            span.ApplyTransformation(formattedText, finalI);
                        }
                        i++;
                    }
                }

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
                    sentenceStartIdx = sentenceEndIdx + 2;
                    sentenceEndIdx = paragraphObj.indexOf('.', sentenceEndIdx +1);
                }
                sentenceSpans.add(localSentenceSpans);
                leadingWordSpans.add(localLeadingWordSpans);
                paragraphEndings.add(new int[] {paragraphStartIdx, paragraphStartIdx + paragraphObj.length() - 1});
                paragraphStartIdx += (paragraphObj.length() + 1);
            }
            formatInfo.put("paragraphSpans", paragraphEndings);
            formatInfo.put("sentenceSpans", sentenceSpans);
            formatInfo.put("leadingWordSpans", leadingWordSpans);
        }

    }
}