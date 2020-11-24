package com.omz.pdf_transformer;

import android.text.style.ForegroundColorSpan;

public class ColorFormatter extends FormatterObject{

    public ColorFormatter(int color, int rule)  {
        this.formattingRule = rule;
        try {
            this.spanConstructor = ForegroundColorSpan.class.getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        this.argVals = new Object[] {color};
    }
}