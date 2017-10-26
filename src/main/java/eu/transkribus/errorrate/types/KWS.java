/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.types;

import com.google.gson.annotations.Expose;
import eu.transkribus.errorrate.aligner.IBaseLineAligner;
import eu.transkribus.errorrate.util.ObjectCounter;
import eu.transkribus.errorrate.util.PolygonUtil;
import java.awt.Polygon;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gundram
 */
public class KWS {

    public static class MatchList {

        public List<Match> matches;
        public ObjectCounter<Type> counter = new ObjectCounter<>();
        private static Logger LOG = LoggerFactory.getLogger(MatchList.class);
//    private final IBaseLineAligner aligner;

        public MatchList(List<Match> matches) {
//        this(aligner);
            setMatches(matches);
        }

//    private MatchList(IBaseLineAligner aligner) {
//        this.aligner = aligner;
//    }
        public MatchList(Word hypos, Word refs, IBaseLineAligner aligner, double toleranceDefault, double thresh) {
//        this(aligner);
            setMatches(match(hypos, refs, aligner, toleranceDefault, thresh));
        }

        private void setMatches(List<Match> matches) {
            this.matches = matches;
            counter.reset();
            for (Match matche : matches) {
                counter.add(matche.type);
            }
        }

        private int getCount(Type type) {
            return (int) counter.get(type);
        }

        public int getRefSize() {
            return getCount(Type.TRUE_POSITIVE) + getCount(Type.FALSE_NEGATIVE);
        }

        public int getHypSize() {
            return getCount(Type.TRUE_POSITIVE) + getCount(Type.FALSE_POSITIVE);
        }
//
//    public IBaseLineAligner getAligner() {
//        return aligner;
//    }

        private List<Polygon> getPolys(List<KWS.Entry> entries) {
            List<Polygon> res = new LinkedList<>();
            for (KWS.Entry entry : entries) {
                res.add(entry.getBaseLineKeyword());
            }
            return res;
        }

        private List<Match> match(Word hypos, Word refs, IBaseLineAligner aligner, double toleranceDefault, double thresh) {

            String keyWord = hypos.getKeyWord();

            HashMap<String, List<KWS.Entry>> page2PolyHypos = generatePloys(hypos);
            HashMap<String, List<KWS.Entry>> page2PolyRefs = generatePloys(refs);
            HashMap<String, List<Double>> page2Tolerance = getTolerances(refs);
//        HashMap<String, List<Polygon>> page2AllBaselines = getAllLines(ref);
            LinkedList<Match> ret = new LinkedList<>();

            Set<String> pages = new HashSet<String>(page2PolyRefs.keySet());
            pages.addAll(page2PolyHypos.keySet());

            for (String pageID : pages) {
//            List<Polygon> allLines = page2AllBaselines.get(pageID);
                List<KWS.Entry> polyHypos = page2PolyHypos.get(pageID);
                List<KWS.Entry> polyRefs = page2PolyRefs.get(pageID);
                List<Double> toleranceRefs = page2Tolerance.get(pageID);
                if (polyHypos == null) {
                    for (KWS.Entry polyRef : polyRefs) {
                        Match kwsMatch = new Match(Type.FALSE_NEGATIVE, Double.NEGATIVE_INFINITY, polyRef.getBaseLineKeyword(), polyRef.getBaseLineLine(), pageID, keyWord);
                        ret.add(kwsMatch);
                    }
                    continue;
                }
                if (polyRefs == null) {
                    for (KWS.Entry polyHypo : polyHypos) {
                        Match kwsMatch = new Match(Type.FALSE_POSITIVE, polyHypo.getConf(), polyHypo.getBaseLineKeyword(), null, pageID, keyWord);
                        ret.add(kwsMatch);
                    }
                    continue;
                }
                double[] tolerancesVec = new double[toleranceRefs.size()];
                for (int i = 0; i < tolerancesVec.length; i++) {
                    Double d = toleranceRefs.get(i);
                    if (d == null || Double.isNaN(d) || Double.isInfinite(d)) {
                        tolerancesVec[i] = toleranceDefault;
                    } else {
                        tolerancesVec[i] = d;
                    }
                }

                int[][] idcs = uniqueAlignment(
                        aligner.getGTLists(
                                getPolys(polyRefs).toArray(new Polygon[0]),
                                tolerancesVec,
                                getPolys(polyHypos).toArray(new Polygon[0]),
                                thresh
                        )
                );

                Set<Integer> idsNotFound = new HashSet<Integer>();
                for (int i = 0; i < polyRefs.size(); i++) {
                    idsNotFound.add(i);
                }
                for (int i = 0; i < idcs.length; i++) {
                    int[] idsPerHypo = idcs[i];
                    KWS.Entry hypo = polyHypos.get(i);
                    if (idsPerHypo.length > 1) {
                        LOG.error("two ground truch baselines match to one querry baseline. Schould not happen. Don't know what to do, so I ignore it!");
                        continue;
                    }
                    if (idsPerHypo.length > 0) {
//                    for (int id : idsPerHypo) {
                        Entry gt = polyRefs.get(idsPerHypo[0]);
                        idsNotFound.remove(idsPerHypo[0]);
                        Match kwsMatch = new Match(Type.TRUE_POSITIVE, hypo.getConf(), hypo.getBaseLineKeyword(), gt.getBaseLineKeyword(), hypo.getImage(), keyWord);
                        ret.add(kwsMatch);
//                    }
                    } else {
                        Match kwsMatch = new Match(Type.FALSE_POSITIVE, hypo.getConf(), hypo.getBaseLineKeyword(), null, hypo.getImage(), keyWord);
                        ret.add(kwsMatch);
                    }
                }
                for (Integer integer : idsNotFound) {
                    KWS.Entry get = polyRefs.get(integer);
                    Match kwsMatch = new Match(KWS.Type.FALSE_NEGATIVE, Double.NEGATIVE_INFINITY, get.getBaseLineKeyword(), get.getBaseLineLine(), pageID, keyWord);
                    ret.add(kwsMatch);
                }
            }
            return ret;

        }

