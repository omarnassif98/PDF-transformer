package com.omz.pdf_transformer;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

public class TextSizeFormatter extends FormatterObject{
    public TextSizeFormatter(int size, int rule)  {
        this.formattingRule = rule;
        try {
            this.spanConstructor = RelativeSizeSpan.class.getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        this.argVals = new Object[] {size};
    }
}
