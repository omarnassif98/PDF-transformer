package com.omz.pdf_transformer;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

public class TextSizeFormatter extends FormatterObject{
    int textSize;
    public TextSizeFormatter(int textSize, int formattingRule){
        this.textSize = textSize;
        this.formattingRule = formattingRule;

    }
}
