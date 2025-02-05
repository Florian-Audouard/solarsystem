package fr.univtln.faudouard595.solarsystem;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import fr.univtln.faudouard595.solarsystem.Astre.Astre;
import fr.univtln.faudouard595.solarsystem.Astre.Planet;
import fr.univtln.faudouard595.solarsystem.Astre.Star;
import fr.univtln.faudouard595.solarsystem.util.CameraTool;
import fr.univtln.faudouard595.solarsystem.Astre.Astre.TYPE;
import lombok.extern.slf4j.Slf4j;

import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;

@Slf4j
public class App extends SimpleApplication {

    private static final float sunSize = 100;
    private double time = 0f;
    private float speed = 1f;
    private float minSpeed = 1f;
    private float timeScaler = 1.1f;
    private float flowOfTime = 1;
    private Star sun;
    private BitmapText helloText;
    private BitmapFont font;
    private float startOfUniver = 9624787761l; // timeStamp before 1970 (01/01/1665)
    private CameraTool ct;

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

        inputManager.addMapping("removeLines", new KeyTrigger(KeyInput.KEY_F8));

        inputManager.addListener(actionListener, "Sprint", "UltaSpeed", "moveSun", "removeLines");
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
                if (flowOfTime == -1) {
                    speed = Math.max(speed / timeScaler, minSpeed);
                    if (speed == minSpeed) {
                        flowOfTime *= -1;
                    }
                } else {
                    speed *= timeScaler;
                }
            }
            if (name.equals("SpeedDown")) {
                if (flowOfTime == 1) {
                    speed = Math.max(speed / timeScaler, minSpeed);
                    if (speed == minSpeed) {
                        flowOfTime *= -1;
                    }
                } else {
                    speed *= timeScaler;
                }

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
            if (keyPressed && name.equals("removeLines")) {
                sun.switchDisplayLines();
            }
        }
    };

    public void createSpace() {

        Texture starTexture = assetManager.loadTexture("Textures/Sky/StarSky.jpg");
        Spatial sky = SkyFactory.createSky(assetManager, starTexture, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);

        sun = new Star("Sun", sunSize, 25.05f, Astre.TYPE.SPHERE);
        sun.generateStar(rootNode, viewPort);
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
        sun.addPlanet("Mercury", 4_879.4f, 57_909_227f, 0.206f,
                88f, 59f, TYPE.SPHERE);
        sun.addPlanet("Venus", 12_104f, 108_208_930f, 0.007f,
                225.025f, 243f, TYPE.SPHERE);
        sun.addPlanet("Earth", 12_756f, 149_597_870f, 0.017f,
                365.25f, 1f, TYPE.SPHERE);
        sun.addPlanet("Mars", 6_779f, 227_939_200f, 0.0963f,
                686.98f, 1.02f, TYPE.SPHERE);
        sun.addPlanet("Jupiter", 139_820f, 778_340_821f, 0.048f,
                4_330.6f, 0.413f, TYPE.SPHERE);
        sun.addPlanet("Saturn", 116_460f, 1_426_666_422f, 0.056f,
                10_756f, 0.446f, TYPE.SPHERE);
        sun.addPlanet("Uranus", 50_724f, 2_870_658_186f, 0.047f,
                30_687f, 0.71f, TYPE.SPHERE);
        sun.addPlanet("Neptune", 49_244f, 4_498_396_441f, 0.248f,
                60_190f, 0.667f, TYPE.SPHERE);
        Planet earth = sun.getPlanet("Earth");
        earth.addPlanet("Moon", 3_474.8f, 384_400f, 0.0549f,
                27.3f, 27.3f, TYPE.SPHERE);
        sun.scalePlanets(1f);
        sun.changeDistancePlanets(0.005f);

        time = startOfUniver + ((double) Instant.now().getEpochSecond());

        // Charger la police par défaut
        font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        // Créer un BitmapText via la factory
        helloText = font.createLabel("Hello, jMonkey!");
        helloText.setSize(40); // Taille du texte
        helloText.setColor(ColorRGBA.White); // Couleur du texte
        helloText.setLocalTranslation(100, settings.getHeight() - 50, 0); // Position à l'écran

        // Ajouter au GUI (HUD)
        guiNode.attachChild(helloText);

        ct = new CameraTool(cam);
        Planet mars = sun.getPlanet("Mars");
        ct.setPlanet(mars);

    }

    @Override
    public void simpleUpdate(float tpf) {
        time += (tpf) * speed * flowOfTime;
        Instant instant = Instant.ofEpochSecond((long) (time - startOfUniver));
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDate = dateTime.format(formatter);
        helloText.setText(formattedDate);
        sun.update(time);
        ct.update(time);

    }
}
