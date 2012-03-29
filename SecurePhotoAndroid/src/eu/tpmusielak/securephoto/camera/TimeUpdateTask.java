package eu.tpmusielak.securephoto.camera;

import android.widget.TextView;
import eu.tpmusielak.securephoto.R;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 02:25
 */
public class TimeUpdateTask extends TimerTask {

    private TakeImage activity;

    public TimeUpdateTask(TakeImage activity) {
        this.activity = activity;
    }

    @Override
    public void run() {
        Calendar c = Calendar.getInstance();
        Date time = c.getTime();
        final String timeString = time.toLocaleString();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView cameraDate = (TextView) activity.findViewById(R.id.camera_date);
                cameraDate.setText(timeString);
            }
        });
    }
}