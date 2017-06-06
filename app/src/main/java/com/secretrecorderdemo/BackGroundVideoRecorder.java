package com.secretrecorderdemo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Toast;

public class BackGroundVideoRecorder extends Service implements SurfaceHolder.Callback {

    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;
    int buttonNumber = -1;
    boolean isCameraQualityHigh = false;
    private String pathString;
    private static int currentapiVersion = Build.VERSION.SDK_INT;
    private static final String TAG = "Suprem";
    private static int typeForVideo = 0; 
    private static int typeForAudio = 1; 

	@Override
    public void onCreate() {
		// no code
    }
		@Override
	   public int onStartCommand(Intent intent, int flags, int startId) {
			Log.d(TAG,"onCreate service");
			buttonNumber = intent.getIntExtra("button", 0);
			Log.d(TAG,"onCreate service intent ="+intent);
			if(buttonNumber == 1 && intent.getStringExtra("quality").equals("High"))
				isCameraQualityHigh = true;
				
	        // Start foreground service to avoid unexpected kill
			Notification note=new NotificationCompat.Builder(this).build();
	        startForeground(1234, note);
	        
	        // Create new SurfaceView, set its size to 1x1, move it to the top left corner and set this service as a callback
	        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
	        surfaceView = new SurfaceView(this);
	        LayoutParams layoutParams = new WindowManager.LayoutParams(
	            1, 1,
	            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
	            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
	            PixelFormat.TRANSLUCENT
	        );
	        // layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
	        windowManager.addView(surfaceView, layoutParams);
	        surfaceView.getHolder().addCallback(this);
	        
	        return START_STICKY;
	   }

    // Method called right after Surface created (initializing and starting MediaRecorder)
    @SuppressLint("NewApi")
	@Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    	mediaRecorder = new MediaRecorder();
    	if(buttonNumber == 1 || buttonNumber ==2 ){
    		Log.d(TAG,"surfaceCreated  service");
    		for(int i=0; i< Camera.getNumberOfCameras(); i++){
    			System.out.println("camera n " +i);
    			Camera.CameraInfo newInfo = new Camera.CameraInfo();
    			Camera. getCameraInfo(i, newInfo);            	
           
    			if( buttonNumber == 1 && newInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
    				Log.d(TAG,"a ="+buttonNumber);
    				camera = Camera.open(0);
    				if(currentapiVersion >= 17 && newInfo.canDisableShutterSound)
    					camera.enableShutterSound(false);
            		}
    			   else if( buttonNumber == 2 && newInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
    				Log.d(TAG,"a ="+buttonNumber);
    				camera = Camera.open(1);
    				if(currentapiVersion >= 17 && newInfo.canDisableShutterSound)
    					camera.enableShutterSound(false);
    			}
            }   
        	camera.unlock();
    	}
    	if(buttonNumber == 1 || buttonNumber == 2) {        // for video recording
        	mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        	mediaRecorder.setCamera(camera);
        	mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        	mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        	Log.d(TAG,"Quality isCameraQualityHigh ="+isCameraQualityHigh);
        	if(isCameraQualityHigh == true)
        		mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        	else
        		mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));	
        
        	
        
        
        	pathString =  openFileForStorage(typeForVideo).getAbsolutePath();
        	mediaRecorder.setOutputFile(pathString);
        
    	}else if(buttonNumber == 3){    // for audio recording
    		mediaRecorder = new MediaRecorder();
    		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    		pathString =  openFileForStorage(typeForAudio).getAbsolutePath();
    	        mediaRecorder.setOutputFile(pathString);
    		
    	} 
    if(buttonNumber ==1 || buttonNumber == 2 || buttonNumber == 3) {
        try { 
        	mediaRecorder.prepare(); 
        	} catch (Exception e) {	}
        Log.d(TAG,"Recording started ");
        
        mediaRecorder.start();
    }

    }

    // Stop recording and remove SurfaceView
    @Override
    public void onDestroy() {
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        if(buttonNumber ==1 || buttonNumber == 2){
        camera.lock();
        camera.release();
        }

        windowManager.removeView(surfaceView);
        Log.d(TAG,"everything Destroy ");

    }
    private File openFileForStorage(int type) {
	    File directory = null;
	    File directoryOP = null;
	    String storageState = Environment.getExternalStorageState();
	    if (storageState.equals(Environment.MEDIA_MOUNTED)) {
	    	directory = new File(
	    			Environment.getExternalStoragePublicDirectory(File.separator),"SpyCamStore");
	      if (!directory.exists() && !directory.mkdirs()) {
	    	  directory = null;
	      } else {
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_mm_dd_hh_mm",
	          Locale.getDefault());
	        if(type == 0)
	        	directoryOP = new File(directory.getPath() +
	      	          File.separator + "video_" +
	    	          dateFormat.format(new Date()) + ".mp4");
	        if(type == 1)
	        	directoryOP = new File(directory.getPath() +
		    	          File.separator + "audio_" +
		    	          dateFormat.format(new Date()) + ".mp3");
	        Toast.makeText(this, "Will save output to: " + directoryOP.getPath(),
	  	          Toast.LENGTH_LONG).show();
	        
	        return (directoryOP);
	      }
	    }
	    return null;
	  }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
    	
    	
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}

    @Override
    public IBinder onBind(Intent intent) { return null; }
}