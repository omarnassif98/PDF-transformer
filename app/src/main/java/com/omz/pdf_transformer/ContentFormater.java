package com.omz.pdf_transformer;

import android.text.SpannableString;

import java.util.ArrayList;

public interface ContentFormater {
    public void ApplyTransformation(SpannableString blurb, ArrayList formatInfo);
    public void ApplyTransformation(SpannableString blurb, int paragraphNumber);
    public void ClearTransformations(SpannableString blurb);

}
