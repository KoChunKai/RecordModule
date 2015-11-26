package com.module.kai.record;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.IOException;

/**
 * Created by kevin on 2015/11/25.
 */
public class Record {

    public static Record instance = null;

    public static Record getInstance(Activity activity){
        if(instance == null){
            instance = new Record(activity);
        }
        return instance;
    }

    Activity self;
    Camera myCamera;
    SurfaceView previewSurfaceView;
    SurfaceHolder previewSurfaceHolder;
    private MediaRecorder mMediaRecorder;


    private Record(Activity activity){
        this.self = activity;
        previewSurfaceView = (SurfaceView)self.findViewById(R.id.surfaceView1);
        previewSurfaceHolder = previewSurfaceView.getHolder();
        previewSurfaceHolder.addCallback(callback);
        previewSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        previewSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myCamera.autoFocus(autoFocus);
            }
        });
    }

    public void startCamera(){
        myCamera.startPreview();
    }

    public void stopCamera(){
        myCamera.stopPreview();
    }

    public void startRecord(){
        File dir = new File(Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_PICTURES
                + File.separator + "KAI");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        myCamera.stopPreview();
        myCamera.unlock();
        File videofile = new File(dir, "KAI_" + System.currentTimeMillis() + ".mp4");
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(myCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setOutputFile(videofile.getAbsolutePath());
        mMediaRecorder.setPreviewDisplay(previewSurfaceView.getHolder().getSurface());
        mMediaRecorder.setMaxDuration(0);
        mMediaRecorder.setMaxFileSize(0);
        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {

            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Log.e("Error Recording", what + " Extra " + extra);

            }
        });
        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {

            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {

                }

            }
        });
        self.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                try {
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void stopRecord(){
        mMediaRecorder.stop();
        mMediaRecorder.reset();
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d("camera", "surfaceCreated");
            myCamera = Camera.open();
            Camera.Parameters params = myCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            myCamera.setParameters(params);
            setCameraDisplayOrientation(self, 0);

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d("camera", "surfaceChange");
            try {
                myCamera.setPreviewDisplay(holder);
                //myCamera.startPreview();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
        }
    };

    private Camera.AutoFocusCallback autoFocus = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Log.d("camera", "autoFocus " + "b:" + success);
        }
    };

    private void setCameraDisplayOrientation(Activity activity, int cameraId) {

        Camera.CameraInfo info = new Camera.CameraInfo();

        Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        myCamera.setDisplayOrientation(result);
    }


}
