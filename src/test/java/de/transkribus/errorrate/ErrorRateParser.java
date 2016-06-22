/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.transkribus.errorrate;

import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.errorrate.ErrorModuleBagOfTokens;
import eu.transkribus.errorrate.ErrorModuleDynProg;
import eu.transkribus.errorrate.categorizer.CategorizerCharacterConfigurable;
import eu.transkribus.errorrate.categorizer.CategorizerWordDftConfigurable;
import eu.transkribus.errorrate.costcalculator.CostCalculatorDft;
import eu.transkribus.errorrate.interfaces.ICategorizer;
import eu.transkribus.errorrate.interfaces.IErrorModule;
import eu.transkribus.errorrate.interfaces.IStringNormalizer;
import eu.transkribus.errorrate.normalizer.StringNormalizerDftConfigurable;
import eu.transkribus.errorrate.normalizer.StringNormalizerLetterNumber;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser to make {@link ErrorModuleDynProg} accessible for the console.
 *
 * @author gundram
 */
public class ErrorRateParser {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorRateParser.class);
    private final Options options = new Options();

    public ErrorRateParser() {
        options.addOption("h", "help", false, "show this help");
        options.addOption("u", "upper", false, "error rate is calculated from upper string (not case sensitive)");
        options.addOption("N", "normcompatibility", false, "compatibility normal form is used (only one of -n or -N is allowed)");
        options.addOption("n", "normcanonic", false, "canonical normal form is used (only one of -n or -N is allowed)");
        options.addOption("c", "category", true, "property file to categorize codepoints with codepoint-category-mapping");
        options.addOption("i", "isolated", true, "property file to define, if a codepoint is used as sigle token or not with codepoint-boolean-mapping");
        options.addOption("s", "separator", true, "property file to define, if a codepoint is a separator with codepoint-boolean-mapping");
        options.addOption("m", "mapper", true, "property file to normalize strings with a string-string-mapping");
        options.addOption("w", "wer", false, "calculate word error rate instead of character error rate");
        options.addOption("d", "detailed", false, "use detailed calculation (creates confusion map) (only one of -d and -D allowed at the same time) ");
        options.addOption("D", "Detailed", false, "use detailed calculation (creates substitution map) (only one of -d and -D allowed at the same time)");
        options.addOption("l", "letters", false, "calculate error rates only for codepoints, belonging to the category \"L\"");
        options.addOption("b", "bag", false, "using bag of words instead of dynamic programming tabular");
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
            boolean wer = cmd.hasOption('w');
            if (cmd.hasOption('d') && cmd.hasOption('D')) {
                help("only one of the parameter -d and -D have to be set. Note that -D includes the output of -d");
            }
            //how detailed should the output be
            Boolean detailed = cmd.hasOption('d') ? null : cmd.hasOption('D');
            //upper case?
            boolean upper = cmd.hasOption('u');
            //canoncal or compatibility composition form?
            boolean normcompatibility = cmd.hasOption('N');
            boolean normcanonic = cmd.hasOption('n');
            if (normcompatibility && normcanonic) {
                help("both normalization options are on - use -n or -N");
            }
            Normalizer.Form form = null;
            if (normcompatibility) {
                form = Normalizer.Form.NFKC;
            }
            if (normcanonic) {
                form = Normalizer.Form.NFC;
            }
            //STRING NORMALIZER
            IStringNormalizer.IPropertyConfigurable snd = new StringNormalizerDftConfigurable(form, upper);
            //property map for substitute substrings while normalization
            if (cmd.hasOption('m')) {
                String optionValue = cmd.getOptionValue('m');
                try {
                    snd.putSubstitutionProperties(optionValue);
                } catch (Throwable e) {
                    help("cannot load file '" + optionValue + "' properly - use java property syntax in file.", e);
                }
            }
            //CATEGORIZER
            ICategorizer.IPropertyConfigurable categorizer = wer ? new CategorizerWordDftConfigurable() : new CategorizerCharacterConfigurable();
            //property map for categorize codepoints
            if (cmd.hasOption('c')) {
                String optionValue = cmd.getOptionValue('c');
                try {
                    categorizer.putCategoryProperties(optionValue);
                } catch (Throwable e) {
                    help("cannot load file '" + optionValue + "' properly - use java property syntax in file.", e);
                }
            }
            //property map for specify separator codepoints
            if (cmd.hasOption('s')) {
                String optionValue = cmd.getOptionValue('s');
                try {
                    categorizer.putSeparatorProperties(optionValue);
                } catch (Throwable e) {
                    help("cannot load file '" + optionValue + "' properly - use java property syntax in file.", e);
                }
            }
            //property map for specify isolated codepoints
            if (cmd.hasOption('i')) {
                String optionValue = cmd.getOptionValue('i');
                try {
                    categorizer.putIsolatedProperties(optionValue);
                } catch (Throwable e) {
                    help("cannot load file '" + optionValue + "' properly - use java property syntax in file.", e);
                }
            }
            //normalize to letter or to all codepoints?
            IStringNormalizer sn = cmd.hasOption('l') ? new StringNormalizerLetterNumber(snd) : snd;
            boolean bagOfWords = cmd.hasOption('b');
            IErrorModule em = bagOfWords ? new ErrorModuleBagOfTokens(categorizer, sn, detailed)
                    : new ErrorModuleDynProg(new CostCalculatorDft(), categorizer, sn, detailed);
            List<String> argList = cmd.getArgList();
            if (argList.size() != 2) {
                help("no arguments given, missing <list_pageXml_groundtruth> <list_pageXml_hypothesis>.");
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
                LOG.debug("process [{0}/{1}]: {2} <> {3}", new Object[]{i + 1, recos.size(), reco, ref});
                String textRef = "";
                String textReco = "";
                try {
                    System.out.println("unmarshal " + ref);
                    //load xml-files and get TextLineTypes, which are in RegionTypes.
                    PcGtsType unmarshalr = PageXmlUtils.unmarshal(new File(ref));
                    textRef = PageXmlUtils.getFulltextFromLines(unmarshalr);
                } catch (JAXBException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    System.out.println("unmarshal " + reco);
                    PcGtsType unmarshal = PageXmlUtils.unmarshal(new File(reco));
                    textReco = PageXmlUtils.getFulltextFromLines(unmarshal);
                } catch (JAXBException ex) {
                    throw new RuntimeException(ex);
                }
                //calculate error rates in ErrorModule
                LOG.debug("ref: '" + textRef + "'");
                LOG.debug("reco:'" + textReco + "'");
                em.calculate(textReco, textRef);
            }
            //print statistic to console
            List<String> results = em.getResults();
            for (String result : results) {
                System.out.println(result);
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
        args = ("--help").split(" ");
        ErrorRateParser erp = new ErrorRateParser();
        erp.run(args);
    }
}
