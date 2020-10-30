/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.core;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gunnar
 */
public class SimpleParser {

    Scanner sc;
    int lineNumber;
    String fileName;
    Matcher matcher;

    SimpleParser(String fileName, InputStream is) {
        sc = new Scanner(is);
        lineNumber = 0;
        this.fileName = fileName;
    }

    void error(String msg) {
        throw new IllegalArgumentException(msg + ", file " + fileName + " line " + lineNumber);
    }

    void assertEqual(Object o1, Object[] o2, String msg) {
        for (Object o : o2) {
            if (o1.equals(o)) {
                return;
            }
        }
        error("Actual: " + o1.toString() + ", expected: " + Arrays.toString(o2) + ", " + msg);
    }

    boolean more() {
        return sc.hasNextLine();
    }

    String readLine() {
        lineNumber++;
        return sc.nextLine();
    }

    void skipLine() {
        readLine();
    }

    void assertAndSkipLine(String text) {
        String line = readLine();
        if (!line.equals(text)) {
            error("Expected line: '" + text + "', saw: '" + line + "'");
        }
    }

    void assertAndSkipLineStart(String text) {
        String line = readLine();
        if (!line.startsWith(text)) {
            error("Expected line start: '" + text + "', saw: '" + line + "'");
        }
    }

    String getString(String p) {
        try {
            Pattern pattern = Pattern.compile(p);
            String line = readLine();
            matcher = pattern.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            error("Did not find pattern '" + p + "': " + e.getMessage());
        }
        error("Did not find pattern '" + p + "'");
        return null;
    }

    int getInteger(String p) {
        String text = "";
        try {
            p = p.replaceAll("\\$i","([0-9]*)");
            text = getString(p);
            return Integer.parseInt(text);
        } catch (Exception e) {
            error("Not an integer for pattern '"+p+"': " + text);
            return 0;
        }
    }

    double getDouble(String p) {
        String text = "";
        try {
            p = p.replaceAll("\\$d", "([0-9]*\\.?[0-9]+)([eE][-+]?[0-9]+)?");
            text = getString(p);
            return Double.parseDouble(text);
        } catch (Exception e) {
            error("Not a double: " + text);
            return 0.0;
        }
    }

    double getDouble() {
        String text = "";
        String text2 = "";
        try {
            text = getString("([0-9]*\\.?[0-9]+)([eE][-+]?[0-9]+)? *([0-9]*\\.?[0-9]+)([eE][-+]?[0-9]+)? *([0-9]*\\.?[0-9]+)([eE][-+]?[0-9]+)? *([0-9]*\\.?[0-9]+)([eE][-+]?[0-9]+)?");
            text2 = getString(2);
            if (text2 != null) {
                text += text2;
            }
            return Double.parseDouble(text);
        } catch (Exception e) {
            error("Not a double: " + text + text2);
            return 0.0;
        }
    }

    String getString(int index) {
        try {
            return matcher.group(index);
        } catch (Exception e) {
            error("Part " + index + " of pattern not found: " + e.getMessage());
        }
        return null;
    }

    int getInteger(int index) {
        String text = "";
        try {
            text = getString(index);
            return Integer.parseInt(text);
        } catch (Exception e) {
            error("Not an integer for matching group " + index + ": " + text);
        }
        return 0;
    }

    double getDouble(int index) {
        String text = "";
        String text2 = "";
        try {
            text = getString(2 * index - 1);
            text2 = getString(2 * index);
            if (text2 != null) {
                text += text2;
            }
            return Double.parseDouble(text);
        } catch (Exception e) {
            error("Not a double: " + text + text2);
            return 0.0;
        }
    }
}
