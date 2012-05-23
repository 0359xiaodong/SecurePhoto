package eu.tpmusielak.securephoto.container;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 15.02.12
 * Time: 11:53
 */
public class SPImageRoll implements Serializable {
    private transient Header rollHeader;

    private byte[] currentHash;
    public final static String defaultExtension = "spr";


    public static class Header {
        protected final Date expiryDate;
        protected final long uniqueID;
        protected int imageCount;
        protected List<List<Byte>> thumbnails;

        protected Header(Date expiryDate, long uniqueID) {
            this.expiryDate = expiryDate;
            this.uniqueID = uniqueID;
            imageCount = 0;

            thumbnails = new ArrayList<List<Byte>>();
        }
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

    private void setRollHeader(Header header) {
        this.rollHeader = header;
    }

    public List<List<Byte>> getThumbnails() {
        return rollHeader.thumbnails;
    }

    public void writeImageRoll(OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        objectOutputStream.writeObject(rollHeader);
        objectOutputStream.writeObject(this);

        objectOutputStream.close();
    }

    public static SPImageRoll readImageRoll(InputStream inputStream) throws IOException, ClassNotFoundException {
        Header header = null;
        SPImageRoll imageRoll = null;

        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        header = (Header) objectInputStream.readObject();
        imageRoll = (SPImageRoll) objectInputStream.readObject();
        imageRoll.setRollHeader(header);

        objectInputStream.close();

        return imageRoll;
    }

    public static Header readHeader(InputStream inputStream) throws IOException, ClassNotFoundException {
        Header header = null;
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

        header = (Header) objectInputStream.readObject();

        objectInputStream.close();
        return header;
    }


}
