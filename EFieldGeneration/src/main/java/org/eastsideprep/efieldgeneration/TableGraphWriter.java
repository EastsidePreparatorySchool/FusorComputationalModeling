/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.efieldgeneration;

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
     * Prints the charges into an output file
     *
     * @param charges
     * @param filePath
     */
    public void writeCSV(Charge[] charges, String filePath) {
        FileWriter outputFile = null;
        try {
            File file = new File(filePath);
            file.createNewFile();
            outputFile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputFile);

            String[] header = {"X", "Y", "Z", "Polarity"};
            writer.writeNext(header);
            for (Charge c : charges) {
                writer.writeNext(c.toCSVString());
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

    /**
     * Prints the charges into an output file
     *
     * @param chargeDistributer
     * @param filePath
     */
    public void writeCSV(ChargeDistributer chargeDistributer, String filePath) {
        Charge[] charges = chargeDistributer.charges;
        FileWriter outputFile = null;
        try {
            File file = new File(filePath);
            file.createNewFile();
            outputFile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputFile);

            String[] header = {"X", "Y", "Z", "Polarity"};
            writer.writeNext(header);
            for (Charge c : charges) {
                writer.writeNext(c.toCSVString());
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
