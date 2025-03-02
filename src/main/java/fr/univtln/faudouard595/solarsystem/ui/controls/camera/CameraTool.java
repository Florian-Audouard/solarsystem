package fr.univtln.faudouard595.solarsystem.ui.controls.camera;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.jme3.asset.AssetManager;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import fr.univtln.faudouard595.solarsystem.App;
import fr.univtln.faudouard595.solarsystem.space.Body;
import fr.univtln.faudouard595.solarsystem.space.Planet;
import fr.univtln.faudouard595.solarsystem.utils.collection.CircularHashMapBody;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class CameraTool {
    public static CircularHashMapBody bodies;
    private static Camera cam;
    public static float distanceFromBody;

    private static Vector2f lastMousePosition;

    private static InputManager inputManager;
    private static float angleHorizontal;
    private static float angleVertical;
    private static boolean isLeftClickPressed = false;
    private static float maxDistance;
    private static float refMaxDistance = 8000;
    public static float minDistance = 1.5f;
    private static float lastAngle;
    private static AssetManager assetManager;
    private static boolean cursorSavePlanet = false;
    public static boolean cursorSaveButton = false;
    private static boolean actualCursor = false;
    private static float zoomSpeed = 2.5f;
    private static int lastScrollTime = -1;
    private static float smoothScrollTime;
    private static float TRANSITION_SCROLL_TIME = 100f;
    private static float TRANSITION_ASTRE_TIME = 50f;
    private static float wantedDistanceFromBody;
    private static float distanceDifference;
    private static float lambdaSmoothZoom;
    private static float expSumSmoothZoom;
    private static Optional<Body> clickableBodies;
    public static App app;

    public static void init(Camera cam, AssetManager assetManager, InputManager inputManager) {

        CameraTool.cam = cam;
        CameraTool.inputManager = inputManager;
        CameraTool.assetManager = assetManager;
        maxDistance = refMaxDistance * zoomSpeed;
        distanceFromBody = minDistance * zoomSpeed * 2;
        wantedDistanceFromBody = distanceFromBody;
        smoothScrollTime = TRANSITION_SCROLL_TIME;
        initSmoothZoomVar();
        inputManager.addMapping("MouseScrollDown", new MouseAxisTrigger(2, true));
        inputManager.addMapping("MouseScrollUp", new MouseAxisTrigger(2, false));
        inputManager.addMapping("leftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addListener(actionListener, "leftClick", "MouseScrollUp", "MouseScrollDown");
        setNormalCursor();
        angleVertical = 10;

    }

    public static void setAngleHorizontal(float angleHorizontal) {
        CameraTool.angleHorizontal = ((angleHorizontal % 360) + 360) % 360;
    }

    public static void initAngle() {
        maxDistance = ((refMaxDistance * Body.reference.getScaleRadius()) / bodies.getCurrentValue().getScaleRadius())
                * zoomSpeed;
        if (bodies.getCurrentValue() instanceof Planet planet) {
            float angle = (90 + (planet.getCurrentAngle() * FastMath.RAD_TO_DEG));
            setAngleHorizontal(angle);
            lastAngle = planet.getCurrentAngle() * FastMath.RAD_TO_DEG;
        }
        wantedDistanceFromBody = minDistance * zoomSpeed * 2;
        if (1f + distanceFromBody > wantedDistanceFromBody && wantedDistanceFromBody > distanceFromBody - 1f) {
            distanceFromBody = wantedDistanceFromBody * 2;
        }
        smoothScrollTime = TRANSITION_ASTRE_TIME;
        initSmoothZoomVar();
    }

    public static void setNormalCursor() {
        JmeCursor customCursor = (JmeCursor) assetManager.loadAsset("Textures/Cursor/normal.cur");
        inputManager.setMouseCursor(customCursor);
        actualCursor = false;
    }

    public static void setClickableCursor() {
        JmeCursor customCursor = (JmeCursor) assetManager.loadAsset("Textures/Cursor/clickable.cur");
        inputManager.setMouseCursor(customCursor);
        actualCursor = true;
    }

    public static void switchCursor(boolean clickable) {
        if (actualCursor == clickable) {
            return;
        }
        if (clickable) {
            setClickableCursor();
        } else {
            setNormalCursor();
        }
    }

    public static void setBody(Body body) {
        bodies = new CircularHashMapBody();
        bodies.createMapFromList(body.getEveryBodies());
        List<Body> mainBodies = new ArrayList<>(body.getPlanets().values());
        mainBodies.add(body);
        bodies.setMainBodies(mainBodies);
    }

    public static void nextMainBody() {
        bodies.nextMainBody();
        initAngle();
    }

    public static void prevMainBody() {
        bodies.previousMainBody();
        initAngle();
    }

    public static void nextBody() {
        bodies.nextBody();
        initAngle();
    }

    public static void prevBody() {
        bodies.previousBody();
        initAngle();
    }

    private static void setBodyByObject(Body body) {
        if (bodies == null) {
            return;
        }
        bodies.setCurrentValue(body);
        initAngle();
    }

    private static Vector3f getClickDirection(Vector2f screenPos) {
        Vector3f worldCoords = cam.getWorldCoordinates(screenPos, 0f);
        Vector3f direction = worldCoords.subtract(cam.getLocation()).normalize();
        return direction;
    }

    private static boolean espilonEqualsVector2d(Vector2f vector1, Vector2f vector2, float nbPixels) {
        return vector1.distance(vector2) < nbPixels;
    }

    private static ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("MouseScrollUp")) {

                wantedDistanceFromBody = Math.max(wantedDistanceFromBody / zoomSpeed, minDistance);
                smoothScrollTime = TRANSITION_SCROLL_TIME;
                initSmoothZoomVar();
            }
            if (name.equals("MouseScrollDown")) {
                wantedDistanceFromBody = Math.min(wantedDistanceFromBody * zoomSpeed, maxDistance);
                smoothScrollTime = TRANSITION_SCROLL_TIME;
                initSmoothZoomVar();
            }
            if (name.equals("leftClick")) {
                isLeftClickPressed = isPressed;
                lastMousePosition = inputManager.getCursorPosition();
                if (clickableBodies.isPresent() && !isPressed) {
                    setBodyByObject(clickableBodies.get());
                }
            }
        }
    };

    public static void initSmoothZoomVar() {

        lastScrollTime = 0;
        distanceDifference = wantedDistanceFromBody - distanceFromBody;
        lambdaSmoothZoom = (float) (Math.log(20) / (smoothScrollTime - 1));
        expSumSmoothZoom = (1 - (float) Math.exp(-lambdaSmoothZoom * smoothScrollTime))
                / (1 - (float) Math.exp(-lambdaSmoothZoom));
    }

    public static void updateZoom() {
        if (lastScrollTime >= smoothScrollTime) {
            lastScrollTime = -1;

        } else if (lastScrollTime != -1) {

            float calc = (float) Math.exp(-lambdaSmoothZoom * lastScrollTime) / expSumSmoothZoom;

            distanceFromBody += (distanceDifference * calc);
            // distanceFromBody = Math.max(minDistance, Math.min(maxDistance,
            // distanceFromBody));
            lastScrollTime++;
        }
        lastMousePosition = inputManager.getCursorPosition().clone();
    }

    public static Vector3f calcCoord() {
        Vector3f bodyPos = bodies.getCurrentValue().getWorldTranslation();
        float practicalDistance = distanceFromBody * bodies.getCurrentValue().getScaleRadius();
        float x = bodyPos.x + practicalDistance * FastMath.cos(FastMath.DEG_TO_RAD * angleVertical)
                * FastMath.sin(FastMath.DEG_TO_RAD * angleHorizontal);
        float z = bodyPos.z + practicalDistance * FastMath.cos(FastMath.DEG_TO_RAD * angleVertical)
                * FastMath.cos(FastMath.DEG_TO_RAD * angleHorizontal);
        float y = bodyPos.y + practicalDistance * FastMath.sin(FastMath.DEG_TO_RAD * angleVertical);

        return new Vector3f(x, y, z);
    }

    public static void calcPos() {
        Vector3f lookAtPos;

        if (bodies.getCurrentValue() instanceof Planet planet) {
            Vector3f primaryPos = planet.getPrimary().getWorldTranslation();

            Vector3f planetPos = planet.getWorldTranslation();

            float minWeight = 0f;
            float maxWeight = 1f;
            float distanceNormalized = (distanceFromBody - minDistance) / (maxDistance - minDistance);

            float expFactor = 0.5f;

            float expDistance = (float) (1 - Math.exp(-expFactor * distanceNormalized));
            float normalizer = (float) (1 - Math.exp(-expFactor));
            expDistance = expDistance / normalizer;

            float weight = 1 - (minWeight + (maxWeight - minWeight) * expDistance);

            lookAtPos = primaryPos.mult(1 - weight).add(planetPos.mult(weight));

        } else {
            lookAtPos = bodies.getCurrentValue().getWorldTranslation();
        }
        Vector3f newPos = calcCoord();
        cam.setLocation(newPos);
        cam.lookAt(lookAtPos, Vector3f.UNIT_Y);
    }

    public static void updateLocation() {
        if (bodies.getCurrentValue() instanceof Planet planet) {
            float newAngle = planet.getCurrentAngle() * FastMath.RAD_TO_DEG;
            setAngleHorizontal(angleHorizontal + (newAngle - lastAngle));
            lastAngle = newAngle;
        }
        calcPos();
    }

    public static void smoothZoom(float zoom) {
        distanceFromBody = zoom;
    }

    public static void updateMousePos() {
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f clickDirection = getClickDirection(cursorPos);

        if (isLeftClickPressed) {
            Vector2f delta = cursorPos.subtract(lastMousePosition);
            setAngleHorizontal(angleHorizontal - (delta.x * 0.1f));
            angleVertical -= delta.y * 0.1f;
            lastMousePosition = cursorPos;

        }
        clickableBodies = bodies.stream().filter(Body::isClickable)
                .filter(a -> {
                    Vector3f camPos = cam.getLocation();
                    Vector3f bodypos = a.getWorldTranslation();
                    Vector3f bodiesScreenPos = cam.getScreenCoordinates(bodypos);
                    Vector2f bodiesScreenPos2d = new Vector2f(bodiesScreenPos.x, bodiesScreenPos.y);
                    float ratioPixel = Body.circleDistance;
                    return espilonEqualsVector2d(cursorPos, bodiesScreenPos2d, ratioPixel * 2f)
                            || a.collision(camPos, clickDirection, 1f);
                })
                .max(Comparator.comparingDouble(Body::getScaleRadius));
        if (clickableBodies.isPresent()) {
            clickableBodies.get().modifColorMult(true);
            bodies.stream()
                    .filter(a -> !a.equals(clickableBodies.get()))
                    .forEach(a -> a.modifColorMult(false));
            cursorSavePlanet = true;
        } else {
            cursorSavePlanet = false;
            bodies.forEach(a -> a.modifColorMult(false));
        }
    }

    public static void updateCircle(Body testedBody, Collection<Body> primaryList) {
        if (testedBody.isShouldBeDisplayed()) {
            testedBody.updateCircle(false);
        }
        boolean res = false;
        Optional<Body> optionalBody = primaryList.stream()
                .filter(Body::isActualDisplayCircle)
                .filter(a -> a.collisionCircle(testedBody))
                .max(Comparator.comparingDouble(Body::getScaleRadius));
        if (optionalBody.isPresent()) {
            if (testedBody.getScaleRadius() > optionalBody.get().getScaleRadius()) {
                res = true;
            }
        } else {
            res = true;
        }
        testedBody.updateCircle(res);
    }

    public static void updateAllCircle() {
        Body primary = Body.reference;
        List<Body> primaryList = new ArrayList<>(primary.getEveryBodies());
        primaryList.add(primary);
        primaryList.sort(Comparator.comparingDouble(Body::getScaleRadius));
        primaryList.forEach(a -> updateCircle(a, primaryList));
    }

    public static void updateCursor() {
        switchCursor(cursorSavePlanet || cursorSaveButton);
    }

    public static void switchFlyCam() {
        FlyByCamera flyCam = app.getFlyByCamera();
        flyCam.setEnabled(!flyCam.isEnabled());
        flyCam.setMoveSpeed(10);
        inputManager.setCursorVisible(!flyCam.isEnabled());
    }

    public static void update() {
        if (!app.getFlyByCamera().isEnabled()) {
            updateMousePos();
            updateZoom();
            updateLocation();
            updateCursor();
        }
        updateAllCircle();
        bodies.getCurrentValue().displayWhenSelected();
    }

}