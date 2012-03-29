package eu.tpmusielak.securephoto.verification;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.ViewGroup;
import android.widget.ImageView;
import eu.tpmusielak.securephoto.R;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 01:22
 */
public abstract class VerifierWrapper {
    public static final int RESOURCE_ID_OFFSET = 37;
    public static AtomicInteger resourceOffset = new AtomicInteger(0);


    private boolean enabled = true;
    protected boolean registered = false;

    public final int iconID = RESOURCE_ID_OFFSET + resourceOffset.incrementAndGet();
    protected Verifier verifier;

    private VerifierGUIReceiver guiReceiver;
    protected Context context;
    ViewGroup pluginsPane;

    protected VerifierBinder manager;

    InitializeVerifierTask initTask;

    public void register(VerifierBinder m) {
        manager = m;
        manager.register(this);
        registered = true;
    }

    public void setReceiver(VerifierGUIReceiver receiver) {
        this.guiReceiver = receiver;
    }

    @SuppressWarnings("unchecked")
    public void initialize() {
        initTask = new InitializeVerifierTask();
        initTask.execute();
    }

    public void bindToGUI() {
        this.context = guiReceiver.getBaseContext();
        pluginsPane = guiReceiver.getPluginsPane();
        putIcon();
        setStateColor();
    }

    public Verifier getVerifier() {
        return verifier;
    }

    public String getName() {
        return verifier.toString();
    }

    public Drawable getDrawable() {
        return context.getResources().getDrawable(R.drawable.ic_stat_padlock);
    }

    public int getPreferenceID() {
        return 0; // No default preferences defined.
    }


    public void setEnabled(boolean val) {
        enabled = val;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void putIcon() {
        guiReceiver.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = new ImageView(guiReceiver.getBaseContext());
                imageView.setImageDrawable(getDrawable());
                imageView.setId(iconID);
                pluginsPane.addView(imageView);
            }
        });

    }

    private void setIconColor(final int filter) {
        guiReceiver.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView) guiReceiver.findViewById(iconID))
                        .setColorFilter(filter, PorterDuff.Mode.MULTIPLY);
            }
        });

    }

    private boolean isBound() {
        return guiReceiver != null;
    }

    protected void setStateColor() {
        switch (verifier.getState()) {
            case UNINITIALIZED:
                setIconColor(Color.GRAY);
                break;
            case INITIALIZING:
                setIconColor(Color.YELLOW);
                break;
            case INIT_SUCCESS:
                setIconColor(Color.GREEN);
                break;
            case INIT_FAILURE:
                setIconColor(Color.RED);
                break;
        }
    }

    protected class InitializeVerifierTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            if (isBound()) {
                setIconColor(Color.YELLOW);
                guiReceiver.startBackgroundOperation();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            verifier.initialize();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isBound()) {
                guiReceiver.endBackgroundOperation();
                setStateColor();
            }
        }
    }

}
