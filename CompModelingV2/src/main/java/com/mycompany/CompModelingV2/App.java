package com.mycompany.CompModelingV2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import javafx.scene.shape.TriangleMesh;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static javafx.application.Application.launch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableFloatArray;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.ObservableFaceArray;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Screen;

/* todo:
*  
* Done. fix triangle bug: this bug is making it so that a triangle is missing
*    from the thin plates I put in. No charges appear on that triangle, 
*    NO BUENO, probably wouldn't even notice the bug if I only ran
*    the simulation on the chamber and grid, and that's a problem
* Done. fix the crowding bug: when randomly distributed, NO BALANCING AT ALL,
*   on the thin plates, a bunch of charges crowd on the bottom and ruin it for everyone
* Both problem 1 and 2 were part of the same bug; a bug in my binary search;
* When there were only two items in the arraylist left to check for my binary search
* the middle edge would automatically move to the left edge, regardless of which
* edge actually contained the result
* this was fixed.
* 3. UNITS. Add units to this for the thin plates in cm and volts and more
* 3. b. capacitance: Use this as an opportunity to check you calculations
*    by using the units to see what the capacitance should be, versus
*    what your program says
* 4. Show the average electric potential of the positive plate and the negative plate
*       those two numbers should approach each other.
* 5. create a CSV Reader for this program, so you can read and write to csvs
* 6. don't hardcode the file paths, get them as inputs from terminal or something
* 7. change Triangle Random Point formula to this: https://jsfiddle.net/jniac/fmx8bz9y/ 
* 8. Correct Charge values: Once you've finished charge balancing, the next
*    step is to find the difference in voltage between your annode and cathode
*    and keep that number as an offset. Then, go to hte center of the wire grid (like the middle)
*    That spot will have a really high electric potential. That spot is at the 
*    voltage difference you want. Find the number which you have to multiply
*    the electric potential there to get the voltage you want, and then
*    every time you get an electric potential, multiply it by that number.
* 9. Electric Potential --> Electric Potential Energy
*    Just switch from EP to EPE.
* 10. Split this program in two
*    Don't keep on having this program do both output of field
*    AAAAND input, split it eventually.
*  Graphs: make your graphs square instead of rectangular; the aspect ratio is off
*  your XY and XZ plane graphs are 100 x 100, but look 80 x 150
*  also, generate quiver graphs, the electric field graphs (using field lines on the quiver graphs basically)
*  generating electric field line graphs is non-trivial, and Dr. Whitmer may
*  have posted about it in Teams (Efield or Deuteron team)
*  Then prep for orbits, by first getting knowledge of euclid's method
*  Euclids method will give you a differential equation (given this field,
*  with this acceleration, and this start position and velocity, where it end)
*  It shouldn't stick to the grid as soon as it gets close. Think of it like this
*  If you have a skateboard on a smooth hill, you'll go up, then down then
*  back up, you won't just stop at the bottom immediately. same with the f
*  orbit, it won't go from the edge to the grid then stay at the grid, it'll
*  loop back around
*  The looping won't happen on electric field lines, because deuterium has mass
*  and the mass means momentum, so it'll take time for the acceleration to
*  turn the ions around once it's shot past/through the grid, so it'll slide off
*  the trajecory that field lines predict (they expect no mass)
*  Then once you've got that as close as possible, move to Runge Kutta
*  Runge Kutta takes a lot of time though
*  after RK, compute orbits fully
*  orbit lines are nearly infinitely thin, so they won't hit each other
*  instead, they'll hit the grid, or a triangle of the grid more likely
*  calculate when they'll do that.
 */
/**
 * JavaFX App
 */
public class App extends Application {


    //IMPORTANT CONTROLS:
    // wasd move camera 
    // left click drag circles the camera
    // right click drag zooms camera
    // q and e move camera up and down


    Group root = new Group();
    final Xform world = new Xform();
    final Camera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    final Xform axisGroup = new Xform();
    final Xform positiveCharges = new Xform();
    final Xform negativeCharges = new Xform();
    Xform arrows = new Xform();
    final double CAMERA_NEAR_CLIP = 1;
    final double CAMERA_FAR_CLIP = 100000;
    final double CAMERA_INITIAL_DISTANCE = 5;
    final double NEGATIVE_SCALE = 1;
    final double POSITIVE_SCALE = 1;
    Vector POSITIVE_TRANSLATE = new Vector(200.0, 50.0, 20.0);
    Vector NEGATIVE_TRANSLATE = new Vector(200.0, 50.0, 20.0);
    final double CAMERA_INITIAL_Y_ANGLE = 0.0;
    final double CAMERA_INITIAL_X_ANGLE = 0.0;
    Stage primaryStage;
    final double wModifier = 1;
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
    // scale determines the distance of 1.0
    // if scale is set to 1, 1.0 = 1 meter
    // if scale is set to 0.01, 1.0 = 1 centimeter
    Double scaleDistance = 0.01; // meters per unit
    EField eField;