        public void sort() {
            Collections.sort(matches);
        }

        private static HashMap<String, List<KWS.Entry>> generatePloys(Word kwsWord) {
            HashMap<String, List<KWS.Entry>> ret = new HashMap<>();
            List<KWS.Entry> poss = kwsWord.getPos();
            Collections.sort(poss, new Comparator<KWS.Entry>() {
                @Override
                public int compare(KWS.Entry o1, KWS.Entry o2) {
                    return -Double.compare(o1.getConf(), o2.getConf());
                }
            }
            );
            for (KWS.Entry pos : poss) {
                String pageID = pos.getImage();
                List<KWS.Entry> get = ret.get(pageID);
                if (get == null) {
                    get = new LinkedList<>();
                    ret.put(pageID, get);
                }
                get.add(pos);

            }
            return ret;
        }

        private static HashMap<String, List<Double>> getTolerances(Word kwsWords) {
            HashMap<String, List<Double>> ret = new HashMap<>();
            for (KWS.Entry pos : kwsWords.getPos()) {
                String pageID = pos.getImage();
                List<Double> get = ret.get(pageID);
                if (get == null) {
                    get = new LinkedList<>();
                    ret.put(pageID, get);
                }
                Line parentLine = pos.getParentLine();
                if (parentLine == null) {
                    throw new NullPointerException("for keywords no parent lines are set.");
                }
                get.add(parentLine.getTolerance());

            }
            return ret;
        }

        private static HashMap<String, List<Polygon>> getAllLines(GroundTruth ref) {
            HashMap<String, List<Polygon>> ret = new HashMap<>();
            for (Page page : ref.getPages()) {
                LinkedList<Polygon> pagePolys = new LinkedList<>();
                ret.put(page.getPageID(), pagePolys);
                for (Line line : page.getLines()) {
                    pagePolys.add(line.getBaseline());
                }
            }
            return ret;
        }

        /**
         * Align indices uniquely!
         *
         * ToDo: can produce a false alignment. Das Problem: Aus der gtList wird
         * nach und nach einfach der erstbeste Index entnommen. Das kann aber zu
         * Problemen führen, wenn später ein anderer Match genau diesen schon
         * entnommenen gtList-Index matched, den ignorierten aber nicht mehr.
         * Dann müsste man eigentlich beim ersten Mal einen hinteren Index
         * verwenden und den ersten für den späteren Match aufsparen. Nach
         * hintengeschoben, weil kombinatorisch aufwendig.
         *
         * @param gtLists
         * @return
         */
        private static int[][] uniqueAlignment(int[][] gtLists) {

            int[][] ret = new int[gtLists.length][];
            LinkedList<Integer> refIdcsUsed = new LinkedList<>();
            loop:
            for (int i = 0; i < gtLists.length; i++) {
                int[] gtList = gtLists[i];
                for (int j : gtList) {
                    if (!refIdcsUsed.contains(j)) {
                        ret[i] = new int[]{j};
                        refIdcsUsed.add(j);
                        continue loop;
                    }
                }
                ret[i] = new int[0];
            }
            return ret;
        }
    }

    public enum Type {
        TRUE_POSITIVE, FALSE_NEGATIVE, FALSE_POSITIVE
    }

    public static class Match implements Comparable<Match> {

        public final Type type;
        public final double conf;

        private final Polygon hyp;
        private final Polygon gt;

