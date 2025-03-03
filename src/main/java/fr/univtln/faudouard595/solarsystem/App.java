package fr.univtln.faudouard595.solarsystem;

import java.time.Instant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;

import fr.univtln.faudouard595.solarsystem.space.AsteroidBelt;
import fr.univtln.faudouard595.solarsystem.space.Body;
import fr.univtln.faudouard595.solarsystem.space.Star;
import fr.univtln.faudouard595.solarsystem.space.Body.TYPE;
import fr.univtln.faudouard595.solarsystem.ui.controls.button.ButtonControl;
import fr.univtln.faudouard595.solarsystem.ui.controls.button.MyButton;
import fr.univtln.faudouard595.solarsystem.ui.controls.camera.CameraTool;
import fr.univtln.faudouard595.solarsystem.ui.controls.trigger.TriggerControls;
import fr.univtln.faudouard595.solarsystem.ui.information.DisplayInformation;
import fr.univtln.faudouard595.solarsystem.ui.loadingscreen.LoadingAppState;
import fr.univtln.faudouard595.solarsystem.utils.Console.ProgressBar;
import fr.univtln.faudouard595.solarsystem.utils.api.ApiBodyInfo;
import fr.univtln.faudouard595.solarsystem.utils.api.DataCreationNeeded;
import fr.univtln.faudouard595.solarsystem.utils.collection.SpeedList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyListener;
import java.io.File;

import com.jme3.light.AmbientLight;
import com.jme3.material.Material;

@Slf4j
@Getter
public class App extends SimpleApplication {
    private boolean loaded = false;
    private boolean startLoading = false;
    private boolean initLoaded = false;
    public Node loadingNode;
    public Node myGuiNode;
    public int tmp = 0;

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
    private Iterator<Integer> assetAsteroid = AsteroidBelt.getIterator();

    public static void main(String[] args) {
        boolean test = true;

        App app = new App();
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(60);
        if (test) {
            settings.setResolution(1920, 780);
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


    public void generateAsteroidBelt(){
        int totalAsteroid = 6_000;

        double INNER_RADIUS_KUIPER = 30 * AU;
        double OUTER_RADIUS_KUIPER = 50 * AU;
        double THICKNESS_KUIPER = 10 * AU;
        double SIZE_OF_ASTEROID_KUIPER = 20_000_000;
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
        Body.guiNode = myGuiNode;
        Body.cam = cam;
        Body.referenceSize = sunSize;
        createBodies();
        if(true){
            return;
        }
        
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.05f));
        rootNode.addLight(al);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Scene);
        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp);
    }

    private void initSettings() {
        CameraTool.app = this;
        

        cam.setFrustumFar(sunSize * maxRenderDistanceMult);
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        // GuiGlobals.getInstance().
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
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

        sun = (Star) apiAstreInfo.getBodies(bodies, TYPE.SPHERE);

        assetBodies = sun.getEveryBodies().iterator();

    }

    public void init(){
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
        myGuiNode = new Node("MyGuiNode");
        loadingNode = new Node("LoadingNode");
        loadingNode.setLocalTranslation(0, 0, 1);
        guiNode.attachChild(loadingNode);
        setDisplayStatView(false);
        setDisplayFps(false);
        font = assetManager.loadFont("Fonts/Segoe.fnt");
    }

    private void startLoading(){
        LoadingAppState.createInstance(font);
        LoadingAppState.init(40);
        LoadingAppState.initialize(this);
        createSpace();
        startLoading = true;

    }

    private void updateLoading(){
        if(assetBodies.hasNext()){
            Body currentAsset = assetBodies.next();
            currentAsset.generateBody();
            LoadingAppState.updateProgress();
            return;
        }
        if(assetAsteroid.hasNext()){
            int i = assetAsteroid.next();
            AsteroidBelt.initModel(assetManager, i+1);
            LoadingAppState.updateProgress();
            return;
        }
        loaded = true;

    }

    @Override
    public void simpleUpdate(float tpf) {
        if(!startLoading){
            startLoading();
        }

        updateLoading();
        if(!loaded){
            return;
        }
        if(!initLoaded){
            init();
        }
        if (!isPause) {
            time += tpf * speedList.getCurrentSpeed();
        }
        sun.update(time);
        CameraTool.update();
        ButtonControl.update();
        DisplayInformation.update();
    }
}
