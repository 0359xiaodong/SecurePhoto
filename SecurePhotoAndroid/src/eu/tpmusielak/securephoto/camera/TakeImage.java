package eu.tpmusielak.securephoto.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.*;
import android.widget.*;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.communication.CommunicationService;
import eu.tpmusielak.securephoto.container.SPFileHandler;
import eu.tpmusielak.securephoto.container.SPImageHandler;
import eu.tpmusielak.securephoto.container.SPImageRollHandler;
import eu.tpmusielak.securephoto.container.VerifierProvider;
import eu.tpmusielak.securephoto.container.wrapper.SPFileWrapper;
import eu.tpmusielak.securephoto.verification.SCVerifierManager;
import eu.tpmusielak.securephoto.verification.Verifier;
import eu.tpmusielak.securephoto.verification.VerifierGUIReceiver;
import eu.tpmusielak.securephoto.viewer.OpenImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: enx
 * Date: 27.11.11
 * Time: 14:01
 */
public class TakeImage extends Activity implements VerifierGUIReceiver, CameraReceiver, VerifierProvider {

    private CameraHandler cameraHandler;

    private Button shutterButton;
    private Button saveModeButton;
    private Button cameraSettingsButton;
    private Button verifierSettingsButton;
    private Button reviewImageButton;

    private ProgressBar backgroundOperationBar;
    private AtomicInteger backgroundOpsCounter;

    private TextView baseStationDisplay;


    private CommunicationService communicationService;
    private SCVerifierManager verifierManager;

    private List<Verifier> verifiers;

    private Map<SaveMode, SPFileHandler> fileHandlers;
    private SPFileHandler fileHandler;
    private SaveMode saveMode;

    private Timer timeUpdateTimer;

    private SharedPreferences preferences;

    private ViewGroup optionsPane;
    private ViewGroup pluginsPane;

    private SPFileWrapper lastImageWrapper;

