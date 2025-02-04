package fr.univtln.faudouard595.solarsystem;

import java.util.Random;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import fr.univtln.faudouard595.solarsystem.Astre.Astre;
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
        inputManager.addMapping("moveSun", new KeyTrigger(KeyInput.KEY_F7));
        inputManager.addListener(actionListener, "Sprint", "UltaSpeed", "moveSun");
        inputManager.addListener(analogListener, "SpeedUp", "SpeedDown", "ScaleUp", "ScaleDown",
                "DistanceUp", "DistanceDown");
    }

    final private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("ScaleUp")) {
                sun.scalePlanets(1.01f);
                // sun.scale(1.01f);
            }
            if (name.equals("ScaleDown")) {
                sun.scalePlanets(0.99f);
                // sun.scale(0.99f);
            }
            if (name.equals("DistanceUp")) {
                sun.changeDistancePlanets(1.01f);
            }
            if (name.equals("DistanceDown")) {
                sun.changeDistancePlanets(0.99f);
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
            if (name.equals("moveSun") && keyPressed) {
                Random rand = new Random();
                sun.getNode().move(rand.nextFloat(sunSize * 2) - sunSize, rand.nextFloat(sunSize * 2) - sunSize,
                        rand.nextFloat(sunSize * 2) - sunSize);
            }
        }
    };

    public void createSpace() {

        Texture starTexture = assetManager.loadTexture("Textures/Sky/StarSky.jpg");
        Spatial sky = SkyFactory.createSky(assetManager, starTexture, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);

        sun = new Star("Sun", sunSize, 25.05f);
        sun.generateStar(rootNode);
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
        Astre.assetManager = assetManager;
        createSpace();
        Planet.sun = sun;
        Planet.realSunSize = 1_392_700;
        sun.addPlanet("Mercury", 4_879.4f, 57_909_227f, 0.206f, 88f,
                59f);
        sun.addPlanet("Venus", 12_104f, 108_208_930f, 0.007f, 225.025f,
                243f);
        sun.addPlanet("Earth", 12_756f, 149_597_870f, 0.017f, 365.25f, 1f);
        sun.addPlanet("Mars", 6_779f, 227_939_200f, 0.0963f, 686.98f, 1.02f);
        sun.addPlanet("Jupiter", 139_820f, 778_340_821f, 0.048f, 4_330.6f,
                0.413f);
        sun.addPlanet("Saturn", 116_460f, 1_426_666_422f, 0.056f, 10_756f,
                0.446f);
        sun.addPlanet("Uranus", 50_724f, 2_870_658_186f, 0.047f, 30_687f,
                0.71f);
        sun.addPlanet("Neptune", 49_244f, 4_498_396_441f, 0.248f, 60_190f,
                0.667f);
        Planet earth = sun.getPlanet("Earth");
        earth.addPlanet("Moon", 3_474.8f, 384_400f, 0.0549f, 27.3f, 27.3f);
        sun.scalePlanets(20f);
        sun.changeDistancePlanets(0.05f);

        log.info(earth.getNode().getWorldTranslation().toString());
        log.info(earth.getPlanet("Moon").getNode().getWorldTranslation().toString());

    }

    @Override
    public void simpleUpdate(float tpf) {
        time += tpf * speed;
        sun.update(time);
    }
}
