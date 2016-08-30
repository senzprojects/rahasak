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
import com.score.chatz.pojo.SenzStream;
import com.score.chatz.services.SenzServiceConnection;
import com.score.chatz.utils.CameraUtils;
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
    private SenzStream.SENZ_STEAM_TYPE streamType;
    //private int pictureWidth = 500;
    //private int pictureHeight = 500;

    //Constructor that obtains context and camera
    public CameraPreview(Context _context, Camera camera, SenzStream.SENZ_STEAM_TYPE streamType) {
        super(_context);
        //context = _context;
        //this.mCamera = camera;
        this.mCamera = camera;
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this); // we get notified when underlying surface is created and destroyed
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //this is a deprecated method, is not requierd after 3.0
        this.streamType = streamType;

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(mCamera != null)
        mCamera.release();
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

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

                byte[] resizedImage = null;
                bytes = rotateBitmapByteArray(bytes, -90);
                if(streamType == SenzStream.SENZ_STEAM_TYPE.CHATZPHOTO) {
                    //Scaled down image
                    resizedImage = CameraUtils.getCompressedImage(bytes, 100); //Compress image ~ 5kbs
                }else if(streamType == SenzStream.SENZ_STEAM_TYPE.PROFILEZPHOTO){
                    resizedImage = CameraUtils.getCompressedImage(bytes, 100); //Compress image ~ 50kbs
                }

                SenzHandler.getInstance(getContext()).sendPhoto(resizedImage, originalSenz);

                activity.finish();
            }
        });
    }

    private byte[] rotateBitmapByteArray(byte[] data, int deg){
        Bitmap decodedBitmap = CameraUtils.decodeBase64(Base64.encodeToString(data, 0));
        Bitmap rotatedBitmap = CameraUtils.getRotatedImage(decodedBitmap, deg);
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
