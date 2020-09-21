package com.example.capstoneproject.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

public class Dropdown extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {

    public Dropdown(Context context) {
        super(context);
    }

    public Dropdown(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public Dropdown(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && getFilter()!=null) {
            performFiltering(getText(), 0);
        }
    }
}
