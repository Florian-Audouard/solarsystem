package fr.univtln.faudouard595.solarsystem;

import java.util.ArrayList;
import java.util.List;

import com.jme3.app.SimpleApplication;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import lombok.extern.slf4j.Slf4j;

import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

@Slf4j
public class App extends SimpleApplication {

    private static final float sunSize = 100;
    private float time = 0f; // Temps écoulé
    private float speed = 1f;
    private List<Planet> planets = new ArrayList<>();

    public static void main(String[] args) {
        App app = new App();
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1920); // Largeur de la fenêtre
        settings.setHeight(1080); // Hauteur de la fenêtre
        app.setSettings(settings);
        app.start();
    }

    public App() {

    }

    private void initKeys() {
        inputManager.addMapping("Sprint", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("UltaSpeed", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("SpeedUp", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addMapping("SpeedDown", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addListener(actionListener, "Sprint", "SpeedUp", "SpeedDown", "UltaSpeed");
    }

    final private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Sprint")) {
                if (keyPressed) {
                    flyCam.setMoveSpeed(sunSize);
                } else {
                    flyCam.setMoveSpeed(sunSize / 50);
                }
            }
            if (name.equals("UltaSpeed")) {
                if (keyPressed) {
                    flyCam.setMoveSpeed(sunSize * 10);
                } else {
                    flyCam.setMoveSpeed(sunSize / 50);
                }
            }
            if (name.equals("SpeedUp") && keyPressed) {
                speed *= 2;
            }
            if (name.equals("SpeedDown") && keyPressed) {
                speed = Math.max(speed / 2, 0.1f);
            }

        }
    };

    public void createSpace() {

        Texture starTexture = assetManager.loadTexture("Textures/Sky/StarSky.jpg");
        Spatial sky = SkyFactory.createSky(assetManager, starTexture, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);

        Sphere sphere = new Sphere(30, 30, sunSize / 2);
        Spatial sun = new Geometry("Sphere", sphere);
        Texture texture = assetManager.loadTexture("Textures/Planet/Sun.jpg");
        Material mat;
        mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", texture);

        sun.setMaterial(mat);
        sun.rotate(-1.5f, 0f, 0.0f);

        rootNode.attachChild(sun);

        PointLight pointLight = new PointLight();
        pointLight.setPosition(Vector3f.ZERO);
        pointLight.setColor(ColorRGBA.White);
        pointLight.setRadius(0f);
        rootNode.addLight(pointLight);
    }

    private void initSettings() {
        flyCam.setMoveSpeed(sunSize / 50);
        cam.setLocation(new Vector3f(sunSize * 10, sunSize * 10, sunSize * 10));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        cam.setFrustumNear(1f);
        cam.setFrustumFar(sunSize * 1000);
    }

    @Override
    public void simpleInitApp() {
        initSettings();
        initKeys();
        createSpace();
        Planet.sunSize = sunSize;
        Planet mars = new Planet("Mars", 6_7790, 227_939_2, 0.0934f, 686.98f, 1.0259f);
        mars.generatePlanet(assetManager, rootNode);
        planets.add(mars);

    }

    @Override
    public void simpleUpdate(float tpf) {
        time += tpf * speed;
        planets.forEach(planet -> planet.update(time));
    }
}
