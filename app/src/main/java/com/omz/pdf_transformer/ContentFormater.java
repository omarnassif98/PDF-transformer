package com.omz.pdf_transformer;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;

import java.util.ArrayList;

public interface ContentFormater {

    //This function applies the content transformations throughout an entire paragraph
    //It's important to note that in the case of SpannableFormatterObjects, the rule is defined elsewhere
    public void ApplyTransformation(SpannableStringBuilder blurb, int paragraphNumber);

    //This clears the transformations.
    //Extremely important to do this when switching pages and activating a new dynamic transformation or else you can run into serious formatting issues, especially in the case of AppendingFormatterObjects
    public void ClearTransformations(SpannableStringBuilder blurb);

}