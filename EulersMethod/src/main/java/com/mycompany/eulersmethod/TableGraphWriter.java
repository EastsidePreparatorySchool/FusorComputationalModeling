/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.EulersMethod;

import com.opencsv.CSVWriter;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author subif
 */
public class TableGraphWriter {


    /**
     *
     * @param table an array of particles to be printed to a CSV file
     * @param columnTitles the titles of the columns of the tables (columns are
     * vertical)
     * @param filePath the file path for the CSV file
     */
    public void writeCSV(Particle[] table, String[] columnTitles, String filePath) {
        FileWriter outputFile = null;
        try {
            File file = new File(filePath);
            file.createNewFile();
            outputFile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputFile);

            writer.writeNext(columnTitles);
            for (Particle row : table) {

                writer.writeNext(row.toCSVString());
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

}