    private ServiceConnection communicationServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            communicationService = ((CommunicationService.CommuncationServiceBinder) iBinder).getService();
        }

        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    private ServiceConnection verifierServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            verifierManager = ((SCVerifierManager.VerifierServiceBinder) iBinder).getService();
            verifierManager.bindToGUI(TakeImage.this);
            verifiers = verifierManager.getVerifiers();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };


    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraHandler = new CameraHandler(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        backgroundOpsCounter = new AtomicInteger(0);

        //TODO: replace with user-definable default mode
        saveMode = SaveMode.SINGLE_IMAGE;

        setupScreen();
        cameraHandler.setupCamera();

        Intent intent = new Intent(this, CommunicationService.class);
        bindService(intent, communicationServiceConnection, Context.BIND_AUTO_CREATE);

        Intent verifierServiceIntent = new Intent(this, SCVerifierManager.class);
        bindService(verifierServiceIntent, verifierServiceConnection, Context.BIND_AUTO_CREATE);

        fileHandlers = new HashMap<SaveMode, SPFileHandler>();
        fileHandlers.put(SaveMode.SINGLE_IMAGE, new SPImageHandler(this));
        fileHandlers.put(SaveMode.IMAGE_ROLL, new SPImageRollHandler(this));

        fileHandler = fileHandlers.get(saveMode);
    }

    @SuppressWarnings(value = "unchecked")
    private void setupScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.camera_view);

        cameraHandler.setCameraPreviewFrame((FrameLayout) findViewById(R.id.preview));

        shutterButton = (Button) findViewById(R.id.btn_shutter);
        cameraHandler.registerShutterButton(shutterButton);

        saveModeButton = (Button) findViewById(R.id.btn_save_mode);
        saveModeButton.setOnClickListener(new SaveModeListener(saveModeButton));

        verifierSettingsButton = (Button) findViewById(R.id.btn_verifier_settings);
        verifierSettingsButton.setOnClickListener(new VerifierSettingsListener());

        cameraSettingsButton = (Button) findViewById(R.id.btn_camera_settings);
        cameraSettingsButton.setOnClickListener(new CameraSettingsListener());

        reviewImageButton = (Button) findViewById(R.id.btn_review_image);
        reviewImageButton.setOnClickListener(new ReviewImageListener());

        optionsPane = (ViewGroup) findViewById(R.id.options_pane);
        pluginsPane = (ViewGroup) findViewById(R.id.plugins_pane);

        timeUpdateTimer = new Timer("TimeUpdateTimer");
        timeUpdateTimer.scheduleAtFixedRate(new TimeUpdateTask(this), 0, 1000);

        String baseStationAddress = preferences.getString("base_station_address", "");

        baseStationDisplay = (TextView) findViewById(R.id.camera_base);
        baseStationDisplay.setText(baseStationAddress);

        backgroundOperationBar = (ProgressBar) findViewById(R.id.camera_save_progress);
        backgroundOperationBar.isIndeterminate();
    }

    private void setFileHandler(SPFileHandler handler) {
        handler.onInitialize(TakeImage.this);
        fileHandler = handler;
    }

    private class SaveModeListener implements Button.OnClickListener {
        public SaveModeListener(Button saveModeButton) {
            saveModeButton.setBackgroundResource(saveMode.getDrawableResID());
        }

        @Override
        public void onClick(View view) {
            saveMode = saveMode.switchMode();
            setFileHandler(fileHandlers.get(saveMode));
            ((Button) view).setBackgroundResource(saveMode.getDrawableResID());
        }
    }

    private class VerifierSettingsListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            verifierManager.showVerificationFactors();
        }
    }

    private class CameraSettingsListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            cameraHandler.showCameraSettings();
        }
    }

    private class ReviewImageListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            viewImage(lastImageWrapper);
        }
    }

    private void viewImage(SPFileWrapper wrapper) {
        if (wrapper == null)
            return;

        Intent i = new Intent(getContext(), OpenImage.class);
        i.putExtra("filename", wrapper.getFile().getAbsolutePath());
        i.putExtra("filewrapper", wrapper);
        getContext().startActivity(i);
    }

    public Context getContext() {
        return this;
    }

    @Override
    public void savePicture(byte[] bytes) {
        new SavePictureTask().execute(new byte[][]{bytes});
    }

    private class SavePictureTask extends AsyncTask<byte[], Void, Pair<SPFileWrapper, String>> {
        byte[] pictureData;

        @Override
        protected void onPreExecute() {
            startBackgroundOperation();
        }

        @Override
        protected Pair<SPFileWrapper, String> doInBackground(byte[]... bytes) {
            pictureData = bytes[0];
            SPFileWrapper fileWrapper = null;
            String outcome = null;

            try {
                fileWrapper = fileHandler.saveFile(pictureData);
            } catch (NullPointerException e) {
                throw e;
            } catch (RuntimeException e) {
                outcome = e.toString();
            }
            return new Pair<SPFileWrapper, String>(fileWrapper, outcome);
        }

        @Override
        protected void onPostExecute(Pair<SPFileWrapper, String> result) {
            SPFileWrapper fileWrapper = result.first;
            String outcome = result.second;

            if (fileWrapper != null) {
                Toast.makeText(TakeImage.this, getString(R.string.image_saved_successfully, fileWrapper.getName()), Toast.LENGTH_SHORT).show();
                onImageSaved(fileWrapper);
            } else
                Toast.makeText(TakeImage.this, getString(R.string.error_saving_image, outcome), Toast.LENGTH_SHORT).show();

            endBackgroundOperation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraHandler.resumeCamera();
    }

    @Override
    protected void onPause() {
        cameraHandler.pauseCamera();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        verifierManager.unbindFromGUI();
        unbindService(verifierServiceConnection);
        unbindService(communicationServiceConnection);
        super.onDestroy();
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
                verifierManager.showVerificationFactors();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public ViewGroup getPluginsPane() {
        return pluginsPane;
    }

    public void startBackgroundOperation() {
        backgroundOperationBar.setVisibility(View.VISIBLE);
        backgroundOpsCounter.incrementAndGet();
    }

    public void endBackgroundOperation() {
        if (backgroundOpsCounter.decrementAndGet() <= 0) {
            backgroundOperationBar.setVisibility(View.GONE);
        }

    }

    public void onImageSaved(SPFileWrapper wrapper) {
        lastImageWrapper = wrapper;
        communicationService.notifyPictureTaken(wrapper);
        if (preferences.getBoolean("review_image", true)) {
            viewImage(lastImageWrapper);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return super.onCreateDialog(id);
    }

    @Override
    public List<Verifier> getVerifiers() {
        return verifiers;
    }


}

