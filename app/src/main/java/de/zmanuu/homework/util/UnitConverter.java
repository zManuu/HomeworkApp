package de.zmanuu.homework.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class UnitConverter {

    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / (DisplayMetrics.DENSITY_XXHIGH - 50));
    }
    public static int convertPixelsToDpInt(float px, Context context){
        return (int) convertPixelsToDp(px, context);
    }

}
