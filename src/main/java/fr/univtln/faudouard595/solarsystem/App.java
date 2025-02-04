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

import de.lessvoid.nifty.controls.Slider;
import fr.univtln.faudouard595.solarsystem.Astre.Planet;
import fr.univtln.faudouard595.solarsystem.Astre.Star;
import lombok.extern.slf4j.Slf4j;

import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
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
        inputManager.addMapping("ScaleUp", new KeyTrigger(KeyInput.KEY_F3));
        inputManager.addMapping("ScaleDown", new KeyTrigger(KeyInput.KEY_F4));
        inputManager.addMapping("DistanceUp", new KeyTrigger(KeyInput.KEY_F5));
        inputManager.addMapping("DistanceDown", new KeyTrigger(KeyInput.KEY_F6));
        inputManager.addListener(actionListener, "Sprint", "UltaSpeed");
        inputManager.addListener(analogListener, "SpeedUp", "SpeedDown", "ScaleUp", "ScaleDown",
                "DistanceUp", "DistanceDown");
    }

    final private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("ScaleUp")) {
                planets.forEach(planet -> planet.scale(1.01f));
                // sun.scale(1.01f);
            }
            if (name.equals("ScaleDown")) {
                planets.forEach(planet -> planet.scale(0.99f));
                // sun.scale(0.99f);
            }
            if (name.equals("DistanceUp")) {
                planets.forEach(planet -> planet.changeDistance(planet.getDistanceMultiplier() * 1.01f));
            }
            if (name.equals("DistanceDown")) {
                planets.forEach(planet -> planet.changeDistance(planet.getDistanceMultiplier() * 0.99f));
            }
            if (name.equals("SpeedUp")) {
                speed *= 1.1;
            }
            if (name.equals("SpeedDown")) {
                speed = Math.max(speed / 1.1f, 0.1f);
            }

        }
    };

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
        planets.add(new Planet("Mercury", 4_879.4f, 57_909_227f, 0.206f, 88f,
                59f));
        planets.add(new Planet("Venus", 12_104f, 108_208_930f, 0.007f, 225.025f,
                243f));
        planets.add(new Planet("Earth", 12_756f, 149_597_870f, 0.017f, 365.25f, 1f));
        planets.add(new Planet("Mars", 6_779f, 227_939_200f, 0.0963f, 686.98f, 1.02f));
        planets.add(new Planet("Jupiter", 139_820f, 778_340_821f, 0.048f, 4_330.6f,
                0.413f));
        planets.add(new Planet("Saturn", 116_460f, 1_426_666_422f, 0.056f, 10_756f,
                0.446f));
        planets.add(new Planet("Uranus", 50_724f, 2_870_658_186f, 0.047f, 30_687f,
                0.71f));
        planets.add(new Planet("Neptune", 49_244f, 4_498_396_441f, 0.248f, 60_190f,
                0.667f));
        planets.forEach(planet -> planet.generatePlanet(assetManager, rootNode));
        planets.forEach(planet -> planet.scale(10f));
        planets.forEach(planet -> planet.changeDistance(0.05f));

    }

    @Override
    public void simpleUpdate(float tpf) {
        time += tpf * speed;
        sun.update(time);
        planets.forEach(planet -> planet.update(time));
    }
}
