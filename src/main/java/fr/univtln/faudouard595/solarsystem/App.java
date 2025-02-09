package fr.univtln.faudouard595.solarsystem;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.terrain.noise.Color;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import fr.univtln.faudouard595.solarsystem.body.Body;
import fr.univtln.faudouard595.solarsystem.body.Planet;
import fr.univtln.faudouard595.solarsystem.body.Star;
import fr.univtln.faudouard595.solarsystem.body.Body.TYPE;
import fr.univtln.faudouard595.solarsystem.utils.camera.CameraTool;
import fr.univtln.faudouard595.solarsystem.utils.api.ApiAstreInfo;
import fr.univtln.faudouard595.solarsystem.utils.api.DataCreationNeeded;

import lombok.extern.slf4j.Slf4j;

import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;

import com.jme3.light.AmbientLight;

@Slf4j
public class App extends SimpleApplication {

    private static final float sunSize = 500;
    private double time = 0f;
    private float speed = 1f;
    private float minSpeed = 1f;
    private float timeScaler = 1.1f;
    private float flowOfTime = 1;
    private Star sun;
    private BitmapText helloText;
    private BitmapFont font;
    private float startOfUniver = 9624787761l; // timeStamp before 1970 (01/01/1665)
    private float maxRenderDistanceMult = 10000000f;

    public static void main(String[] args) {
        App app = new App();
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1920);
        settings.setHeight(1080);
        // settings.setHeight(500);
        app.setSettings(settings);
        app.start();

    }

    private void initKeys() {
        inputManager.addMapping("SpeedUp", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addMapping("SpeedDown", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addMapping("ScaleUp", new KeyTrigger(KeyInput.KEY_F3));
        inputManager.addMapping("ScaleDown", new KeyTrigger(KeyInput.KEY_F4));
        inputManager.addMapping("DistanceUp", new KeyTrigger(KeyInput.KEY_F5));
        inputManager.addMapping("DistanceDown", new KeyTrigger(KeyInput.KEY_F6));
        inputManager.addMapping("Test", new KeyTrigger(KeyInput.KEY_F7));
        inputManager.addMapping("nextAstre", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("prevAstre", new KeyTrigger(KeyInput.KEY_LEFT));

        inputManager.addMapping("removeLines", new KeyTrigger(KeyInput.KEY_F8));

        // inputManager.addMapping("lockPlanet", new KeyTrigger(KeyInput.KEY_F9));

        inputManager.addListener(actionListener, "Test", "removeLines", "nextAstre", "prevAstre");
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
                    if (speed == minSpeed) {
                        flowOfTime *= -1;
                    }
                    speed = Math.max(speed / timeScaler, minSpeed);
                } else {
                    speed *= timeScaler;
                }
            }
            if (name.equals("SpeedDown")) {
                if (flowOfTime == 1) {
                    if (speed == minSpeed) {
                        flowOfTime *= -1;
                    }
                    speed = Math.max(speed / timeScaler, minSpeed);

                } else {
                    speed *= timeScaler;
                }

            }

        }
    };

    final private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {

            if (name.equals("Test")) {
                // Random rand = new Random();
                // sun.getNode().move(rand.nextFloat(sunSize * 2) - sunSize,
                // rand.nextFloat(sunSize * 2) - sunSize,
                // rand.nextFloat(sunSize * 2) - sunSize);
                ;
            }
            if (keyPressed && name.equals("removeLines")) {
                sun.switchDisplayLines();
            }
            if (keyPressed && name.equals("nextAstre")) {
                CameraTool.nextBody();
            }
            if (keyPressed && name.equals("prevAstre")) {
                CameraTool.prevBody();
            }
        }
    };

    public void createSpace() {

        Texture starTexture = assetManager.loadTexture("Textures/Sky/StarSky.jpg");
        Spatial sky = SkyFactory.createSky(assetManager, starTexture, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);
        Body.assetManager = assetManager;
        Body.guiNode = guiNode;
        Body.cam = cam;
        Body.referenceSize = sunSize;
        createBodies();

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.1f));
        rootNode.addLight(al);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Scene);
        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp);
    }

    private void initSettings() {
        flyCam.setEnabled(false);

        cam.setFrustumFar(sunSize * maxRenderDistanceMult);

    }

    public void createBodies() {
        ApiAstreInfo apiAstreInfo = new ApiAstreInfo();
        List<DataCreationNeeded> bodies = new ArrayList<>();
        bodies.add(new DataCreationNeeded("soleil", ColorRGBA.Yellow));
        bodies.add(new DataCreationNeeded("mercure", new ColorRGBA(151f / 255, 104f / 255, 172f / 255, 1f)));
        bodies.add(new DataCreationNeeded("venus", new ColorRGBA(176f / 255, 121f / 255, 25f / 255, 1f)));
        bodies.add(new DataCreationNeeded("terre", new ColorRGBA(0f / 255, 153f / 255, 204f / 255, 1f)));
        bodies.add(new DataCreationNeeded("mars", new ColorRGBA(154f / 255, 78f / 255, 25f / 255, 1f)));
        bodies.add(new DataCreationNeeded("jupiter", new ColorRGBA(218f / 255, 139f / 255, 114f / 255, 1f)));
        bodies.add(new DataCreationNeeded("saturne", new ColorRGBA(213f / 255, 193f / 255, 135f / 255, 1f)));
        bodies.add(new DataCreationNeeded("uranus", new ColorRGBA(104f / 255, 204f / 255, 218f / 255, 1f)));
        bodies.add(new DataCreationNeeded("neptune", new ColorRGBA(112f / 255, 140f / 255, 227f / 255, 1f)));
        bodies.add(new DataCreationNeeded("lune", ColorRGBA.Gray));

        sun = (Star) apiAstreInfo.getBodies(bodies, TYPE.SPHERE);
        sun.generateBody(rootNode, viewPort);
    }

    @Override
    public void simpleInitApp() {
        initSettings();
        initKeys();
        createSpace();
        time = startOfUniver + ((double) Instant.now().getEpochSecond());

        // sun.changeDistancePlanets(0.01f);
        // sun.scalePlanets(30f);

        font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        helloText = font.createLabel("Hello, jMonkey!");
        helloText.setSize(40);
        helloText.setColor(ColorRGBA.White);
        helloText.setLocalTranslation(100, settings.getHeight() - 50, 0);

        guiNode.attachChild(helloText);

        CameraTool.init(cam, assetManager, inputManager);
        CameraTool.setBody(sun);

    }

    @Override
    public void simpleUpdate(float tpf) {
        time += (tpf) * speed * flowOfTime;
        sun.update(time);
        CameraTool.update(time, speed);
        Instant instant = Instant.ofEpochSecond((long) (time - startOfUniver));
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDate = dateTime.format(formatter);
        helloText.setText(formattedDate);
    }
}
