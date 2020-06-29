package org.eastsideprep.javaneutrons;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.eastsideprep.javaneutrons.core.MonteCarloSimulation;
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
    Button bRunSV;
    Button bRunET;

    Label progress;

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
        this.sim = TestGM.simulationTest(viewGroup);

        // control buttons and progress 
        TextField tf = new TextField("200");
        tf.setPrefWidth(200);

        bRun = new Button("Start simulation - GM");
        bRunSV = new Button("Start simulation - SV");
        bRunET = new Button("Start simulation - ET");

        bRun.setOnAction((e) -> {
            sim = TestGM.simulationTest(viewGroup);
            this.runSim(Long.parseLong(tf.getText()));
            if (this.sim.lastCount <= 10) {
                root.setRight(heatMap);
            } else {
                root.setRight(null);
            }

        });
        bRun.setPrefWidth(200);

        bRunSV.setOnAction((e) -> {
            sim = TestSV.simulationTest(viewGroup);
            this.runSim(Long.parseLong(tf.getText()));
            if (this.sim.lastCount <= 10) {
                root.setRight(heatMap);
            } else {
                root.setRight(null);
            }
        });
        bRunSV.setPrefWidth(200);

        bRunET.setOnAction((e) -> {
            sim = TestET.simulationTest(viewGroup);
            this.runSim(Long.parseLong(tf.getText()));
            if (this.sim.lastCount <= 10) {
                root.setRight(heatMap);
            } else {
                root.setRight(null);
            }
        });
        bRunET.setPrefWidth(200);

        Button bStats = new Button("Show stats");
        bStats.setOnAction((e) -> {
            Group stats = new StatsDisplay(sim, root);
            this.stats.getChildren().clear();
            this.stats.getChildren().add(stats);
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
        buttons.getChildren().addAll(tf, bRun, bRunSV, bRunET, progress, new Separator(),
                bStats, bView, bCopy, bTest, new Separator(),
                stats);
        root.setLeft(buttons);

        // set scene and stage
        view = mainScene.outer;
        mainScene.root.getChildren().add(viewGroup);
        root.setCenter(view);
        root.setRight(heatMap);

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

    }

    public void runSim(long count) {
        if (this.sim == null) {
            return;
        }

        //
        // here is where we run the actual simulation
        //
        bRun.setDisable(true);
        bRunSV.setDisable(true);
        bRunET.setDisable(true);
        progress.setText("Complete: 0 %");

        root.setCenter(view);
        this.stats.getChildren().clear();

        Group p = new Group();

        final Timeline tl = new Timeline();
        tl.getKeyFrames().add(new KeyFrame(Duration.seconds(0.1),
                (e) -> {
                    long completed;
                    completed = sim.update();
                    progress.setText("Complete: " + Math.round(100 * completed / count) + " %");
                    if (completed == count) {
                        tl.stop();
                        bRun.setDisable(false);
                        bRunET.setDisable(false);
                        bRunSV.setDisable(false);
                        progress.setText("Complete: 100 % , time: " + (sim.getElapsedTime() / 1000) + " s");
                    }
                }));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();

        sim.simulateNeutrons(count, 100000);
        root.setCenter(view);
    }

    public static void main(String[] args) {
        launch();
    }

    static void processKeyEvent(KeyEvent event, Camera camera) {
        switch (event.getCode()) {

            case PAGE_UP:
            case EQUALS:
                camera.translateZProperty().set(camera.getTranslateZ() + 10);
                break;
            case PAGE_DOWN:
            case MINUS:
                camera.translateZProperty().set(camera.getTranslateZ() - 10);
                break;

            case UP:
                camera.setRotationAxis(new Point3D(1, 0, 0));
                camera.setRotate(camera.getRotate() - 1);
                break;
            case LEFT:
                camera.setRotationAxis(new Point3D(0, 1, 0));
                camera.setRotate(camera.getRotate() - 1);
                break;
            case DOWN:
                camera.setRotationAxis(new Point3D(1, 0, 0));
                camera.setRotate(camera.getRotate() + 1);
                break;
            case RIGHT:
                camera.setRotationAxis(new Point3D(0, 1, 0));
                camera.setRotate(camera.getRotate() + 1);
                break;
        }
    }

}