    @Override
    public void start(Stage stage) {

        InputHandler input = new InputHandler();
        input.getInput();
        input.eField.deScale();
        
        //Don't Read from Output file
        buildCharges(input.charges);

        eField = input.eField;
        buildCamera();
        buildAxes();
        buildWorld();
        primaryStage = new Stage();
        buildStage(primaryStage);
        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();

        var label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        Scene scene = new Scene(root, 640, 480, true);
        scene.setFill(Color.GREY);

        keyInput(scene, primaryStage);
        handleMouse(scene, root);
        primaryStage.setScene(scene);
        scene.setCamera(camera);
        primaryStage.show();

    }




    public void buildStage(Stage primaryStage) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());

    }

    public void scaleCharges(double scale) {
        positiveCharges.setScaleX(positiveCharges.getScaleX() + scale);
        positiveCharges.setScaleY(positiveCharges.getScaleY() + scale);
        positiveCharges.setScaleZ(positiveCharges.getScaleZ() + scale);
        negativeCharges.setScaleX(negativeCharges.getScaleX() + scale);
        negativeCharges.setScaleY(negativeCharges.getScaleY() + scale);
        negativeCharges.setScaleZ(negativeCharges.getScaleZ() + scale);
    }

    public void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }

    public void buildCharges(Charge[] charges) {
        world.getChildren().add(negativeCharges);
        world.getChildren().add(positiveCharges);
        final PhongMaterial BlueColor = new PhongMaterial(Color.BLUE);
        BlueColor.setSpecularColor(Color.BLUE);
        final PhongMaterial RedColor = new PhongMaterial(Color.RED);
        RedColor.setSpecularColor(Color.RED);
        for (int i = 0; i < charges.length; i++) {
            Charge c = charges[i];
            if (c.polarity < 0) {
                //System.out.println("hieh");
                final Sphere s = new Sphere(0.4);
                s.setMaterial(RedColor);
                s.setTranslateX(c.pos.x);
                s.setTranslateY(c.pos.y);
                s.setTranslateZ(c.pos.z);
                negativeCharges.getChildren().add(s);
                negativeCharges.setScale(NEGATIVE_SCALE);
            } else {
                final Sphere s = new Sphere(0.4);
                s.setMaterial(BlueColor);
                s.setTranslateX(c.pos.x);
                s.setTranslateY(c.pos.y);
                s.setTranslateZ(c.pos.z);
                positiveCharges.getChildren().add(s);
            }
        }

    }

    public void buildWorld() {
        root.getChildren().add(world);
    }

    private void buildAxes() { // Red = x, green = y, blue = z
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(240.0, 1, 1);
        final Box yAxis = new Box(1, 240.0, 1);
        final Box zAxis = new Box(1, 1, 240.0);
        final Sphere posXAxis = new Sphere(2.0);
        final Sphere posYAxis = new Sphere(2.0);
        final Sphere posZAxis = new Sphere(2.0);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
        posXAxis.setMaterial(redMaterial);
        posXAxis.setTranslateX(120);
        posYAxis.setMaterial(greenMaterial);
        posYAxis.setTranslateY(120);
        posZAxis.setMaterial(blueMaterial);
        posZAxis.setTranslateZ(120);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis, posXAxis, posZAxis, posYAxis);
        world.getChildren().addAll(axisGroup);
    }

    private void handleMouse(Scene scene, final Node root) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });

        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX);
                mouseDeltaY = (mousePosY - mouseOldY);

                double modifier = 1;
                double modifierFactor = 1;

                if (me.isControlDown()) {
                    modifier = 0.1;
                }
                if (me.isShiftDown()) {
                    modifier = 10.0;
                }
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * modifierFactor * modifier * 2.0);  // +
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * modifierFactor * modifier * 2.0);  // -

                } else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX * modifierFactor * modifier;
                    camera.setTranslateZ(newZ);
                } else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
                }

            }
        });
    }

    public void keyInput(Scene scene, Stage stage) {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            double translateStep = 15;
            double rotateStep = 1;
            Double step;
            Double rotStep;

            @Override
            public void handle(KeyEvent event) {
                if (event.isControlDown()) {
                    step = translateStep * -1.5;
                } else {
                    step = translateStep;
                }
                rotStep = rotateStep;
                if (event.isControlDown()) {
                    rotStep = rotateStep * -1;
                } else {
                    rotStep = rotateStep;
                }
                switch (event.getCode()) {
                    case X: // CTRL+X closes window
                        if (event.isControlDown()) {
                            // Close down window
                            stage.close();
                        }
                        break;
                    case PAGE_UP: // Get larger
                        scaleCharges(1.1);
                        break;
                    case PAGE_DOWN: // Get smaller
                        scaleCharges(-1.1);
                        break;
                    case W:
                        cameraXform.t.setZ(cameraXform.t.getZ() + wModifier);
                        break;
                    case A:
                        cameraXform.t.setX(cameraXform.t.getX() + wModifier);
                        break;
                    case S:
                        cameraXform.t.setZ(cameraXform.t.getZ() - wModifier);
                        break;
                    case D:
                        cameraXform.t.setX(cameraXform.t.getX() - wModifier);
                        break;
                    case E:
                        cameraXform.t.setY(cameraXform.t.getY() - wModifier);
                        break;
                    case Q:
                        cameraXform.t.setY(cameraXform.t.getY() + wModifier);
                        break;

                }
            }
        });
    }

    public static void main(String[] args) {

        launch();
    }

}
