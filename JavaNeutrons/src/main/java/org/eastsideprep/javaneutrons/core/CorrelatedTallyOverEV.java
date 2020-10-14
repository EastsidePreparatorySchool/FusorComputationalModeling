/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.core;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

public class CorrelatedTallyOverEV extends TallyOverEV {

    final Map<Particle, TallyOverEV> hMap = new HashMap<>();
    final TallyOverEV sumSquares = new TallyOverEV();
    double[][] covLog;
    double[][] covFlat;
    double[][] covLow;
    public CorrelatedTallyOverEV() {
        covLog = new double[sumSquares.bins.length][sumSquares.bins.length];
        covFlat = new double[sumSquares.hFlat.bins.length][sumSquares.hFlat.bins.length];
        covLow = new double[sumSquares.hLow.bins.length][sumSquares.hLow.bins.length];
    }

    public void record(Particle p, double value, double energy) {
        TallyOverEV h;
        synchronized (hMap) {
            h = hMap.get(p);
        }

        if (h == null) {
            synchronized (hMap) {
                h = new TallyOverEV();
                hMap.put(p, h);
            }
        }

        h.record(value, energy);
        p.fluences.add(this);
    }

    public void tally(Particle p) {
        TallyOverEV h;
        synchronized (hMap) {
            h = hMap.get(p);
        }

        if (h == null) {
            return;
        }

        synchronized (this) {
            this.add(h);
            this.hLow.add(h.hLow);
            this.hFlat.add(h.hFlat);
            sumSquares.addSquares(h);
            sumSquares.hLow.addSquares(h.hLow);
            sumSquares.hFlat.addSquares(h.hFlat);

            for (int i = 0; i < bins.length; i++) {
                for (int j = 0; j < bins.length; j++) {
                    covLog[i][j] += bins[i] * bins[j];
                }
            }

            for (int i = 0; i < hFlat.bins.length; i++) {
                for (int j = 0; j < hFlat.bins.length; j++) {
                    covFlat[i][j] += hFlat.bins[i] * hFlat.bins[j];
                }
            }

            for (int i = 0; i < hLow.bins.length; i++) {
                for (int j = 0; j < hLow.bins.length; j++) {
                    covLow[i][j] += hLow.bins[i] * hLow.bins[j];
                }
            }
        }

        synchronized (hMap) {
            hMap.remove(p);
        }
    }

    public XYChart.Series makeErrorSeries(String seriesName, double count, String scale) {
        XYChart.Series<String, Number> smu;
        XYChart.Series<String, Number> sss;
        switch (scale) {
            case "Log":
                smu = makeSeries(seriesName, count);
                sss = sumSquares.makeSeries(seriesName, count);
                break;
            case "Linear (all)":
                smu = hFlat.makeSeries(seriesName, count);
                sss = sumSquares.hFlat.makeSeries(seriesName, count);
                break;
            case "Linear (thermal)":
                smu = hLow.makeSeries(seriesName, count);
                sss = sumSquares.hLow.makeSeries(seriesName, count, LOW_VISUAL_LIMIT);
                break;
            default:
                //return hFlat.makeSeries(seriesName, count);
                return null;
        }

        // take the square root of every item and put it back in
        for (int i = 0; i < sss.getData().size(); i++) {
            Data<String, Number> dmu = smu.getData().get(i);
            Data<String, Number> dss = sss.getData().get(i);
            double mu = dmu.getYValue().doubleValue();
            double ss = dss.getYValue().doubleValue();
            //System.out.print(""+mu+"("+ss +","+count+") -> ");
            dss.setYValue(mu > 0 ? Math.sqrt((ss - (mu * mu)) / count) / mu : 0);
            //System.out.println(""+dss.getYValue());
        }

        return sss;
    }

    @Override
    public boolean equals(Object other) {
        // not null

        // instanceof CTOEV
        // compute chi^2
        return false;
    }
    // CompareToRef(String)
    public String compareToRef(String input, long y) throws Exception {
        CorrelatedTallyOverEV histogram2 = parseFromString(input);
        String x= compareToRef(histogram2,y);
        return x;
    }
    static int fluences_length;//for some reason hLow.bins has 2 more bins of fluences than the fluences
    public String compareToRef(CorrelatedTallyOverEV other, long neutroncountthis){
        double chisq=0;
        double tempx=1;                                         //bin to bin
        for (int i=0; i< this.hLow.bins.length-1; i++) {
            for(int j=0; j<other.hLow.bins.length-1;j++){
                tempx=(this.hLow.bins[i]/neutroncountthis-other.hLow.bins[i])*(this.hLow.bins[j]/neutroncountthis-other.hLow.bins[j]);
                                     //Use inverse matrix
                tempx=tempx*other.covLow[i][j];
                chisq+=tempx;
            }
            System.out.println("Our fluence: "+this.hLow.bins[i]/neutroncountthis+"    "+"Whitmer's fluence: "+other.hLow.bins[i]);//divide tally bins by neutron count
        }
        System.out.println(neutroncount+" "+neutroncountthis);
        
        chisq=Math.abs(chisq*neutroncount*neutroncountthis/(neutroncount+neutroncountthis));
       //chisq = chisq*(1/(1/neutroncount+1/neutroncountthis));
        System.out.println("Did we get here?-146");
        String x = "Comparing the fluences of the "+"currently selected part \n"+"and "+Title;
        x+="\nChisq value: " + chisq;
        return x;
    }
    static int neutroncount;
    static String Title;
    public static CorrelatedTallyOverEV parseFromString (String s) throws Exception{ 
        CorrelatedTallyOverEV output = new CorrelatedTallyOverEV(); //to fill
        String[] lines = s.split("\n"); //
        ArrayList<String> collection = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            collection.add(lines[i]);
        }
        System.out.println("      FOR THE TEST REFERENCE:");
        Title = collection.remove(0);
        System.out.println("This is the title: "+ Title);
        
