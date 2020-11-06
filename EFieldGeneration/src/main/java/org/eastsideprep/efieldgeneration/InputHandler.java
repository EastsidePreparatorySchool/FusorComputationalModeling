/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.efieldgeneration;

/**
 *
 * @author subif
 */
import java.io.File;
import java.util.Scanner;

public class InputHandler {

    int numCharges = 100;
    String positiveFilePath = "ThinRightPlate.csv";
    String negativeFilePath = "ThinPlate.csv";
    String outputFilePath = "outputFile.csv";
    Double posCharge = 100.0;
    Double negCharge = 100.0;
    Double scaleDistance = 0.01;
    Double vAnnode;
    Double vCathode;
    int shakeUps = 100;
    Geometry geometry;
    ChargeDistributer chargeDistributer;
    Scanner s = new Scanner(System.in);
    Charge[] charges;
    Charge[] positiveCharges;
    Charge[] negativeCharges;

    public InputHandler() {
    }

    public void getInput() {

        generateFile();

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

    public void generateFile() {
        positiveFilePath = "";
        negativeFilePath = "";
        outputFilePath = "";

        positiveFilePath = fileNameGet("Please type in the annode (positive) file name");

        negativeFilePath = fileNameGet("Please type cathode (negative) file name");

        outputFilePath = fileCreate("Please enter your output file location");

        System.out.println("How many charges of each polarity do you want?");
        numCharges = Integer.valueOf(s.nextLine());

        System.out.println("What charge are the positive charges?");
        posCharge = Double.valueOf(s.nextLine());
        System.out.println("What charge are the negative charges?");
        negCharge = Double.valueOf(s.nextLine());

        System.out.println("What is the distance, in meters, of 1 unit in the stl files?");
        scaleDistance = Double.valueOf(s.nextLine());

        System.out.println("How many shake-ups do you want?");
        shakeUps = Integer.valueOf(s.nextLine());

        geometry = new Geometry(positiveFilePath, negCharge, negativeFilePath, posCharge);
        /*
        
        NOTICE: the following are the offsets
        for moving the editedchamber.stl into the position for
        wiregrid.stl
        
        Vector offset = new Vector(77.5, -75.0, 240.0);
        geometry.translatePositiveTriangles(offset);
        // the factor here flips the direction of the chamber and grid
        Vector factor = new Vector(1.0, 1.0, -1.0);
        geometry.multiplyTriangles(factor);
        
        
        */
        /*
        Vector offset = new Vector(77.5, -75.0, 240.0);
        geometry.translatePositiveTriangles(offset);
        */
        Vector factor = new Vector(1.0, 1.0, -1.0);
        geometry.multiplyTriangles(factor);
        geometry.sumUpSurfaceArea();

        chargeDistributer = new ChargeDistributer(geometry, scaleDistance, numCharges);
        chargeDistributer.balanceCharges(shakeUps);
        charges = chargeDistributer.charges;
        TableGraphWriter writer = new TableGraphWriter();
        writer.writeCSV(charges, outputFilePath);

    }

    public void scaleOver() {

        String inputFilePath = fileNameGet("Please enter your input file location");
        outputFilePath = fileCreate("Please enter your output file location");
        System.out.println("Scale by what factor?");
        Double scale = Double.valueOf(s.nextLine());
        EFieldFileParser efp = new EFieldFileParser();
        Charge[][] chargeArrayArray = efp.parseFile(inputFilePath);
        Charge[] charges = chargeArrayArray[0];

        for (Charge c : charges) {
            c.scale(scale);
        }
        TableGraphWriter writer = new TableGraphWriter();
        writer.writeCSV(charges, outputFilePath);
    }
    
    public void move(){
        String inputFilePath = fileNameGet("Please enter your input file location");
        outputFilePath = fileCreate("Please enter your output file location");
        System.out.println("Move over how much?");
        System.out.println("x: ");
        Double x = Double.valueOf(s.nextLine());

        System.out.println("y: ");
        Double y = Double.valueOf(s.nextLine());

        System.out.println("z: ");
        Double z = Double.valueOf(s.nextLine());

        Vector offset = new Vector(x,y,z);
        EFieldFileParser efp = new EFieldFileParser();
        Charge[][] chargeArrayArray = efp.parseFile(inputFilePath);
        Charge[] charges = chargeArrayArray[0];

        for (Charge c : charges) {
            c.pos.plusEquals(offset);

        }
        TableGraphWriter writer = new TableGraphWriter();
        writer.writeCSV(charges, outputFilePath);
    
    }
}
