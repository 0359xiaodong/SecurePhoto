package eu.tpmusielak.securephoto.camera;

import eu.tpmusielak.securephoto.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 26/03/12
 * Time: 02:39
 */

enum SaveMode {
    SINGLE_IMAGE("SPImage"),
    IMAGE_ROLL("SPImageRoll"),;

    private String name;
    private static Map<SaveMode, Integer> resIDs;

    static {
        resIDs = new HashMap<SaveMode, Integer>();
        resIDs.put(SINGLE_IMAGE, R.string.camera_image_single);
        resIDs.put(IMAGE_ROLL, R.string.camera_image_roll);
    }

    SaveMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getTextResID() {
        return resIDs.get(this);
    }

    public SaveMode switchMode() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
