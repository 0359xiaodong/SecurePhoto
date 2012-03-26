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
    protected boolean initialized = false;

    public final int iconID = RESOURCE_ID_OFFSET + resourceOffset.incrementAndGet();
    protected Verifier verifier;

    private VerifierGUIReceiver activity;
    protected Context context;
    ViewGroup pluginsPane;

    VerifierBinder manager;

    InitializeVerifierTask initTask;


    public void register(VerifierBinder m) {
        manager = m;
        m.register(this);
        putIcon();
        setIconColor(Color.GRAY);

        registered = true;
    }

    public void setActivity(VerifierGUIReceiver activity) {
        this.activity = activity;
        this.context = activity.getBaseContext();
    }

    public void setPluginsPane(ViewGroup vg) {
        pluginsPane = vg;
    }

    @SuppressWarnings("unchecked")
    public void initialize() {
        initTask = new InitializeVerifierTask();
        initTask.execute();
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


    private void putIcon() {
        putIcon(0);
    }

    private void putIcon(final int filter) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = new ImageView(activity.getBaseContext());
                imageView.setImageDrawable(getDrawable());
                imageView.setId(iconID);
                imageView.setColorFilter(filter, PorterDuff.Mode.MULTIPLY);
                pluginsPane.addView(imageView);
            }
        });

    }

    private void setIconColor(final int filter) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView) activity.findViewById(iconID))
                        .setColorFilter(filter, PorterDuff.Mode.MULTIPLY);
            }
        });

    }

    protected class InitializeVerifierTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            setIconColor(Color.RED);
            activity.startBackgroundOperation();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            verifier.onCreate();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            activity.endBackgroundOperation();
            initialized = true;
            setIconColor(Color.GREEN);
        }
    }

}
