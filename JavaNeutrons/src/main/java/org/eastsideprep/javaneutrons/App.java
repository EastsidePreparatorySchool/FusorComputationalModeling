package org.eastsideprep.javaneutrons;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.eastsideprep.javaneutrons.core.MC0D;
import org.eastsideprep.javaneutrons.core.MonteCarloSimulation;
import org.eastsideprep.javaneutrons.core.Part;
import org.eastsideprep.javaneutrons.core.Util;

/**
 * JavaFX App
 */
public class App extends Application {

    BorderPane root;
    Group view;
    MonteCarloSimulation sim;
    Group viewGroup;
    Group stats;
    Button bRun;

    Label progress;
    boolean idleFlag = false;

    @Override
    public void start(Stage stage) {
        this.root = new BorderPane();
        this.stats = new Group();
        this.progress = new Label(""); // need to hand this to Test()

        // create camera control
        CameraControl mainScene = new CameraControl(1000, 500);
        ImageView heatMap = new ImageView(Util.Graphics.createHeatMap(100, 500));
        heatMap.fitHeightProperty().bind(root.heightProperty());
        mainScene.subScene.heightProperty().bind(root.heightProperty());

        // prepare sim for later
        this.viewGroup = new Group();

        // control buttons and progress 
        Label src = new Label("Src: ");
        TextField tf = new TextField("1");

        tf.setPrefWidth(85);
        Label grid = new Label("Grid: ");
        TextField gridSize = new TextField("2.0");
        gridSize.setPrefWidth(40);
        HBox settings = new HBox();
        settings.getChildren().addAll(src, tf, grid, gridSize);
        settings.setAlignment(Pos.CENTER);
        settings.setSpacing(5);

        ChoiceBox cb = new ChoiceBox();
        addItems(cb, TestGM.class);
        addItems(cb, TestSV.class);
        addItems(cb, TestET.class);
        cb.setPrefWidth(200);
        cb.setOnAction(e -> {
            this.progress.setText("");
            this.sim = fromString((String) cb.getValue(), viewGroup);
            gridSize.setText("" + this.sim.suggestedGrid);
            if (this.sim instanceof MC0D) {
                ((MC0D) sim).init();
            }
            if (this.sim.suggestedCount != -1) {
                tf.setText(Long.toString(this.sim.suggestedCount));
            }
            root.setCenter(view);
            this.stats.getChildren().clear();
            if (this.sim.lastCount <= 10) {
                root.setRight(heatMap);
            } else {
                root.setRight(null);
            }
        });
        cb.setValue(cb.getItems().get(0));

        bRun = new Button("Start simulation");
        bRun.setOnAction((e) -> {
            if (gridSize.getText().trim().length() > 0) {
                double side = Double.parseDouble(gridSize.getText());
                this.sim.grid = null;
                this.sim.prepareGrid(side, view);
            } else {
                this.sim.grid = null;
                this.sim.prepareGrid(50, view);
            }
            this.runSim(Integer.parseInt(tf.getText()));
            if (this.sim.lastCount <= 10) {
                root.setRight(heatMap);
            } else {
                root.setRight(null);
            }

        }
        );
        bRun.setPrefWidth(200);

        Button bStop = new Button("Stop");
        bStop.setOnAction((e) -> {
            this.sim.stop = true;
            this.bRun.setDisable(false);
        });
        bStop.setPrefWidth(200);

        Button bLoad = new Button("Load assembly");
        bLoad.setOnAction((e) -> {
            // Egan's code goes here.
        });
        bLoad.setPrefWidth(200);

        Button bStats = new Button("Show stats");
        bStats.setOnAction((e) -> {
            Group newstats = new StatsDisplay(sim, root);
            this.stats.getChildren().clear();
            this.stats.getChildren().add(newstats);
            progress.setText("");
            root.setRight(null);
        });
        bStats.setPrefWidth(200);

        Button bView = new Button("Show assembly");
        bView.setOnAction((e) -> {
            root.setCenter(view);
            this.stats.getChildren().clear();
            if (this.sim.lastCount <= 10) {
                root.setRight(heatMap);
            } else {
                root.setRight(null);
            }
        });
        bView.setPrefWidth(200);

        Button bCopy = new Button("Copy to clipboard");
        bCopy.setOnAction((e) -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            Image snapshot = root.getCenter().snapshot(new SnapshotParameters(), null);
            content.putImage(snapshot);
            clipboard.setContent(content);
        });
        bCopy.setPrefWidth(200);

        Button bTest = new Button("Test visuals");
        bTest.setOnAction((e) -> {
            viewGroup.getChildren().clear();
            viewGroup.getChildren().add(TestGM.testVisuals());
            root.setRight(null);
        });
        bTest.setPrefWidth(200);

        VBox buttons = new VBox();
        buttons.getChildren().addAll(cb, bLoad, settings, new Separator(), bRun, bStop, progress, new Separator(),
                bStats, bView, bCopy, bTest, new Separator(),
                stats);
        root.setLeft(buttons);

        // set scene and stage
        view = mainScene.outer;
        mainScene.root.getChildren().add(viewGroup);
        root.setCenter(view);
        root.setRight(null);

        Scene scene;
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        System.out.println("Screen size: " + ((int) screenBounds.getWidth()) + "x" + ((int) screenBounds.getHeight()));
        scene = new Scene(root, screenBounds.getWidth() * 0.7, screenBounds.getHeight() * 0.7, true);
//        if (screenBounds.getWidth() > 3000) {
//            scene = new Scene(root, 1900, 1000, true);
//        } else {
//            scene = new Scene(root, 1300, 500, true);
//        }

