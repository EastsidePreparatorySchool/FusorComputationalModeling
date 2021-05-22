package org.eastsideprep.javaneutrons;

import java.util.List;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

/**
 *
 * @author gunnar
 */
public class CameraControl {

    // window
    private final int pxX;
    private final int pxY;

    public Group root;

    // camera
    private final PerspectiveCamera camera;
    private double focusX;
    private double focusY;
    private double xRot;
    private double yRot;
    private double zTrans;
    public Group outer;
    public SubScene subScene;

    public CameraControl(int widthPX, int heightPX) {
        // initial camera values
        this.xRot = -100;
        this.yRot = 0;
        this.zTrans = -2100;
        this.pxX = widthPX;
        this.pxY = heightPX;
        this.focusX = 0;
        this.focusY = 0.0;

        this.root = new Group();

        AmbientLight aLight = new AmbientLight(Color.rgb(200, 200, 200));
        root.getChildren().addAll(aLight/*, pLight*/);

        // Create and position camera
        camera = new PerspectiveCamera(true);
        updateCamera();
        camera.setNearClip(0.1);
        camera.setFarClip(30000);

        root.getChildren().add(camera);

        // Use a SubScene       
        subScene = new SubScene(root, pxX, pxY, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.WHITE);
        subScene.setCamera(camera);
        outer = new Group(subScene);
        root.setAutoSizeChildren(true);

        updateCamera();

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

    private void updateCamera() {
        camera.getTransforms().setAll(
                new Rotate(yRot, focusX, focusY, 0, Rotate.Y_AXIS),
                new Rotate(xRot, focusX, focusY, 0, Rotate.X_AXIS),
                new Translate(focusX, focusY, 0),
                new Translate(0, 0, zTrans)
        );
//        System.out.println("xRot "+xRot);
//        System.out.println("yRot "+yRot);
//        System.out.println("zTrans "+zTrans);
//        System.out.println("focusX "+focusX);
//        System.out.println("focusY "+focusY);
//        System.out.println("");
    }

    public void handleKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE:
                this.xRot = -100;
                this.yRot = 0;
                this.zTrans = -2100;
                this.focusX = 0;
                this.focusY = 0.0;
                break;
            case PLUS:
            case EQUALS:
                if (zTrans < 2000) {
                    zTrans += 5;
                }
                break;
            case MINUS:
                if (zTrans > -2000) {
                    zTrans -= 5;
                }
                break;
            case RIGHT:
                focusX += 5 * Math.abs(zTrans) / 800;
                break;
            case LEFT:
                focusX -= 5 * Math.abs(zTrans) / 800;
                break;
            case DOWN:
                if (e.isShiftDown()) {
                    zTrans -= 5;
                } else {
                    focusY += 5 * Math.abs(zTrans) / 800;
                }
                break;
            case UP:
                if (e.isShiftDown()) {
                    zTrans += 5;
                } else {
                    focusY -= 5 * Math.abs(zTrans) / 800;
                }
                break;

            case PAGE_UP:
                break;
            case PAGE_DOWN:
                break;

            default:
                return; // do not consume this event if you can't handle it
        }

        updateCamera();

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
            focusX -= mouseDeltaX;//* modifierFactor;
            focusY -= mouseDeltaY;//* modifierFactor;
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

    private void focus(MouseEvent e) {
        if (e.getClickCount() > 1) {
            PickResult res = e.getPickResult();

            if (res.getIntersectedNode() != null) {
                Point3D f = new Point3D(res.getIntersectedPoint().getX(), res.getIntersectedPoint().getY(), res.getIntersectedPoint().getY());
                try {
                    List<Transform> l = res.getIntersectedNode().getTransforms();
                    for (Transform t : l) {
                        f = t.transform(f);
                    }

                    focusX = f.getX();
                    focusY = f.getY();
                    //zTrans = -f.getZ();
                } catch (Exception ex) {
                }
            }

            updateCamera();
        }
    }

}
