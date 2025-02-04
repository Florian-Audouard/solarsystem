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

import fr.univtln.faudouard595.solarsystem.Astre.Planet;
import fr.univtln.faudouard595.solarsystem.Astre.Star;
import lombok.extern.slf4j.Slf4j;

import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

@Slf4j
public class App extends SimpleApplication {

    private static final float sunSize = 100;
    private float time = 0f;
    private float speed = 1f;
    private List<Planet> planets = new ArrayList<>();
    private Star sun;

    public static void main(String[] args) {
        App app = new App();
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1920);
        settings.setHeight(1080);
        app.setSettings(settings);
        app.start();
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

        sun = new Star("Sun", sunSize, 25.05f);
        sun.generateStar(assetManager, rootNode);
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
        Planet.sun = sun;
        Planet.realSunSize = 1_392_700;
        // TODO Verify the values
        // planets.add(new Planet("Mercury", 2_439.7f, 57_909_227f, 0.0553f, 58.646f,
        // 3.7f));
        // planets.add(new Planet("Venus", 6_051.8f, 108_208_930f, 0.815f, 243.025f,
        // 8.87f));
        // planets.add(new Planet("Earth", 6_371f, 149_597_870f, 1.0f, 24f, 9.81f));
        planets.add(new Planet("Mars", 6_77900f, 227_939_2f, 0.107f, 686.98f, 3.72076f));
        // planets.add(new Planet("Jupiter", 69_911f, 778_340_821f, 317.8f, 9.925f,
        // 24.79f));
        // planets.add(new Planet("Saturn", 58_232f, 1_426_666_422f, 95.2f, 10.7f,
        // 10.44f));
        // planets.add(new Planet("Uranus", 25_362f, 2_870_658_186f, 14.5f, 17.24f,
        // 8.69f));
        // planets.add(new Planet("Neptune", 24_622f, 4_498_396_441f, 17.1f, 16.11f,
        // 11.15f));
        planets.forEach(planet -> planet.generatePlanet(assetManager, rootNode));

    }

    @Override
    public void simpleUpdate(float tpf) {
        time += tpf * speed;
        sun.update(time);
        planets.forEach(planet -> planet.update(time));
    }
}
