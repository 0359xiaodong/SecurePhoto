package eu.tpmusielak.securephoto.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.*;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.communication.CommunicationService;
import eu.tpmusielak.securephoto.communication.CommunicationService.CommServiceBinder;
import eu.tpmusielak.securephoto.container.SPFileHandler;
import eu.tpmusielak.securephoto.container.SPImageHandler;
import eu.tpmusielak.securephoto.container.SPImageRollHandler;
import eu.tpmusielak.securephoto.verification.Verifier;
import eu.tpmusielak.securephoto.verification.VerifierGUIReceiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 27.11.11
 * Time: 14:01
 */
public class TakeImage extends Activity implements VerifierGUIReceiver {
    private FrameLayout cameraPreviewFrame = null;
    private CameraPreview cameraPreview = null;
    private int cameraCount = 0;
    private int defaultCameraId = 0;
    private Camera camera;
    private int usedCamera;

    private Button shutterButton;
    private Button saveModeButton;
    private Button verifierSettingsButton;

    private ProgressBar backgroundOperationBar;
    private AtomicInteger backgroundOpsCounter;

    private TextView baseStationDisplay;

    private PictureCallback pictureCallback;

    private String cameraFocusMode;

    private LocationProvider locationProvider;

    private CommunicationService communicationService;
    private boolean boundToCommService = false;

    private SCVerifierManager SCVerifierManager;

    private List<Verifier> verifiers;

    private Map<SaveMode, SPFileHandler> fileHandlers;
    private SPFileHandler fileHandler;
    private SaveMode saveMode;

    private TimeUpdateTask timeUpdateTask;

    SharedPreferences preferences;

    ViewGroup optionsPane;
    ViewGroup pluginsPane;

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


    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        backgroundOpsCounter = new AtomicInteger(0);

        saveMode = SaveMode.IMAGE_ROLL;

        setupScreen();
        setupCamera();

        Intent intent = new Intent(this, CommunicationService.class);
        bindService(intent, communicationServiceConnection, Context.BIND_AUTO_CREATE);

        locationProvider = new LocationProvider();

        SCVerifierManager = new SCVerifierManager(this);
        verifiers = SCVerifierManager.getVerifiers();


//        String tsaAddress = preferences.getString(
//                getResources().getString(R.string.kpref_TSA_address), "");

//        verifiers.add(new RFC3161Timestamp(tsaAddress));

        fileHandlers = new HashMap<SaveMode, SPFileHandler>();
        fileHandlers.put(SaveMode.SINGLE_IMAGE, new SPImageHandler(verifiers));
        fileHandlers.put(SaveMode.IMAGE_ROLL, new SPImageRollHandler(verifiers));

        fileHandler = fileHandlers.get(saveMode);
    }

    @SuppressWarnings(value = "unchecked")
    private void setupScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.camera_view);

        cameraPreviewFrame = (FrameLayout) findViewById(R.id.preview);

        pictureCallback = new mPictureCallback();

        shutterButton = (Button) findViewById(R.id.btn_shutter);
        shutterButton.setOnClickListener(new ShutterListener());

        saveModeButton = (Button) findViewById(R.id.btn_save_mode);
        saveModeButton.setOnClickListener(new SaveModeListener(saveModeButton));

        verifierSettingsButton = (Button) findViewById(R.id.btn_verifier_settings);
        verifierSettingsButton.setOnClickListener(new VerifierSettingsListener());

        optionsPane = (ViewGroup) findViewById(R.id.options_pane);
        pluginsPane = (ViewGroup) findViewById(R.id.plugins_pane);

        timeUpdateTask = new TimeUpdateTask(this);
        timeUpdateTask.execute();

        String baseStationAddress = preferences.getString(getResources().getString(R.string.kpref_base_station_address), "");

        baseStationDisplay = (TextView) findViewById(R.id.camera_base);
        baseStationDisplay.setText(baseStationAddress);

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

    private class SaveModeListener implements Button.OnClickListener {
        public SaveModeListener(Button saveModeButton) {
            saveModeButton.setBackgroundResource(saveMode.getDrawableResID());
        }

        @Override
        public void onClick(View view) {
            saveMode = saveMode.switchMode();
            fileHandler = fileHandlers.get(saveMode);
            ((Button) view).setBackgroundResource(saveMode.getDrawableResID());
        }
    }

    private class VerifierSettingsListener implements Button.OnClickListener {
        public void onClick(View view) {
            SCVerifierManager.showVerificationFactors();
        }
    }

    private class mPictureCallback implements PictureCallback {

        public void onPictureTaken(byte[] bytes, Camera camera) {
            new SavePictureTask().execute(new byte[][]{bytes});
            camera.startPreview();
            shutterButton.setEnabled(true);
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
        shutterButton.setEnabled(false);
        try {
            camera.takePicture(null, null, null, pictureCallback);
        } catch (RuntimeException ignore) {
            /* If the shutter is pressed too often, the shutter button doesn't get disabled
               and camera.takePicture fails */
            shutterButton.setEnabled(true);
        }
    }

    private class SavePictureTask extends AsyncTask<byte[], Void, String> {
        ProgressBar progressBar;

        @Override
        protected void onPreExecute() {
            startBackgroundOperation();
        }

        @Override
        protected String doInBackground(byte[]... bytes) {
            byte[] pictureData = bytes[0];
            return fileHandler.saveFile(pictureData);
        }

        @Override
        protected void onPostExecute(String result) {
            endBackgroundOperation();
            if (result != null)
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

        timeUpdateTask.cancel(true);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.camera_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.v_factors:
                SCVerifierManager.showVerificationFactors();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startBackgroundOperation() {
        backgroundOperationBar = (ProgressBar) findViewById(R.id.camera_save_progress);
        backgroundOperationBar.isIndeterminate();
        backgroundOperationBar.setVisibility(View.VISIBLE);
        backgroundOpsCounter.incrementAndGet();
    }

    public void endBackgroundOperation() {
        if (backgroundOpsCounter.decrementAndGet() == 0)
            backgroundOperationBar.setVisibility(View.GONE);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return super.onCreateDialog(id);
    }

    @SuppressWarnings("unchecked")
    void updateTime() {
        timeUpdateTask = new TimeUpdateTask(this);
        timeUpdateTask.execute();
    }

}

