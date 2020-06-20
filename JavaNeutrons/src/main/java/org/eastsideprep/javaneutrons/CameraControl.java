package org.eastsideprep.javaneutrons;

import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 *
 * @author gunnar
 */
public class CameraControl {

    // window
    private int pxX;
    private int pxY;

    public Group root;

    // camera
    private PerspectiveCamera camera;
    private PointLight pLight;
    private double focusX;
    private double focusY;
    private double xRot;
    private double yRot;
    private double zTrans;
    public  HBox outer;

    public CameraControl(int widthPX, int heightPX) {
        // initial camera values
        this.xRot = -20;
        this.yRot = -10;
        this.zTrans = -1000;
        this.pxX = widthPX;
        this.pxY = heightPX;
        this.focusX = 0.0;
        this.focusY = -80.0;

        this.root = new Group();

        AmbientLight aLight = new AmbientLight(Color.rgb(200, 200, 200));
//        pLight = new PointLight(Color.ANTIQUEWHITE);
//        pLight.getTransforms().setAll(
//                new Translate(focusX, 0, focusY),
//                new Rotate(yRot, Rotate.Y_AXIS),
//                new Rotate(xRot, Rotate.X_AXIS),
//                new Translate(0, 0, zTrans)
//        );
        root.getChildren().addAll(aLight/*, pLight*/);

        // Create and position camera
        camera = new PerspectiveCamera(true);
        camera.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );
        camera.setNearClip(0.1);
        camera.setFarClip(2000);

        root.getChildren().add(camera);

        // Use a SubScene       
        SubScene subScene = new SubScene(root, pxX, pxY, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.WHITE);
        subScene.setCamera(camera);
        outer = new HBox(subScene);
        subScene.widthProperty().bind(outer.widthProperty());
        subScene.heightProperty().bind(outer.heightProperty());
        //subScene.setOnMouseClicked((e) -> focus(e));

        outer.setPrefSize(pxX, pxY);
        outer.setMaxSize(pxX, pxY);
        outer.setMinSize(200, 200);
        outer.setAlignment(Pos.TOP_LEFT);
        root.setAutoSizeChildren(true);

    }

    double cameraDistance = 450;
    double DELTA_MULTIPLIER = 200.0;
    double CONTROL_MULTIPLIER = 0.1;
    double SHIFT_MULTIPLIER = 0.1;
    double ALT_MULTIPLIER = 0.5;
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;

    public void updateCamera() {
        camera.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );
    }

    public void handleKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE:
                this.xRot = -20;
                this.yRot = -10;
                this.zTrans = -530;
                this.focusX = 0.0;
                this.focusY = -80.0;
                break;
            case PLUS:
            case EQUALS:
                if (zTrans < 1000) {
                    zTrans += 5;
                }
                break;
            case MINUS:
                if (zTrans > -1000) {
                    zTrans -= 5;
                }
                break;
            case RIGHT:
                yRot -= 5;
                break;
            case LEFT:
                yRot += 5;
                break;
            case DOWN:
                xRot += 2;
                break;
            case UP:
                xRot -= 2;
                break;

            case PAGE_UP:
                break;
            case PAGE_DOWN:
                break;

            default:
                return; // do not consume this event if you can't handle it
        }

        updateCamera();
        /*pLight.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );*/

        e.consume();
    }

    public void handleDrag(MouseEvent me) {
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseDeltaX = (mousePosX - mouseOldX);
        mouseDeltaY = (mousePosY - mouseOldY);

        double modifierFactor = 0.1;

        if (me.isPrimaryButtonDown()) {
            yRot += mouseDeltaX * modifierFactor * 2.0;
            xRot -= mouseDeltaY * modifierFactor * 2.0;
        } else if (me.isSecondaryButtonDown()) {
            focusX -= mouseDeltaX * modifierFactor;
            focusY += mouseDeltaY * modifierFactor;
        }

        updateCamera();
    }

    public void handleClick(MouseEvent me) {
        mousePosX = mouseOldX = me.getSceneX();
        mousePosY = mouseOldY = me.getSceneY();

        focus(me);
        //mouseOldX = me.getSceneX();
        //mouseOldY = me.getSceneY();

    }

    public void handleScroll(ScrollEvent se) {
        zTrans += se.getDeltaY() * 0.2;

        se.consume();
        updateCamera();
    }

    public void focus(MouseEvent e) {
        if (e.getClickCount() > 1) {
            PickResult res = e.getPickResult();
            if (res.getIntersectedNode() instanceof Shape3D) {
                Shape3D shape = (Shape3D) res.getIntersectedNode();
                focusX = shape.getTranslateX() + res.getIntersectedPoint().getX();
                focusY = shape.getTranslateZ() + res.getIntersectedPoint().getZ();
            } else {
                focusX = 0;
                focusY = 0;
            }

            zTrans *= 0.7;

            updateCamera();
        }
        //e.consume();
    }

   
}
