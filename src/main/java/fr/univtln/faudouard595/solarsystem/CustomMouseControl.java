package fr.univtln.faudouard595.solarsystem;

import java.time.Duration;
import java.time.Instant;

import com.jme3.app.SimpleApplication;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import fr.univtln.faudouard595.solarsystem.Astre.Astre;
import fr.univtln.faudouard595.solarsystem.Astre.Star;

public class CustomMouseControl extends SimpleApplication {

    private Vector3f initialCameraPos = new Vector3f(1000, 1000, 1000);
    private float zoomSpeed = 30f;
    private int directionZoom = 1;
    private int scrollTime = 10;
    private int lastScrollTime = -1;
    private boolean isLeftClickPressed = false;
    private Vector2f lastMousePosition = new Vector2f();
    private float angleHorizontal = 0f;
    private float angleVertical = 10f;
    Star sun;

    public static void main(String[] args) {
        CustomMouseControl app = new CustomMouseControl();
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1920);
        settings.setHeight(1080);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Positionner la caméra initiale
        cam.setLocation(initialCameraPos);
        Texture starTexture = assetManager.loadTexture("Textures/Sky/StarSky.jpg");
        Spatial sky = SkyFactory.createSky(assetManager, starTexture, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);
        // Désactiver le FlyByCamera par défaut
        flyCam.setEnabled(false);
        cam.setFrustumFar(1000000);

        // Ajouter un listener pour détecter l'événement de scroll
        inputManager.addMapping("MouseScrollDown", new MouseAxisTrigger(2, true));
        inputManager.addMapping("MouseScrollUp", new MouseAxisTrigger(2, false));
        inputManager.addMapping("leftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("moveMouse", new MouseAxisTrigger(0, true), new MouseAxisTrigger(1, true),
                new MouseAxisTrigger(0, false), new MouseAxisTrigger(1, false));

        inputManager.addListener(actionListener, "leftClick", "MouseScrollUp", "MouseScrollDown");
        inputManager.addListener(analogListener, "moveMouse");

        Astre.assetManager = assetManager;

        sun = new Star("Sun", 100f, 25.05f, Astre.TYPE.SPHERE, ColorRGBA.White);
        sun.generateStar(rootNode, viewPort);

    }

    @Override
    public void simpleUpdate(float tpf) {
        sun.update(tpf);
        if (lastScrollTime == scrollTime) {
            lastScrollTime = -1;
        }
        if (lastScrollTime != -1) {
            double fractionElapsed = (float) lastScrollTime / scrollTime;
            double countdownValue = 1 - fractionElapsed;
            float zoom = (float) (directionZoom * zoomSpeed * countdownValue);
            cam.setLocation(cam.getLocation().add(cam.getDirection().mult(zoom)));

            lastScrollTime++;
        }
        lastMousePosition = inputManager.getCursorPosition().clone();
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("MouseScrollUp")) {
                lastScrollTime = 0;
                directionZoom = 1;
            }
            if (name.equals("MouseScrollDown")) {
                lastScrollTime = 0;
                directionZoom = -1;
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
                angleHorizontal -= delta.x * 0.1f; // Ajuster la vitesse avec un facteur
                angleVertical += delta.y * 0.1f;
                // angleVertical = FastMath.clamp(angleVertical, -90f, 90f);

                lastMousePosition = cursorPos;
            }
        }
    };
}
