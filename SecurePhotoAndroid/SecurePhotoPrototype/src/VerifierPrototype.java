import eu.tpmusielak.bouncy.tsp.TSPException;
import eu.tpmusielak.bouncy.tsp.TimeStampResponse;
import eu.tpmusielak.securephoto.container.SPImage;
import eu.tpmusielak.securephoto.verification.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 09.02.12
 * Time: 12:11
 */
public class VerifierPrototype {
    private List<Verifier> verifiers;

    public VerifierPrototype() {
        verifiers = new ArrayList<Verifier>();
        verifiers.add(new RFC3161Timestamp("http://www.cryptopro.ru/tsp/tsp.srf"));
        verifiers.add(new DummyVerifier());
    }

    public SPImage makeSPI(String inputFilename) throws IOException {
        File file = new File(inputFilename);
        InputStream inputStream = new FileInputStream(file);

        int fileSize = (int) file.length();
        int bytesRead = 0;

        byte[] imageData = new byte[fileSize];
        bytesRead = inputStream.read(imageData);


        System.out.println(String.format("Bytes read: %d, file size: %d", bytesRead, fileSize));

        SPImage spImage = SPImage.getInstance(imageData, verifiers);

        inputStream.close();

        return spImage;
    }

    public void saveSPI(SPImage image, String filename) throws IOException {
        File file = new File(filename);
        OutputStream outputStream = new FileOutputStream(file);

        outputStream.write(image.toByteArray());

        outputStream.close();
    }

    public SPImage openSPI(String filename) throws IOException, ClassNotFoundException {
        File file = new File(filename);
        InputStream inputStream = new FileInputStream(file);

        int fileSize = (int) file.length();
        int bytesRead = 0;

        byte[] SPIData = new byte[fileSize];
        bytesRead = inputStream.read(SPIData);

        return SPImage.fromBytes(SPIData);
    }

    public void extractTimestampData(SPImage image) throws TSPException, IOException {
        Map<Class<Verifier>, VerificationFactorData> verificationFactorData = image.getVerificationFactorData();
        Class<RFC3161Timestamp> timestampDataClass = RFC3161Timestamp.class;
        TimestampData timestampData = (TimestampData) verificationFactorData.get(timestampDataClass);

        TimeStampResponse tsr = new TimeStampResponse(timestampData.getResponse());

        System.out.println(tsr.getTimeStampToken().getTimeStampInfo().getGenTime());

    }

    public void printVerificationFactors(SPImage image) {
        List<Class<Verifier>> verificationFactors = image.getVerificationFactors();
        System.out.println("Verification factors:");
        for (Class<Verifier> factorClass : verificationFactors) {
            System.out.println(factorClass.getCanonicalName());
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, TSPException {
        Scanner scanner = new Scanner(System.in);
        String filename = scanner.next();


        VerifierPrototype vp = new VerifierPrototype();
//        SPImage image = vp.makeSPI("./SecurePhotoPrototype/res/picture.jpg");
//        vp.saveSPI(image, "./SecurePhotoPrototype/pic3.spi");

//        SPImage img = vp.openSPI("./SecurePhotoPrototype/SCIMG_20120209_174216.spi");
        SPImage img = vp.openSPI(String.format("./SecurePhotoPrototype/%s", filename));
        vp.extractTimestampData(img);
        vp.printVerificationFactors(img);


    }


}
