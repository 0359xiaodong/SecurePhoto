package eu.tpmusielak.securephoto.container;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 15.02.12
 * Time: 11:53
 */
public class SPImageRoll implements Serializable {
    private List<SPImage> frames;

    public SPImageRoll() {
        frames = new LinkedList<SPImage>();
    }

    public void addFrame(SPImage frame) {
        frames.add(frame);
    }

    public byte[] toByteArray() {
        byte[] bytes = null;
        try {
            ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(byteArrayOutput);

            objectOutput.writeObject(SPImageRoll.this);
            bytes = byteArrayOutput.toByteArray();

            objectOutput.close();
            byteArrayOutput.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }


}
