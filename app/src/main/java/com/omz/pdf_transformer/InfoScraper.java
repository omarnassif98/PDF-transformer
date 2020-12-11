package com.omz.pdf_transformer;

import android.util.Log;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.text.TextPosition;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

public class InfoScraper extends PDFTextStripper {
    boolean startedScraping = false;
    float lastCharX = -1, lastCharY = -1;
    char lastChar;
    public InfoScraper(InputStream inputStream) throws IOException {
        super();
        document = PDDocument.load(inputStream);
        this.setLineSeparator("");
    }

    public String ScrapePage(int pageNumb) throws IOException{
        this.setStartPage(pageNumb+1);
        this.setEndPage(pageNumb+1);
        return getText(document);

    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws  IOException{
        float currentFirstX = textPositions.get(0).getXDirAdj(), currentFirstY = textPositions.get(0).getYDirAdj();
        if (!startedScraping){
            lastCharX = textPositions.get(textPositions.size()-1).getXDirAdj();
            lastCharY = textPositions.get(textPositions.size()-1).getYDirAdj();
            startedScraping = true;
            string = "\t" + string;
        }else{
            if(Character.isUpperCase(string.charAt(0)) && currentFirstY > lastCharY) {
                string = "\n\t" + string;
            }
        }
        output.write(string);
        document.close();
    }
}