package com.boris.colornamer.analyzer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.boris.colornamer.R;
import com.boris.colornamer.imageutils.ImageConverter;
import com.boris.colornamer.model.CompleteColor;

import org.jetbrains.annotations.NotNull;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageAnalyzer implements ImageAnalysis.Analyzer {

    private final ImageView mPreviewView;
    private final TextView mTextViewColor;
    private final TextView mTextViewRGB;
    private final TextView mTextViewHSV;
    private Context mContext;
    private Bitmap bmp;
    private Mat rgb;
    private final Size SQUARE_SIZE = new Size(500, 500);

    private final CompleteColor meanCompleteColor;

    /**
     * Constructor
     *
     * @param imageView     the ImageView to draw the camera image on
     * @param textViewColor the TextView to display the color name
     * @param textViewRGB   the TextView to display the RGB values
     * @param textViewHSV   the TextView to display the HSV values
     * @param context       the context to get the string values of the rgb and hsv format depending on the used locale
     */
    public ImageAnalyzer(@NotNull ImageView imageView, @NotNull TextView textViewColor, @NotNull TextView textViewRGB, @NotNull TextView textViewHSV, @NotNull Context context) {
        this.mPreviewView = imageView;
        this.mTextViewColor = textViewColor;
        this.mTextViewRGB = textViewRGB;
        this.mTextViewHSV = textViewHSV;
        this.mContext = context;
        this.meanCompleteColor = new CompleteColor(context);
    }

    /**
     * Setter
     *
     * @param context the context to get the string values of the rgb and hsv format depending on the used locale
     */
    public void setContext(Context context) {
        this.mContext = context;
        //update the context of the mean color for its name
        this.meanCompleteColor.setContext(context);
    }

    /**
     * Processes the given image from camera
     *
     * @param image The image to analyze
     */
    @Override
    public void analyze(@NotNull ImageProxy image) {
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            mTextViewColor.setText(mContext.getString(R.string.format_error));
            image.close();
            return;
        }

        //avoid creation of Mat every frame
        if (rgb == null) rgb = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC3);

        //convert image Yuv to Mat RGB
        ImageConverter.convYUV2RGB(image, rgb);

        //resize to a square image
        Imgproc.resize(rgb, rgb, SQUARE_SIZE);

        //update mean color member
        computeMeanColorOfImage();

        //draw square using mean color with black and white contours
        drawMiddleSquareOnImage();

        //display rgb and hsv values
        displayColorValues();

        //display the color name of the mean color
        displayColorName();

        //avoid creation of Bitmap every frame
        if (bmp == null) bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888);

        //display output image using bmp
        ImageConverter.MatToBitmap(rgb, bmp);
        mPreviewView.post(() -> mPreviewView.setImageBitmap(bmp));

        //free memory
        image.close();
        rgb.release();
    }

    /**
     * Computes the mean color of the center of the image
     */
    private void computeMeanColorOfImage() {
        int midX = rgb.rows() / 2;
        int midY = rgb.cols() / 2;

        int thickness = 7;

        //read data of mat
        byte[] data = new byte[3 * rgb.rows() * rgb.cols()];
        rgb.get(0, 0, data);

        //read data to get the mean color in the area
        meanCompleteColor.setBlack();
        int sum = 0;
        for (int i = midX - (thickness - 1) / 2; i < midX - (thickness - 1) / 2 + thickness; i++) {
            for (int j = midY - (thickness - 1) / 2; j < midY - (thickness - 1) / 2 + thickness; j++) {
                int idx = 3 * (i * rgb.cols() + j);
                meanCompleteColor.setR(meanCompleteColor.getR() + (data[idx] & 0xff));
                meanCompleteColor.setG(meanCompleteColor.getG() + (data[1 + idx] & 0xff));
                meanCompleteColor.setB(meanCompleteColor.getB() + (data[2 + idx] & 0xff));
                sum++;
            }
        }

        meanCompleteColor.setR(meanCompleteColor.getR() / sum);
        meanCompleteColor.setG(meanCompleteColor.getG() / sum);
        meanCompleteColor.setB(meanCompleteColor.getB() / sum);
    }

    /**
     * Draws a square at the center of the image with its exterior contours in black and interior contours in white
     */
    private void drawMiddleSquareOnImage() {
        int midX = rgb.rows() / 2;
        int midY = rgb.cols() / 2;

        int thickness = 7;

        //read data of mat
        byte[] data = new byte[3 * rgb.rows() * rgb.cols()];
        rgb.get(0, 0, data);

        //update a square whose size is the thickness
        for (int i = midX - (thickness - 1) / 2; i < midX - (thickness - 1) / 2 + thickness; i++) {
            for (int j = midY - (thickness - 1) / 2; j < midY - (thickness - 1) / 2 + thickness; j++) {
                int idx = 3 * (i * rgb.cols() + j);
                if (i == midX - (thickness - 1) / 2 || i == midX - (thickness - 1) / 2 + thickness - 1 || j == midY - (thickness - 1) / 2 || j == midX - (thickness - 1) / 2 + thickness - 1) {
                    //extern contours are in black
                    data[idx] = 0;
                    data[1 + idx] = 0;
                    data[2 + idx] = 0;
                } else if (i == midX - (thickness - 1) / 2 + 1 || i == midX - (thickness - 1) / 2 + thickness - 2 || j == midY - (thickness - 1) / 2 + 1 || j == midX - (thickness - 1) / 2 + thickness - 2) {
                    //intern contours are in white
                    data[idx] = (byte) 255;
                    data[1 + idx] = (byte) 255;
                    data[2 + idx] = (byte) 255;
                } else {
                    //interior has the color of the mean color
                    data[idx] = (byte) meanCompleteColor.getR();
                    data[1 + idx] = (byte) meanCompleteColor.getG();
                    data[2 + idx] = (byte) meanCompleteColor.getB();
                }
            }
        }

        //update the image with the central square
        rgb.put(0, 0, data);
    }

    /**
     * Displays RGB and HSV values of the mean color using the TextViews
     */
    private void displayColorValues() {
        int r = meanCompleteColor.getR();
        int g = meanCompleteColor.getG();
        int b = meanCompleteColor.getB();

        String nr = r < 100 ? r < 10 ? "00" + r : "0" + r : "" + r;
        String ng = g < 100 ? g < 10 ? "00" + g : "0" + g : "" + g;
        String nb = b < 100 ? b < 10 ? "00" + b : "0" + b : "" + b;

        int h = meanCompleteColor.getH();
        float s = meanCompleteColor.getS();
        float v = meanCompleteColor.getV();

        int is = (int) (s * 100);
        int iv = (int) (v * 100);

        String nh = h < 100 ? h < 10 ? "00" + h : "0" + h : "" + h;
        String ns = is < 100 ? is < 10 ? "00" + is : "0" + is : "" + is;
        String nv = iv < 100 ? iv < 10 ? "00" + iv : "0" + iv : "" + iv;

        mTextViewRGB.setText(this.mContext.getString(R.string.rgb_color, nr, ng, nb));
        mTextViewHSV.setText(this.mContext.getString(R.string.hsv_color, nh, ns, nv));
    }

    /**
     * Displays the color name of the mean color using the TextView
     */
    private void displayColorName() {
        mTextViewColor.setText(meanCompleteColor.getName());
    }

}
