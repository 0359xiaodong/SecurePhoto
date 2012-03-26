package eu.tpmusielak.securephoto.camera;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.widget.ImageView;
import eu.tpmusielak.securephoto.verification.VerificationFactorWrapper;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 02:33
 */
public class InitializeFactorTask extends AsyncTask<List<VerificationFactorWrapper>, Void, Void> {
    private TakeImage activity;

    public InitializeFactorTask(TakeImage activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        activity.startBackgroundOperation();
    }

    @Override
    protected Void doInBackground(List<VerificationFactorWrapper>... lists) {
        List<VerificationFactorWrapper> verificationFactors = lists[0];
        int id = 10;

        for (final VerificationFactorWrapper v : verificationFactors) {

            final String name = v.toString();
            final int finalId = id;

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView imageView = new ImageView(activity.getApplicationContext());
                    imageView.setImageDrawable(v.getDrawable());
                    imageView.setId(finalId);
                    imageView.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                    activity.pluginsPane.addView(imageView);
                }
            });

            v.getVerificationFactor().onCreate();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ImageView) activity.findViewById(finalId)).setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                }
            });
            id++;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        activity.endBackgroundOperation();
    }
}