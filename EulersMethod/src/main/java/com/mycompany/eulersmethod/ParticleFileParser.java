/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.EulersMethod;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 *
 * @author subif
 */
public class ParticleFileParser {

    public Particle[] parseFile(String filePath) {
        Reader reader;
        reader = null;
        List<String[]> rawParticles;

        Particle[] particles = null;

        try {
            reader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withSkipLines(1)
                    .build();
            rawParticles = csvReader.readAll();
            reader.close();
            csvReader.close();
            particles = new Particle[rawParticles.size()];
            int i = 0;
            int j = 0; // positive charge counter
            int k = 0; // negative charge counter
            for (String[] s : rawParticles) {
                particles[i] = (new Particle(s));

            }

        } catch (FileNotFoundException ex) {
            System.out.println(" file not found");
        } catch (IOException ex) {
            System.out.println("io exception");
        }

        return particles;

    }
}
