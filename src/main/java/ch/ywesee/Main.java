package ch.ywesee;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.*;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;
import org.apache.commons.cli.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class Main {
    public static void main(String[] args) throws Exception {
        Options options = new Options();

        Option help = new Option("h", "help", false, "Show help message");
        options.addOption(help);

        Option input = new Option("c", "chmed16a", true, "CHMED16A string");
        options.addOption(input);

        Option qrImage = new Option("q", "qr-code", true, "Path to QRCode");
        options.addOption(qrImage);

        Option pdfOption = new Option("p", "pdf", true, "Path to PDF file");
        options.addOption(pdfOption);

        Option emailHost = new Option(null, "mail-host", true, "Host of IMAP server to connect to");
        options.addOption(emailHost);

        Option emailPort = new Option(null, "mail-port", true, "Port of IMAP server to connect to");
        emailPort.setType(Number.class);
        options.addOption(emailPort);

        Option emailUsername = new Option(null, "mail-username", true, "Username of IMAP server to connect to");
        options.addOption(emailUsername);

        Option emailPassword = new Option(null, "mail-password", true, "Password of IMAP server to connect to");
        options.addOption(emailPassword);

        Option emailNoSecure = new Option(null, "mail-no-secure", false, "Disable SSL for IMAP connection");
        options.addOption(emailNoSecure);

        Option mailboxOption = new Option(null, "mailbox", true, "Which folder in the mailbox to read. Case insensitive. Default: inbox.");
        mailboxOption.setType(String.class);
        options.addOption(mailboxOption);

        Option skipSeenOption = new Option(null, "mail-skip-seen", false, "Skip seen message?");
        options.addOption(skipSeenOption);

        Option markAsSeenOption = new Option(null, "mail-mark-as-seen", false, "Mark message as seen after processing?");
        options.addOption(markAsSeenOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
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

        new Main(cmd).run();

    }

    CommandLine cmd;

    Main(CommandLine cmd) {
        this.cmd = cmd;
    }

    void run() throws Exception {
        String chmed16A = cmd.getOptionValue("chmed16a");
        if (chmed16A != null) {
            EPrescription ep = new EPrescription(chmed16A);
            handleEPrescription(ep);
            return;
        }

        String qrCodeImagePath = cmd.getOptionValue("qr-code");
        if (qrCodeImagePath != null) {
            handleImage(ImageIO.read(new File(qrCodeImagePath)));
        }

        String pdfPath = cmd.getOptionValue("pdf");
        if (pdfPath != null) {
            PDDocument document = Loader.loadPDF(new File(pdfPath));
            handlePDF(document);
        }

        fetchEmails();
    }

    public static String scanQRImage(BufferedImage bufferedImage) throws IOException {
        String contents = null;

        BufferedImageLuminanceSource bufferedImageLuminanceSource = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(bufferedImageLuminanceSource));

        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
        }
        catch (Exception e) {
//            System.err.println("Error decoding barcode " + e);
        }
        return contents;
    }

    public void handlePDF(PDDocument document) throws Exception {
        int imageCountInPDF = 0;
        boolean foundQRCodeInPDF = false;
        for (PDPage page : document.getPages()) {
            PDResources resources = page.getResources();
            for (COSName name : resources.getXObjectNames()) {
                PDXObject obj = resources.getXObject(name);
                if (obj instanceof PDImageXObject) {
                    imageCountInPDF++;
                    BufferedImage image = ((PDImageXObject) obj).getImage();
                    handleImage(image);
                }
            }
        }
        if (imageCountInPDF == 0) {
            System.err.println("No image found in PDF.");
        } else if (!foundQRCodeInPDF) {
            System.err.println(imageCountInPDF + " images found in PDF, but no QRCode is found");
        }
    }

    public void handleImage(BufferedImage image) throws Exception {
        String content = scanQRImage(image);
        if (content != null) {
            System.out.println("Content in QRCode:" + content);
            EPrescription ep = new EPrescription(content);
            handleEPrescription(ep);
        }
    }

    public void handleEPrescription(EPrescription ePrescription) throws XMLStreamException, IOException {
        ZurRosePrescription zp = ePrescription.toZurRosePrescription();
        System.out.println("xml: " + zp.toXML());
    }

    public void fetchEmails() throws Exception {
        String emailHost = this.cmd.getOptionValue("mail-host");
        int emailPort = ((Number)this.cmd.getParsedOptionValue("mail-port", 993)).intValue();
        String emailUsername = this.cmd.getOptionValue("mail-username");
        String emailPassword = this.cmd.getOptionValue("mail-password");
        boolean emailNoSecure = this.cmd.hasOption("mail-no-secure");
        String mailboxName = this.cmd.getOptionValue("mailbox", "Inbox");
        boolean skipSeenMessage = this.cmd.hasOption("mail-skip-seen");
        boolean markAsSeen = this.cmd.hasOption("mail-mark-as-seen");

        if (emailHost == null || emailUsername == null || emailPassword == null) {
            // TODO: if verbose, log
            return;
        }

        final Properties properties = new Properties();
        if (!emailNoSecure) {
            properties.put("mail.imap.ssl.enable", "true");
        }
        properties.setProperty("mail.imap.host", emailHost); // imap.gmail.com
        properties.setProperty("mail.imap.port", Integer.toString(emailPort)); // 993
        properties.setProperty("mail.imap.connectiontimeout", "5000");
        properties.setProperty("mail.imap.timeout", "5000");
        properties.setProperty("mail.imap.ssl.protocols", "TLSv1.2 TLSv1.3");

        Session imapSession = Session.getInstance(properties, null);
        boolean showDebugMessages = true;
        if (showDebugMessages) {
//            imapSession.setDebug(true);
        }
        Store imapStore = imapSession.getStore("imap");

        imapStore.connect("imap.gmail.com", emailUsername, emailPassword);

        Folder defaultFolder = imapStore.getDefaultFolder();
        Folder[] folders = defaultFolder.list();

        Folder inbox = null;
        for (Folder f : folders) {
            if (f.getFullName().equalsIgnoreCase(mailboxName)) {
                inbox = f;
                break;
            }
        }
        if (inbox == null) {
            System.err.println("Cannot find mailbox named " + mailboxName + ". Available folders are:");
            for (Folder f : folders) {
                System.err.println(f.getFullName());
            }
            throw new Exception("Cannot find mailbox.");
        }

        inbox.open(Folder.READ_WRITE);

        Message[] ms = skipSeenMessage
                ? inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false))
                : inbox.getMessages();
        System.out.println("Found " + ms.length + " messages");

        for (Message message : ms) {
            long uid = inbox instanceof UIDFolder ? ((UIDFolder)inbox).getUID(message) : message.getMessageNumber();
            System.out.println("Found message. UID=" + uid);
            System.out.println("Subject: " + message.getSubject());

            System.out.println("Getting attachment");
            Object content = message.getContent();
            if (content instanceof MimeMultipart) {
                MimeMultipart multipart = (MimeMultipart) content;
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    String contentType = bodyPart.getContentType();
                    if (contentType.toLowerCase().startsWith("application/pdf")) {
                        PDDocument document = Loader.loadPDF(IOUtils.toByteArray(bodyPart.getInputStream()));
                        handlePDF(document);
                    } else {
                        System.out.println("Skipping unrecognised content-type: " + contentType);
                    }
                }
            }
            if (markAsSeen) {
                System.out.println("Marking message as seen.");
                message.setFlag(Flags.Flag.SEEN, true);
            }
        }
        inbox.close(false);
    }
}
