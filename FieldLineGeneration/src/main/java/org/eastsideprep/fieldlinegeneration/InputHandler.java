/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.fieldlinegeneration;

/**
 *
 * @author subif
 */
import java.io.File;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class InputHandler {

    int numCharges = 100;
    String outputFilePath = "outputFile.csv";
    String inputFilePath = "inputFilePath.csv";
    Double posCharge = 100.0;
    Double negCharge = 100.0;
    Double scaleDistance = 0.001;
    Double vAnnode;
    Double vCathode;
    int shakeUps = 100;
    Scanner s;
    EField eField;
    Charge[] charges;
    Charge[] positiveCharges;
    Charge[] negativeCharges;

    public InputHandler() {
    }

    public void getInput() {
        s = new Scanner(System.in);
        readFromFile();

        s.close();
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

    GridBox makeBoundingBox(Double scaleDistance) {
        GridBox bounds;
        Boolean inputReceived = false;
        System.out.println("Give the Bottom lower Corner Coordinates");
        System.out.println("x:");
        Double blcx = Double.valueOf(s.nextLine());
        System.out.println("y:");
        Double blcy = Double.valueOf(s.nextLine());
        System.out.println("z:");
        Double blcz = Double.valueOf(s.nextLine());

        System.out.println("upper right corner");
        System.out.println("x:");
        Double urcx = Double.valueOf(s.nextLine());
        System.out.println("y:");
        Double urcy = Double.valueOf(s.nextLine());
        System.out.println("z:");
        Double urcz = Double.valueOf(s.nextLine());
        Vector bl = new Vector(blcx, blcy, blcz);
        Vector ur = new Vector(urcx, urcy, urcz);
        bl.scale(scaleDistance);
        ur.scale(scaleDistance);
        bounds = new GridBox(bl, ur);

        return bounds;
    }

    public void readFromFile() {
        boolean inputRecieved = false;
        GridBox bounds;
        Double stepSize = 0.001;
        Double threshold = 0.001;
        int numberOfGaps = 12;
        int checkerBoard = 2;
        Double vAnnode = 1.0;
        Double vCathode = -40000.0;
        Boolean skipInput = false;
        inputFilePath = "outputChamberGrid20kCharges1kShakes.csv";
        String outputFilePath = "testFieldLine1.csv";
        /*
        outputFilePath = fileCreate("please enter the output file location: ");
        
        System.out.println("skip input? (Y/N)");
        String input = "";
        inputReceived = false;
        while (!inputReceived) {
            input = s.nextLine();
            if (input.equals("Y") || input.equals("y")) {
                skipInput = true;
                inputReceived = true;
                break;
            } else if (input.equals("N") || input.equals("n")) {
                skipInput = false;
                inputReceived = true;
                break;
            } else {
                System.out.println("Please enter (Y) or (N)");
            }
        }
        if (!skipInput) {
            inputFilePath = fileNameGet("Please enter your input (including the .csv): ");

            System.out.println("Please enter the anode (positive) voltage");
            vAnnode = Double.valueOf(s.nextLine());

            System.out.println("Please enter the cathode (negative) voltage");
            vCathode = Double.valueOf(s.nextLine());

            System.out.println("What is the distance, in meters, of 1 unit in the stl files?");
            scaleDistance = Double.valueOf(s.nextLine());

            System.out.println("What is the step size?");
            stepSize = Double.valueOf(s.nextLine()) * scaleDistance;

            System.out.println("What is the threshold?");
            threshold = Double.valueOf(s.nextLine());

            System.out.println("What is the number of gaps?");
            numberOfGaps = Integer.valueOf(s.nextLine());

            System.out.println("every ? a field line: ");
            checkerBoard = Integer.valueOf(s.nextLine());

        }
        
        inputReceived = false;
         */
        EFieldFileParser parser = new EFieldFileParser();

        Charge[][] chargeArrayArray = parser.parseFile(inputFilePath);
        charges = chargeArrayArray[0];
        positiveCharges = chargeArrayArray[1];
        negativeCharges = chargeArrayArray[2];

        eField = new EField(charges, vAnnode, vCathode, scaleDistance, new Vector(0.0, 0.0, 0.0));
        Vector origin = new Vector(0.0, 0.0, 0.0);
        Vector incorrectPosition = new Vector(0.024, 0.072, 0.0);
        Vector offPosition = new Vector(-0.00972, -0.01954, -4.67e-4);
        Particle centered = new Particle(origin, origin, 1, 1.0);
        Particle offCenter = new Particle(offPosition, origin, 1, 1.0);
        System.out.println("Centered: "+ centered.electricPotentialEnergy(eField));
        System.out.println("origin: " + eField.electricPotential(origin));
        System.out.println("OffCenter: " + offCenter.electricPotentialEnergy(eField));
        System.out.println("offPosition: " + eField.electricPotential(offPosition));
        /*
        Vector testPosition = new Vector(0.032, 0.072, 0.0);
        Vector testPosition2 = new Vector(0.025, 0.072, 0.0);
        System.out.println("incorrect position force: " + eField.fieldAtPoint(incorrectPosition));
        System.out.println("testPosition force: " + eField.fieldAtPoint(testPosition));
        System.out.println("testPosition2 force: " + eField.fieldAtPoint(testPosition2));
        */
        /*bounds = makeBoundingBox(scaleDistance);
        System.out.println("bounds: " + bounds);
        FieldLineGenerator flGenerator = new FieldLineGenerator(eField, bounds,
                stepSize, threshold, numberOfGaps, checkerBoard);
        TableGraphWriter tgw = new TableGraphWriter();
        LinkedList<LinkedList<Vector>> fieldLines = flGenerator.drawFieldLines();
        String[] headers = {"Line", "X", "Y", "Z"};
        tgw.writeCSV(fieldLines, headers, outputFilePath);
        inputReceived = false;
        input = "";
         */
    }

}
