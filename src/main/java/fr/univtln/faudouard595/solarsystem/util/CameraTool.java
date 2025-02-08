package fr.univtln.faudouard595.solarsystem.util;

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

import fr.univtln.faudouard595.solarsystem.Astre.Astre;
import fr.univtln.faudouard595.solarsystem.Astre.Planet;
import fr.univtln.faudouard595.solarsystem.util.map.CircularHashMap;
import fr.univtln.faudouard595.solarsystem.util.map.CircularHashMapAstre;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class CameraTool {
    private static Astre astre;
    private static CircularHashMapAstre astres;
    private static Camera cam;
    private static float distanceFromAstre;

    private static Vector2f lastMousePosition;

    private static InputManager inputManager;
    private static float angleHorizontal;
    private static float angleVertical;
    private static boolean isLeftClickPressed = false;
    private static float maxDistance;
    private static float minDistance = 1.5f;
    private static float lastAngle;
    private static AssetManager assetManager;
    private static boolean cursorSave = false;
    private static float ratioScreen;
    private static float zoomSpeed = 2.5f;
    private static int lastScrollTime = -1;
    private static float smoothScrollTime;
    private static float TRANSITION_SCROLL_TIME = 200f;
    private static float TRANSITION_ASTRE_TIME = 50f;
    private static float wantedDistanceFromAstre;
    private static float distanceDifference;
    private static float lambdaSmoothZoom;
    private static float expSumSmoothZoom;

    public static void init(Camera camReceive, AssetManager assetManagerReceive, InputManager inputManagerReceive) {
        cam = camReceive;
        inputManager = inputManagerReceive;
        assetManager = assetManagerReceive;
        maxDistance = minDistance * 100000 * zoomSpeed;
        distanceFromAstre = minDistance * zoomSpeed * 2;
        wantedDistanceFromAstre = distanceFromAstre;
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
        if (astres.getCurrentValue() instanceof Planet planet) {
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
            wantedDistanceFromAstre = minDistance * zoomSpeed * 2;
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

    public static void setAstre(Astre astreReceive) {
        astre = astreReceive;
        astres = new CircularHashMapAstre();
        astres.createMapFromList(astre.getEveryAstres());
    }

    public static void nextAstre() {
        astres.nextValue();
        initAngle();
    }

    public static void prevAstre() {
        astres.prevValue();
        initAngle();
    }

    private static void setAstreByObject(Astre astreReceive) {
        if (astres == null) {
            return;
        }
        astres.setCurrentValue(astreReceive);
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

    private static Astre setClosestAstre(List<Astre> astresDetecte) {
        float distance = Float.MAX_VALUE;
        Astre closestAstre = null;
        for (Astre a : astresDetecte) {
            float newDistance = a.getWorldTranslation().distance(cam.getLocation());

            if (newDistance < distance) {
                distance = newDistance;
                closestAstre = a;
            }
        }
        return closestAstre;
    }

    private static boolean detectAstre(boolean changeAstre) {
        List<Astre> astresDetecte = new ArrayList<>();
        boolean res = false;
        Vector2f mousePos = inputManager.getCursorPosition();
        for (Astre a : astres.getValues()) {
            Vector3f camPos = cam.getLocation();
            Vector3f clickDirection = getClickDirection(mousePos);
            Vector3f astrepos = a.getWorldTranslation();
            Vector3f astresScreenPos = cam.getScreenCoordinates(astrepos);
            Vector2f astresScreenPos2d = new Vector2f(astresScreenPos.x, astresScreenPos.y);
            float ratioPixel = ratioScreen;
            if (!a.equals(astres.getCurrentValue()) &&
                    (espilonEqualsVector2d(mousePos, astresScreenPos2d, ratioPixel) ||
                            a.collision(camPos, clickDirection, 1f))) {
                astresDetecte.add(a);
                res = true;
            }
        }
        if (!res) {
            if (!changeAstre) {
                astres.forEach(a -> a.modifColorMult(false));
            }
            return res;
        }
        Astre closest = setClosestAstre(astresDetecte);
        if (changeAstre) {
            setAstreByObject(closest);
        } else {
            for (Astre a : astres.getValues()) {
                a.modifColorMult(a.equals(closest));
            }
        }

        return res;
    }

    private static ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("MouseScrollUp")) {

                wantedDistanceFromAstre = Math.max(wantedDistanceFromAstre / zoomSpeed, minDistance);
                smoothScrollTime = TRANSITION_SCROLL_TIME;
                initSmoothZoomVar();
            }
            if (name.equals("MouseScrollDown")) {
                wantedDistanceFromAstre = Math.min(wantedDistanceFromAstre * zoomSpeed, maxDistance);
                smoothScrollTime = TRANSITION_SCROLL_TIME;
                initSmoothZoomVar();
            }
            if (name.equals("leftClick")) {
                isLeftClickPressed = isPressed;
                lastMousePosition = inputManager.getCursorPosition();
                detectAstre(isPressed);
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
                switchCursor(detectAstre(false));

            }
        }
    };

    public static void initSmoothZoomVar() {
        lastScrollTime = 0;
        distanceDifference = wantedDistanceFromAstre - distanceFromAstre;
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

            distanceFromAstre += (distanceDifference * calc);
            lastScrollTime++;
        }
        lastMousePosition = inputManager.getCursorPosition().clone();
    }

    public static Vector3f calcCoord() {
        Vector3f astrePos = astres.getCurrentValue().getWorldTranslation();
        float practicalDistance = Math.max(distanceFromAstre * astres.getCurrentValue().getScaleSize(), 1.5f);

        float x = astrePos.x + practicalDistance * FastMath.cos(FastMath.DEG_TO_RAD * angleVertical)
                * FastMath.sin(FastMath.DEG_TO_RAD * angleHorizontal);
        float z = astrePos.z + practicalDistance * FastMath.cos(FastMath.DEG_TO_RAD * angleVertical)
                * FastMath.cos(FastMath.DEG_TO_RAD * angleHorizontal);
        float y = astrePos.y + practicalDistance * FastMath.sin(FastMath.DEG_TO_RAD * angleVertical);

        return new Vector3f(x, y, z);
    }

    public static void calcPos() {
        Vector3f lookAtPos;
        if (astres.getCurrentValue() instanceof Planet planet) {
            Vector3f primaryPos = planet.getPrimary().getWorldTranslation();

            Vector3f planetPos = planet.getWorldTranslation();

            float minWeight = 0f;
            float maxWeight = 1f;
            float distanceNormalized = (distanceFromAstre - minDistance) / (maxDistance - minDistance);

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
            lookAtPos = astres.getCurrentValue().getWorldTranslation();
        }
        Vector3f newPos = calcCoord();
        cam.setLocation(newPos);
        cam.lookAt(lookAtPos,
                Vector3f.UNIT_Y);
    }

    public static void updateLocation(double time, float speed) {
        if (astres.getCurrentValue() instanceof Planet planet) {
            float newAngle = planet.getCurrentAngle() * FastMath.RAD_TO_DEG;
            setAngleHorizontal(angleHorizontal - (newAngle - lastAngle));
            lastAngle = newAngle;
        }
        calcPos();
    }

    public static void smoothZoom(float zoom) {
        distanceFromAstre = zoom;
    }

    public static void update(double time, float speed) {
        updateZoom();
        updateLocation(time, speed);
    }

}