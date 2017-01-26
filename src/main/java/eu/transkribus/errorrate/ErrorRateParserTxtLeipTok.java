/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate;

import eu.transkribus.errorrate.ErrorModuleBagOfTokens;
import eu.transkribus.errorrate.ErrorModuleDynProg;
import eu.transkribus.errorrate.categorizer.CategorizerCharacterConfigurable;
import eu.transkribus.errorrate.categorizer.CategorizerWordDftConfigurable;
import eu.transkribus.errorrate.costcalculator.CostCalculatorDft;
import eu.transkribus.errorrate.interfaces.ICategorizer;
import eu.transkribus.errorrate.interfaces.IErrorModule;
import eu.transkribus.errorrate.normalizer.StringNormalizerDftConfigurable;
import eu.transkribus.errorrate.normalizer.StringNormalizerLetterNumber;
import eu.transkribus.interfaces.IStringNormalizer;
import eu.transkribus.languageresources.tokenizer.ConfigTokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Pair;

/**
 * Parser to make {@link ErrorModuleDynProg} accessible for the console.
 *
 * @author gundram
 */
public class ErrorRateParserTxtLeipTok {

    private static final Logger LOG = Logger.getLogger(ErrorRateParserTxtLeipTok.class.getName());
    private final Options options = new Options();

    public ErrorRateParserTxtLeipTok() {
        options.addOption("h", "help", false, "show this help");
        options.addOption("u", "upper", false, "error rate is calculated from upper string (not case sensitive)");
        options.addOption("p", "pfile", true, "property file to configure tokenizer");
        options.addOption("d", "detailed", false, "use detailed calculation (creates confusion map) (only one of -d and -D allowed at the same time) ");
        options.addOption("D", "Detailed", false, "use detailed calculation (creates substitution map) (only one of -d and -D allowed at the same time)");
    }

    public void run(String[] args) {

        CommandLine cmd = null;
        try {
            cmd = new DefaultParser().parse(options, args);

            //Help?
            if (cmd.hasOption("h")) {
                help();
            }
            //Word or Character Error Rate?
//            boolean wer = cmd.hasOption('w');
            if (cmd.hasOption('d') && cmd.hasOption('D')) {
                help("only one of the parameter -d and -D have to be set. Note that -D includes the output of -d");
            }
            //how detailed should the output be
            Boolean detailed = cmd.hasOption('d') ? null : cmd.hasOption('D');
            if (!cmd.hasOption('p')) {
                help("Please set path to property file \'-p\'");
            }
            //CATEGORIZER
            //normalize to letter or to all codepoints?
            IErrorModule em = new ErrorModuleDynProg(new CostCalculatorDft(), new ConfigTokenizer(cmd.getOptionValue('p')), null, detailed);
            List<String> argList = cmd.getArgList();
            if (argList.size() != 2) {
                help("no arguments given, missing <txt_groundtruth> <txt_hypothesis>.");
            }
            List<String> refs;
            try {
                refs = FileUtils.readLines(new File(argList.get(0)), "UTF-8");
            } catch (IOException ex) {
                throw new RuntimeException("cannot load file '" + argList.get(0) + "'", ex);
            }
            List<String> recos;
            try {
                recos = FileUtils.readLines(new File(argList.get(1)), "UTF-8");
            } catch (IOException ex) {
                throw new RuntimeException("cannot load file '" + argList.get(1) + "'", ex);
            }
            if (refs.size() != recos.size()) {
                throw new RuntimeException("loaded list " + argList.get(0) + " and " + argList.get(1) + " do not have the same number of lines.");
            }
            for (int i = 0; i < recos.size(); i++) {
                String reco = recos.get(i);
                String ref = refs.get(i);
                LOG.log(Level.FINE, "process [{0}/{1}]:{2} <> {3}", new Object[]{i + 1, recos.size(), reco, ref});
                LOG.log(Level.FINE, "ref: ''{0}''", ref);
                LOG.log(Level.FINE, "reco: ''{0}''", reco);
                em.calculate(reco, ref);
            }
            //print statistic to console
            List<String> results = em.getResults();
            for (String result : results) {
                System.out.println(result);
            }
            List<Pair<String, Long>> resultOccurrence = em.getCounter().getResultOccurrence();
            Map<String, Long> map = new HashMap<>();
            for (Pair<String, Long> pair : resultOccurrence) {
                map.put(pair.getFirst(), pair.getSecond());
            }
            if (map.containsKey("GT")) {
                int error = 0;
                if (map.containsKey("SUB")) {
                    error += map.get("SUB");
                    System.out.println("SUB = " + ((double) map.get("SUB")) / ((double) map.get("GT")));
                }
                if (map.containsKey("DEL")) {
                    error += map.get("DEL");
                    System.out.println("DEL = " + ((double) map.get("DEL")) / ((double) map.get("GT")));
                }
                if (map.containsKey("INS")) {
                    error += map.get("INS");
                    System.out.println("INS = " + ((double) map.get("INS")) / ((double) map.get("GT")));
                }
                System.out.println("TER = " + ((double) error) / ((double) map.get("GT")));
            }

        } catch (ParseException e) {
            help("Failed to parse comand line properties", e);
        }
    }

    private void help() {
        help(null, null);
    }

    private void help(String suffix) {
        help(suffix, null);
    }

    private void help(String suffix, Throwable e) {
        // This prints out some help
        if (suffix != null && !suffix.isEmpty()) {
            suffix = "ERROR:\n" + suffix;
            if (e != null) {
                suffix += "\n" + e.getMessage();
            }
        }
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp(
                "java -jar errorrate.jar <list_pageXml_groundtruth> <list_pageXml_hypothesis>",
                "This method calculates the (character) error rates between two lists of PAGE-XML-files."
                + " As input it requires two lists of PAGE-XML-files. The first one is the ground truth, the second one is the hypothesis."
                + " The programm returns the number of manipulations (corrects, substitution, insertion or deletion)"
                + " and the corresponding percentage to come from the hyothesis to the ground truth."
                + " The order of the xml-files in both lists has to be the same.",
                options,
                suffix,
                true
        );
        System.exit(0);
    }

    public static void main(String[] args) {
//        args = ("--help").split(" ");
        ErrorRateParserTxtLeipTok erp = new ErrorRateParserTxtLeipTok();
        erp.run(args);
    }
}
