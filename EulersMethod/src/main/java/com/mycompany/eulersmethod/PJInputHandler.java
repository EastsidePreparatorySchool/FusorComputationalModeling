/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.EulersMethod;

import java.io.File;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author pjain
 */
public class PJInputHandler {

    int numCharges = 100;
    String positiveFilePath = "ThinRightPlate.csv";
    String negativeFilePath = "ThinPlate.csv";
    String outputFilePath = "outputFilePath.csv";
    String inputFilePath = "inputFilePath.csv";
    Double posCharge = 100.0;
    Double negCharge = 100.0;
    Double scaleDistance = 0.01;
    int shakeUps = 100;
    Scanner s;
    EField eField;
    Charge[] charges;
    Charge[] positiveCharges;
    Charge[] negativeCharges;

    Double stepSize = 1E-4;
    Boolean skipInput = false;

    public PJInputHandler() {

    }

    public void getInput() {

        if (!skipInput) {
            s = new Scanner(System.in);
            readFromFile();

            s.close();

        }
    }

    /**
     *
     * @param PJ
     * @param MY
     * @param initial
     * @param numberOfSteps
     * @param stepSize
     * @param outputFilePath
     * @return
     */
    public void orbitStuff(Boolean PJ, Boolean MY, Particle initial, Double numberOfSteps, Double stepSize, String outputFilePath, Boolean batch, int batchSize, Boolean eu, Boolean rk) {
        Particle[] particles = new Particle[1];
        initial.setScaleDistance(scaleDistance);
        initial.totalEnergy(eField);
        if (batch) {
            if (PJ && eu) {
                PJEulersMethod pj = new PJEulersMethod(eField);
                particles = pj.epoch(initial, stepSize, numberOfSteps, batchSize);
            } else if (MY && eu) {
                MYEulersMethod my = new MYEulersMethod(eField);
                particles = my.epoch(initial, stepSize, numberOfSteps, batchSize);

            } else if (PJ && rk) {

                PJRungeKutta pj = new PJRungeKutta(eField);
                particles = pj.epoch(initial, stepSize, numberOfSteps, batchSize);
            } else if (MY && rk) {
                MYRungeKutta my = new MYRungeKutta(eField);
                particles = my.epoch(initial, stepSize, numberOfSteps, batchSize);
            }

        } else {
            particles = new Particle[numberOfSteps.intValue()];
            if (PJ && eu) {

                PJEulersMethod pj = new PJEulersMethod(eField);
                particles[0] = initial;
                Particle p = particles[0].clone();
                for (int i = 1; i < numberOfSteps; i++) {
                    particles[i] = pj.step(p, stepSize).clone();
                    p = particles[i];
                }

            } else if (MY && eu) {

                MYEulersMethod my = new MYEulersMethod(eField);
                particles[0] = initial;
                Particle p = particles[0].clone();
                for (int i = 1; i < numberOfSteps; i++) {
                    particles[i] = my.step(p, stepSize).clone();
                    p = particles[i];
                }

            } else if (PJ && rk) {

                PJRungeKutta pj = new PJRungeKutta(eField);
                particles[0] = initial;
                Particle p = particles[0].clone();
                for (int i = 1; i < numberOfSteps; i++) {
                    particles[i] = pj.step(p, stepSize).clone();
                    p = particles[i];
                }

            } else if (MY && rk) {
                MYRungeKutta my = new MYRungeKutta(eField);
                particles[0] = initial;
                Particle p = particles[0].clone();
                for (int i = 1; i < numberOfSteps; i++) {
                    particles[i] = my.step(p, stepSize).clone();
                    p = particles[i];
                }
            }
        }

        String[] headers = {"X", "Y", "Z", "Vx", "Vy", "Vz", "Polarity", "Charge", "Time",
            "Mass", "Electric Potential Energy", "Kinetic Energy", "Total Energy"};

        TableGraphWriter tgw = new TableGraphWriter();

        tgw.writeCSV(particles, headers, outputFilePath);

    }

    public Particle randomParticle() {
        Random r = new Random();
        Vector position = new Vector(r.nextDouble(), r.nextDouble(), r.nextDouble());
        Vector velocity = new Vector(r.nextDouble(), r.nextDouble(), r.nextDouble());
        Particle p = new Particle(position, velocity, r.nextInt(), r.nextDouble());
        return p;
    }

