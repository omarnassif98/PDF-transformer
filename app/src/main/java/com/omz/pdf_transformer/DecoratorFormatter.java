package com.omz.pdf_transformer;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import org.json.JSONArray;

public class DecoratorFormatter extends FormatterObject{

    public DecoratorFormatter(JSONArray decorations, int rule)  {
        this.formattingRule = rule;
        for(int i = 0; i<decorations.length(); i++){

        }
        int colorVal = Color.parseColor("color");
        try {
            this.spanConstructor = ForegroundColorSpan.class.getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        this.argVals = new Object[] {colorVal};
    }
}