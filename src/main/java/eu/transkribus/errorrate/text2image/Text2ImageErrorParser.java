/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.text2image;

//github.com/Transkribus/TranskribusErrorRate.git
import eu.transkribus.errorrate.*;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import eu.transkribus.errorrate.costcalculator.CostCalculatorDft;
import eu.transkribus.errorrate.interfaces.IErrorModule;
import eu.transkribus.errorrate.normalizer.StringNormalizerDftConfigurable;
import eu.transkribus.errorrate.normalizer.StringNormalizerLetterNumber;
import eu.transkribus.errorrate.types.Count;
import eu.transkribus.interfaces.IStringNormalizer;
import eu.transkribus.languageresources.extractor.xml.XMLExtractor;
import eu.transkribus.tokenizer.categorizer.CategorizerCharacterConfigurable;
import eu.transkribus.tokenizer.categorizer.CategorizerCharacterDft;
import eu.transkribus.tokenizer.categorizer.CategorizerWordDftConfigurable;
import eu.transkribus.tokenizer.categorizer.CategorizerWordMergeGroups;
import eu.transkribus.tokenizer.interfaces.ICategorizer;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser to make {@link ErrorModuleDynProg} accessible for the console.
 *
 * @author gundram
 */
public class Text2ImageErrorParser {

    private static final Logger LOG = LoggerFactory.getLogger(Text2ImageErrorParser.class.getName());
    private final Options options = new Options();

    public Text2ImageErrorParser() {
        options.addOption("h", "help", false, "show this help");
        options.addOption("u", "upper", false, "error rate is calculated from upper string (not case sensitive)");
        options.addOption("N", "normcompatibility", false, "compatibility normal form is used (only one of -n or -N is allowed)");
        options.addOption("n", "normcanonic", false, "canonical normal form is used (only one of -n or -N is allowed)");
        options.addOption("c", "category", true, "property file to categorize codepoints with codepoint-category-mapping");
        options.addOption("i", "isolated", true, "property file to define, if a codepoint is used as single token or not with codepoint-boolean-mapping");
        options.addOption("s", "separator", true, "property file to define, if a codepoint is a separator with codepoint-boolean-mapping");
        options.addOption("m", "mapper", true, "property file to normalize strings with a string-string-mapping");
        options.addOption("w", "wer", false, "calculate word error rate instead of character error rate");
        options.addOption("p", "pagewise", false, "output values pagewise");
        options.addOption("l", "letters", false, "calculate error rates only for codepoints, belonging to the category \"L\"");
        options.addOption("b", "bag", false, "using bag of words instead of dynamic programming tabular");
        options.addOption("t", "thresh", true, "threshold for alignment of textlines (the higher the harder is alignment. value have to be in [0,1]");
    }

    public Map<String, Double> run(String[] args) {
        return run(args, null, null);
    }

