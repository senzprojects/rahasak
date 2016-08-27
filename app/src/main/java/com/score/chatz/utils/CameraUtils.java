package com.score.chatz.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Lakmal on 8/27/16.
 */
public class CameraUtils {

    /**
     * Rotate bitmap to your degree
     * @param image original bitmap image
     * @param degrees amount you want to turn
     * @return rotated bitmap
     */
    public static Bitmap getRotatedImage(Bitmap imageBitmap, int degrees){
        //Setup matrix rotate image
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        //Work on the bitmap
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        return rotatedBitmap;
    }

    /**
     * Resize and compress image
     * @param image original image
     * @param frameReduction how small or big you want the frame to look. Original lengths are divided by this value, maintaining same ratio.
     * @param imageCompression quality of the image. 100 - best, 0 - worst
     * @return image as a byte array
     */
    public static byte[] getResizedImage(byte[] image, int frameReduction, int imageCompression) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(image , 0, image.length);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap resizeBitmap =  Bitmap.createScaledBitmap(bitmap, width/frameReduction, height/frameReduction, true);

        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        resizeBitmap.compress(Bitmap.CompressFormat.JPEG, imageCompression, baos);

        return baos.toByteArray();
    }

    /**
     * utility method to get bytes from Bitmap
     * @param image
     * @return
     */
    public static byte[] getBytesFromImage(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    /**
     * utility method to get Bitmap from bytes
     * @param byteArray
     * @return
     */
    public static Bitmap getBitmapFromBytes(byte[] byteArray) {
        byte[] imageAsBytes = Base64.decode(byteArray, Base64.DEFAULT);

        Bitmap imgBitmap= BitmapFactory.decodeByteArray(imageAsBytes,0,imageAsBytes.length);
        return imgBitmap;
    }
}
