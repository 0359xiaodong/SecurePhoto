package eu.tpmusielak.securephoto.camera;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import eu.tpmusielak.securephoto.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 21:33
 */
public class CameraHandler {
    public static final String FLASH_MODE_PREF = "FLASH_MODE";
    public static final String WHITE_BALANCE_PREF = "WHITE_BALANCE";
    public static final String IMG_WIDTH_PREF = "IMG_WIDTH";
    public static final String IMG_HEIGHT_PREF = "IMG_HEIGHT";

    private final int PICTURE_SIZE = 0;
    private final int FLASH_MODE = 1;
    private final int WHITE_BALANCE = 2;

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
    private SharedPreferences preferences;

    // Camera parameters:
    private String flashMode;
    private String wbMode;
    private int imgWidth;
    private int imgHeight;

    public CameraHandler(CameraReceiver r) {
        receiver = r;
        pictureCallback = new mPictureCallback();

        preferences = r.getContext().getSharedPreferences("CAMERA_PREFERENCES", Context.MODE_WORLD_READABLE);
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

    public void showCameraSettings() {
        Camera.Parameters parameters = camera.getParameters();

        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        List<String> flashModes = parameters.getSupportedFlashModes();
        List<String> whiteBalanceModes = parameters.getSupportedWhiteBalance();


        AlertDialog.Builder builder = new AlertDialog.Builder(receiver.getContext());
        builder.setCancelable(true);
        builder.setItems(R.array.camera_parameters, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                displayParameterSetting(i);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void displayParameterSetting(int setting) {
        Camera.Parameters parameters = camera.getParameters();
        List options = null;
        List<String> optionNames = new LinkedList<String>();

        switch (setting) {
            case PICTURE_SIZE:
                options = parameters.getSupportedPictureSizes();
                break;
            case FLASH_MODE:
                options = parameters.getSupportedFlashModes();
                break;
            case WHITE_BALANCE:
                options = parameters.getSupportedWhiteBalance();
                break;
        }

        if (options == null) {
            return; // Nothing to do
        }

        for (Object option : options) {
            if (option instanceof String) {
                String o = (String) option;
                optionNames.add(o);
            } else if (option instanceof Camera.Size) {
                Camera.Size size = (Camera.Size) option;
                optionNames.add(String.format("(%dx%d)", size.width, size.height));
            } else {
                return;
            }
        }

        String[] optionNamesArray = new String[optionNames.size()];
        optionNames.toArray(optionNamesArray);

        AlertDialog.Builder builder = new AlertDialog.Builder(receiver.getContext());
        builder.setCancelable(true);

        builder.setItems(optionNamesArray, new CameraSettingListener(setting, options));

        AlertDialog alert = builder.create();
        alert.show();

    }

    private class CameraSettingListener implements DialogInterface.OnClickListener {
        private final int setting;
        private final List options;

        private CameraSettingListener(int setting, List options) {
            this.setting = setting;
            this.options = options;
        }


        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Camera.Parameters parameters = camera.getParameters();
            Object option = options.get(i);


            if (setting == PICTURE_SIZE) {
                Camera.Size size = (Camera.Size) option;
                parameters.setPictureSize(size.width, size.height);
                imgWidth = size.width;
                imgHeight = size.height;

            } else if (setting == FLASH_MODE) {
                flashMode = (String) option;
                parameters.setFlashMode(flashMode);

            } else if (setting == WHITE_BALANCE) {
                wbMode = (String) option;
                parameters.setWhiteBalance(wbMode);

            } else {
            }

            saveCameraParameters();
            camera.setParameters(parameters);
        }
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

        restoreCameraParameters();
    }

    private void restoreCameraParameters() {
        Camera.Parameters parameters = camera.getParameters();

        flashMode = preferences.getString(FLASH_MODE_PREF, null);
        wbMode = preferences.getString(WHITE_BALANCE_PREF, null);
        imgWidth = preferences.getInt(IMG_WIDTH_PREF, -1);
        imgHeight = preferences.getInt(IMG_HEIGHT_PREF, -1);

        if (flashMode != null)
            parameters.setFlashMode(flashMode);
        if (wbMode != null)
            parameters.setWhiteBalance(wbMode);
        if ((imgWidth > 0) && (imgHeight > 0)) {
            parameters.setPictureSize(imgWidth, imgHeight);
        }

        camera.setParameters(parameters);
    }

    private void saveCameraParameters() {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(FLASH_MODE_PREF, flashMode);
        editor.putString(WHITE_BALANCE_PREF, wbMode);
        editor.putInt(IMG_WIDTH_PREF, imgWidth);
        editor.putInt(IMG_HEIGHT_PREF, imgHeight);

        editor.commit();
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
