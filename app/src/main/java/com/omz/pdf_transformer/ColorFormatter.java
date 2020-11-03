package com.omz.pdf_transformer;

import android.text.style.ForegroundColorSpan;

public class ColorFormatter extends FormatterObject{

    public ColorFormatter(int color, int rule) throws NoSuchMethodException {
        this.formattingRule = rule;
        this.spanConstructor = ForegroundColorSpan.class.getConstructor(int.class);
        this.argVals = new Object[] {color};
    }
}