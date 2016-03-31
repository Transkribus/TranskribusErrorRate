/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.types;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gundram, http://www.coderanch.com/t/660196/Wiki/Extended-Properties
 */
public class Properties {

    protected Map<String, String> map;
    protected Map<String, String> mapChangeable;
    private List<String> order;
    private Charset cs = Charset.forName("UTF-8");

    public Properties() {
        map = new HashMap<>();
        mapChangeable = new HashMap<>();
        order = new ArrayList<>();
    }

//    private static final String keyValueSeparators = "=: \t\r\n\f";
    private static final String keyValueSeparators = "=:";

    private static final String strictKeyValueSeparators = "=:";

//    private static final String specialSaveChars = "=: \t\r\n\f#!";
    private static final String specialSaveChars = "=:#!";

//    private static final String whiteSpaceChars = " \t\r\n\f";
    public synchronized void load(String fileName) {
        try {
            load(new FileInputStream(fileName));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public synchronized void load(InputStream inStream) {
        mapChangeable = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream, cs));
        while (true) {
            try {
                // Get next line
                String line = in.readLine();
                if (line == null) {
                    return;
                }

                if (line.length() > 0) {

                    // Find start of key
                    int len = line.length();
                    int keyStart = 0;
//                    for (keyStart = 0; keyStart < len; keyStart++) {
//                        if (whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1) {
//                            break;
//                        }
//                    }

                    // Blank lines are ignored
                    if (keyStart == len) {
                        continue;
                    }

                    // Continue lines that end in slashes if they are not comments
                    char firstChar = line.charAt(keyStart);
                    if ((firstChar == '#') || (firstChar == '!')) {
                        // add comment to order list
                        order.add(line.substring(keyStart));
                    } else {
                        while (continueLine(line)) {
                            String nextLine = in.readLine();
                            if (nextLine == null) {
                                nextLine = "";
                            }
                            String loppedLine = line.substring(0, len - 1);
                            // Advance beyond whitespace on new line
                            int startIndex = 0;
//                            for (startIndex = 0; startIndex < nextLine.length(); startIndex++) {
//                                if (whiteSpaceChars.indexOf(nextLine.charAt(startIndex)) == -1) {
//                                    break;
//                                }
//                            }
                            nextLine = nextLine.substring(startIndex, nextLine.length());
                            line = loppedLine + nextLine;
                            len = line.length();
                        }

                        // Find separation between key and value
                        int separatorIndex;
                        for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
                            char currentChar = line.charAt(separatorIndex);
                            if (currentChar == '\\') {
                                separatorIndex++;
                            } else if (keyValueSeparators.indexOf(currentChar) != -1) {
                                break;
                            }
                        }

                        // Skip over whitespace after key if any
                        int valueIndex = separatorIndex;
//                        for (valueIndex = separatorIndex; valueIndex < len; valueIndex++) {
//                            if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1) {
//                                break;
//                            }
//                        }

                        // Skip over one non whitespace key value separators if any
                        if (valueIndex < len) {
                            if (strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1) {
                                valueIndex++;
                            }
                        }

                        // Skip over white space after other separators if any
//                        while (valueIndex < len) {
//                            if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1) {
//                                break;
//                            }
//                            valueIndex++;
//                        }
                        String key = line.substring(keyStart, separatorIndex);
                        String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";

                        // Convert then store key and value
                        key = loadConvert(key);
                        value = loadConvert(value);
                        map.put(key, value);
                        // add key value to order list
                        order.add(key);
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /*
     * Returns true if the given line is a line that must be appended to the next line
     */
    private boolean continueLine(String line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while ((index >= 0) && (line.charAt(index--) == '\\')) {
            slashCount++;
        }
        return (slashCount % 2 == 1);
    }

    /*
     * Converts encoded &#92;uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    private String loadConvert(String theString) {
        char aChar;
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len);

        for (int x = 0; x < len;) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    switch (aChar) {
                        case 't':
                            aChar = '\t';
                            break;
                        case 'r':
                            aChar = '\r';
                            break;
                        case 'n':
                            aChar = '\n';
                            break;
                        case 'f':
                            aChar = '\f';
                            break;
                        default:
                            break;
                    }
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    /*
     * writes out any of the characters in specialSaveChars with a preceding slash
     */
    private String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len * 2);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(' ');
                    break;
                case '\\':
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                default:
                    if (specialSaveChars.indexOf(aChar) != -1) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    public synchronized void save(OutputStream out, String header) throws IOException {
        BufferedWriter awriter;
        awriter = new BufferedWriter(new OutputStreamWriter(out, cs));
        if (header != null) {
            writeln(awriter, "#" + header);
        }
        writeln(awriter, "#" + new Date().toString());

        Set<String> newKeys = new HashSet<>(map.keySet());
        for (String str : order) {
            if ((str.charAt(0) == '#') || (str.charAt(0) == '!')) {
                writeln(awriter, str);
            } else if (newKeys.contains(str)) {
                String key = saveConvert(str, true);
                String val = saveConvert(map.get(key), false);
                writeln(awriter, key + "=" + val);
                newKeys.remove(str);
            }
        }
        for (Iterator<String> iter = newKeys.iterator(); iter.hasNext();) {
            String key = saveConvert(iter.next(), true);
            String val = saveConvert(map.get(key), false);
            writeln(awriter, key + "=" + val);
        }

        awriter.flush();
    }

    private static void writeln(BufferedWriter bw, String s) throws IOException {
        bw.write(s);
        bw.newLine();
    }

    public String getProperty(String key) {
        return map.get(key);
    }

    public String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return (val == null) ? defaultValue : val;
    }

    public synchronized String setProperty(String key, String value) {
        return map.put(key, value);
    }

    public synchronized String removeProperty(String key) {
        return map.remove(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Iterator<String> propertyNames() {
        return map.keySet().iterator();
    }

}
