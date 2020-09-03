/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.CompModelingV2;

/**
 *
 * @author subif
 */
import java.io.File;
import java.util.Scanner;

public class InputHandler {

    int numCharges = 100;
    String inputFilePath = "inputFilePath.csv";
    Double posCharge = 100.0;
    Double negCharge = 100.0;
    Double scaleDistance = 0.01;
    Double vAnnode;
    Double vCathode;

    Scanner s = new Scanner(System.in);
    EField eField;
    Charge[] charges;
    Charge[] positiveCharges;
    Charge[] negativeCharges;

    public InputHandler() {
    }

    public void getInput() {

        readFromFile();

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

    String[] makeLineTable() {
        String graphAxis = "";
        Boolean inputReceived = false;
        while (!inputReceived) {
            System.out.println("Along which axis do you want your graph? (X/Y/Z)");

            graphAxis = s.nextLine();
            if (graphAxis.equals("Y") || graphAxis.equals("y")) {
                inputReceived = true;
            } else if (graphAxis.equals("X") || graphAxis.equals("x")) {
                inputReceived = true;
            } else if (graphAxis.equals("Z") || graphAxis.equals("z")) {
                inputReceived = true;
            } else {
                System.out.println("Please respond with (X), (Y) or (Z)");
            }

        }
        inputReceived = false;
        System.out.println("How many points do you want plotted?");
        String gaps = s.nextLine();

        System.out.println("What is the lower bound");
        String lowerBound = s.nextLine();

        System.out.println("What is the upper bound?");
        String upperBound = s.nextLine();

        String tableFilePath = fileCreate("Please enter the table file location");
        String[] tableSettings = {graphAxis, gaps, lowerBound, upperBound, tableFilePath};
        return tableSettings;
    }

    String[] makeSliceTable() {
        Boolean inputRecieved = false;
        String graphPlane = "";
        while (!inputRecieved) {
            System.out.println("Do you want an XY slice, a YZ slice, or an XZ slice? (XY/YZ/XZ)");

            graphPlane = s.nextLine();
            if (graphPlane.equals("XZ") || graphPlane.equals("xz") || graphPlane.equals("xZ") || graphPlane.equals("Xz")) {
                inputRecieved = true;
            } else if (graphPlane.equals("YZ") || graphPlane.equals("yz") || graphPlane.equals("Yz") || graphPlane.equals("yZ")) {
                inputRecieved = true;
            } else if (graphPlane.equals("XY") || graphPlane.equals("xy") || graphPlane.equals("xY") || graphPlane.equals("Xy")) {
                inputRecieved = true;
            } else {
                System.out.println("Please respond with (XY), (YZ), or (XZ)");
            }

        }

        System.out.println("Give the Bottom lower Corner Coordinates");
        System.out.println("x:");
        String blcx = s.nextLine();
        System.out.println("y:");
        String blcy = s.nextLine();
        System.out.println("z:");
        String blcz = s.nextLine();

        System.out.println("upper right corner");
        System.out.println("x:");
        String urcx = s.nextLine();
        System.out.println("y:");
        String urcy = s.nextLine();
        System.out.println("z:");
        String urcz = s.nextLine();

        System.out.println("number of points");
        String points = s.nextLine();

        String tableFilePath = fileCreate("Please enter the table file location");

        String[] tableSettings = {graphPlane, blcx, blcy, blcz, urcx, urcy, urcz, points, tableFilePath};
        return tableSettings;
    }

    public void readFromFile() {
        boolean inputRecieved = false;
        boolean makeGraph = false;
        boolean makePotentials = false;
        boolean makeForceVectors = false;
        Integer gaps = 0;
        Double lowerBound = 0.0;
        Double upperBound = 0.0;
        Double[][] potentialsTable;
        Vector[][] forceVectorsTable;
        String graphAxis = "";
        String graphPlane = "";
        String[] tableSettings = {};
        Boolean makeSlice = false;
        Boolean makeLine = false;
        String tableFilePath = "";
        inputFilePath = fileNameGet("Please enter your input file location");

        System.out.println("Please enter the annode (positive) voltage");
        vAnnode = Double.valueOf(s.nextLine());

        System.out.println("Please enter the cathode (negative) voltage");
        vCathode = Double.valueOf(s.nextLine());

        System.out.println("What is the distance, in meters, of 1 unit in the stl files?");
        scaleDistance = Double.valueOf(s.nextLine());

        String input = "";
        inputRecieved = false;
        while (!inputRecieved) {
            System.out.println("Do you want to create a table? (Y/N)");

            input = s.nextLine();
            if (input.equals("Y") || input.equals("y")) {
                inputRecieved = true;
                makeGraph = true;
            } else if (input.equals("N") || input.equals("n")) {
                inputRecieved = true;
            } else {
                System.out.println("Please respond with (Y) or (N)");
            }
        }
        if (makeGraph) {
            inputRecieved = false;
            while (!inputRecieved) {
                System.out.println("Do you want a table of potentials or force vectors? (P/F)");
                input = s.nextLine();
                if (input.equals("P") || input.equals("p")) {
                    inputRecieved = true;
                    makePotentials = true;
                } else if (input.equals("F") || input.equals("f")) {
                    inputRecieved = true;
                    makeForceVectors = true;
                } else {
                    System.out.println("Please respond with (P) or (F)");
                }

            }
            inputRecieved = false;
            while (!inputRecieved) {
                System.out.println("Do you want a slice or a line? (S/L)");

                input = s.nextLine();
                if (input.equals("S") || input.equals("s")) {
                    inputRecieved = true;
                    makeSlice = true;
                    tableSettings = makeSliceTable();
                } else if (input.equals("L") || input.equals("l")) {
                    makeLine = true;
                    tableSettings = makeLineTable();
                    inputRecieved = true;
                } else {
                    System.out.println("Please respond with (Y) or (N)");
                }
            }
            inputRecieved = false;
        }

        EFieldFileParser parser = new EFieldFileParser();

        Charge[][] chargeArrayArray = parser.parseFile(inputFilePath);
        charges = chargeArrayArray[0];
        positiveCharges = chargeArrayArray[1];
        negativeCharges = chargeArrayArray[2];

        eField = new EField(charges, vAnnode, vCathode, scaleDistance, new Vector(0.0, 0.0, 0.0));

        if (makeForceVectors && makeLine) {
            graphAxis = tableSettings[0];
            gaps = Integer.valueOf(tableSettings[1]);
            lowerBound = Double.valueOf(tableSettings[2]);
            upperBound = Double.valueOf(tableSettings[3]);
            tableFilePath = tableSettings[4];
            potentialsTable = eField.potentialTable(graphAxis, lowerBound, upperBound, gaps);
            TableGraphWriter tableGraphWriter = new TableGraphWriter();
            String[] headers = {"X", "Y", "Z", "FX", "FY", "FZ", "Force"};
            tableGraphWriter.writeCSV(potentialsTable, headers, tableFilePath);

        } else if (makeForceVectors && makeSlice) {
            graphPlane = tableSettings[0];
            Vector blc = new Vector(tableSettings[1], tableSettings[2], tableSettings[3]);
            Vector urc = new Vector(tableSettings[4], tableSettings[5], tableSettings[6]);
            gaps = Integer.valueOf(tableSettings[7]);
            tableFilePath = tableSettings[8];
            forceVectorsTable = eField.forceVectorSlice(graphPlane, blc, urc, gaps);
            String[] headers = {"X", "Y", "Z", "FX", "FY", "FZ", "Force"};
            TableGraphWriter tableGraphWriter = new TableGraphWriter();
            tableGraphWriter.writeCSV(forceVectorsTable, headers, tableFilePath);
        } else if (makeLine) {

            graphAxis = tableSettings[0];
            gaps = Integer.valueOf(tableSettings[1]);
            lowerBound = Double.valueOf(tableSettings[2]);
            upperBound = Double.valueOf(tableSettings[3]);
            tableFilePath = tableSettings[4];
            potentialsTable = eField.potentialTable(graphAxis, lowerBound, upperBound, gaps);
            TableGraphWriter tableGraphWriter = new TableGraphWriter();
            String[] headers = {graphAxis + "-coordinate", "Electric Potential"};
            tableGraphWriter.writeCSV(potentialsTable, headers, tableFilePath);
        } else if (makeSlice) {
            graphPlane = tableSettings[0];
            Vector blc = new Vector(tableSettings[1], tableSettings[2], tableSettings[3]);
            Vector urc = new Vector(tableSettings[4], tableSettings[5], tableSettings[6]);
            gaps = Integer.valueOf(tableSettings[7]);
            tableFilePath = tableSettings[8];
            potentialsTable = eField.potentialSlice(graphPlane, blc, urc, gaps);
            String[] headers;
            if (graphPlane.equals("XZ") || graphPlane.equals("xz") || graphPlane.equals("xZ") || graphPlane.equals("Xz")) {
                System.out.println("xz");
                String[] headers2 = {"X-coordinate", "Z-coordinate", "Electric Potential"};
                headers = headers2;
            } else {
                System.out.println("xy");
                String[] headers2 = {"X-coordinate", "Y-coordinate", "Electric Potential"};
                headers = headers2;
            }
            TableGraphWriter tableGraphWriter = new TableGraphWriter();
            tableGraphWriter.writeCSV(potentialsTable, headers, tableFilePath);
        }
    }

}
