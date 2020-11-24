package com.omz.pdf_transformer;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

//adapted from: https://gist.github.com/mpetlyuk/ec2d64659fbedfd47f7e0e650c9608dd

public class ContentScrollMovementMethod extends LinkMovementMethod {
    private float initialX;
    private float initialY;
    private final int CLICK_MARGIN = 100;

    private boolean isClick(float xStart, float yStart, float xEnd, float yEnd){
        return Math.abs(xEnd - xStart) < CLICK_MARGIN && Math.abs(yEnd - yStart) < CLICK_MARGIN;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();
                int relativeX = (int) endX - widget.getTotalPaddingLeft();
                int relativeY = (int) endY - widget.getTotalPaddingTop() + widget.getScrollY();
                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(relativeY);
                int offset = layout.getOffsetForHorizontal(line,relativeX);
                ClickableSpan[] clickableSpans = buffer.getSpans(offset, offset, ClickableSpan.class);
                if(clickableSpans.length > 0){
                    Log.d("Scroll", "" + clickableSpans.length);
                    if (isClick(initialX, initialY, endX, endY)){
                        clickableSpans[0].onClick(widget);
                        return true;
                    }
                }
                break;
        }
        return Touch.onTouchEvent(widget, buffer, event);
    }
}
