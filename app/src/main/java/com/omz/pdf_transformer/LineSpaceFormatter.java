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
