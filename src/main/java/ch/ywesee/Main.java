package ch.ywesee;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.*;
import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Options options = new Options();

        Option help = new Option("h", "help", false, "Show help message");
        options.addOption(help);

        Option input = new Option("c", "chmed16a", true, "CHMED16A string");
        options.addOption(input);

        Option qrImage = new Option("q", "qr-code", true, "Path to QRCode");
        options.addOption(qrImage);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }
        if (cmd.hasOption("help") || args.length == 0) {
            formatter.printHelp("chmed2email", options);
            return;
        }
        String chmed16A = cmd.getOptionValue("chmed16a");
        if (chmed16A != null) {
            EPrescription ep = new EPrescription(chmed16A);
            ZurRosePrescription zp = ep.toZurRosePrescription();
            System.out.println(zp.toXML());
            return;
        }

        String qrCodeImagePath = cmd.getOptionValue("qr-code");
        if (qrCodeImagePath != null) {
            String content = scanQRImage(qrCodeImagePath);
            if (content != null) {
                System.out.println("Content in QRCode:" + content);
                EPrescription ep = new EPrescription(content);
                ZurRosePrescription zp = ep.toZurRosePrescription();
                System.out.println(zp.toXML());
            }
        }

    public static String scanQRImage(String path) throws IOException {
        String contents = null;

        BufferedImage bufferedImage = ImageIO.read(new File(path));
        BufferedImageLuminanceSource bufferedImageLuminanceSource = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(bufferedImageLuminanceSource));

        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
        }
        catch (Exception e) {
            System.err.println("Error decoding barcode " + e);
        }
        return contents;
    }

}
