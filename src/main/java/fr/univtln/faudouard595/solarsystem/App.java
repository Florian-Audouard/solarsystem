package fr.univtln.faudouard595.solarsystem;

import java.time.Instant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.jme3.app.SimpleApplication;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;

import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;

import fr.univtln.faudouard595.solarsystem.space.AsteroidBelt;
import fr.univtln.faudouard595.solarsystem.space.Body;
import fr.univtln.faudouard595.solarsystem.space.Star;
import fr.univtln.faudouard595.solarsystem.ui.controls.button.ButtonControl;
import fr.univtln.faudouard595.solarsystem.ui.controls.camera.CameraTool;
import fr.univtln.faudouard595.solarsystem.ui.controls.trigger.TriggerControls;
import fr.univtln.faudouard595.solarsystem.ui.controls.updatable.Updatable;
import fr.univtln.faudouard595.solarsystem.ui.information.DisplayInformation;
import fr.univtln.faudouard595.solarsystem.ui.loadingscreen.LoadingAppState;
import fr.univtln.faudouard595.solarsystem.utils.api.ApiBodyInfo;
import fr.univtln.faudouard595.solarsystem.utils.api.DataCreationNeeded;
import fr.univtln.faudouard595.solarsystem.utils.collection.SpeedList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import com.jme3.light.AmbientLight;

@Slf4j
@Getter
public class App extends SimpleApplication {
    private boolean firstUpdate = true;
    private boolean loaded = false;
    private boolean startLoading = false;
    private boolean initLoaded = false;
    public Node loadingNode;
    private Node myGuiNode;
    private Node circleNode;
    public int tmp = 0;
    public boolean changeCam = false;

    public double AU = 149_597_870.7;
    public final List<String> ASTEROID_MODELS = Arrays.asList("Ceres", "Haumea", "Eris");

    private static final float sunSize = 500;
    public double time = 0f;
    public boolean isPause = false;
    private Star sun;
    private BitmapText timeText;
    public BitmapFont font;
    public float startOfUniver = 9624787761l; // timeStamp before 1970 (01/01/1665)
    private float maxRenderDistanceMult = 10000000f;
    public SpeedList speedList = new SpeedList();
    public Random random = new Random();
    private Iterator<Body> assetBodies;
    private Iterator<Integer> assetAsteroid;
    private int loadingNumber = 0;
    private Runnable currentGeneration = () -> {
    };

    public static void main(String[] args) {
        boolean test = false;

        App app = new App();
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(60);
        if (test) {
            settings.setResolution(1920, 1080);
            settings.setFullscreen(false);
            settings.setResizable(true);
        } else {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            DisplayMode dm = gd.getDisplayMode();
            int screenWidth = dm.getWidth();
            int screenHeight = dm.getHeight();
            settings.setResolution(screenWidth, screenHeight);
            settings.setFullscreen(true);
        }
        app.setSettings(settings);
        app.start();
    }

    public void generateAsteroidBelt() {
        int totalAsteroid = 6_000;

        double INNER_RADIUS_KUIPER = 30 * AU;
        double OUTER_RADIUS_KUIPER = 50 * AU;
        double THICKNESS_KUIPER = 10 * AU;
        double SIZE_OF_ASTEROID_KUIPER = 30_000_000;
        int NUM_OBJECTS_KUIPER = (totalAsteroid * 2) / 3;
        double ROATION_PERIOD_KUIPER = 200 * 325 * 24 * 60 * 60;
        sun.addAsteroidBelt(ROATION_PERIOD_KUIPER, Body.convertion(INNER_RADIUS_KUIPER),
                Body.convertion(OUTER_RADIUS_KUIPER),
                Body.convertion(THICKNESS_KUIPER), Body.convertion(SIZE_OF_ASTEROID_KUIPER), NUM_OBJECTS_KUIPER);

        double INNER_RADIUS_MAIN = 2.2 * AU;
        double OUTER_RADIUS_MAIN = 4.2 * AU;
        double THICKNESS_ASTEROID_MAIN = 1 * AU;
        double SIZE_OF_ASTEROID_MAIN = 3_000_000;
        int NUM_OBJECTS_ASTEROID_MAIN = totalAsteroid / 3;
        double ROATION_PERIOD_MAIN = 4.5 * 325 * 24 * 60 * 60;
        sun.addAsteroidBelt(ROATION_PERIOD_MAIN, Body.convertion(INNER_RADIUS_MAIN),
                Body.convertion(OUTER_RADIUS_MAIN),
                Body.convertion(THICKNESS_ASTEROID_MAIN), Body.convertion(SIZE_OF_ASTEROID_MAIN),
                NUM_OBJECTS_ASTEROID_MAIN);
    }

