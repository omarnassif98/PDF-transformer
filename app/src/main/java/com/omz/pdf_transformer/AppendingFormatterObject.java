package com.omz.pdf_transformer;

import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class AppendingFormatterObject implements ContentFormater {
    int lineWidth;
    TextPaint universalPaint, leadingPaint;
    ArrayList<int[]> addedSpans;
    @RequiresApi(api = Build.VERSION_CODES.R)
    public  AppendingFormatterObject(TextView textView, float screenResolution){
        Log.d("RES", "Res: " + screenResolution);
        lineWidth = textView.getWidth();
        universalPaint = new TextPaint();
        universalPaint.setTypeface(textView.getPaint().getTypeface());
        try{
            universalPaint.setTextSize((int)PDFContentManager.singleton.GetFormatSpan("universal_size"));
        }catch (Exception err){
            universalPaint.setTextSize(32);
        }
        Log.d("RES", "Size: " + universalPaint.getTextSize());
        Log.d("RES", "Actual size: " + textView.getPaint().getTextSize());
        Log.d("RES", "Measure: " + universalPaint.measureText(" "));
        Log.d("RES", "Actual measure: " + textView.getPaint().measureText(" "));

        leadingPaint = new TextPaint();
        leadingPaint.setTypeface(textView.getPaint().getTypeface());
        try{
            leadingPaint.setTextSize((int)PDFContentManager.singleton.GetFormatSpan("leading_word_size"));
        }catch (Exception err){
            leadingPaint.setTextSize(32);
        }
        Log.d("RES", "Lead size: " + leadingPaint.getTextSize());
        Log.d("RES", "Lead measure: " + leadingPaint.measureText(" "));
        addedSpans = new ArrayList<int[]>();
    }

    @Override
    public void ApplyTransformation(SpannableStringBuilder blurb, ArrayList formatInfo) {

    }

    @Override
    public void ApplyTransformation(SpannableStringBuilder blurb, int paragraphNumber) {
        Log.d("Appending", "(Jenga) Format for paragraph " + paragraphNumber);
        int[] paragraphSpan = ((ArrayList<int[]>) PDFContentManager.singleton.GetFormatSpan("paragraphSpans")).get(paragraphNumber);
        ArrayList<int[]> leadingWords = ((ArrayList<ArrayList>) PDFContentManager.singleton.GetFormatSpan("leadingWordSpans")).get(paragraphNumber);
        String paragraph = blurb.toString().substring(paragraphSpan[0], paragraphSpan[1]);
        int leadingSpaceSize = 0;
        int sentenceCount = 0;
        int correctionOffset = 0;
        for (String sentence: paragraph.split("\\.")) {
            String[] headTail = sentence.split(" ", 2);
            leadingSpaceSize += leadingPaint.measureText(headTail[0]);
            for (String trailingWord : headTail[1].split(" ")) {
                if (leadingSpaceSize + (universalPaint.measureText(" " + trailingWord)) > lineWidth) {
                    leadingSpaceSize = 0;
                    leadingSpaceSize += universalPaint.measureText(trailingWord);
                } else {
                    leadingSpaceSize += universalPaint.measureText(" " + trailingWord);
                }
            }
            try {
                sentenceCount++;
                int leadingSpaceCount = (int) (leadingSpaceSize/leadingPaint.measureText(" ")) + (sentenceCount-1);
                String sep = "\n";
                for (int i = 0; i <= leadingSpaceCount; i++){
                    sep += "\u0020";
                }
                int startIdx = leadingWords.get(sentenceCount)[0] + correctionOffset;
                blurb.insert(startIdx, sep);
                addedSpans.add(new int[]{startIdx, startIdx+sep.length()});
                correctionOffset += (leadingSpaceCount+2);
                Log.d("APP", "APPENDED: [" + startIdx + ", " + (startIdx+sep.length()) + "]");
            }catch (Exception err){
                Log.d("SENTENCE", "It's ok, last sentences don't do anything");
            }
        }
    }

    @Override
    public void ClearTransformations(SpannableStringBuilder blurb) {
        for (int i = addedSpans.size()-1; i >= 0; i--){
            int[] range = addedSpans.get(i);
            blurb.delete(range[0],range[1]);
            Log.d("Delete", "Deleted characters [" + range[0] + ", " + range[1] + "]");
        }
        addedSpans.clear();
    }
}