package com.omz.pdf_transformer;

import android.text.style.AbsoluteSizeSpan;

public class TextSizeFormatter extends SpannableFormatterObject {
    public TextSizeFormatter(int size, int rule)  {
        this.formattingRule = rule;
        try {
            this.spanConstructor = AbsoluteSizeSpan.class.getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        this.argVals = new Object[] {size};
    }
}
