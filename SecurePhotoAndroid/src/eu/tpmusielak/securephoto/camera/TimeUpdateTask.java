package eu.tpmusielak.securephoto.camera;

import android.os.AsyncTask;
import android.widget.TextView;
import eu.tpmusielak.securephoto.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 02:25
 */
public class TimeUpdateTask extends AsyncTask<Void, Void, String> {

    private TakeImage activity;

    public TimeUpdateTask(TakeImage activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();
        Date time = c.getTime();

        return time.toLocaleString();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onPostExecute(String result) {
        TextView cameraDate = (TextView) activity.findViewById(R.id.camera_date);
        cameraDate.setText(result);
        activity.updateTime();
    }


}