    // gets user input on selecting a file for input
    String fileNameGet(String mainText) {
        System.out.println(mainText);
        String filePath = s.nextLine();
        File fileExists = new File(filePath);
        System.out.println(fileExists);
        while (!fileExists.exists()) {
            System.out.println("this file does not exist; please enter a valid file name");
            filePath = s.nextLine();
            System.out.println(filePath);
            fileExists = new File(filePath);
        }
        return filePath;
    }

    // gets user input for creating a file
    String fileCreate(String mainText) {
        System.out.println(mainText);
        String filePath = s.nextLine();
        File fileExists = new File(filePath);
        while (fileExists.exists()) {
            System.out.println("This file already exists; do you want to replace it? Y/N");
            String input = "";
            input = s.nextLine();
            if (input.equals("Y") || input.equals("y")) {
                break;
            } else {
                System.out.println(mainText);
                filePath = s.nextLine();
                fileExists = new File(filePath);
            }

        }
        return filePath;
    }

    Particle getInitialParticle() {
        System.out.println("Initial Position (recommended >-80 and <80 \n" + " x: ");
        Double x = Double.valueOf(s.nextLine());

        System.out.println("y: ");
        Double y = Double.valueOf(s.nextLine());

        System.out.println("z: ");
        Double z = Double.valueOf(s.nextLine());

        System.out.println("Initial Velocity \n" + " Vx: ");
        Double Vx = Double.valueOf(s.nextLine());

        System.out.println("Vy: ");
        Double Vy = Double.valueOf(s.nextLine());

        System.out.println("Vz: ");
        Double Vz = Double.valueOf(s.nextLine());

        System.out.println("Polarity: ");
        int polarity = Integer.valueOf(s.nextLine());

        System.out.println("Charge (measured in elementary charge [charge of an electron or proton]): ");
        Double charge = Double.valueOf(s.nextLine()) * 1.602E-19;

        System.out.println("To denote a number several decimals below the ones position,"
                + "\n use the following 2.014E-27, where -27 is the number of,"
                + "\n digits behind the ones position that the number starts");
        System.out.println("Intital Time (seconds): ");
        Double time = Double.valueOf(s.nextLine());

        System.out.println("Mass (in Atomic Mass Units, for Deuterium this is 2.0141)");
        Double mass = 2.014102 * 1.66053906660E-27;;

        return new Particle(x, y, z, Vx, Vy, Vz, polarity, charge, time, mass);
    }

