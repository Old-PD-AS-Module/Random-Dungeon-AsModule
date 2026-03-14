
package com.lh64.noosa;

import java.util.HashMap;
import android.graphics.RectF;

public class FontData {
    private HashMap<Character, RectF> charMap;

    public FontData() {
        charMap = new HashMap<Character, RectF>();
    }

    public void addChar(char c, RectF rect) {
        charMap.put(c, rect);
    }

    public RectF getRect(char c) {
        return charMap.get(c);
    }
}
