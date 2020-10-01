/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.eastsideprep.javaneutrons.core.CorrelatedTallyOverEV;
import org.eastsideprep.javaneutrons.core.Nuclide;
import org.eastsideprep.javaneutrons.core.Material;
import org.eastsideprep.javaneutrons.core.Part;
import org.eastsideprep.javaneutrons.core.MonteCarloSimulation;
import org.eastsideprep.javaneutrons.core.Neutron;
import org.eastsideprep.javaneutrons.core.Util;

/**
 *
 * @author gunnar
 */
public class StatsDisplay extends Group {

    HBox hb = new HBox();
    VBox controls = new VBox();
    VBox chartType = new VBox();
    ChoiceBox object = new ChoiceBox();
    Pane chartPane = new Pane();
    ChoiceBox selectScale = new ChoiceBox();
    Button ref = new Button("Compare to reference");
    ProgressBar uploadBar = new ProgressBar(0);
    
    String scale;

    Slider slider = new Slider();

    MonteCarloSimulation sim;
    BorderPane root;
    ToggleGroup tg;

    private class TickConverter extends StringConverter<Number> {

        @Override
        public String toString(Number n) {
            return String.format("%6.3e", n);
        }

        @Override
        public Number fromString(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    public StatsDisplay(MonteCarloSimulation sim, BorderPane root) {

        this.sim = sim;
        this.root = root;
        ref.setOnAction((e) -> {
            //Location to open file browser
            String dir = System.getProperty("user.dir");
            uploadBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            
            // file explorer pop up for text box
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File(dir + "\\src\\main\\resources\\Test References"));
            File file = fc.showOpenDialog(s);
//            System.out.println(file.getPath());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(StatsDisplay.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //Read file to String:
            String input = "";
            if (file != null) {
                try {
                    FileReader f = new FileReader(file.getPath());
                    int i;
                    while ((i = f.read()) != -1) {
                        input += (char) i;
                    }
                    f.close();
                } catch (IOException eer) {
                    System.err.println("Error reading text file");
                }
            } else {
                System.out.println("File couldn't be selected, loaded, or used");
                 //to quit
            }
           
            //get Current Correlated Tally
            String key = "";
            String part = (String) this.object.getValue();
            for (String k : this.sim.getPartByName(part).fluenceMap.keySet()) {
                if (k.equals("neutron")) {
                    key = k;
                }                                                                                   //way to get the current Correlated Tally being displayed
            }                                                                                        //more complex than I thought
            CorrelatedTallyOverEV current = this.sim.getPartByName(part).fluenceMap.get(key);
            System.out.println(this.object.getValue());
            current.works(); //just a test
            
            String display = "String didn't load";//what we want to show, placeholder if .compareToRef doesn't work
            //parse String input into CorrelatedTally Over EV & compare Histograms
            try {
                String results = current.compareToRef(input);
                display=results;
            } catch (Exception ex) {
                System.out.println(ex);                
            }
        
            
            //display results via new window
            VBox v = new VBox(20);

           
            v.getChildren().add(new Text("  File being read: \n"+input.substring(0, 1000)+"\n        Results: \n"+display));
            v.setAlignment(Pos.TOP_LEFT);
            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.setTitle("Reference Results");
            stage.setScene(new Scene(v, 450, 450));
            stage.show();

            uploadBar.setProgress(1);
            //   System.out.println(input.substring(0, 2000));
        });

        slider.setMin(0);
        slider.setMax(100);
        slider.setValue(100);
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(false);
        slider.setMajorTickUnit(20);
        slider.setMinorTickCount(5);
        slider.setBlockIncrement(5);
        slider.setPadding(new Insets(10, 0, 10, 0));
        slider.valueProperty().addListener((ov, old_val, new_val) -> {
            Node n = root.getCenter();
            if (n != null && n instanceof Chart) {
                XYChart c = (XYChart) n;
                double v = new_val.doubleValue();
                int scale = 5;
                v = Math.pow(v, scale) / Math.pow(100, scale - 1);
                NumberAxis a = (NumberAxis) c.getYAxis();
                a.setAutoRanging(false);
                a.setTickLabelFormatter(new TickConverter());

                // go through all series, find max
                double max = 0;
                for (XYChart.Series<String, Number> s : (ObservableList<XYChart.Series<String, Number>>) c.getData()) {
                    ObservableList<Data<String, Number>> data = s.getData();
                    for (Data<String, Number> item : data) {
                        max = Math.max(max, item.getYValue().doubleValue());
                    }
                }
                a.setUpperBound(max * v / 100);
                a.setTickUnit(max * v / (100 * 10));
            }
        });
        this.object.setPrefWidth(200);
        this.object.setOnAction(e -> {
            setChart();
        });

        selectScale.getItems().addAll("Log", "Linear (all)", "Linear (thermal)");
        selectScale.setValue("Linear (thermal)");
        selectScale.valueProperty().addListener((a, b, c) -> {
            this.setChart();
        });
        this.scale = "Linear (thermal)";
        selectScale.setPrefWidth(200);

        controls.getChildren().addAll(chartType, new Separator(), selectScale, new Separator(),
                new Text("Zoom"), slider, new Separator(), object, ref, new Separator(), uploadBar);
        controls.setPadding(new Insets(10, 0, 10, 0));
        hb.getChildren().addAll(controls, chartPane);
        this.getChildren().add(hb);

        this.populateRadioButtons();
        this.setComboBox();
        this.setChart();

    }

