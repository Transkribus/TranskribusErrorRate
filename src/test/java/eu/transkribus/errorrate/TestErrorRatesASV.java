/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate;

import eu.transkribus.errorrate.htr.ErrorModuleBagOfTokens;
import eu.transkribus.errorrate.htr.ErrorModuleDynProg;
import eu.transkribus.errorrate.costcalculator.CostCalculatorDft;
import eu.transkribus.errorrate.costcalculator.CostCalculatorDftUpper;
import eu.transkribus.errorrate.interfaces.ICostCalculator;
import eu.transkribus.errorrate.interfaces.IErrorModule;
import eu.transkribus.errorrate.normalizer.StringNormalizerDft;
import eu.transkribus.errorrate.normalizer.StringNormalizerLetterNumber;
import eu.transkribus.errorrate.types.Count;
import eu.transkribus.interfaces.IStringNormalizer;
import eu.transkribus.interfaces.ITokenizer;
import eu.transkribus.tokenizer.TokenizerConfig;

import java.text.Normalizer;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * Here every one can add groundtruth (GT) and hypothesis (HYP) text. Then some
 * parameters can be given (uppercase, word error rate,bag of words and only
 * letters and numbers). The output is a map with counts on the comparison.
 * Counts are:<br>
 * Dynamic programming (bagoftokens==false):<br>
 * COR=Correct<br>
 * SUB=Substitution<br>
 * INS=Insertion (gt has more tokens)<br>
 * DEL=Deletion (hyp has more tokens)<br>
 * <br>
 * Bag of Tokens (bagoftokens==true):<br>
 * TP=True Positives <br>
 * FN=False Negatives (gt has more tokens)<br>
 * FP=False Posotives (hyp has more tokens)<br>
 *
 * @author gundram
 */
public class TestErrorRatesASV {

    @Test
    public void testLineBreak() {
        Assert.assertEquals(new Long(4), getCount(false, true, false, false, "\n this is \n with\n linebreaks\n ", "this is with linebreaks").get(Count.COR));
    }

    @Test
    public void testOrder() {
        Assert.assertEquals(new Long(1), getCount(false, true, false, false, "this is text ", "is this text").get(Count.COR));
        Assert.assertEquals(new Long(3), getCount(false, true, true, false, "this is text ", "is this text").get(Count.TP));
    }

    @Test
    public void testComposition() {
        Assert.assertEquals(new Long(1), getCount(false, true, false, false, "sa\u0308ße", "säße").get(Count.COR));
        Assert.assertEquals(new Long(1), getCount(true, true, false, false, "SA\u0308SSE", "säße").get(Count.COR));
    }

    @Test
    public void testTokenizer() {
        Assert.assertEquals(new Long(1), getCount(false, true, false, false, "it's wrong", "its wrong").get(Count.COR));
        Assert.assertEquals(new Long(2), getCount(false, true, false, false, "its wrong", "its wrong").get(Count.COR));
        Assert.assertEquals(new Long(3), getCount(false, true, false, false, "its, wrong", "its, wrong").get(Count.COR));
//        Assert.assertEquals(2, get(false, true, false, true, "COR", "it's wrong", "its wrong"));
    }

    @Test
    public void testErrorType() {
        Assert.assertEquals(new Long(1), getCount(false, true, false, false, "its, wrong", "its wrong").get(Count.INS));
        Assert.assertEquals(new Long(1), getCount(false, true, false, false, "its, wrong", "its. wrong").get(Count.SUB));//substitution
        Assert.assertEquals(new Long(2), getCount(false, true, false, false, "its, wrong", "its. wrong").get(Count.COR));//correct
        Assert.assertEquals(new Long(2), getCount(false, true, true, false, "its, wrong", "its. wrong").get(Count.TP));//true positive
        Assert.assertEquals(new Long(1), getCount(false, true, true, false, "its, wrong", "its. wrong").get(Count.FN));//false negative
        Assert.assertEquals(new Long(1), getCount(false, true, true, false, "its, wrong", "its. wrong").get(Count.FP));//false positive
        Assert.assertEquals(new Long(2), getCount(false, true, false, false, "wrong", "its, wrong").get(Count.DEL));
        Assert.assertEquals(new Long(2), getCount(false, true, true, false, "wrong", "its, wrong").get(Count.FP));
//        Assert.assertEquals(2, get(false, true, false, true, "COR", "it's wrong", "its wrong"));
    }

    @Test
    public void testLetter() {
        Assert.assertEquals(new Long(1), getCount(false, true, false, false, "it's wrong", "its wrong").get(Count.COR));
        Assert.assertEquals(new Long(2), getCount(false, true, false, true, "it's wrong", "its wrong").get(Count.COR));
    }

    public Map<Count, Long> getCount(boolean upper, boolean word, boolean bagoftokens, boolean letterNumber, String gt, String hyp) {
        System.out.println("\"" + gt + "\" vs \"" + hyp + "\"");
        ICostCalculator cc = upper ? new CostCalculatorDft() : new CostCalculatorDftUpper();
        ITokenizer tokenizer = new TokenizerConfig("src/test/res/tokenizer_config.properties");
//        ITokenizer tokenizer = new TokenizerCategorizer(word ? new CategorizerWordDft() : new CategorizerCharacterDft());
        IStringNormalizer sn = new StringNormalizerDft(Normalizer.Form.NFKC, upper);
        if (letterNumber) {
            sn = new StringNormalizerLetterNumber(sn);
        }
        IErrorModule impl = bagoftokens ? new ErrorModuleBagOfTokens(tokenizer, sn, false) : new ErrorModuleDynProg(cc, tokenizer, sn, false);
        impl.calculate(hyp, gt);
        return impl.getCounter().getMap();
    }

    @Test
    public void testCaseSensitive() {
        System.out.println("CaseSensitive");

    }
}
