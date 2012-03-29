package eu.tpmusielak.securephoto.camera;

import android.content.Context;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 27/03/12
 * Time: 17:33
 */
public interface CameraReceiver {
    public void savePicture(byte[] bytes);

    public Context getContext();
}
