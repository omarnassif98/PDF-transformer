package com.omz.pdf_transformer;

import android.os.Build;
import android.text.style.LineHeightSpan;

import androidx.annotation.RequiresApi;

public class LineSpaceFormatter extends SpannableFormatterObject {
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public LineSpaceFormatter(int color, int rule)  {
        this.formattingRule = rule;
        try {
            this.spanConstructor = LineHeightSpan.Standard.class.getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        this.argVals = new Object[] {color};
    }
}