        //scene.setOnKeyPressed((ex) -> mainScene.controlCamera(ex));
        scene.setOnKeyPressed((ex) -> mainScene.handleKeyPress(ex));
        scene.setOnMouseDragged((ex) -> mainScene.handleDrag(ex));
        scene.setOnMousePressed((ex) -> mainScene.handleClick(ex));
        scene.setOnScroll((ex) -> mainScene.handleScroll(ex));

        // scene and keyboard controls (old)
        scene.addEventFilter(KeyEvent.KEY_PRESSED, (e) -> {
            mainScene.handleKeyPress(e);
        });
        stage.setScene(scene);
        stage.show();
        idle();

        stage.setOnCloseRequest((e) -> noIdle());
    }

    void addItems(ChoiceBox cb, Class c) {
        Method[] methods = c.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getReturnType() == MonteCarloSimulation.class) {
                String name = c.getName() + "::" + methods[i].getName();
                // cut off fully qualified name parts
                name = name.substring(name.lastIndexOf(".") + 1);
                System.out.println("public method: " + name);
                cb.getItems().add(name);
            }
        }
    }

    MonteCarloSimulation fromString(String m, Group g) {
        String cname = App.class.getPackageName() + "." + m.substring(0, m.indexOf("::"));
        String mname = m.substring(m.indexOf("::") + 2);
        MonteCarloSimulation mcs = null;

        try {
            Class c = Class.forName(cname);
            Method method = c.getMethod(mname, Group.class);
            method.setAccessible(true);
            mcs = (MonteCarloSimulation) method.invoke(null, g);
        } catch (Exception e) {
            System.err.println();
            System.err.println("Didn't work: " + e);
            e.printStackTrace();
            System.err.println();
            return null;
        }
        return mcs;
    }

    public void runSim(int count) {
        if (this.sim == null) {
            return;
        }

        //
        // here is where we run the actual simulation
        //
        this.sim.stop = false;
        this.sim.lastCount = count;

        bRun.setDisable(true);
        progress.setText("Complete: 0 %");

        root.setCenter(view);
        this.stats.getChildren().clear();

        Group p = new Group();

        noIdle();
        this.progressTimeline();
        sim.simulateParticles(count, count<=10?100000:1000, true);
        root.setCenter(view);
    }

    private Timeline progressTimeline() {
        root.setCenter(view);
        final Timeline tl = new Timeline();
        tl.getKeyFrames().add(new KeyFrame(Duration.seconds(0.1),
                (e) -> {
                    long completed;
                    completed = sim.update();
                    if (this.sim.lastCount > 0) {
                        progress.setText("Complete: " + Math.round(100 * completed / this.sim.lastCount) + " %");
                    }
                    if (completed >= this.sim.lastCount || (this.sim.lastCount == 0 && sim.scatter)) {
                        tl.stop();
                        sim.postProcess();
                        bRun.setDisable(false);
                        progress.setText("Complete: 100 % , time: " + (sim.getElapsedTime() / 1000) + " s");
                        if (this.sim.designatedDetector != null && this.sim.lastCount > 10) {
                            Platform.runLater(() -> {
                                // collect exposure in Sievert from designated detector part
                                Part p = sim.designatedDetector;
                                double n = p.getSieverts("neutron") / this.sim.lastCount;
                                double g = p.getSieverts("gamma") / this.sim.lastCount;

                                // want per hour
                                n *= this.sim.srcRate * 3600;
                                g *= this.sim.srcRate * 3600;

                                // make a nice string
                                DecimalFormat f = new DecimalFormat("0.###E0");
                                String text = "";
                                if (this.sim.name != null) {
                                    text += "Simulation name: \"" + this.sim.name + "\"\n";
                                }
                                text += "Source rate: " + f.format(this.sim.srcRate) + " n/s\n";
                                text += "Neutrons simulated: " + this.sim.lastCount + "\n";
                                text += "Designated Detector \"" + p.name + "\":\n\n";
                                text += f.format(n) + " Sv/hr (neutron)\n";
                                text += f.format(g) + " Sv/hr (gamma)\n";
                                text += f.format(g + n) + " Sv/hr (total)\n\n";
                                text += "(\"Banana criterion\": total < 1.2E-6 Sv/h)";

                                // put onto clipboard
                                Clipboard clipboard = Clipboard.getSystemClipboard();
                                ClipboardContent content = new ClipboardContent();
                                content.putString(text);
                                clipboard.setContent(content);

                                // show in alert
                                Alert alert = new Alert(AlertType.INFORMATION, text, ButtonType.OK);
                                alert.setTitle("Simulation finished");
                                alert.setHeaderText("Exposure summary");
                                alert.showAndWait();
                            });
                        }
                        idle();
                        if (sim instanceof MC0D) {
                            Group newstats = new StatsDisplay(sim, root);
                            this.stats.getChildren().clear();
                            this.stats.getChildren().add(newstats);
                            progress.setText("");
                            root.setRight(null);

                        }
                    }
                }));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
        return tl;
    }

    public static void runWithDelay(Runnable r, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
            Platform.runLater(r);
        }).start();
    }

    private void idle() {
        idleFlag = true;
        //      idleLoop();
    }

    private void idleLoop() {
        if (!idleFlag) {
            return;
        }
        sim.simulateParticles(1, 3000, false);
        sim.update();

        if (this.idleFlag) {
            runWithDelay(() -> idleLoop(), 1000);
        }
    }

    private void noIdle() {
        idleFlag = false;
    }

    public static void main(String[] args) {
        launch();
    }

}
