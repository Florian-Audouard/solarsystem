package fr.univtln.faudouard595.solarsystem.util;

import java.util.List;

import com.jme3.input.ChaseCamera;
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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class CameraTool {
    private static Astre astre;
    private static List<Astre> astres;
    private static int currentAstre = 0;
    private static Camera cam;
    private static float distance;
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

    public static void init(Camera camReceive, InputManager inputManagerReceive) {
        cam = camReceive;
        inputManager = inputManagerReceive;

        distance = 20f;
        inputManager.addMapping("MouseScrollDown", new MouseAxisTrigger(2, true));
        inputManager.addMapping("MouseScrollUp", new MouseAxisTrigger(2, false));
        inputManager.addMapping("leftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addMapping("moveMouse", new MouseAxisTrigger(0, true), new MouseAxisTrigger(1, true),
                new MouseAxisTrigger(0, false), new MouseAxisTrigger(1, false));
        inputManager.addListener(actionListener, "leftClick", "MouseScrollUp", "MouseScrollDown");
        inputManager.addListener(analogListener, "moveMouse");

    }

    public static void setAngleHorizontal(float angleHorizontalReceive) {
        angleHorizontal = ((angleHorizontalReceive % 360) + 360) % 360;
    }

    public static void initAngle() {
        if (astre instanceof Planet planet) {
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

    public static void setAstre(Astre astreReceive) {
        astre = astreReceive;
        astres = astre.getEveryAstres();
    }

    public static void nextAstre() {
        if (astres == null) {
            return;
        }
        if (currentAstre >= astres.size() - 1) {
            currentAstre = 0;
        } else {
            currentAstre++;
        }
        astre = astres.get(currentAstre);
        initAngle();
    }

    public static void prevAstre() {
        if (astres == null) {
            return;
        }
        if (currentAstre <= 0) {
            currentAstre = astres.size() - 1;
        } else {
            currentAstre--;
        }
        astre = astres.get(currentAstre);
        initAngle();
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
            }
        }
    };
    private static AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("moveMouse") && isLeftClickPressed) {
                Vector2f cursorPos = inputManager.getCursorPosition();
                Vector2f delta = cursorPos.subtract(lastMousePosition);
                log.info("delta: {}", delta);
                setAngleHorizontal(angleHorizontal - (delta.x * 0.1f));
                angleVertical -= delta.y * 0.1f;
                log.info("angleHorizontal: {} , angleVertical: {}", angleHorizontal, angleVertical);
                lastMousePosition = cursorPos;
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

            distance += zoom;
            distance = FastMath.clamp(distance, minDistance, maxDistance);

            lastScrollTime++;
        }
        lastMousePosition = inputManager.getCursorPosition().clone();
    }

    public static Vector3f calcCoord() {
        Vector3f astrePos = astre.getModel().getWorldTranslation();
        float practicalDistance = Math.max(distance * astre.getScaleSize(), 1.5f);

        float x = astrePos.x + practicalDistance * FastMath.cos(FastMath.DEG_TO_RAD * angleVertical)
                * FastMath.sin(FastMath.DEG_TO_RAD * angleHorizontal);
        float z = astrePos.z + practicalDistance * FastMath.cos(FastMath.DEG_TO_RAD * angleVertical)
                * FastMath.cos(FastMath.DEG_TO_RAD * angleHorizontal);
        float y = astrePos.y + practicalDistance * FastMath.sin(FastMath.DEG_TO_RAD * angleVertical);

        return new Vector3f(x, y, z);
    }

    public static void calcPos() {
        Vector3f lookAtPos;
        if (astre instanceof Planet planet) {
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
                    - ((distance - minDistance) / (maxDistance - minDistance)) * (maxWeight - minWeight);

            lookAtPos = newPrimary.mult(1 - weight).add(newPlanet.mult(weight));

        } else {
            lookAtPos = astre.getModel().getWorldTranslation();
        }
        Vector3f newPos = calcCoord();
        cam.setLocation(newPos);
        cam.lookAt(lookAtPos,
                Vector3f.UNIT_Y);
    }

    public static void updateLocation(double time, float speed) {
        if (astre instanceof Planet planet) {
            float newAngle = planet.getCurrentAngle() * FastMath.RAD_TO_DEG;
            setAngleHorizontal(angleHorizontal - (newAngle - lastAngle));
            lastAngle = newAngle;
            // log.info("anglePlanet: {} , angleHorizontal: {} , orbitalPeriode: {}",
            // planet.getCurrentAngle(),
            // angleHorizontal, planet.getOrbitalPeriod());
        }
        calcPos();
    }

    public static void update(double time, float speed) {
        updateZoom();
        updateLocation(time, speed);
    }

}