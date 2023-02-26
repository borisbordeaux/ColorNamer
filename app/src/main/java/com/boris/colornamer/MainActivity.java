package com.boris.colornamer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;

import com.boris.colornamer.Locale.LocaleHelper;
import com.boris.colornamer.analyzer.ImageAnalyzer;
import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.OpenCVLoader;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch mTorchSwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch mLocaleSwitch;
    private Camera mCamera;
    private ImageAnalyzer mAnalyzer;

    private final static String TAG = "MainActivity";
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    //load opencv statically
    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV is configured or connected successfully");
        } else {
            Log.e(TAG, "OpenCV not working or loaded");
        }
    }

    /**
     * Called at the opening of the app
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //continue initialization if all permissions granted
        if (allPermissionsGranted()) {
            init();
        } else {
            //else request permissions and when permission request
            //ended, onRequestPermissionsResult will be called
            int REQUEST_CODE_PERMISSIONS = 101;
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param requestCode  The request code passed in {@link androidx.core.app.ActivityCompat#requestPermissions(
     *android.app.Activity, String[], int)}
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *                     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allowed = true;
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allowed = false;
            }
        }
        if (allowed) {
            init();
        } else {
            finish();
        }
    }

    private void init() {
        ImageView mPreviewView = findViewById(R.id.previewView);
        mTorchSwitch = findViewById(R.id.torchSwitch);
        mLocaleSwitch = findViewById(R.id.localeSwitch);
        TextView mTextViewColor = findViewById(R.id.textColor);
        TextView mTextViewRGB = findViewById(R.id.textRGB);
        TextView mTextViewHSV = findViewById(R.id.textHSV);

        mAnalyzer = new ImageAnalyzer(mPreviewView, mTextViewColor, mTextViewRGB, mTextViewHSV, getBaseContext());

        setTitle(getString(R.string.app_title));

        mLocaleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Context c = LocaleHelper.setLocale(getBaseContext(), isChecked ? "en" : "fr");
            mAnalyzer.setContext(c);
            mTorchSwitch.setText(c.getString(R.string.torch));
            mLocaleSwitch.setText(c.getString(R.string.language));
            setTitle(c.getString(R.string.app_title));
        });

        startCamera();
    }

    private void startCamera() {
        //create the potential (future) camera provider
        //it will contains the camera provider when the
        //camera will be available
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        //define what to do when the camera provider will be available
        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Choose the camera by requiring a lens facing
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

                //Images are processed by passing an executor in which the image analysis is run
                ImageAnalysis.Builder builder = new ImageAnalysis.Builder();

                //build the imageAnalysis
                ImageAnalysis imageAnalysis = builder
                        //set the resolution of the view
                        .setTargetResolution(new android.util.Size(1000, 1000))
                        //set image format
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        //set rotation
                        .setOutputImageRotationEnabled(true)
                        //the executor receives the last available frame from the camera at the time that the analyze() method is called
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                //sets the analyzer
                imageAnalysis.setAnalyzer(getMainExecutor(), mAnalyzer);

                // Attach use cases to the camera with the same lifecycle owner
                mCamera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);

                mTorchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mCamera.getCameraControl().enableTorch(isChecked));

            } catch (InterruptedException | ExecutionException e) {
                // Currently no exceptions thrown. cameraProviderFuture.get() should
                // not block since the listener is being called, so no need to
                // handle InterruptedException.
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

}