package com.secretrecorderdemo;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ActionBarActivity {
    public int buttonNumber = -1;
    public boolean twoCamera = false;
    private String quality = "";
    private int cameraId = -1;
    private static final String TAG = "Suprem";
    private ImageView imgPreview;
    private Bitmap mCameraBitmap;
    private Button mSaveImageButton;
    private boolean mIsRecording;
    private static final String KEY_IS_RECORDING = "key_is_recording";
    private static final String MY_PREFS_NAME = "shared_pref";
    private int userRingerMode = 0;
    private AudioManager audio;
    Uri imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            System.out.println("camera n " + i);
            Camera.CameraInfo newInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, newInfo);
            cameraId = i;
            if (newInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                twoCamera = true;
            }
        }
        audio = (AudioManager) this.getSystemService(this.AUDIO_SERVICE);
        mIsRecording = false;
        mSaveImageButton = (Button) findViewById(R.id.btnSave);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        getUserRingerMode();

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(KEY_IS_RECORDING, mIsRecording);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mIsRecording = savedInstanceState.getBoolean(KEY_IS_RECORDING, false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        // will be executed onResume

        if (twoCamera)
            findViewById(R.id.btn2).setVisibility(View.VISIBLE);
        else {
            Button btn1 = (Button) findViewById(R.id.btn1);
            btn1.setText("Recording with Camera");
            // findViewById(R.id.btnFront).setEnabled(false);
        }
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME,
                MODE_PRIVATE);
        mIsRecording = prefs.getBoolean(KEY_IS_RECORDING, false);
        if (!mIsRecording) {
            findViewById(R.id.btn2).setEnabled(true);
            findViewById(R.id.btn1).setEnabled(true);
            findViewById(R.id.btnAudiostart).setEnabled(true);
            findViewById(R.id.btn0).setEnabled(false);
            findViewById(R.id.btnAudioStop).setEnabled(false);
        } else {

            findViewById(R.id.btn2).setEnabled(false);
            findViewById(R.id.btn1).setEnabled(false);
            findViewById(R.id.btnAudiostart).setEnabled(false);
            findViewById(R.id.btn0).setEnabled(true);
            findViewById(R.id.btnAudioStop).setEnabled(true);
        }

    }

    public void buttononClick(View v) {
        final Intent i = new Intent(this, BackGroundVideoRecorder.class);

        if (v.getId() == R.id.btn1) {
            buttonNumber = 1;

            final CharSequence[] list = {"High Quality Recording",
                    "Low Qality Recording"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select recording qaulity");
            builder.setItems(list, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0) {
                        quality = "High";
                        i.putExtra("button", buttonNumber);
                        i.putExtra("quality", quality);
                    } else if (item == 1) {
                        quality = "Low";
                        i.putExtra("button", buttonNumber);
                        i.putExtra("quality", quality);
                    }
                    startService(i);
                    mIsRecording = true;
                    findViewById(R.id.btn1).setEnabled(false);
                    findViewById(R.id.btn2).setEnabled(false);
                    findViewById(R.id.btnAudiostart).setEnabled(false);
                    findViewById(R.id.btn0).setEnabled(true);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
        }
        if (v.getId() == R.id.btn2) {
            buttonNumber = 2;
            i.putExtra("button", buttonNumber);
            startService(i);
            mIsRecording = true;
            findViewById(R.id.btn1).setEnabled(false);
            findViewById(R.id.btn2).setEnabled(false);
            findViewById(R.id.btnAudiostart).setEnabled(false);
            findViewById(R.id.btn0).setEnabled(true);
            Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.btn0) {
            Toast.makeText(this, "Stoped", Toast.LENGTH_SHORT).show();
            buttonNumber = 0;
            mIsRecording = false;
            if (!mIsRecording) {
                findViewById(R.id.btn2).setEnabled(true);
                findViewById(R.id.btn1).setEnabled(true);
                findViewById(R.id.btnAudiostart).setEnabled(true);
            }
            findViewById(R.id.btn0).setEnabled(false);
            // Intent i = new Intent(this, BackGroundVideoRecorder.class);
            stopService(i);
        } else if (v.getId() == R.id.btnAudiostart) {
            buttonNumber = 3;
            i.putExtra("button", buttonNumber);
            mIsRecording = true;
            findViewById(R.id.btnAudioStop).setEnabled(true);
            findViewById(R.id.btnAudiostart).setEnabled(false);
            findViewById(R.id.btn1).setEnabled(false);
            findViewById(R.id.btn2).setEnabled(false);
            startService(i);
        } else if (v.getId() == R.id.btnAudioStop) {
            mIsRecording = false;
            if (!mIsRecording) {
                findViewById(R.id.btnAudiostart).setEnabled(true);
                findViewById(R.id.btn1).setEnabled(true);
                findViewById(R.id.btn2).setEnabled(true);
            }
            findViewById(R.id.btnAudioStop).setEnabled(false);
            stopService(i);
        } else if (v.getId() == R.id.btnRear) {
            startImageCapture();

        } else if (v.getId() == R.id.btnSave) {

            imgPreview.setVisibility(View.VISIBLE);

            if (App._file != null) {
                imgUri = Uri.fromFile(App._file);
                if (!App._file.exists()) {
                    Toast.makeText(this, "You have not captured picture yet",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else
                    Log.d(TAG, "imgUri = suprem");
                LongOperation lng = new LongOperation();
                lng.execute("");
            } else {
                Log.d(TAG, "App._file is null else");

                Toast.makeText(this, "You have not captured picture yet",
                        Toast.LENGTH_SHORT).show();
            }
            findViewById(R.id.btnSave).setEnabled(false);

        } else if (v.getId() == R.id.helpbutton) {
            LayoutInflater inflater = (LayoutInflater) getBaseContext()
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup, null);
            final PopupWindow popup = new PopupWindow(popupView,
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            popup.setFocusable(true);
            popup.setOutsideTouchable(true);
            popup.setBackgroundDrawable(new BitmapDrawable());
            popup.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {
                    // TODO Auto-generated method stub
                    findViewById(R.id.helpbutton).setEnabled(true);
                }
            });
            Button disButton = (Button) popupView.findViewById(R.id.dismiss);
            disButton.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    findViewById(R.id.helpbutton).setEnabled(true);
                    popup.dismiss();
                }
            });
            popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
            findViewById(R.id.helpbutton).setEnabled(false);
        } else if (v.getId() == R.id.tostorage) {
            final Uri selectedUri = Uri.fromFile(new File(Environment
                    .getExternalStoragePublicDirectory(File.separator),
                    "SpyCamStore"));
            final Intent intent = new Intent(Intent.ACTION_VIEW);

            final CharSequence[] list = {"Video Recordings",
                    "Audio Recordings", "Pictures"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select content type");
            builder.setItems(list, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0) {
                        intent.setDataAndType(selectedUri, "video/*");
                    } else if (item == 1) {
                        intent.setDataAndType(selectedUri, "audio/*");
                    } else if (item == 2) {
                        intent.setDataAndType(selectedUri, "image/*");
                    }
                    startActivity(intent);

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private class LongOperation extends AsyncTask<String, Void, String> {
        Bitmap rotatedScaledToyImage;
        ProgressDialog prgrs = new ProgressDialog(MainActivity.this);

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground ....");
            Bitmap bitmap = BitmapFactory.decodeFile(imgUri.getPath());
            // Convert it to byte
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Compress image to lower quality scale 1 - 100
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] image = stream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            Bitmap toyImageScaled = Bitmap.createScaledBitmap(bitmap, 100, 200
                    * bitmap.getHeight() / bitmap.getWidth(), false);
            // Override Android default landscape orientation and save portrait
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            rotatedScaledToyImage = Bitmap.createBitmap(toyImageScaled, 0, 0,
                    toyImageScaled.getWidth(), toyImageScaled.getHeight(),
                    matrix, true);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute ....");
            prgrs.dismiss();
            imgPreview.setImageBitmap(rotatedScaledToyImage);
        }

        @Override
        protected void onPreExecute() {
            prgrs = ProgressDialog.show(MainActivity.this, "Image Loading", "Please wait while image loading");
            Log.d(TAG, "onPreExecute ....");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.d(TAG, "onPreProgress ....");

        }
    }

    private File openFileForImage() {
        File imageDirectory = null;
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            imageDirectory = new File(
                    Environment
                            .getExternalStoragePublicDirectory(File.separator),
                    "SpyCamStore");
            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                imageDirectory = null;
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "yyyy_mm_dd_hh_mm", Locale.getDefault());

                return new File(imageDirectory.getPath() + File.separator
                        + "image_" + dateFormat.format(new Date()) + ".jpeg");
            }
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        findViewById(R.id.btnSave).setEnabled(true);
        Log.d(TAG, "currentRingerMode = " + userRingerMode);
        if (App._file.exists())
            Toast.makeText(this, "image saved to " + App._file,
                    Toast.LENGTH_SHORT).show();
        audio.setRingerMode(userRingerMode);
    }

    private void startImageCapture() {
        // startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
        // TAKE_PICTURE_REQUEST_B);
        // startActivityForResult(new Intent(MainActivity.this,
        // CameraActivity.class), TAKE_PICTURE_REQUEST_B);
        takeAPicture();
    }

    private void takeAPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        App._file = openFileForImage();// new File(App._dir,
        // String.Format("myPhoto_{0}.jpg",
        // Guid.NewGuid()));

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(App._file));
        audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);

        startActivityForResult(intent, 0);
    }

    private int getUserRingerMode() {

        userRingerMode = audio.getRingerMode();
        return userRingerMode;
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME,
                MODE_PRIVATE).edit();
        editor.putBoolean(KEY_IS_RECORDING, mIsRecording);
        editor.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class App {
        public static File _file;
        public static File _dir;
        public static Bitmap bitmap;
    }
}