        private final String pageId;

        private final String word;

//        public Match(Type type, Entry entry, String word) {
//            this(type, entry.getConf(), entry.getBaseLineKeyword(), entry.getBaseLineLine(), entry.getImage(), word);
//        }
        public Match(Type type, double conf, Polygon hyp, Polygon gt, String pageId, String word) {
            this.type = type;
            this.conf = conf;
            this.hyp = hyp;
            this.gt = gt;
            this.pageId = pageId;
            this.word = word;
        }

//        public Match(Type type, double conf, Polygon poly, String pageId, String word) {
//            this.type = type;
//            this.conf = conf;
//            this.hyp = poly;
//            this.pageId = pageId;
//            this.word = word;
//
//        }
        public String getHyp() {
            return word;
        }

        @Override
        public int compareTo(Match o) {
            return Double.compare(o.conf, conf);
        }

        @Override
        public String toString() {
            return "KwsMatch{" + "pageId=" + pageId + ", word=" + word + ", type=" + type + ", conf=" + conf + '}';
        }

        public String getPageId() {
            return pageId;
        }

        public Polygon getKeyword() {
            return hyp;
        }

        public Polygon getGT() {
            return gt;
        }

        public double getConf() {
            return conf;
        }

    }

    public static class Line {

        @Expose
        private Map<String, List<String>> kws = new TreeMap<>();
        private transient HashMap<String, List<Polygon>> kwsL = new HashMap<>();

//    @Expose
//    private Page parent;
        @Expose
        private String bl;
        private transient Polygon blL;
        private transient double tolerance;

        public Line(Polygon baseline) {
            this.blL = baseline;
            this.bl = PolygonUtil.polygon2String(blL);
        }

        public Line(String baseline) {
            this.bl = baseline;
            this.blL = PolygonUtil.string2Polygon(baseline);
        }

        public double getTolerance() {
            return tolerance;
        }

        public void setTolerance(double tolerance) {
            this.tolerance = tolerance;
        }

//    public Line(Page parent) {
//        this.parent = parent;
//    }
        public void addKeyword(String word, Polygon polygon) {
            List<Polygon> getL = kwsL.get(word);
            List<String> get = kws.get(word);
            if (getL == null) {
                getL = new LinkedList<>();
                kwsL.put(word, getL);
                get = new LinkedList<>();
                kws.put(word, get);
            }
            getL.add(polygon);
            get.add(PolygonUtil.polygon2String(polygon));
        }

        public void removeKeyword(String kw) {
            kwsL.remove(kw);
            kws.remove(kw);
        }

        public HashMap<String, List<Polygon>> getKeyword2Baseline() {
            if (kwsL == null) {
                kwsL = new HashMap<>();
                for (String keyword : kws.keySet()) {
                    LinkedList<Polygon> polyL = new LinkedList<>();
                    kwsL.put(keyword, polyL);
                    for (String poly : kws.get(keyword)) {
                        polyL.add(PolygonUtil.string2Polygon(poly));
                    }
                }
            }
            return kwsL;
        }

//    public Page getParent() {
//        return parent;
//    }
        public Polygon getBaseline() {
            if (blL == null && bl != null) {
                blL = PolygonUtil.string2Polygon(bl);
            }
            return blL;
        }
    }

    public static class GroundTruth {

        @Expose
        private List<Page> pages;

        public GroundTruth() {
            pages = new LinkedList<>();
        }

        public GroundTruth(List<Page> pages) {
            this.pages = pages;
        }

        public void addPages(Page page) {
            pages.add(page);
        }

        public List<Page> getPages() {
            return pages;
        }

        public int getRefCount() {
            int cnt = 0;
            for (Page page : pages) {
                for (Line line : page.getLines()) {
                    for (List<String> value : line.kws.values()) {
                        cnt += value.size();
                    }
                }
            }
            return cnt;
        }

    }

    public static class Entry implements Comparable<Entry> {

        @Expose
        private double conf;
        @Expose
        private String bl;
        @Expose
        private String line;
        @Expose
        private String image = null;
        private transient Polygon poly;

        private transient Line parentLine;

        public void setParentLine(Line parentLine) {
            this.parentLine = parentLine;
        }

        public Line getParentLine() {
            return parentLine;
        }

        public Entry(double conf, String lineID, Polygon bl, String pageId) {
            this(conf, lineID, array2String(bl.xpoints, bl.ypoints, bl.npoints), pageId);
            this.poly = bl;
        }

        public Entry(double conf, String lineID, String bl, String pageId) {
            this.conf = conf;
            this.bl = bl;
            this.image = pageId;
            this.line = lineID;
        }

        public Polygon getBaseLineKeyword() {
            if (poly == null) {
                poly = string2Polygon(bl);
            }
            return poly;
        }