    private void populateRadioButtons() {
        RadioButton rb1 = new RadioButton("Escape counts");
        RadioButton rb2 = new RadioButton("Fluence");
        RadioButton rb3 = new RadioButton("Entry counts");
        RadioButton rb3b = new RadioButton("Exit counts");
        RadioButton rb4 = new RadioButton("Scatter counts");
        RadioButton rb4b = new RadioButton("Scatter angles");
        RadioButton rb5 = new RadioButton("Path lengths");
        RadioButton rb6 = new RadioButton("Sigmas");
        RadioButton rb7 = new RadioButton("Cross-sections");
        RadioButton rb8 = new RadioButton("0-D Monte Carlo");
        RadioButton rb4c = new RadioButton("Capture counts");

        rb2.setSelected(true);

        RadioButton[] rbs = new RadioButton[]{rb2, rb3, rb3b, rb4, rb4b, rb4c, rb1, rb5, rb6, rb7, rb8};

        this.tg = new ToggleGroup();
        for (RadioButton rb : rbs) {
            rb.setToggleGroup(tg);
            rb.setUserData(rb.getText());
        }

        tg.selectedToggleProperty().addListener((ov, ot, nt) -> setComboBox());

        chartType.getChildren()
                .addAll(rbs);
    }

    private void populateComboBoxWithParts() {
        this.object.getItems().clear();
        populateComboBox(this.sim.namedParts.keySet());
    }

    private void populateComboBoxWithMaterials() {
        this.object.getItems().clear();
        populateComboBox(this.sim.materials.keySet());
    }

    private void populateComboBoxWithElements() {
        this.object.getItems().clear();
        ArrayList<Nuclide> elements = new ArrayList<>(Nuclide.elements.values());
        elements.sort((a, b) -> (a.atomicNumber - b.atomicNumber));
        List<String> s = elements.stream().map(e -> e.name).collect(Collectors.toList());
        populateComboBox(s);
    }

    private void populateComboBoxWithPartsAndMaterials() {
        this.object.getItems().clear();
        populateComboBoxWithParts();
        ArrayList<String> ms = new ArrayList<>(this.sim.materials.keySet());
        populateComboBox(ms);
    }

    private void populateComboBoxWithPartsAndInterstitial() {
        this.object.getItems().clear();
        populateComboBoxWithParts();
        if (this.sim.assembly != null) {
            Set<Material> sm = this.sim.assembly.getContainedMaterials();
            sm.add(this.sim.interstitialMaterial);
            populateComboBox(sm.stream().map(m -> m.name).collect(Collectors.toList()));
        }
    }

    private void populateComboBox(Collection<String> s) {
        if (!(s instanceof ArrayList)) {
            ArrayList<String> items = new ArrayList<>(s);
            items.sort(null);
            s = items;
        }
        this.object.getItems().addAll(s);
        this.object.setValue(this.object.getItems().get(0));
    }

    private void setComboBox() {
        Toggle t = tg.getSelectedToggle();
        if (t != null) {
            switch ((String) t.getUserData()) {
                case "Escape counts":
                    this.object.setVisible(false);
                    break;

                case "Fluence":
                    this.populateComboBoxWithPartsAndInterstitial();
                    this.object.setVisible(true);
                    break;

                case "Scatter counts":
                    this.populateComboBoxWithParts();
                    this.object.setVisible(true);
                    break;

                case "Scatter angles":
                    this.populateComboBoxWithParts();
                    this.object.setVisible(true);
                    break;

                case "Entry counts":
                    this.populateComboBoxWithParts();
                    this.object.setVisible(true);
                    break;

                case "Exit counts":
                    this.populateComboBoxWithParts();
                    this.object.setVisible(true);
                    break;

                case "Capture counts":
                    this.populateComboBoxWithParts();
                    this.object.setVisible(true);
                    break;

                case "Event counts":
                    this.populateComboBoxWithPartsAndMaterials();
                    this.object.setVisible(true);
                    break;

                case "Path lengths":
                    this.populateComboBoxWithMaterials();
                    this.object.setVisible(true);
                    break;

                case "Sigmas":
                    this.populateComboBoxWithMaterials();
                    this.object.setVisible(true);
                    break;

                case "Cross-sections":
                    this.populateComboBoxWithElements();
                    this.object.setVisible(true);
                    break;

                default:
                    this.object.setVisible(false);
                    break;
            }
        }
        setChart();
    }

    private void setChart() {
        this.scale = (String) selectScale.getValue();
        Toggle t = tg.getSelectedToggle();
        if (t != null) {
            switch ((String) t.getUserData()) {
                case "Escape counts":
                    root.setCenter(this.sim.makeChart(null, null, scale));
                    break;

                case "Fluence":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Fluence", scale));
                    break;

                case "Entry counts":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Entry counts", scale));
                    break;

                case "Exit counts":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Exit counts", scale));
                    break;

                case "Scatter counts":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Scatter counts", scale));
                    break;

                case "Scatter angles":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Scatter angles", scale));
                    break;

                case "Capture counts":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Capture counts", scale));
                    break;

                case "Path lengths":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Path lengths", scale));
                    break;

                case "Sigmas":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Sigmas", scale));
                    break;

                case "Cross-sections":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Cross-sections", scale));
                    break;

                default:
                    root.setCenter(null);
                    break;
            }
        }
        slider.setValue(100);

    }

}