    public void readFromFile() {
        Boolean PJ = null;
        Boolean MY = null;
        Boolean eu = null; 
        Boolean rk = null;
        Boolean batch = null;
        int batchSize = 0;
        Double numberOfSteps;
        Particle initial;
        boolean inputReceived;
        Double vAnnode = null;
        Double vCathode = null;
        int i = 0;
                

        inputFilePath = "outputChamberGrid20kCharges1kShakes.csv";

        System.out.println("Do you want to skip the input process?");
        String input;
        input = s.nextLine();
        
        if (input.equals("Y") || input.equals("y")) {
            inputReceived = true;
            skipInput = true;      

        } else if (input.equals("N") || input.equals("n")) {
            inputReceived = true;
            skipInput = false;
        } else{
            System.out.println("Please respond with a (Y) or (N)");
        }
        
        outputFilePath = "PJinputrktest" + i + ".csv";
        File fileExists = new File(outputFilePath);
        while(fileExists.exists()){
        i++;
        System.out.println("hello");
        outputFilePath = "PJinputrktest" + i + ".csv";
        }
        
        if (skipInput) {
            

            EFieldFileParser parser = new EFieldFileParser();
            Charge[][] chargeArrayArray = parser.parseFile(inputFilePath);

            charges = chargeArrayArray[0];
            positiveCharges = chargeArrayArray[1];
            negativeCharges = chargeArrayArray[2];

            eField = new EField(charges, 1.0, -40000.0, 0.001, new Vector(0.0, 0.0, 0.0));
            
            //put variables up above w/values instead of directly in particle and orbitStuff
                   
            Particle Partikel = new Particle (70.0, 70.0, 70.0, 0.0, 0.0, 0.0, 1, 1.0, 0.0); //you can decide the values of the velocity, position, etc. of the particle here
            //Particle(Double x, Double y, Double z, Double vx, Double vy,Double vz, int polarity, Double charge, Double time)
            
            orbitStuff(true,false, Partikel, 1000.0, 1e-10, outputFilePath,true, 100, false, true); 

            //orbitStuff(Boolean PJ, Boolean MY, Particle initial, Double numberOfSteps, Double stepSize, String outputFilePath, Boolean batch, int batchSize, Boolean eu, Boolean rk) {
            // you can also decide what values to put in 
        
            
            
        } else{
            inputFilePath = fileNameGet("Please enter your input (including the .csv): ");

            System.out.println("Please enter the anode (positive) voltage");
            vAnnode = Double.valueOf(s.nextLine());

            System.out.println("Please enter the cathode (negative) voltage");
            vCathode = Double.valueOf(s.nextLine());

            System.out.println("What is the distance, in meters, of 1 unit in the stl files?");
            scaleDistance = Double.valueOf(s.nextLine());

            inputReceived = false;

            EFieldFileParser parser = new EFieldFileParser();

            Charge[][] chargeArrayArray = parser.parseFile(inputFilePath);
            charges = chargeArrayArray[0];
            positiveCharges = chargeArrayArray[1];
            negativeCharges = chargeArrayArray[2];

            eField = new EField(charges, vAnnode, vCathode, scaleDistance, new Vector(0.0, 0.0, 0.0));
            Boolean calculateOrbit = false;
            input = "";
            inputReceived = false;
            while (!inputReceived) {
                System.out.println("Do you want to calculate an orbit?(Y/N)");

                input = s.nextLine();
                if (input.equals("Y") || input.equals("y")) {
                    inputReceived = true;
                    calculateOrbit = true;
                } else if (input.equals("N") || input.equals("n")) {
                    inputReceived = true;
                    calculateOrbit = false;
                } else {
                    System.out.println("Please respond with (Y) or (N)");
                }

            }
            inputReceived = false;
            input = "";
            if (calculateOrbit) {
                initial = getInitialParticle();

                outputFilePath = fileCreate("Please enter the orbit output file name \n"
                        + " (including extensions like .csv): ");
                System.out.println("Step Size: ");
                stepSize = Double.valueOf(s.nextLine());

                System.out.println("Number of Steps:");
                numberOfSteps = Double.valueOf(s.nextLine());

                inputReceived = false;

                while (!inputReceived) {
                    System.out.println("Who's Orbit do you want? Praveer (PJ) Maggie (MY)");
                    input = s.nextLine();
                    if (input.equals("PJ") || input.equals("Pj") || input.equals("pJ") || input.equals("pj")) {
                        inputReceived = true;
                        PJ = true;
                    } else if (input.equals("MY") || input.equals("My") || input.equals("mY") || input.equals("my")) {
                        inputReceived = true;
                        MY = true;
                    } else {
                        System.out.println("Please respond with (PJ) or (MY)");
                    }
                }

                input = "";
                inputReceived = false;
                while (!inputReceived) {
                    System.out.println("Which method do you want? Eulers (EU) Runge Kutta (RK)");
                    input = s.nextLine();
                    if (input.equals("EU") || input.equals("Eu") || input.equals("eU") || input.equals("eu")) {
                        inputReceived = true;
                        eu = true;
                    } else if (input.equals("RK") || input.equals("Rk") || input.equals("rK") || input.equals("rk")) {
                        inputReceived = true;
                        rk = true;
                    } else {
                        System.out.println("Please respond with (EU) or (RK)");
                    }
                }

                input = "";
                inputReceived = false;
                while (!inputReceived && numberOfSteps < 100000) {
                    System.out.println("Do want to split them into batches? (Y/N)");

                    input = s.nextLine();
                    if (input.equals("Y") || input.equals("y")) {
                        inputReceived = true;
                        batch = true;
                    } else if (input.equals("N") || input.equals("n")) {
                        inputReceived = true;
                        batch = false;
                    } else {
                        System.out.println("Please respond with (Y) or (N)");
                    }

                }

                if (batch) {
                    if (numberOfSteps > 100000) {
                        System.out.println("Your number of steps is too large without batching");
                    }
                    System.out.println("Please enter the size of a batch");
                    batchSize = Integer.valueOf(s.nextLine());
                }

                orbitStuff(PJ, MY, initial, numberOfSteps, stepSize, outputFilePath, batch, batchSize, eu, rk);
            }

            inputReceived = false;
            input = "";


        }

    }

}
