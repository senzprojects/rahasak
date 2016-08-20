package com.score.chatz.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.ContextMenu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.handlers.SenzHandler;
import com.score.chatz.pojo.Secret;
import com.score.chatz.services.SenzServiceConnection;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lakmalcaldera on 8/16/16.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CameraPreview.class.getName();;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private int pictureWidth = 800;
    private int pictureHeight = 800;

    //Constructor that obtains context and camera
    public CameraPreview(Context _context, Camera camera) {
        super(_context);
        //context = _context;
        //this.mCamera = camera;
        this.mCamera = camera;
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this); // we get notified when underlying surface is created and destroyed
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //this is a deprecated method, is not requierd after 3.0

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(mCamera != null)
        mCamera.release();
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        Camera.Parameters params = mCamera.getParameters();
        params.setPictureSize(pictureWidth, pictureHeight);

        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.setDisplayOrientation(90);

            mCamera.startPreview();
        } catch (IOException e) {
            // left blank for now
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());

        }

    }

    public void takePhoto(final PhotoActivity activity, final Senz originalSenz){
        mCamera.takePicture(null, null, new Camera.PictureCallback(){
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {

                SenzHandler.getInstance(getContext()).sendPhoto(getResizedBitmap(bytes, 1), originalSenz);

                activity.finish();
            }
        });

        Camera.Parameters params = mCamera.getParameters();

        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Size mSize;
        for (Camera.Size size : sizes) {
            Log.i(TAG, "Available resolution: "+size.width+" "+size.height);
            //mSize = size;
        }
}

    public byte[] getResizedBitmap(byte[] image, int raducingFactor) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(image , 0, image.length);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap resizeBitmap =  Bitmap.createScaledBitmap(bitmap, width/raducingFactor, height/raducingFactor, true);

        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

        return baos.toByteArray();
    }

    public byte[] scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);

        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

        return baos.toByteArray();
    }




    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            Log.d(TAG,"Stopping preview in SurfaceDestroyed().");
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
                               int width, int height) {


        if(mSurfaceHolder.getSurface()==null){
            //preview surface does not exist
            return;
        }
        try {
            mCamera.stopPreview();
        }catch(Exception e){
            //ignore: tried to stop a non-existent preview
        }

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (Exception e) {
            // intentionally left blank for a test
            Log.d(TAG, "Error starting camera preview: "+e.getMessage());
        }
    }


}