        public Polygon getBaseLineLine() {
            return parentLine == null ? null : parentLine.getBaseline();
        }

        public double getConf() {
            return conf;
        }

//    public String getId() {
//        return id;
//    }
        public String getImage() {
            return image;
        }

        public String getLineID() {
            return line;
        }

        public String getBl() {
            return bl;
        }

        @Override
        public String toString() {
            return "Entry{" + "conf=" + conf + ", bl=" + bl + ", lineID=" + line + ", image=" + image + '}';
        }

        @Override
        public int compareTo(Entry o) {
            return Double.compare(o.conf, conf);
        }

        private static Polygon string2Polygon(String string) {
            String[] split = string.split(" ");
            int size = split.length;
            int[] x = new int[size];
            int[] y = new int[size];
            for (int i = 0; i < size; i++) {
                String[] point = split[i].split(",");
                x[i] = Integer.parseInt(point[0]);
                y[i] = Integer.parseInt(point[1]);
            }
            return new Polygon(x, y, size);
        }

        private static String array2String(int[] x, int[] y, int n) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                sb.append(x[i]).append(',').append(y[i]).append(' ');
            }
            return sb.toString().trim();
        }

    }

    public static class Page {

        @Expose
        private String pageID;
        @Expose
        private List<Line> lines = new LinkedList<>();

        public Page(String pageId) {
            this.pageID = pageId;
        }

        /**
         *
         * @param pageId
         * @param lines
         */
        public Page(String pageId, List<Line> lines) {
            this.pageID = pageId;
            this.lines = lines;
        }

        public List<Line> getLines() {
            return lines;
        }

        public String getPageID() {
            return pageID;
        }

        public void addLine(Line line) {
            lines.add(line);
        }

    }

    public static class Result {

        @Expose
        private Set<Word> keywords;

        @Expose
        private Long time;

        public Result(Set<Word> keywords, Long totalTime) {
            this.keywords = keywords;
            this.time = totalTime;
        }

        public Result(Set<Word> keywords) {
            this(keywords, null);
        }

        public Set<Word> getKeywords() {
            return keywords;
        }

        public Long getTotalTime() {
            return time;
        }

        public void setTotalTime(Long totalTime) {
            this.time = totalTime;
        }

        public void append(Result resPart) {
            time += resPart.getTotalTime();
            for (Word wordPart : resPart.getKeywords()) {
                if (keywords.contains(wordPart)) {
                    String keyWord = wordPart.getKeyWord();
                    for (Word word : keywords) {
                        if (word.getKeyWord().equals(keyWord)) {
                            word.addAll(wordPart.getPos());
                            break;
                        }
                        word.time += wordPart.getTime();
                    }
                } else {
                    keywords.add(wordPart);
                }
            }
        }

    }

    public static class Word {

        @Expose
        private String kw;
        @Expose
        LinkedList<Entry> pos = new LinkedList<>();
        @Expose
        private Long time = 0L;

        private int maxSize = -1;
        private double minConf = Double.MAX_VALUE;
        private boolean isSorted = false;
        private ObjectCounter<String> oc = new ObjectCounter<>();

        public Word(String kw, int maxSize, double minConf) {
            this(kw, maxSize);
            this.minConf = minConf;
        }

        public Word(String kw, int maxSize) {
            this.kw = kw;
            this.maxSize = maxSize;
        }

        public double getMinConf() {
            return minConf;
        }

        public void addCount(String key) {
            oc.add(key);
        }

        public ObjectCounter<String> getStatistic() {
            return oc;
        }

        public boolean addAll(Collection<? extends Entry> c) {
            for (Entry entry : c) {
                add(entry);
            }
            return true;
        }

        public Word(String kw) {
            this.kw = kw;
        }

        public String getKeyWord() {
            return kw;
        }

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }

        public void addTime(Long time) {
            this.time += time;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public synchronized void add(KWS.Entry entry) {
            if (maxSize <= 0 || pos.size() < maxSize) {
                pos.add(entry);
                isSorted = false;
//            Collections.sort(pos);
                return;
            }
            if (!isSorted) {
                Collections.sort(pos);
                isSorted = true;
            }
            if (pos.getLast().getConf() < entry.getConf()) {
                pos.removeLast();
                pos.add(entry);
                Collections.sort(pos);
                isSorted = true;
//            System.out.println(minConf + " -> " + entry.getConf());
                minConf = entry.getConf();
            }
        }

        public int size() {
            return pos.size();
        }

        public List<KWS.Entry> getPos() {
            return pos;
        }

        @Override
        public String toString() {
            return "KeyWord{" + "kw=" + kw + ", entries=" + pos + '}';
        }

    }

}
