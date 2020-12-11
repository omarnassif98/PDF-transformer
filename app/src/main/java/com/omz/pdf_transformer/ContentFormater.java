package com.omz.pdf_transformer;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;

import java.util.ArrayList;

public interface ContentFormater {
    public void ApplyTransformation(SpannableStringBuilder blurb, ArrayList formatInfo);
    public void ApplyTransformation(SpannableStringBuilder blurb, int paragraphNumber);
    public void ClearTransformations(SpannableStringBuilder blurb);

}