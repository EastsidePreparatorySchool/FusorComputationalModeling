/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.core;

import java.io.InputStream;
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

    void assertEqual(Object o1, Object o2, String msg) {
        if (!o1.equals(o2)) {
            error(msg);
        }
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
        Pattern pattern = Pattern.compile(p);
        String line = readLine();
        matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        error("Did not find pattern '" + p + "'");
        return null;
    }

    int getInteger(String p) {
        String text = getString(p);
        return Integer.parseInt(text);
    }

    double getDouble(String p) {
        String text = getString(p);
        return Double.parseDouble(text);
    }

    double getDouble() {
        String text = getString("([0-9]*\\.?[0-9]+)([eE][-+]?[0-9]+)? *([0-9]*\\.?[0-9]+)([eE][-+]?[0-9]+)? *([0-9]*\\.?[0-9]+)([eE][-+]?[0-9]+)? *([0-9]*\\.?[0-9]+)([eE][-+]?[0-9]+)?");
        String text2 = getString(2);
        if (text2 != null) {
            text += text2;
        }
        return Double.parseDouble(text);
    }

    String getString(int index) {
        return matcher.group(index);
    }

    int getInteger(int index) {
        String text = getString(index);
        return Integer.parseInt(text);
    }

    double getDouble(int index) {
        String text = getString(2 * index - 1);
        String text2 = getString(2 * index);
        if (text2 != null) {
            text += text2;
        }
        return Double.parseDouble(text);
    }
}
