package fr.univtln.faudouard595.solarsystem.utils.camera;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.jme3.asset.AssetManager;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import fr.univtln.faudouard595.solarsystem.body.Body;
import fr.univtln.faudouard595.solarsystem.body.Planet;
import fr.univtln.faudouard595.solarsystem.utils.map.CircularHashMapBody;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class CameraTool {
    private static Body body;
    private static CircularHashMapBody bodies;
    private static Camera cam;
    private static float distanceFromBody;

    private static Vector2f lastMousePosition;

    private static InputManager inputManager;
    private static float angleHorizontal;
    private static float angleVertical;
    private static boolean isLeftClickPressed = false;
    private static float maxDistance;
    private static float minDistance = 3f;
    private static float lastAngle;
    private static AssetManager assetManager;
    private static boolean cursorSave = false;
    private static float ratioScreen;
    private static float zoomSpeed = 2.5f;
    private static int lastScrollTime = -1;
    private static float smoothScrollTime;
    private static float TRANSITION_SCROLL_TIME = 200f;
    private static float TRANSITION_ASTRE_TIME = 50f;
    private static float wantedDistanceFromBody;
    private static float distanceDifference;
    private static float lambdaSmoothZoom;
    private static float expSumSmoothZoom;

    public static void init(Camera camReceive, AssetManager assetManagerReceive, InputManager inputManagerReceive) {
        cam = camReceive;
        inputManager = inputManagerReceive;
        assetManager = assetManagerReceive;
        maxDistance = minDistance * 100000 * zoomSpeed;
        distanceFromBody = minDistance * zoomSpeed * 2;
        wantedDistanceFromBody = distanceFromBody;
        smoothScrollTime = TRANSITION_SCROLL_TIME;
        initSmoothZoomVar();
        inputManager.addMapping("MouseScrollDown", new MouseAxisTrigger(2, true));
        inputManager.addMapping("MouseScrollUp", new MouseAxisTrigger(2, false));
        inputManager.addMapping("leftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addMapping("moveMouse", new MouseAxisTrigger(0, true), new MouseAxisTrigger(1, true),
                new MouseAxisTrigger(0, false), new MouseAxisTrigger(1, false));
        inputManager.addListener(actionListener, "leftClick", "MouseScrollUp", "MouseScrollDown");
        inputManager.addListener(analogListener, "moveMouse");
        setNormalCursor();
        ratioScreen = ((float) cam.getWidth() / (float) cam.getHeight()) * 20;
        angleVertical = 10;

    }

    public static void setAngleHorizontal(float angleHorizontalReceive) {
        angleHorizontal = ((angleHorizontalReceive % 360) + 360) % 360;
    }

    public static void initAngle() {
        if (bodies.getCurrentValue() instanceof Planet planet) {
            setAngleHorizontal(0);
            Vector3f newPos = calcCoord();
            newPos.y = 0;
            Vector3f planetPos = planet.getWorldTranslation();
            Vector3f directionCamPlanet = newPos.subtract(planetPos).normalize();
            Vector3f primary = planet.getPrimary().getWorldTranslation();
            Vector3f directionPrimaryPlanet = planetPos.subtract(primary).normalize();
            float angle = FastMath.acos(directionCamPlanet.dot(directionPrimaryPlanet));
            float sign = directionPrimaryPlanet.x > 0 ? 1 : -1;
            angle = (sign * (angle) * FastMath.RAD_TO_DEG) - 30;
            setAngleHorizontal(angle);
            lastAngle = planet.getCurrentAngle() * FastMath.RAD_TO_DEG;
            wantedDistanceFromBody = minDistance * zoomSpeed * 2;
            smoothScrollTime = TRANSITION_ASTRE_TIME;
            initSmoothZoomVar();
        }
    }

    public static void setNormalCursor() {
        JmeCursor customCursor = (JmeCursor) assetManager.loadAsset("Textures/Cursor/normal.cur");
        inputManager.setMouseCursor(customCursor);
    }

    public static void setClickableCursor() {
        JmeCursor customCursor = (JmeCursor) assetManager.loadAsset("Textures/Cursor/clickable.cur");
        inputManager.setMouseCursor(customCursor);
    }

    public static void switchCursor(boolean clickable) {
        if (cursorSave == clickable) {
            return;
        }
        if (clickable) {
            setClickableCursor();

        } else {
            setNormalCursor();
        }
        cursorSave = clickable;
    }

    public static void setBody(Body bodyReceive) {
        body = bodyReceive;
        bodies = new CircularHashMapBody();
        bodies.createMapFromList(body.getEveryBodies());
    }

    public static void nextBody() {
        bodies.nextValue();
        initAngle();
    }

    public static void prevBody() {
        bodies.prevValue();
        initAngle();
    }

    private static void setBodyByObject(Body bodyReceive) {
        if (bodies == null) {
            return;
        }
        bodies.setCurrentValue(bodyReceive);
        initAngle();
    }

    private static Vector3f getClickDirection(Vector2f screenPos) {
        Vector3f worldCoords = cam.getWorldCoordinates(screenPos, 0f).clone();
        Vector3f direction = worldCoords.subtract(cam.getLocation()).normalize();
        return direction;
    }

    private static boolean espilonEqualsVector3d(Vector3f vector1, Vector3f vector2, float epsilon) {
        float x = vector1.x - vector2.x;
        float y = vector1.y - vector2.y;
        float z = vector1.z - vector2.z;
        return x * x + y * y + z * z < epsilon * epsilon;
    }

    private static boolean espilonEqualsVector2d(Vector2f vector1, Vector2f vector2, float nbPixels) {
        float x = vector1.x - vector2.x;
        float y = vector1.y - vector2.y;
        return x * x + y * y < (nbPixels * nbPixels) * 2;
    }

    private static Body setClosestBody(List<Body> bodiesDetecte) {
        float distance = Float.MAX_VALUE;
        Body closestBody = null;
        for (Body a : bodiesDetecte) {
            float newDistance = a.getWorldTranslation().distance(cam.getLocation());

            if (newDistance < distance) {
                distance = newDistance;
                closestBody = a;
            }
        }
        return closestBody;
    }

    private static boolean detectBody(boolean changeBody) {
        List<Body> bodiesDetecte = new ArrayList<>();
        boolean res = false;
        Vector2f mousePos = inputManager.getCursorPosition();
        for (Body a : bodies.getValues()) {
            Vector3f camPos = cam.getLocation();
            Vector3f clickDirection = getClickDirection(mousePos);
            Vector3f bodypos = a.getWorldTranslation();
            Vector3f bodiesScreenPos = cam.getScreenCoordinates(bodypos);
            Vector2f bodiesScreenPos2d = new Vector2f(bodiesScreenPos.x, bodiesScreenPos.y);
            float ratioPixel = ratioScreen;
            if (!a.equals(bodies.getCurrentValue()) &&
                    (espilonEqualsVector2d(mousePos, bodiesScreenPos2d, ratioPixel) ||
                            a.collision(camPos, clickDirection, 1f))) {
                bodiesDetecte.add(a);
                res = true;
            }
        }
        if (!res) {
            if (!changeBody) {
                bodies.forEach(a -> a.modifColorMult(false));
            }
            return res;
        }
        Body closest = setClosestBody(bodiesDetecte);
        if (changeBody) {
            setBodyByObject(closest);
        } else {
            for (Body a : bodies.getValues()) {
                a.modifColorMult(a.equals(closest));
            }
        }

        return res;
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
                detectBody(isPressed);
            }
        }
    };
    private static AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("moveMouse")) {
                if (isLeftClickPressed) {
                    Vector2f cursorPos = inputManager.getCursorPosition();
                    Vector2f delta = cursorPos.subtract(lastMousePosition);
                    setAngleHorizontal(angleHorizontal - (delta.x * 0.1f));
                    angleVertical -= delta.y * 0.1f;
                    lastMousePosition = cursorPos;
                }
                switchCursor(detectBody(false));

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

            // Calcul du déplacement avec normalisation
            float calc = (float) Math.exp(-lambdaSmoothZoom * lastScrollTime) / expSumSmoothZoom;

            distanceFromBody += (distanceDifference * calc);
            lastScrollTime++;
        }
        lastMousePosition = inputManager.getCursorPosition().clone();
    }

    public static Vector3f calcCoord() {
        Vector3f bodyPos = bodies.getCurrentValue().getWorldTranslation();
        float practicalDistance = Math.max(distanceFromBody * bodies.getCurrentValue().getScaleSize(), 1.5f);

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

            // Facteur d'exponentielle (plus grand = plus prononcé)
            float expFactor = 5f; // Essaie 10f, 20f pour plus d'effet

            // Calcul exponentiel inversé et normalisation
            float expDistance = (float) (1 - Math.exp(-expFactor * distanceNormalized));
            float normalizer = (float) (1 - Math.exp(-expFactor)); // Assure que max = 1
            expDistance = expDistance / normalizer;

            // **Inversion du poids**
            float weight = 1 - (minWeight + (maxWeight - minWeight) * expDistance);

            lookAtPos = primaryPos.mult(1 - weight).add(planetPos.mult(weight));

        } else {
            lookAtPos = bodies.getCurrentValue().getWorldTranslation();
        }
        Vector3f newPos = calcCoord();
        cam.setLocation(newPos);
        cam.lookAt(lookAtPos,
                Vector3f.UNIT_Y);
    }

    public static void updateLocation(double time, float speed) {
        if (bodies.getCurrentValue() instanceof Planet planet) {
            float newAngle = planet.getCurrentAngle() * FastMath.RAD_TO_DEG;
            setAngleHorizontal(angleHorizontal - (newAngle - lastAngle));
            lastAngle = newAngle;
        }
        calcPos();
    }

    public static void smoothZoom(float zoom) {
        distanceFromBody = zoom;
    }

    public static void update(double time, float speed) {
        updateZoom();
        updateLocation(time, speed);
    }

}