    public void createSpace() {

        Texture starTexture = assetManager.loadTexture("Textures/Sky/StarSky.jpg");
        Spatial sky = SkyFactory.createSky(assetManager, starTexture, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);
        Body.app = this;
        circleNode = new Node("CircleNode");
        guiNode.attachChild(circleNode);
        Body.guiNode = circleNode;
        Body.cam = cam;
        Body.referenceSize = sunSize;
        createBodies();

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.05f));
        rootNode.addLight(al);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bloomFilter = new BloomFilter();
        bloomFilter.setBlurScale(0.5f); // Higher values for blur quality
        bloomFilter.setBloomIntensity(4f); // Adjust as needed
        bloomFilter.setDownSamplingFactor(1.0f);
        fpp.addFilter(bloomFilter);
        viewPort.addProcessor(fpp);
    }

    private void initSettings() {
        CameraTool.app = this;
        cam.setFrustumFar(sunSize * maxRenderDistanceMult);

    }

    public void createBodies() {
        ApiBodyInfo apiAstreInfo = new ApiBodyInfo();
        ApiBodyInfo.app = this;
        List<DataCreationNeeded> bodies = new ArrayList<>();
        bodies.add(new DataCreationNeeded("soleil", ColorRGBA.Yellow, 0));
        bodies.add(new DataCreationNeeded("terre", new ColorRGBA(0f / 255, 153f / 255, 204f / 255, 1f), 1));
        bodies.add(new DataCreationNeeded("mercure", new ColorRGBA(151f / 255, 104f / 255, 172f / 255, 1f), 0));
        bodies.add(new DataCreationNeeded("venus", new ColorRGBA(176f / 255, 121f / 255, 25f / 255, 1f), 0));
        bodies.add(new DataCreationNeeded("mars", new ColorRGBA(154f / 255, 78f / 255, 25f / 255, 1f), 2));
        bodies.add(new DataCreationNeeded("jupiter", new ColorRGBA(218f / 255, 139f / 255, 114f / 255, 1f), 4));
        bodies.add(new DataCreationNeeded("saturne", new ColorRGBA(213f / 255, 193f / 255, 135f / 255, 1f), 8));
        bodies.add(new DataCreationNeeded("uranus", new ColorRGBA(104f / 255, 204f / 255, 218f / 255, 1f), 5));
        bodies.add(new DataCreationNeeded("neptune", new ColorRGBA(112f / 255, 140f / 255, 227f / 255, 1f), 1));

        sun = (Star) apiAstreInfo.getBodies(bodies);

        Collection<Body> tmp = sun.getEveryBodies();
        loadingNumber += tmp.size();
        assetBodies = tmp.iterator();
        Collection<Integer> tmpAsteroid = AsteroidBelt.getCollection();
        loadingNumber += tmpAsteroid.size();
        assetAsteroid = tmpAsteroid.iterator();

    }

    public void init() {
        guiNode.attachChild(myGuiNode);
        initSettings();
        generateAsteroidBelt();
        TriggerControls.init(this);
        ButtonControl.init(this);

        time = startOfUniver + ((double) Instant.now().getEpochSecond());

        CameraTool.init(cam, assetManager, inputManager);
        CameraTool.setBody(sun);
        DisplayInformation.app = this;
        DisplayInformation.init();
        guiNode.attachChild(myGuiNode);
        initLoaded = true;
        LoadingAppState.cleanUp();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        GuiGlobals.initialize(this);

        myGuiNode = new Node("MyGuiNode");
        loadingNode = new Node("LoadingNode");
        loadingNode.setLocalTranslation(0, 0, 1);
        guiNode.attachChild(loadingNode);
        setDisplayStatView(false);
        setDisplayFps(false);
        font = assetManager.loadFont("Fonts/Segoe.fnt");
        LoadingAppState.createInstance(font);
        LoadingAppState.initialize(this);
        LoadingAppState.initBackground(this);
    }

    private void startLoading() {
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        createSpace();
        LoadingAppState.initBar(this);
        LoadingAppState.init(loadingNumber);
        startLoading = true;

    }

    private void updateLoading() {
        if (initLoaded) {
            return;
        }
        currentGeneration.run();

        if (assetBodies.hasNext()) {
            Body currentAsset = assetBodies.next();
            currentGeneration = () -> currentAsset.generateBody();
            LoadingAppState.updateProgress("Loading ... %s (" + currentAsset.getName() + ")");
            return;
        }
        if (assetAsteroid.hasNext()) {
            int i = assetAsteroid.next() + 1;
            currentGeneration = () -> AsteroidBelt.initModel(assetManager, i);
            LoadingAppState.updateProgress("Loading ... %s (Asteroid " + i + ")");
            return;
        }
        LoadingAppState.updateProgress("Loading ... %s (Done)");
        loaded = true;

    }

    @Override
    public void simpleUpdate(float tpf) {
        if (firstUpdate) {
            firstUpdate = false;
            return;
        }
        if (!startLoading) {
            startLoading();
            return;
        }
        if (!loaded) {
            updateLoading();
            return;
        }
        if (!initLoaded) {
            init();
        }

        if (!isPause) {
            time += tpf * speedList.getCurrentSpeed();
        }

        sun.update(time);
        if (changeCam) {
            CameraTool.switchFlyCam();
        }
        CameraTool.update();
        ButtonControl.update();
        DisplayInformation.update();
        Updatable.updateAll();
    }
}