    public Map<String, Double> run(String[] args, File[] gts, File[] hyps) {
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
            ICategorizer categorizer = null;
            if (!cmd.hasOption('c') && !cmd.hasOption('s') && !cmd.hasOption('i')) {
                categorizer = wer ? new CategorizerWordMergeGroups() : new CategorizerCharacterDft();
            }
            ICategorizer.IPropertyConfigurable categorizerConfigurable = wer ? new CategorizerWordDftConfigurable() : new CategorizerCharacterConfigurable();
            categorizer = categorizerConfigurable;
            //property map for categorize codepoints
            if (cmd.hasOption('c')) {
                String optionValue = cmd.getOptionValue('c');
                try {
                    categorizerConfigurable.putCategoryProperties(optionValue);
                } catch (Throwable e) {
                    help("cannot load file '" + optionValue + "' properly - use java property syntax in file.", e);
                }
            }
            //property map for specify separator codepoints
            if (cmd.hasOption('s')) {
                String optionValue = cmd.getOptionValue('s');
                try {
                    categorizerConfigurable.putSeparatorProperties(optionValue);
                } catch (Throwable e) {
                    help("cannot load file '" + optionValue + "' properly - use java property syntax in file.", e);
                }
            }
            //property map for specify isolated codepoints
            if (cmd.hasOption('i')) {
                String optionValue = cmd.getOptionValue('i');
                try {
                    categorizerConfigurable.putIsolatedProperties(optionValue);
                } catch (Throwable e) {
                    help("cannot load file '" + optionValue + "' properly - use java property syntax in file.", e);
                }
            }
            double threshold = 0.7;
            if (cmd.hasOption('t')) {
                threshold = Double.parseDouble(cmd.getOptionValue('t'));
            } else {
                LOG.warn("threshold not set, use {} as default", threshold);;
            }
            //ouput calculate pagewise
            boolean pagewise = cmd.hasOption('p');
            //normalize to letter or to all codepoints?
            IStringNormalizer sn = cmd.hasOption('l') ? new StringNormalizerLetterNumber(snd) : snd;
            IErrorModule errorModule = new ErrorModuleDynProg(new CostCalculatorDft(), categorizerConfigurable, sn, detailed);
//            IErrorModule emRec = new ErrorModuleDynProg(new CostCalculatorDft(), categorizer, sn, detailed);
//            IBaseLineAligner baseLineAligner = new BaseLineAlignerSameBaselines();
            IBaseLineAligner baseLineAligner = new BaseLineAligner();
            List<String> argList = cmd.getArgList();
            List<String> refs;
            List<String> recos;
            if (argList.size() != 2) {
                if (gts == null || hyps == null) {
                    help("no arguments given, missing <list_pageXml_groundtruth> <list_pageXml_hypothesis>.");
                }
                refs = new LinkedList<>();
                recos = new LinkedList<>();
                if (gts.length != hyps.length) {
                    throw new RuntimeException("groundtruth list and hypothesis list differ in length");
                }
                for (int i = 0; i < hyps.length; i++) {
                    refs.add(gts[i].getPath());
                    recos.add(hyps[i].getPath());
                }
            } else {
                try {
                    refs = FileUtils.readLines(new File(argList.get(0)), "UTF-8");
                } catch (IOException ex) {
                    throw new RuntimeException("cannot load file '" + argList.get(0) + "'", ex);
                }
                try {
                    recos = FileUtils.readLines(new File(argList.get(1)), "UTF-8");
                } catch (IOException ex) {
                    throw new RuntimeException("cannot load file '" + argList.get(1) + "'", ex);
                }
            }
            if (refs.size() != recos.size()) {
                throw new RuntimeException("loaded list " + argList.get(0) + " and " + argList.get(1) + " do not have the same number of lines.");
            }
            double sum = 0;
            double sumAll = 0;
            double sumGT = 0;
            double correct = 0;
            for (int i = 0; i < recos.size(); i++) {
                double sumGTCur = 0;
                double sumCur = 0;
                double sumAllCur = 0;
                double correctCur = 0;
                String reco = recos.get(i);
                String ref = refs.get(i);
                LOG.debug("process [{}/{}]:{} <> {}", i + 1, recos.size(), reco, ref);
                List<XMLExtractor.Line> linesGT = XMLExtractor.getLinesFromFile(new File(ref));
                List<XMLExtractor.Line> linesLA = XMLExtractor.getLinesFromFile(new File(reco));
                List<XMLExtractor.Line> linesHyp = new LinkedList<>();
                for (XMLExtractor.Line line : linesLA) {
                    if (line.textEquiv != null) {
                        linesHyp.add(line);
                    }
                }
                IBaseLineAligner.IAlignerResult alignment = baseLineAligner.getAlignment(toArray(linesGT), toArray(linesLA), toArray(linesHyp), threshold, null);
                int[][] precAlignment = alignment.getGTLists();
                for (int j = 0; j < precAlignment.length; j++) {
                    int[] idsGT = precAlignment[j];
                    String textHyp = linesHyp.get(j).textEquiv;
                    switch (idsGT.length) {
                        case 0:
                            errorModule.calculate("", textHyp);
                            break;
                        case 1:
                            errorModule.calculate(linesGT.get(idsGT[0]).textEquiv, textHyp);
                            break;
                        default: {
                            StringBuilder sb = new StringBuilder();
                            sb.append(linesGT.get(idsGT[0]).textEquiv);
                            for (int k = 1; k < linesGT.size(); k++) {
                                sb.append(' ').append(linesGT.get(idsGT[k]).textEquiv);
                            }
                            errorModule.calculate(sb.toString(), textHyp);
                        }
                    }
                }
                double[] recValue = alignment.getRecallsLA();
                for (int j = 0; j < recValue.length; j++) {
                    sumCur += linesGT.get(j).textEquiv.length() * recValue[j];
                }
                for (XMLExtractor.Line line : linesGT) {
                    sumAllCur += line.textEquiv.length();
                }
                correctCur = errorModule.getCounter().get(Count.COR);
                sumGTCur = errorModule.getCounter().get(Count.GT);
                errorModule.reset();
                sum += sumCur;
                sumAll += sumAllCur;
                sumGT += sumGTCur;
                correct += correctCur;
                if (pagewise) {
                    System.out.println(String.format("P-Value(text): %.4f R-Value(text): %.4f R-Value(geom): %.4f xmlRef: %s", ((double) correctCur) / sumGTCur, ((double) correctCur) / sumCur, ((double) sumCur) / sumAllCur, ref));
                }
            }
            HashMap res = new HashMap();
            res.put("P_text", ((double) correct) / sumGT);
            res.put("R_text", ((double) correct) / sum);
            res.put("R_geom", ((double) sum) / sumAll);
            return res;
        } catch (ParseException e) {
            help("Failed to parse comand line properties", e);
            return null;
        }

    }

    private static Polygon[] toArray(List<XMLExtractor.Line> lines) {
        Polygon[] res = new Polygon[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            res[i] = lines.get(i).baseLine;
        }
        return res;
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
                "java -cp errorrate.jar eu.transkribus.errorrate.text2image.ErrorRateParser <list_pageXml_groundtruth> <list_pageXml_hypothesis>",
                "This method calculates the precision and recall between two lists of PAGE-XML-files, assuming that the Hypthesis is the result of a Text2Image alignment"
                + " As input it requires two lists of PAGE-XML-files. The first one is the ground truth, the second one is the hypothesis/alignment."
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
        Text2ImageErrorParser erp = new Text2ImageErrorParser();
        Map<String, Double> run = erp.run(args);
        for (String string : run.keySet()) {
            System.out.println(String.format("%10s: %.4f", string, run.get(string)));
        }
    }
}
