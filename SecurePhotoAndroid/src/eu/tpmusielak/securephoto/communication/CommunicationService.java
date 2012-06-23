package eu.tpmusielak.securephoto.communication;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.widget.Toast;
import eu.tpmusielak.securephoto.R;
import eu.tpmusielak.securephoto.container.SPImageRoll;
import eu.tpmusielak.securephoto.container.wrapper.SPFileWrapper;
import eu.tpmusielak.securephoto.container.wrapper.SPRWrapper;
import eu.tpmusielak.securephoto.tools.FileHandling;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 06.12.11
 * Time: 00:37
 */
public class CommunicationService extends Service {
    private SubmitHashTask submitHashTask;
    private GetSPRTask getSPRTask;

    private String baseStationAddress;
    private String androidID;
    private static final String HASH_SUBMIT_PATH = "hash_submit";
    private static final String GET_SPR_PATH = "request_spr";

    public class CommuncationServiceBinder extends Binder {
        private WeakReference<CommunicationService> serviceReference;

        public CommuncationServiceBinder(CommunicationService communicationService) {
            serviceReference = new WeakReference<CommunicationService>(communicationService);
        }

        public CommunicationService getService() {
            return serviceReference.get();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        androidID = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);


        submitHashTask = new SubmitHashTask();

        updateBaseStationAddress();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new CommuncationServiceBinder(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateBaseStationAddress() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        baseStationAddress = preferences.getString("base_station_address", null);

        if (baseStationAddress == null) {
            Toast.makeText(getBaseContext(), getString(R.string.base_station_address_not_set), Toast.LENGTH_SHORT).show();
        }
    }

    public void notifyPictureTaken(SPFileWrapper wrapper) {
        updateBaseStationAddress();
        if (baseStationAddress == null) {
            return;
        }


        submitHashTask = new SubmitHashTask();
        submitHashTask.execute(wrapper);
    }

    public void getSPRoll() {
        updateBaseStationAddress();
        if (baseStationAddress == null) {
            return;
        }
        getSPRTask = new GetSPRTask();
        //noinspection unchecked
        getSPRTask.execute();
    }

    private class SubmitHashTask extends AsyncTask<SPFileWrapper, Void, Void> {
        HttpClient client;

        @Override
        protected void onPreExecute() {
            client = AndroidHttpClient.newInstance("Android");
        }

        @Override
        protected Void doInBackground(SPFileWrapper... wrappers) {
            for (SPFileWrapper wrapper : wrappers) {
                try {
                    HttpPost request = new HttpPost(String.format("%s/%s", baseStationAddress, HASH_SUBMIT_PATH));
                    List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();

                    nameValuePairs.add(new BasicNameValuePair("type", wrapper.getFileTypeName()));
                    nameValuePairs.add(new BasicNameValuePair("device_id", androidID));

                    if (wrapper instanceof SPRWrapper) {
                        SPImageRoll.Header header = ((SPRWrapper) wrapper).getHeader();
                        nameValuePairs.add(
                                new BasicNameValuePair("spr_id",
                                        Base64.encodeToString(header.getUniqueID(), Base64.NO_WRAP)));
                    }
                    nameValuePairs.add(
                            new BasicNameValuePair("image_id",
                                    Base64.encodeToString(wrapper.getUniqueFrameID(), Base64.NO_WRAP)));


                    nameValuePairs.add(
                            new BasicNameValuePair("image_hash",
                                    Base64.encodeToString(wrapper.getFrameHash(), Base64.NO_WRAP)));

                    request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = client.execute(request);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    private class GetSPRTask extends AsyncTask<Void, Void, SPFileWrapper> {
        HttpClient client;

        @Override
        protected void onPreExecute() {
            client = AndroidHttpClient.newInstance("Android");
        }

        @Override
        protected SPFileWrapper doInBackground(Void... voids) {
            try {
                HttpPost request = new HttpPost(String.format("%s/%s", baseStationAddress, GET_SPR_PATH));
                List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("device_id", androidID));
                request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = client.execute(request);
                Header[] headers = response.getHeaders("spr_id");

                if (headers == null) {
                    return null;
                }

                String identifier = headers[0].getValue();
                byte[] id = Base64.decode(identifier, Base64.NO_WRAP);

                if (id.length != 20) { // For some reason identifier is invalid
                    return null;
                }

                File sprFile = FileHandling.getOutputFile(SPImageRoll.DEFAULT_EXTENSION);
                SPImageRoll roll = new SPImageRoll(sprFile, id);
                return SPRWrapper.wrapFile(sprFile);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(SPFileWrapper wrapper) {
            if (wrapper == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.spr_retrieval_failed), Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(getApplicationContext(), getString(R.string.spr_retrieved, wrapper.getName()), Toast.LENGTH_LONG).show();
        }
    }

}




