package fr.univtln.faudouard595.solarsystem;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;

import fr.univtln.faudouard595.solarsystem.body.Body;
import fr.univtln.faudouard595.solarsystem.body.Star;
import fr.univtln.faudouard595.solarsystem.body.Body.TYPE;
import fr.univtln.faudouard595.solarsystem.utils.controls.button.ButtonControl;
import fr.univtln.faudouard595.solarsystem.utils.controls.camera.CameraTool;
import fr.univtln.faudouard595.solarsystem.utils.controls.trigger.TriggerControls;
import fr.univtln.faudouard595.solarsystem.utils.information.DisplayInformation;
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

    private static final float sunSize = 500;
    public double time = 0f;
    public boolean isPause = false;
    private Star sun;
    private BitmapText timeText;
    public BitmapFont font;
    public float startOfUniver = 9624787761l; // timeStamp before 1970 (01/01/1665)
    private float maxRenderDistanceMult = 10000000f;
    public SpeedList speedList = new SpeedList();

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

    public void createSpace() {

        Texture starTexture = assetManager.loadTexture("Textures/Sky/StarSky.jpg");
        Spatial sky = SkyFactory.createSky(assetManager, starTexture, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);
        Body.app = this;
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
        setDisplayStatView(false);
        setDisplayFps(false);
        cam.setFrustumFar(sunSize * maxRenderDistanceMult);
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
    }

    public void createBodies() {
        ApiBodyInfo apiAstreInfo = new ApiBodyInfo();
        List<DataCreationNeeded> bodies = new ArrayList<>();
        bodies.add(new DataCreationNeeded("soleil", ColorRGBA.Yellow, 0));
        bodies.add(new DataCreationNeeded("terre", new ColorRGBA(0f / 255, 153f / 255, 204f / 255, 1f), 1));
        bodies.add(new DataCreationNeeded("mercure", new ColorRGBA(151f / 255, 104f / 255, 172f / 255, 1f), 0));
        bodies.add(new DataCreationNeeded("venus", new ColorRGBA(176f / 255, 121f / 255, 25f / 255, 1f), 0));
        bodies.add(new DataCreationNeeded("mars", new ColorRGBA(154f / 255, 78f / 255, 25f / 255, 1f), 2));
        bodies.add(new DataCreationNeeded("jupiter", new ColorRGBA(218f / 255, 139f / 255, 114f / 255, 1f), 4));
        bodies.add(new DataCreationNeeded("saturne", new ColorRGBA(213f / 255, 193f / 255, 135f / 255, 1f), 8, true));
        bodies.add(new DataCreationNeeded("uranus", new ColorRGBA(104f / 255, 204f / 255, 218f / 255, 1f), 5));
        bodies.add(new DataCreationNeeded("neptune", new ColorRGBA(112f / 255, 140f / 255, 227f / 255, 1f), 1));

        sun = (Star) apiAstreInfo.getBodies(bodies, TYPE.SPHERE);
        sun.generateBody(rootNode, viewPort);
    }

    @Override
    public void simpleInitApp() {
        font = assetManager.loadFont("Fonts/Segoe.fnt");
        // font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        initSettings();
        TriggerControls.init(this);
        ButtonControl.init(this);
        createSpace();
        time = startOfUniver + ((double) Instant.now().getEpochSecond());

        CameraTool.init(cam, assetManager, inputManager);
        CameraTool.setBody(sun);
        DisplayInformation.app = this;
        DisplayInformation.init();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!isPause) {
            time += tpf * speedList.getCurrentSpeed();
        }
        sun.update(time);
        CameraTool.update();
        ButtonControl.update();
        DisplayInformation.update();
    }
}
