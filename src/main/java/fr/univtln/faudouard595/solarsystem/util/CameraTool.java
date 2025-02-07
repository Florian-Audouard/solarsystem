package fr.univtln.faudouard595.solarsystem.util;

import java.util.ArrayList;
import java.util.List;

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
    private static int directionZoom = 1;
    private static int lastScrollTime = -1;
    private static int scrollTime = 10;
    private static Vector2f lastMousePosition;
    private static float zoomSpeed = 1f;
    private static InputManager inputManager;
    private static float angleHorizontal;
    private static float angleVertical;
    private static boolean isLeftClickPressed = false;
    private static float maxDistance = 100f;
    private static float minDistance = 5f;
    private static float lastAngle;
    private static AssetManager assetManager;
    private static boolean cursorSave = false;

    public static void init(Camera camReceive, AssetManager assetManagerReceive, InputManager inputManagerReceive) {
        cam = camReceive;
        inputManager = inputManagerReceive;
        assetManager = assetManagerReceive;
        distanceFromAstre = 20f;
        inputManager.addMapping("MouseScrollDown", new MouseAxisTrigger(2, true));
        inputManager.addMapping("MouseScrollUp", new MouseAxisTrigger(2, false));
        inputManager.addMapping("leftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addMapping("moveMouse", new MouseAxisTrigger(0, true), new MouseAxisTrigger(1, true),
                new MouseAxisTrigger(0, false), new MouseAxisTrigger(1, false));
        inputManager.addListener(actionListener, "leftClick", "MouseScrollUp", "MouseScrollDown");
        inputManager.addListener(analogListener, "moveMouse");
        setNormalCursor();
    }

    public static void setAngleHorizontal(float angleHorizontalReceive) {
        angleHorizontal = ((angleHorizontalReceive % 360) + 360) % 360;
    }

    public static void initAngle() {
        if (astres.getCurrentValue() instanceof Planet planet) {
            setAngleHorizontal(0);
            Vector3f newPos = calcCoord();
            newPos.y = 0;
            Vector3f planetPos = planet.getModel().getWorldTranslation();
            Vector3f directionCamPlanet = newPos.subtract(planetPos).normalize();
            Vector3f primary = planet.getPrimary().getModel().getWorldTranslation();
            Vector3f directionPrimaryPlanet = planetPos.subtract(primary).normalize();
            float angle = FastMath.acos(directionCamPlanet.dot(directionPrimaryPlanet));
            float sign = directionPrimaryPlanet.x > 0 ? 1 : -1;
            angle = (sign * (angle) * FastMath.RAD_TO_DEG) - 30;
            setAngleHorizontal(angle);
            lastAngle = planet.getCurrentAngle() * FastMath.RAD_TO_DEG;
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
        log.info("next");
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

    private static boolean espilonEquals(Vector3f vector1, Vector3f vector2, float epsilon) {
        float x = vector1.x - vector2.x;
        float y = vector1.y - vector2.y;
        float z = vector1.z - vector2.z;
        return x * x + y * y + z * z < epsilon * epsilon;
    }

    private static Astre setClosestAstre(List<Astre> astresDetecte) {
        float distance = Float.MAX_VALUE;
        Astre closestAstre = null;
        for (Astre a : astresDetecte) {
            float newDistance = a.getModel().getWorldTranslation().distance(cam.getLocation());
            log.info("planet : {} , res : {}", a.getName(), newDistance);

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
        for (Astre a : astres.getValues()) {
            Vector3f clickDirection = getClickDirection(lastMousePosition);
            Vector3f camPos = cam.getLocation();
            Vector3f camAstreDirection = a.getModel().getWorldTranslation().subtract(camPos).normalize();
            boolean tmp = espilonEquals(clickDirection, camAstreDirection, 0.1f);
            if (tmp && !a.equals(astres.getCurrentValue())) {
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
        log.info("closest : {}", closest.getName());
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
                lastScrollTime = 0;
                directionZoom = -1;
            }
            if (name.equals("MouseScrollDown")) {
                lastScrollTime = 0;
                directionZoom = 1;
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

    public static void updateZoom() {
        if (lastScrollTime == scrollTime) {
            lastScrollTime = -1;
        }
        if (lastScrollTime != -1) {
            double fractionElapsed = (float) lastScrollTime / scrollTime;
            double countdownValue = 1 - fractionElapsed;
            float zoom = (float) (directionZoom * zoomSpeed * countdownValue);

            distanceFromAstre += zoom;
            distanceFromAstre = FastMath.clamp(distanceFromAstre, minDistance, maxDistance);

            lastScrollTime++;
        }
        lastMousePosition = inputManager.getCursorPosition().clone();
    }

    public static Vector3f calcCoord() {
        Vector3f astrePos = astres.getCurrentValue().getModel().getWorldTranslation();
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
            Vector3f primaryPos = planet.getPrimary().getModel().getWorldTranslation();
            float primaryRadius = planet.getPrimary().getScaleSize() / 2;

            Vector3f planetPos = planet.getModel().getWorldTranslation();
            float planetRadius = planet.getScaleSize() / 2;

            Vector3f distancePrimaryPlanet = planetPos.subtract(primaryPos);
            Vector3f direction = distancePrimaryPlanet.normalize();

            Vector3f newPrimary = primaryPos.add(direction.mult(primaryRadius));
            Vector3f newPlanet = planetPos.subtract(direction.mult(planetRadius));

            float ratio = planetRadius / distancePrimaryPlanet.length();

            float minWeight = FastMath.exp((-180f * ratio));
            float maxWeight = 1f;
            float weight = maxWeight
                    - ((distanceFromAstre - minDistance) / (maxDistance - minDistance)) * (maxWeight - minWeight);

            lookAtPos = newPrimary.mult(1 - weight).add(newPlanet.mult(weight));

        } else {
            lookAtPos = astres.getCurrentValue().getModel().getWorldTranslation();
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

    public static void update(double time, float speed) {
        updateZoom();
        updateLocation(time, speed);
    }

}