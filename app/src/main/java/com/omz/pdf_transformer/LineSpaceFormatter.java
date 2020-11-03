package com.omz.pdf_transformer;

import android.os.Build;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineHeightSpan;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class LineSpaceFormatter extends FormatterObject{
    int height;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public LineSpaceFormatter(int height, int formattingRule){
        this.height = height;
        this.formattingRule = formattingRule;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void ApplyTransformation(SpannableString blurb, int paragraphNumber) {
        int spanCount;
        if(this.formattingRule == 2){
            spanCount = ((ArrayList<int[]>)PDFContentManager.singleton.GetFormatSpan("leadingWordSpans").get(paragraphNumber)).size();
            Log.d("SPANCOUNT", "ApplyTransformation: " + spanCount);
        }else {
            spanCount = 1;
        }
        this.span = new ParcelableSpan[spanCount];
        for (int i = 0; i < spanCount; i++){
            this.span[i] = new LineHeightSpan.Standard(this.height);
        }
        super.ApplyTransformation(blurb,paragraphNumber);
    }
}
