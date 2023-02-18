package com.boris.colornamer.model;

import android.content.Context;
import android.graphics.Color;

import com.boris.colornamer.R;

import org.jetbrains.annotations.NotNull;

public class CompleteColor {
    private int r;
    private int g;
    private int b;

    private Context mContext;

    /**
     * Constructor with a context to get the color name depending on the locale used
     *
     * @param context the context from which the color names are extracted
     */
    public CompleteColor(@NotNull Context context) {
        this.mContext = context;
        setBlack();
    }

    /**
     * Setter
     *
     * @param context the new context to use for the color names extraction
     */
    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * Getter
     *
     * @return the red value of the color
     */
    public int getR() {
        return r;
    }

    /**
     * Setter
     *
     * @param r the new red value of the color
     */
    public void setR(int r) {
        this.r = r;
    }

    /**
     * Getter
     *
     * @return the green value of the color
     */
    public int getG() {
        return g;
    }

    /**
     * Setter
     *
     * @param g the new green value of the color
     */
    public void setG(int g) {
        this.g = g;
    }

    /**
     * Getter
     *
     * @return the blue value of the color
     */
    public int getB() {
        return b;
    }

    /**
     * Setter
     *
     * @param b the new blue value of the color
     */
    public void setB(int b) {
        this.b = b;
    }

    /**
     * Setter to set all red, green and blue values to 0
     */
    public void setBlack() {
        this.r = 0;
        this.g = 0;
        this.b = 0;
    }

    /**
     * Getter
     *
     * @return the hue of the color between 0 and 360
     */
    public int getH() {
        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);
        return (int) hsv[0];
    }

    /**
     * Getter
     *
     * @return the saturation of the color between 0 and 1
     */
    public float getS() {
        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);
        return hsv[1];
    }

    /**
     * Getter
     *
     * @return the value of the color between 0 and 1
     */
    public float getV() {
        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);
        return hsv[2];
    }

    /**
     * Getter
     *
     * @return the name of the current color
     */
    public String getName() {
        int h = getH();
        float s = getS();
        float v = getV();

        String color = "";
        if (h < 15 || h >= 346) color = mContext.getString(R.string.red);

        if (h >= 15 && h < 40) if (s < 0.75) {
            color = mContext.getString(R.string.brown);
        } else {
            color = mContext.getString(R.string.orange);
        }

        if (h >= 40 && h < 74) color = mContext.getString(R.string.yellow);

        if (h >= 74 && h < 155) color = mContext.getString(R.string.green);

        if (h >= 155 && h < 186) color = mContext.getString(R.string.cyan);

        if (h >= 186 && h < 278) color = mContext.getString(R.string.blue);

        if (h >= 278 && h < 330) color = mContext.getString(R.string.purple);

        if (h >= 330 && h < 346) color = mContext.getString(R.string.pink);

        if (v < 0.18) {
            color = mContext.getString(R.string.black);
        } else {
            if (s < 0.1) {
                if (v < 0.85) {
                    color = mContext.getString(R.string.gray);
                } else {
                    color = mContext.getString(R.string.white);
                }
            }
        }

        return color;
    }

}
