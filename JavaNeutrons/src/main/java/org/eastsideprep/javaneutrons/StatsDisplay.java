/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.eastsideprep.javaneutrons.assemblies.Element;
import org.eastsideprep.javaneutrons.assemblies.Material;
import org.eastsideprep.javaneutrons.assemblies.Part;
import org.eastsideprep.javaneutrons.core.MonteCarloSimulation;

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

    Slider slider = new Slider();

    MonteCarloSimulation sim;
    BorderPane root;
    ToggleGroup tg;

    private class TickConverter extends StringConverter<Number>{

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
                v = v*v/100;
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
                a.setTickUnit(max * v /(100*10));
            }
        });
        object.setPrefWidth(200);

        controls.getChildren().addAll(chartType, slider, object);
        controls.setPadding(new Insets(10, 0, 10, 0));
        hb.getChildren().addAll(controls, chartPane);
        this.getChildren().add(hb);

        this.populateRadioButtons();
        this.populateComboBoxWithParts();
        this.setChart();
        
    }

    private void populateRadioButtons() {
        RadioButton rb1 = new RadioButton("Escape counts");
        RadioButton rb2 = new RadioButton("Fluence");
        RadioButton rb3 = new RadioButton("Entry counts");
        RadioButton rb4 = new RadioButton("Event counts");
        RadioButton rb5 = new RadioButton("Path lengths");
        RadioButton rb6 = new RadioButton("Sigmas");
        RadioButton rb7 = new RadioButton("Cross-sections");

        rb2.setSelected(true);

        RadioButton[] rbs = new RadioButton[]{rb2, rb3, rb4, rb1, rb5, rb6, rb7};

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
        populateComboBox(Part.namedParts.keySet());
    }

    private void populateComboBoxWithMaterials() {
        this.object.getItems().clear();
        populateComboBox(Material.materials.keySet());
    }

    private void populateComboBoxWithElements() {
        this.object.getItems().clear();
        ArrayList<Element> elements = new ArrayList<>(Element.elements.values());
        elements.sort((a, b) -> (a.atomicNumber - b.atomicNumber));
        List<String> s = elements.stream().map(e -> e.name).collect(Collectors.toList());
        populateComboBox(s);
    }

    private void populateComboBoxWithPartsAndMaterials() {
        this.object.getItems().clear();
        populateComboBoxWithParts();
        ArrayList<String> ms = new ArrayList<>(Material.materials.keySet());
        List<String> as = ms.stream().map(s -> "Interstitial " + s).collect(Collectors.toList());
        populateComboBox(as);
    }

    private void populateComboBox(Collection<String> s) {
        if (!(s instanceof ArrayList)) {
            ArrayList<String> items = new ArrayList<>(s);
            items.sort(null);
            s = items;
        }
        this.object.getItems().addAll(s);
        this.object.setValue(this.object.getItems().get(0));
        this.object.valueProperty().addListener((ov, t, t1) -> setChart());
    }

    private void setComboBox() {
        Toggle t = tg.getSelectedToggle();
        if (t != null) {
            switch ((String) t.getUserData()) {
                case "Escape counts":
                    this.object.setVisible(false);
                    break;

                case "Fluence":
                    this.populateComboBoxWithParts();
                    this.object.setVisible(true);
                    break;

                case "Entry counts":
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
        Toggle t = tg.getSelectedToggle();
        if (t != null) {
            switch ((String) t.getUserData()) {
                case "Escape counts":
                    root.setCenter(this.sim.makeChart(null, null));
                    break;

                case "Fluence":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Fluence"));
                    break;

                case "Entry counts":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Entry counts"));
                    break;

                case "Event counts":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Event counts"));
                    break;

                case "Path lengths":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Path lengths"));
                    break;

                case "Sigmas":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Sigmas"));
                    break;

                case "Cross-sections":
                    root.setCenter(this.sim.makeChart((String) this.object.getValue(), "Cross-sections"));
                    break;

                default:
                    root.setCenter(null);
                    break;
            }
        }
        slider.setValue(100);
    }

}
