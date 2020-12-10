package com.omz.pdf_transformer;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

public class ColorFormatter extends SpannableFormatterObject {

    public ColorFormatter(String color, int rule)  {
        this.formattingRule = rule;
        int colorVal = Color.parseColor(color);
        Log.d("ADD", "ColorFormatter: " + color + colorVal);
        try {
            this.spanConstructor = ForegroundColorSpan.class.getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        this.argVals = new Object[] {colorVal};
    }
}