        String NeutronCount_pre = collection.remove(0);
        String[] NeutronCountArray= NeutronCount_pre.split(" ");
        String Neutron_String = NeutronCountArray[0];
        int neutronCount = Integer.valueOf(Neutron_String);
        neutroncount=neutronCount;
        System.out.println("This is the number of neutrons: "+neutronCount);
                
        String BinCount_pre = collection.remove(0);
        String[] BinCountArray= BinCount_pre.split(" ");
        String BinCount_String = BinCountArray[0];
        int binCount = Integer.valueOf(BinCount_String);
        System.out.println("This is the number of bins: "+binCount);
        
        String BinSizeandInfo = collection.remove(0);
        String[] BinInfo = BinSizeandInfo.split(" ");
        String BinSize_String = BinInfo[0];
        String BinType = BinInfo[1];
        double binSize = Double.parseDouble(BinSize_String);
        System.out.println("This is the binSize: " + binSize);
        
        //Conditions for text file met
        if (binCount != 250 || binSize != 0.001){
            throw new Exception("Unrecognized binCount or binSize");
        }
        
        String fluencesCount = collection.remove(0);
        System.out.println("This is the number of fluences: "+fluencesCount);
        String Fluences = collection.remove(0);
        System.out.println("These are the fluences: "+Fluences);
        String CovarianceMatrixDesc = collection.remove(0);
        System.out.println("This is the covariance matrix description: "+CovarianceMatrixDesc);
           
        System.out.println("\nAfter String parsing & calculations: \n");
        //the rest of collection should be the Covariance Matrix
                    //
                    //Setting up CTOEV
                    //
        output.covLog = null;
        output.covFlat = null;
        
        // put values into low tally
        String[] fluencesStrings = Fluences.split(" ");
        System.out.println("The length of fluencesStrings: " +fluencesStrings.length);
        fluences_length=fluencesStrings.length;
        for (int i = 1; i < output.hLow.bins.length-1; i++) {
            double value = Double.parseDouble(fluencesStrings[i-1]);
            output.hLow.bins[i] = value;
        }
      
        // construct covariance matrix
        // for every every row, parse it
        System.out.println("Should be 250");
        for (int i = 0; i < output.hLow.bins.length-2; i++) { //originall output.Hlow.bins.length
            String[] covStrings = collection.remove(0).split(" ");
            // for this row, put all the values into the matrix                                                             //1-250
            for (int j = 0; j <output.hLow.bins.length-2; j++) {
                double value = Double.parseDouble(covStrings[i]); //I think fluencesStrings should be covStrings
                output.covLow[i][j] = value;
            }
        }

        System.gc();
        return output;
    }

    public static void works() {
        System.out.println("YES");
    }
    
    public void toTextFile(String filename, long neutron_count){ //will expand functionality if needed
        String output="";
        String titletext="\"H-wax Neutron Prison, Standard Fluences\"";
        String neutrontext=neutron_count+" neutrons";
        String bincounttext=hLow.bins.length-2+" bins";
        String binsizetext="0.001 linear binsize";
        String fluencecounttext="250 fluences";
        String fluences="";
            for (int i = 1; i < hLow.bins.length-1; i++) {
                fluences=fluences+hLow.bins[i]/neutron_count+" ";         
            }
            fluences=fluences.substring(0, fluences.length());
       
        String matrixdesctext=(covLow.length-2)+" by "+(covLow.length-2)+" covariance matrix";
        String covariancematrix="";
            System.out.println(covLow.length);
            for (int i=1; i < (covLow.length-1); i++) {
                covariancematrix=covariancematrix+System.getProperty("line.separator");
                for (int j = 1; j<(covLow.length-1); j++) {
                    covariancematrix=covariancematrix+covLow[i][j]+" ";
                }
            }
       
        
        
        //Combine
        output=titletext+System.getProperty("line.separator")+neutrontext+
                System.getProperty("line.separator")+bincounttext+System.getProperty("line.separator")
                +binsizetext+System.getProperty("line.separator")+fluencecounttext+
                System.getProperty("line.separator")+fluences+
                System.getProperty("line.separator")+matrixdesctext+covariancematrix;
        //write to text file
        String may = System.getProperty("user.dir");
        String x = may + "\\src\\main\\resources\\Test References\\" + filename + ".txt";
        try {
            FileWriter f = new FileWriter(x);
            f.write(output);          
            f.close();
        } catch (IOException e) {
            System.err.println("Error saving text file \n" + x + "\n" + e);
        }        
    }
}
