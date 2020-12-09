package com.omz.pdf_transformer;

import android.graphics.Color;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class SpannableFormatterObject implements ContentFormater {

    public ParcelableSpan[] span = null;
    public int formattingRule;
    public Constructor spanConstructor;
    public Object[] argVals;
    @Override
    public void ApplyTransformation(SpannableStringBuilder blurb, ArrayList formatInfo) {
        switch (formattingRule){
            case 0:

                //blurb.setSpan(span,);
        }
    }

    @Override
    public void ApplyTransformation(SpannableStringBuilder blurb, int paragraphNumber) {
        Log.d("Span", "Formatting change in paragraph " + paragraphNumber);
        int spanCount = (this.formattingRule == 2)? ((ArrayList<int[]>)((ArrayList)PDFContentManager.singleton.GetFormatSpan("leadingWordSpans")).get(paragraphNumber)).size():1;
        this.span = new ParcelableSpan[spanCount];
        for (int i=0; i < this.span.length; i++){
            try {
                this.span[i] = (ParcelableSpan) spanConstructor.newInstance(argVals);
            } catch ( IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        //UNIVERSAL
        switch (formattingRule){
            case 0:
                int[] paragraphSpan = (int[])((ArrayList)PDFContentManager.singleton.GetFormatSpan("paragraphSpans")).get(paragraphNumber);
                blurb.setSpan(span[0], paragraphSpan[0], paragraphSpan[1], SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
                break;
            case 1: case 2:
                ArrayList<int[]> sentenceSpans = (formattingRule == 1)?(ArrayList<int[]>)((ArrayList)PDFContentManager.singleton.GetFormatSpan("sentenceSpans")).get(paragraphNumber):(ArrayList<int[]>)((ArrayList)PDFContentManager.singleton.GetFormatSpan("leadingWordSpans")).get(paragraphNumber);
                int i = 0;
                for (int[] bound : sentenceSpans){
                    blurb.setSpan(span[i], bound[0], bound[1], SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
                    i++;
                }
                break;
        }
    }

    @Override
    public void ClearTransformations(SpannableStringBuilder blurb){
        if (span != null){
            for(ParcelableSpan parcelableSpan : span){
                blurb.removeSpan(parcelableSpan);
            }
            span = null;
        }
    }
}
