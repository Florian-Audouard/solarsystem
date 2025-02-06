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
import com.jme3.scene.Node;

import fr.univtln.faudouard595.solarsystem.Astre.Astre;
import fr.univtln.faudouard595.solarsystem.Astre.Planet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class CameraTool {
    private Astre astre;
    private List<Astre> astres;
    private int currentAstre = 0;
    private Camera cam;
    private boolean chaseCamActive;
    private float distance;
    private int directionZoom = 1;
    private int lastScrollTime = -1;
    private int scrollTime = 10;
    private Vector2f lastMousePosition;
    private float zoomSpeed = 1f;
    private InputManager inputManager;
    private float angleHorizontal;
    private float angleVertical;
    private boolean isLeftClickPressed = false;
    private float maxDistance = 100f;
    private float minDistance = 5f;
    private float lastAngle;

    public CameraTool(Camera cam, Node rootNode, InputManager inputManager) {
        this.cam = cam;
        this.inputManager = inputManager;

        this.distance = 20f;
        inputManager.addMapping("MouseScrollDown", new MouseAxisTrigger(2, true));
        inputManager.addMapping("MouseScrollUp", new MouseAxisTrigger(2, false));
        inputManager.addMapping("leftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addMapping("moveMouse", new MouseAxisTrigger(0, true), new MouseAxisTrigger(1, true),
                new MouseAxisTrigger(0, false), new MouseAxisTrigger(1, false));
        inputManager.addListener(actionListener, "leftClick", "MouseScrollUp", "MouseScrollDown");
        inputManager.addListener(analogListener, "moveMouse");
        angleHorizontal = 4;
        angleVertical = 5;

    }

    public void setAngleHorizontal(float angleHorizontal) {
        this.angleHorizontal = ((angleHorizontal % 360) + 360) % 360;
    }

    public void initLastAngle() {
        if (astre instanceof Planet planet) {
            setAngleHorizontal(planet.getCurrentAngle());
            log.info("anglePlanet: {}", planet.getCurrentAngle());
        }
    }

    public void setAstre(Astre astre) {
        this.astre = astre;
        astres = astre.getEveryAstres();
    }

    public void nextAstre() {
        if (astres == null) {
            return;
        }
        if (currentAstre >= astres.size() - 1) {
            currentAstre = 0;
        } else {
            currentAstre++;
        }
        astre = astres.get(currentAstre);
        initLastAngle();
    }

    public void prevAstre() {
        if (astres == null) {
            return;
        }
        if (currentAstre <= 0) {
            currentAstre = astres.size() - 1;
        } else {
            currentAstre--;
        }
        astre = astres.get(currentAstre);
        initLastAngle();
    }

    private ActionListener actionListener = new ActionListener() {
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
    private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("moveMouse") && isLeftClickPressed) {
                Vector2f cursorPos = inputManager.getCursorPosition();
                Vector2f delta = cursorPos.subtract(lastMousePosition);
                setAngleHorizontal(angleHorizontal - (delta.x * 0.1f));
                angleVertical += delta.y * 0.1f;

                lastMousePosition = cursorPos;
            }
        }
    };

    public void updateZoom() {
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

    public void calcPos() {
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
        Vector3f astrePos = astre.getModel().getWorldTranslation();

        float practicalDistance = distance * astre.getScaleSize();
        float x = astrePos.x + practicalDistance * FastMath.cos(FastMath.DEG_TO_RAD * angleVertical)
                * FastMath.sin(FastMath.DEG_TO_RAD * angleHorizontal);
        float z = astrePos.z + practicalDistance * FastMath.cos(FastMath.DEG_TO_RAD * angleVertical)
                * FastMath.cos(FastMath.DEG_TO_RAD * angleHorizontal);
        float y = astrePos.y + practicalDistance * FastMath.sin(FastMath.DEG_TO_RAD * angleVertical);

        Vector3f newPos = new Vector3f(x, y, z);
        cam.setLocation(newPos);
        cam.lookAt(lookAtPos,
                Vector3f.UNIT_Y);
    }

    public void updateLocation(double time, float speed) {
        if (astre instanceof Planet planet) {
            float newAngle = planet.getAngle(time) * FastMath.RAD_TO_DEG;
            setAngleHorizontal(angleHorizontal - (newAngle - lastAngle));
            lastAngle = newAngle;
        }
        calcPos();
    }

    public void update(double time, float speed) {
        updateZoom();
        updateLocation(time, speed);
        log.info("angleHorizontal: {} angleVertical: {} distance: {}", angleHorizontal, angleVertical, distance);
    }

}