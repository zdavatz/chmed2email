package ch.ywesee;

import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Options options = new Options();

        Option help = new Option("h", "help", false, "Show help message");
        options.addOption(help);

        Option input = new Option("c", "chmed16a", true, "CHMED16A string");
        options.addOption(input);

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

    }
}