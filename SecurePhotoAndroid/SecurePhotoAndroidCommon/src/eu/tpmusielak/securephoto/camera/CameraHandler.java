package eu.tpmusielak.securephoto.camera;

import android.hardware.Camera;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 21:33
 */
public class CameraHandler {
    private FrameLayout cameraPreviewFrame = null;
    private CameraPreview cameraPreview = null;
    private int cameraCount = 0;
    private int defaultCameraId = 0;
    private Camera camera;
    private int usedCamera;
    private String cameraFocusMode;

    private Camera.PictureCallback pictureCallback;
    private Button shutter;

    private CameraReceiver receiver;

    public CameraHandler(CameraReceiver r) {
        receiver = r;
        pictureCallback = new mPictureCallback();
    }

    public void setCameraPreviewFrame(FrameLayout frame) {
        this.cameraPreviewFrame = frame;
    }

    public void setupCamera() {
        cameraCount = Camera.getNumberOfCameras();

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
            }
        }
    }

    private void takePicture() {
        shutter.setEnabled(false);
        try {
            camera.takePicture(null, null, null, pictureCallback);
        } catch (RuntimeException ignore) {
            /* If the shutter is pressed too often, the shutter button doesn't get disabled
         and camera.takePicture fails */
            shutter.setEnabled(true);
        }
    }

    public void registerShutterButton(Button shutterButton) {
        shutter = shutterButton;
        shutter.setOnClickListener(new ShutterListener());
    }


    private class mPictureCallback implements Camera.PictureCallback {

        public void onPictureTaken(byte[] bytes, Camera camera) {
            receiver.savePicture(bytes);

            camera.startPreview();
            shutter.setEnabled(true);
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
        return cameraFocusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO) ||
                cameraFocusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO);
    }

    public void pauseCamera() {
        cameraPreview.setCamera(null);
        camera.release();
    }

    public void resumeCamera() {
        camera = Camera.open();
        cameraPreview = new CameraPreview(receiver.getContext());

        cameraFocusMode = camera.getParameters().getFocusMode();

        cameraPreviewFrame.removeAllViews();
        cameraPreviewFrame.addView(cameraPreview);

        cameraPreview.setCamera(camera);
    }

    private class ShutterListener implements View.OnClickListener {
        public void onClick(View view) {
            Camera.Parameters camParameters = camera.getParameters();
            camParameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            camera.setParameters(camParameters);

            if (doAF()) {
                camera.autoFocus(new AFCallback(true));
            } else {
                takePicture();
            }

        }
    }


}
