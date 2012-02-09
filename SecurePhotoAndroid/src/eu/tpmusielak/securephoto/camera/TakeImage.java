package eu.tpmusielak.securephoto.camera;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.*;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import eu.tpmusielak.securephoto.FileHandling;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.container.SPImage;
import eu.tpmusielak.securephoto.communication.CommunicationService;
import eu.tpmusielak.securephoto.communication.CommunicationService.CommServiceBinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 27.11.11
 * Time: 14:01
 * To change this template use File | Settings | File Templates.
 */
public class TakeImage extends Activity {
    private FrameLayout cameraPreviewFrame = null;
    private CameraPreview cameraPreview = null;
    private int cameraCount = 0;
    private int defaultCameraId = 0;
    private Camera camera;
    private int usedCamera;

    private Button shutterButton;

    private PictureCallback pictureCallback;

    private String cameraFocusMode;

    private LocationProvider locationProvider;

    private CommunicationService communicationService;
    private boolean boundToCommService = false;

    private ServiceConnection communicationServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            CommServiceBinder binder = (CommServiceBinder) iBinder;
            communicationService = binder.getService();
            boundToCommService = true;
        }

        public void onServiceDisconnected(ComponentName componentName) {
            boundToCommService = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupScreen();
        setupCamera();

        Intent intent = new Intent(this, CommunicationService.class);
        bindService(intent, communicationServiceConnection, Context.BIND_AUTO_CREATE);

        locationProvider = new LocationProvider();
    }

    private void setupScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.camera_view);

        cameraPreviewFrame = (FrameLayout) findViewById(R.id.preview);

        pictureCallback = new mPictureCallback();

        shutterButton = (Button) findViewById(R.id.btn_shutter);
        shutterButton.setOnClickListener(new ShutterListener());
    }

    private class ShutterListener implements View.OnClickListener {
        public void onClick(View view) {
            Parameters camParameters = camera.getParameters();
            camParameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
            camera.setParameters(camParameters);

            if (doAF()) {
                camera.autoFocus(new AFCallback(true));
            } else {
                takePicture();
            }

        }
    }

    private class mPictureCallback implements PictureCallback {

        public void onPictureTaken(byte[] bytes, Camera camera) {
            new SavePictureTask().execute(new byte[][]{bytes});
            camera.startPreview();
        }
    }

    private class AFCallback implements Camera.AutoFocusCallback {
        private boolean takePicture;

        private AFCallback(boolean takePicture) {
            this.takePicture = takePicture;
        }

        public void onAutoFocus(boolean success, Camera camera) {
            if (takePicture) {
                takePicture();
            }
        }
    }

    private boolean doAF() {
        return cameraFocusMode.equals(Parameters.FOCUS_MODE_AUTO) ||
                cameraFocusMode.equals(Parameters.FOCUS_MODE_MACRO);
    }

    private void takePicture() {
        camera.takePicture(null, null, null, pictureCallback);
    }

    private class SavePictureTask extends AsyncTask<byte[], Void, String> {

        @Override
        protected String doInBackground(byte[]... bytes) {
            File pictureFile = null;
            byte[] pictureData = bytes[0];

            try {
                pictureFile = FileHandling.getOutputFile(SPImage.defaultExtension);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
                SPImage image = new SPImage(pictureData);
                fileOutputStream.write(image.toByteArray());
                notifyBaseStation(image);
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return pictureFile.getName();
        }

        private void notifyBaseStation(SPImage image) {
            communicationService.sendImageNotification(image.getImageDigest());
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(TakeImage.this, "File " + result + " saved successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCamera() {
        cameraCount = Camera.getNumberOfCameras();


        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);

            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        camera = Camera.open();
        cameraPreview = new CameraPreview(this);

        cameraFocusMode = camera.getParameters().getFocusMode();

        cameraPreviewFrame.removeAllViews();
        cameraPreviewFrame.addView(cameraPreview);

        cameraPreview.setCamera(camera);
    }

    @Override
    protected void onPause() {
        cameraPreview.setCamera(null);
        camera.release();

        if (boundToCommService) {
            unbindService(communicationServiceConnection);
            boundToCommService = false;
        }

        super.onPause();
    }

    /**
     * Taken from Android API Examples
     * <p/>
     * (C) Google
     * <p/>
     * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
     * to the surface. We need to center the SurfaceView because not all devices have cameras that
     * support preview sizes at the same aspect ratio as the device's display.
     */
    private class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
        private final String TAG = "Preview";

        SurfaceView mSurfaceView;
        SurfaceHolder mHolder;
        Size mPreviewSize;
        List<Size> mSupportedPreviewSizes;
        Camera mCamera;

        CameraPreview(Context context) {
            super(context);

            mSurfaceView = new SurfaceView(context);
            addView(mSurfaceView);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void setCamera(Camera camera) {
            mCamera = camera;
            if (mCamera != null) {
                mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                requestLayout();
            }
        }

        public void switchCamera(Camera camera) {
            setCamera(camera);
            try {
                camera.setPreviewDisplay(mHolder);
            } catch (IOException exception) {
                //
            }
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();

            camera.setParameters(parameters);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            camera.autoFocus(new AFCallback(false));
            return true;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // We purposely disregard child measurements because act as a
            // wrapper to a SurfaceView that centers the camera preview instead
            // of stretching it.
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (changed && getChildCount() > 0) {
                final View child = getChildAt(0);

                final int width = r - l;
                final int height = b - t;

                int previewWidth = width;
                int previewHeight = height;
                if (mPreviewSize != null) {
                    previewWidth = mPreviewSize.width;
                    previewHeight = mPreviewSize.height;
                }

                // Center the child SurfaceView within the parent.
                if (width * previewHeight > height * previewWidth) {
                    final int scaledChildWidth = previewWidth * height / previewHeight;
                    child.layout((width - scaledChildWidth) / 2, 0,
                            (width + scaledChildWidth) / 2, height);
                } else {
                    final int scaledChildHeight = previewHeight * width / previewWidth;
                    child.layout(0, (height - scaledChildHeight) / 2,
                            width, (height + scaledChildHeight) / 2);
                }
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, acquire the camera and tell it where
            // to draw.
            try {
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(holder);
                }
            } catch (IOException exception) {
                //
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }


        private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) w / h;
            if (sizes == null) return null;

            Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            // Try to find an size match aspect ratio and size
            for (Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }

            // Cannot find the one match the aspect ratio, ignore the requirement
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Size size : sizes) {
                    if (Math.abs(size.height - h) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - h);
                    }
                }
            }
            return optimalSize;
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // Now that the size is known, set up the camera parameters and begin
            // the preview.
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();

            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
    }
}

