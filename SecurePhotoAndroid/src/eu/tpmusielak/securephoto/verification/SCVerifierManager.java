package eu.tpmusielak.securephoto.verification;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.preference.*;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.verification.geo.CellularVerifierWrapper;
import eu.tpmusielak.securephoto.verification.geo.GeolocationVerifierWrapper;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 02:38
 */
public class SCVerifierManager extends Service implements VerifierBinder {

    private VerifierGUIReceiver guiReceiver;
    private VerifierPreferenceReceiver preferenceReceiver;

    protected List<VerifierWrapper> verifierWrappers;

    public static SCVerifierManager instance;

    private void initialize() {
        verifierWrappers = new LinkedList<VerifierWrapper>();

        new GeolocationVerifierWrapper().register(this);
        new CellularVerifierWrapper().register(this);
//        new TimestampVerifierWrapper().register(this);
//        new GenericVerifierWrapper(new DummyVerifier()).register(this);
//        new GenericVerifierWrapper(new DummyVerifier(2000)).register(this);
//        new GenericVerifierWrapper(new DummyVerifier(30000)).register(this);
    }

    public class VerifierServiceBinder extends Binder {
        private WeakReference<SCVerifierManager> managerReference;

        public VerifierServiceBinder(SCVerifierManager verifierManager) {
            managerReference = new WeakReference<SCVerifierManager>(verifierManager);
        }

        public SCVerifierManager getService() {
            return managerReference.get();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
        initializeVWrappers();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new VerifierServiceBinder(this);
    }

    @Override
    public void onDestroy() {
        tearDownVWrappers();
        super.onDestroy();
    }

    public void register(VerifierWrapper wrapper) {
        verifierWrappers.add(wrapper);
    }

    public List<Verifier> getVerifiers() {
        List<Verifier> verifiers = new LinkedList<Verifier>();

        for (VerifierWrapper verifierWrapper : verifierWrappers) {
            verifiers.add(verifierWrapper.getVerifier());
        }

        return verifiers;
    }


    public void initializeVWrappers() {
        for (VerifierWrapper verifierWrapper : verifierWrappers) {
            verifierWrapper.initializeWrapper();
        }
    }

    public void tearDownVWrappers() {
        for (VerifierWrapper verifierWrapper : verifierWrappers) {
            verifierWrapper.onDestroy();
        }
    }

    public void bindToGUI(VerifierGUIReceiver r) {
        this.guiReceiver = r;

        for (VerifierWrapper verifierWrapper : verifierWrappers) {
            verifierWrapper.setReceiver(guiReceiver);
            verifierWrapper.bindToGUI();
            verifierWrapper.onCameraStart();
        }
    }

    public void unbindFromGUI() {
        for (VerifierWrapper verifierWrapper : verifierWrappers) {
            verifierWrapper.onCameraExit();
        }
    }

    public void bindToPreferences(VerifierPreferenceReceiver r) {
        this.preferenceReceiver = r;
    }

    public void generateVerifierEnablers() {
        PreferenceActivity preferenceActivity = preferenceReceiver.getPreferenceActivity();
        String title = preferenceActivity.getResources().getString(R.string.pick_vfactor);

        PreferenceScreen ps = preferenceReceiver.getPreferenceScreen();
        PreferenceCategory preferenceCategory =
                new PreferenceCategory(preferenceActivity);
        preferenceCategory.setTitle(title);

        ps.addPreference(preferenceCategory);

        for (VerifierWrapper verifierWrapper : verifierWrappers) {
            CheckBoxPreference preference = new CheckBoxPreference(preferenceActivity);
            preference.setKey(verifierWrapper.getName());
            preference.setTitle(verifierWrapper.getName());
            preference.setChecked(verifierWrapper.isEnabled());
            preference.setOnPreferenceChangeListener(new VerifierEnableListener(verifierWrapper));

            preferenceCategory.addPreference(preference);
        }

    }

    public void loadVerifierPreferences() {
        Set<Integer> addedPreferences = new HashSet<Integer>();

        for (VerifierWrapper verifierWrapper : verifierWrappers) {
            int id = verifierWrapper.getPreferenceID();

            if (id > 0 && !addedPreferences.contains(id)) {
                preferenceReceiver.addPreferencesFromResource(id);
                addedPreferences.add(id);
            }
        }
    }

    private boolean isBoundToGUI() {
        return guiReceiver != null;
    }

    public void showVerificationFactors() {
        if (!isBoundToGUI())
            return;

        List<CharSequence> verificationFactorNames = new LinkedList<CharSequence>();
        boolean[] enabledV = new boolean[verifierWrappers.size()];
        int i = 0;

        for (VerifierWrapper v : verifierWrappers) {
            verificationFactorNames.add(v.getName());
            enabledV[i++] = v.isEnabled();
        }

        CharSequence[] items = new CharSequence[verificationFactorNames.size()];

        verificationFactorNames.toArray(items);

        AlertDialog.Builder builder = new AlertDialog.Builder(guiReceiver.getContext());
        builder.setTitle(R.string.pick_vfactor);
        builder.setCancelable(true);
        builder.setMultiChoiceItems(items, enabledV, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                verifierWrappers.get(i).setEnabled(b);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    private class VerifierEnableListener implements Preference.OnPreferenceChangeListener {
        VerifierWrapper wrapper;

        public VerifierEnableListener(VerifierWrapper wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            wrapper.setEnabled((Boolean) newValue);
            return true;
        }
    }

}
