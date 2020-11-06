/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.fieldlinegeneration;

import com.opencsv.CSVWriter;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 *
 * @author subif
 */
public class TableGraphWriter {

    /**
     *
     * @param table a list of fieldLines to be printed to a CSV file
     * @param columnTitles the titles of the columns of the tables (columns are
     * vertical)
     * @param filePath the file path for the CSV file
     */
    public void writeCSV(LinkedList<LinkedList<Vector>> table, String[] columnTitles, String filePath) {
        FileWriter outputFile = null;
        try {
            File file = new File(filePath);
            file.createNewFile();
            outputFile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputFile);

            writer.writeNext(columnTitles);
            int lineNumber = 1;
            for (LinkedList<Vector> streamLine : table) {
                for(Vector v: streamLine){
                writer.writeNext(pointToCSVString(lineNumber, v));
                }
                lineNumber++;
            }

            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace(); // try catch for file stuff
        } finally {
            try {
                outputFile.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
 
    public String[] pointToCSVString(int lineNumber, Vector point) {
        String[] csvString = {"" + lineNumber, "" + point.x, "" + point.y, "" + point.z};
        return csvString;
    }

}
