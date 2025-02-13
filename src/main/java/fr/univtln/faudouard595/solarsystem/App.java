package fr.univtln.faudouard595.solarsystem;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

import fr.univtln.faudouard595.solarsystem.body.Body;
import fr.univtln.faudouard595.solarsystem.body.Star;
import fr.univtln.faudouard595.solarsystem.body.Body.TYPE;
import fr.univtln.faudouard595.solarsystem.utils.controls.button.ButtonControl;
import fr.univtln.faudouard595.solarsystem.utils.controls.camera.CameraTool;
import fr.univtln.faudouard595.solarsystem.utils.controls.trigger.TriggerControls;
import fr.univtln.faudouard595.solarsystem.utils.api.ApiAstreInfo;
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
    private double time = 0f;
    public boolean isPause = false;
    public boolean actualPauseTimeText = false;
    private Star sun;
    private BitmapText timeText;
    public BitmapFont font;
    private float startOfUniver = 9624787761l; // timeStamp before 1970 (01/01/1665)
    private float maxRenderDistanceMult = 10000000f;
    public SpeedList speedList = new SpeedList();

    public static void main(String[] args) {
        boolean test = true;

        App app = new App();
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(60);
        if (!test) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            DisplayMode dm = gd.getDisplayMode();
            int screenWidth = dm.getWidth();
            int screenHeight = dm.getHeight();
            settings.setResolution(screenWidth, screenHeight);
            settings.setFullscreen(true);
        } else {
            settings.setResolution(1920, 780);
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

    }

    public void createBodies() {
        ApiAstreInfo apiAstreInfo = new ApiAstreInfo();
        List<DataCreationNeeded> bodies = new ArrayList<>();
        bodies.add(new DataCreationNeeded("soleil", ColorRGBA.Yellow));
        bodies.add(new DataCreationNeeded("terre", new ColorRGBA(0f / 255, 153f / 255, 204f / 255, 1f)));
        bodies.add(new DataCreationNeeded("mercure", new ColorRGBA(151f / 255, 104f / 255, 172f / 255, 1f)));
        bodies.add(new DataCreationNeeded("venus", new ColorRGBA(176f / 255, 121f / 255, 25f / 255, 1f)));
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
        font = assetManager.loadFont("Fonts/Segoe.fnt");
        // font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        initSettings();
        TriggerControls.init(this);
        ButtonControl.init(this);
        createSpace();
        time = startOfUniver + ((double) Instant.now().getEpochSecond());

        timeText = font.createLabel("");
        timeText.setSize(40);
        timeText.setColor(ColorRGBA.White);
        timeText.setLocalTranslation(100, settings.getHeight() - 50, 0);

        guiNode.attachChild(timeText);

        CameraTool.init(cam, assetManager, inputManager);
        CameraTool.setBody(sun);

    }

    public void updateTextDate() {

        if (actualPauseTimeText && isPause) {
            return;
        }
        if (actualPauseTimeText != isPause) {
            if (isPause) {
                timeText.setColor(ColorRGBA.Orange);
            } else {
                timeText.setColor(ColorRGBA.White);

            }
        }
        Instant instant = Instant.ofEpochSecond((long) (time - startOfUniver));
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDate = dateTime.format(formatter);
        timeText.setText(formattedDate);
        actualPauseTimeText = isPause;
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!isPause) {
            time += (tpf) * speedList.getCurrentSpeed();
        }
        sun.update(time);
        CameraTool.update(time, speed);
        updateTextDate();
        ButtonControl.update();
    }
}
