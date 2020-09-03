/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.EulersMethod;

/**
 *
 * @author subif
 */
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class EFieldFileParser {

    public Charge[][] parseFile(String filePath) {
        Reader reader;
        reader = null;
        List<String[]> rawCharges;

        Charge[] charges = null;
        Charge[] posCharges = null;
        Charge[] negCharges = null;
        try {
            reader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withSkipLines(1)
                    .build();
            rawCharges = csvReader.readAll();
            reader.close();
            csvReader.close();
            charges = new Charge[rawCharges.size()];
            posCharges = new Charge[rawCharges.size()/2];
            negCharges = new Charge[rawCharges.size()/2];
            int i = 0;
            int j = 0; // positive charge counter
            int k = 0; // negative charge counter
            for (String[] s : rawCharges) {
                charges[i] = (new Charge(s));
                if(charges[i].polarity == 1){
                    posCharges[j] = charges[i];
                    j++;
                }else{
                    negCharges[k] = charges[i];
                    k++;
                }
                i++;
            }

        } catch (FileNotFoundException ex) {
            System.out.println(" file not found");
        } catch (IOException ex) {
            System.out.println("io exception");
        }
        Charge[][] chargeArrayArray = {charges, posCharges, negCharges};
        
        return chargeArrayArray;

    }

}
