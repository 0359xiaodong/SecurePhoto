import eu.tpmusielak.securephoto.container.SPImage;
import eu.tpmusielak.securephoto.container.SPImageRoll;
import eu.tpmusielak.securephoto.verification.VerificationFactorData;
import eu.tpmusielak.securephoto.verification.Verifier;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz P. Musielak
 * Date: 22/05/12
 * Time: 20:46
 */
public class Main implements ActionListener {
    private final static String APPLICATION_NAME = "SecurePhotoTools";

    private JButton openButton;
    private JButton exitButton;
    private JPanel mainPanel;
    private JButton prevButton;
    private JButton nextButton;
    private JPanel imagePanel;
    private JSplitPane splitPane;
    private JTextPane textPane;
    private JLabel imageLabel;
    private JLabel frameNumberLabel;
    private JLabel frameCountLabel;
    private JButton newSPRButton;
    private JButton addToSPRButton;
    private JButton saveButton;


    final JFileChooser openFileChooser = new JFileChooser();
    final JFileChooser saveFileChooser = new JFileChooser();

    private File currentFile;
    private BufferedImage currentImage;


    private static JFrame mainFrame;

    public Main() {
        openButton.addActionListener(Main.this);
        exitButton.addActionListener(Main.this);
        prevButton.addActionListener(Main.this);
        nextButton.addActionListener(Main.this);
        newSPRButton.addActionListener(Main.this);
        addToSPRButton.addActionListener(Main.this);
        saveButton.addActionListener(Main.this);


        openFileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String name = f.getName();
                int dotIndex = name.lastIndexOf('.');
                String ext = name.substring(dotIndex + 1).toLowerCase();

                return ext.equals(SPImage.defaultExtension)
                        || ext.equals(SPImageRoll.defaultExtension)
                        || ext.equals("jpg")
                        || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "SPImage, SPImageRoll or JPEG";
            }
        });

        saveFileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(SPImageRoll.defaultExtension);
            }

            @Override
            public String getDescription() {
                return "SPImageRoll (*.spr)";
            }
        });

        splitPane.setDividerLocation(0.80d);
        textPane.setEditable(false);
        setNaviButtonsState(false);
    }


    /**
     * Invoked when an action occurs.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == openButton) {
            int retVal = openFileChooser.showOpenDialog(mainPanel);

            if (retVal == JFileChooser.APPROVE_OPTION) {
                openFile(openFileChooser.getSelectedFile());
            }
        } else if (source == newSPRButton) {
            int retVal = saveFileChooser.showSaveDialog(mainPanel);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                newSPR(saveFileChooser.getSelectedFile());
            }
        } else if (source == addToSPRButton) {
            int retVal = openFileChooser.showOpenDialog(mainPanel);

            if (retVal == JFileChooser.APPROVE_OPTION) {

            }
        } else if (source == exitButton) {
            System.exit(0);
        }

    }

    private void newSPR(File file) {
        if (!file.getName().endsWith("." + SPImageRoll.defaultExtension)) {
            file = new File(file.getAbsolutePath() + "." + SPImageRoll.defaultExtension);
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(0);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openFile(File file) {
        currentFile = file;
        clearImage();
        textPane.setText("");

        if (file.getName().endsWith(SPImage.defaultExtension)) {
            openSPIFile(file);
        } else if (file.getName().endsWith(SPImageRoll.defaultExtension)) {
            openSPRFile(file);
        } else if (file.getName().toLowerCase().endsWith("jpg")) {
            openJPEGFile(file);
        }
        displayImage();
        mainFrame.setTitle(APPLICATION_NAME + " - " + currentFile.getName());
    }

    private void openJPEGFile(File file) {
        try {
            currentImage = ImageIO.read(file);
            setNaviButtonsState(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setNaviButtonsState(boolean state) {
        prevButton.setEnabled(state);
        nextButton.setEnabled(state);
        saveButton.setEnabled(state);
        addToSPRButton.setEnabled(state);
    }

    private void openSPRFile(File file) {
        setNaviButtonsState(true);
    }

    private void openSPIFile(File file) {
        try {
            SPImage image = SPImage.fromFile(file);
            byte[] imageBytes = image.getImageData();

            currentImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            setNaviButtonsState(false);
            printVerifierData(image);
        } catch (IOException e) {
            textPane.setText(Arrays.toString(e.getStackTrace()));
        } catch (ClassNotFoundException e) {
            textPane.setText(Arrays.toString(e.getStackTrace()));
        }
    }

    public void printVerifierData(SPImage image) {
        if (image == null)
            return;

        StringBuilder sb = new StringBuilder();

        Map<Class<Verifier>, VerificationFactorData> verificationFactorData = image.getVerificationFactorData();
        for (VerificationFactorData data : verificationFactorData.values()) {
            sb.append(data.toString());
            sb.append('\n');
        }
        textPane.setText(sb.toString());
    }

    private void clearImage() {
        imageLabel.setIcon(null);
    }

    private void displayImage() {
        if (currentImage == null)
            return;

        int imgHeight = currentImage.getHeight();
        int imgWidth = currentImage.getWidth();

        double maxDimImg = Math.max(imgHeight, imgWidth);
        double maxDimWindow = Math.max(imagePanel.getHeight(), imagePanel.getWidth());

        double scalingFactor = maxDimWindow / maxDimImg;

        int scaledWidth = (int) Math.ceil(imgWidth * scalingFactor);
        int scaledHeight = (int) Math.ceil(imgHeight * scalingFactor);

        Image scaledPicture = currentImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

        imageLabel.setIcon(new ImageIcon(scaledPicture));
    }

    public static void main(String[] args) {
        mainFrame = new JFrame(APPLICATION_NAME);
        mainFrame.setContentPane(new Main().mainPanel);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setResizable(false);
    }
}
