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
    private static Map<SaveMode, Integer> textResIDs;
    private static Map<SaveMode, Integer> imgResIDs;

    static {
        textResIDs = new HashMap<SaveMode, Integer>();
        textResIDs.put(SINGLE_IMAGE, R.string.camera_image_single);
        textResIDs.put(IMAGE_ROLL, R.string.camera_image_roll);

        imgResIDs = new HashMap<SaveMode, Integer>();
        imgResIDs.put(SINGLE_IMAGE, R.drawable.ic_single_image);
        imgResIDs.put(IMAGE_ROLL, R.drawable.ic_filmroll);
    }

    SaveMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getTextResID() {
        return textResIDs.get(this);
    }

    public int getDrawableResID() {
        return imgResIDs.get(this);
    }

    public SaveMode switchMode() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
