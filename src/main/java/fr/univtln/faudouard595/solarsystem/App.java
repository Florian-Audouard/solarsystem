package fr.univtln.faudouard595.solarsystem;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jme3.app.SimpleApplication;
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

import fr.univtln.faudouard595.solarsystem.body.Body;
import fr.univtln.faudouard595.solarsystem.body.Star;
import fr.univtln.faudouard595.solarsystem.body.Body.TYPE;
import fr.univtln.faudouard595.solarsystem.body.Planet;
import fr.univtln.faudouard595.solarsystem.ui.controls.button.ButtonControl;
import fr.univtln.faudouard595.solarsystem.ui.controls.camera.CameraTool;
import fr.univtln.faudouard595.solarsystem.ui.controls.trigger.TriggerControls;
import fr.univtln.faudouard595.solarsystem.ui.information.DisplayInformation;
import fr.univtln.faudouard595.solarsystem.utils.api.ApiBodyInfo;
import fr.univtln.faudouard595.solarsystem.utils.api.DataCreationNeeded;
import fr.univtln.faudouard595.solarsystem.utils.collection.SpeedList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import com.jme3.light.AmbientLight;
import com.jme3.material.Material;

@Slf4j
@Getter
public class App extends SimpleApplication {
    double AU = 149_597_870.7;

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

    public float calcObjSize(Spatial model) {
        BoundingVolume worldBound = model.getWorldBound();
        if (worldBound instanceof BoundingBox) {
            BoundingBox box = (BoundingBox) worldBound;
            Vector3f min = box.getMin(null);
            Vector3f max = box.getMax(null);
            Vector3f size = max.subtract(min);
            return (size.x + size.y + size.z) / 3;
        }
        return 1;
    }

    private Geometry createIrregularAsteroid(Vector3f position, Material material, Random random,
            float sizeOfAsteroid) {
        float min = sizeOfAsteroid / 2;
        float max = sizeOfAsteroid;
        float randomSize = min + (max - min) * (float) Math.random();
        Sphere sphere = new Sphere(12, 12, randomSize);
        Geometry geom = new Geometry("Asteroid", sphere);
        geom.setLocalScale(1f, 1.5f, 1f);
        geom.setMaterial(material);
        geom.setLocalTranslation(position);
        Quaternion randomRotation = new Quaternion();
        randomRotation.fromAngles(random.nextFloat() * FastMath.TWO_PI, random.nextFloat() * FastMath.TWO_PI,
                random.nextFloat() * FastMath.TWO_PI);
        geom.setLocalRotation(randomRotation);
        return geom;
    }

    private void generateAsteroidBelt(String name, float innerRadius, float outerRadius, float heightVariation,
            float sizeOfAsteroid,
            int numObjects) {
        Node beltNode = new Node("AsteroidBelt_" + name);
        Random random = new Random();
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Diffuse", new ColorRGBA(new Vector3f(192 / 255, 184 / 255, 171 / 255)).mult(0.5f));
        material.setColor("Specular", new ColorRGBA(1f, 1f, 1f, 1f).mult(0.2f));
        material.setColor("Ambient", ColorRGBA.Gray);
        material.setFloat("Shininess", 12f);

        for (int i = 0; i < numObjects; i++) {
            float radius = innerRadius + random.nextFloat() * (outerRadius - innerRadius);
            float angle = random.nextFloat() * FastMath.TWO_PI;
            float x = radius * FastMath.cos(angle);
            float y = (random.nextFloat() - 0.5f) * heightVariation; // Some vertical displacement
            float z = radius * FastMath.sin(angle);

            Vector3f position = new Vector3f(x, y, z);
            Geometry asteroid = createIrregularAsteroid(position, material, random, sizeOfAsteroid);
            beltNode.attachChild(asteroid);
        }
        rootNode.attachChild(beltNode);
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
        double INNER_RADIUS_KUIPER = 30 * AU;
        double OUTER_RADIUS_KUIPER = 50 * AU;
        double THICKNESS_KUIPER = 10 * AU;
        double SIZE_OF_ASTEROID_KUIPER = 15_000_000;
        int NUM_OBJECTS_KUIPER = 7_000;
        generateAsteroidBelt("Kuiper", Body.convertion(INNER_RADIUS_KUIPER), Body.convertion(OUTER_RADIUS_KUIPER),
                Body.convertion(THICKNESS_KUIPER), Body.convertion(SIZE_OF_ASTEROID_KUIPER), NUM_OBJECTS_KUIPER);

        double INNER_RADIUS_MAIN = 2.2 * AU;
        double OUTER_RADIUS_MAIN = 4.2 * AU;
        double THICKNESS_ASTEROID_MAIN = 1 * AU;
        double SIZE_OF_ASTEROID_MAIN = 3_000_000;
        int NUM_OBJECTS_ASTEROID_MAIN = 3_000;
        generateAsteroidBelt("Main", Body.convertion(INNER_RADIUS_MAIN),
                Body.convertion(OUTER_RADIUS_MAIN),
                Body.convertion(THICKNESS_ASTEROID_MAIN), Body.convertion(SIZE_OF_ASTEROID_MAIN),
                NUM_OBJECTS_ASTEROID_MAIN);
    }

    private void initSettings() {
        CameraTool.app = this;
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
