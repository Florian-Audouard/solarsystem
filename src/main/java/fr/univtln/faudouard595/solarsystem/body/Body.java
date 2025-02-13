package fr.univtln.faudouard595.solarsystem.body;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.BufferUtils;

import fr.univtln.faudouard595.solarsystem.utils.controls.camera.CameraTool;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public abstract class Body {
    protected String name;
    private float radius;
    private float realSize;
    private float scaleSize;
    private Spatial model;
    private Node node;
    private Node rotationNode;
    private Map<String, Planet> planets;
    private double rotationPeriod;
    protected static final String TEXTUREPATH = "Textures/Body/";
    protected static final String OBJPATH = "Models/Body/";
    public static AssetManager assetManager;
    public TYPE type;
    public float scaleMultiplier;
    public float objSize;
    private ColorRGBA color;
    private float colorMultiplier;
    public static Node guiNode;
    private BitmapText text;
    public static Camera cam;
    private float rotationInclination;
    public static Body reference;
    public static float referenceSize;
    public static RESOLUTION planetTexture = RESOLUTION.LOW;
    protected Material circleMat;
    protected Geometry circleGeo;
    public static InputManager inputManager;
    public static int circleDistance = 10;
    public boolean displayCircle = false;
    public boolean actualDisplayCircle = false;
    protected boolean displayLines;
    public static BitmapFont font;
    private BitmapText circleText;
    private int textSize = 10;
    protected boolean isClickable = false;
    protected boolean shouldBeDisplayed = true;

    public enum RESOLUTION {
        LOW {
            @Override
            public String toString() {
                return "Low";
            }
        },
        HIGH {
            @Override
            public String toString() {
                return "High";
            }
        }
    }

    public enum TYPE {
        OBJ, SPHERE
    }

    public Body(String name, float size, float rotationPeriod, float rotationInclination, TYPE type, ColorRGBA color) {
        this.name = name;
        this.realSize = size;
        if (reference == null) {
            reference = this;
            this.radius = referenceSize;
            font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        } else {
            this.radius = convertion(size);
        }
        this.scaleSize = this.radius;
        this.rotationPeriod = rotationPeriod * 60 * 60;
        this.rotationInclination = rotationInclination;
        this.planets = new HashMap<>();
        this.node = new Node(name);
        this.type = type;
        this.scaleMultiplier = 1;
        this.objSize = 1;
        this.color = color;
        this.rotationNode = new Node(name + "_rotation");
        this.colorMultiplier = 0.5f;
        this.displayLines = true;

    }

    public static float convertion(double value) {
        return (float) (value * reference.getRadius()) / reference.getRealSize();
    }

    public float calcObjSize() {
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

    public abstract Material generateMat();

    public void generateBody(Node rootNode) {

        if (this.type == TYPE.OBJ) {
            this.model = assetManager.loadModel(OBJPATH + name + ".j3o");
            this.objSize = calcObjSize();
        } else {
            Sphere sphere = new Sphere(32, 32, radius);
            sphere.setTextureMode(Sphere.TextureMode.Projected);
            this.model = new Geometry("Sphere_" + name, sphere);
        }
        model.setMaterial(generateMat());
        model.setShadowMode(ShadowMode.CastAndReceive);

        rotationNode.attachChild(model);
        rotationNode
                .setLocalRotation(new Quaternion().fromAngles(FastMath.DEG_TO_RAD * (rotationInclination - 90f),
                        0, 0));
        node.attachChild(rotationNode);
        rootNode.attachChild(node);
        circleGeo = createCircle();
        guiNode.attachChild(circleGeo);
    }

    public void rotation(double time) {
        double rotationSpeed = (rotationPeriod != 0) ? (FastMath.TWO_PI / rotationPeriod) : 0;
        float rotationZ = (float) ((rotationSpeed * time) % FastMath.TWO_PI);
        Quaternion rotation = new Quaternion().fromAngles(
                0,
                0,
                rotationZ);

        model.setLocalRotation(rotation);

    }

    public void scale(float scaleMultiplier) {
        objSize = calcObjSize();
        this.scaleMultiplier *= scaleMultiplier;
        scalePlanets(scaleMultiplier);
        Float newSize = (getRadius() / getObjSize()) * this.scaleMultiplier;
        model.setLocalScale(new Vector3f(newSize, newSize, newSize));

    }

    public Planet addPlanet(String name, float size, double semimajorAxis, float eccentricity, float orbitalPeriod,
            float rotationPeriod, float orbitalInclination, float rotationInclination, TYPE type, ColorRGBA lineColor) {
        Planet planet = new Planet(name, size, semimajorAxis, eccentricity, orbitalPeriod, rotationPeriod,
                orbitalInclination, rotationInclination, this,
                type, lineColor);
        planet.generateBody(node);
        planets.put(name, planet);
        return planet;
    }

    public Planet getPlanet(String name) {
        return planets.get(name);
    }

    public void scalePlanets(float scaleMultiplier) {
        this.scaleSize *= scaleMultiplier;
        planets.values().forEach(planet -> planet.scale(scaleMultiplier));
    }

    public void changeDistancePlanets(float distanceMultiplier) {
        planets.values().forEach(planet -> planet.changeDistance(planet.getDistanceMultiplier() * distanceMultiplier));
    }

    public void switchDisplayLine() {
        displayLines = !displayLines;
        if (displayLines) {
            displayLine();
        } else {
            removeLine();
        }
    }

    public void switchDisplayLines() {
        switchDisplayLine();
        planets.values().forEach(planet -> planet.switchDisplayLines());
    }

    public List<Body> getEveryBodies() {
        List<Body> bodies = new ArrayList<>();
        bodies.add(this);
        planets.values().forEach(planet -> bodies.addAll(planet.getEveryBodies()));
        return bodies;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Body body) {
            return this.name.equals(body.getName());
        }
        return false;
    }

    public void removeLine() {
        changeDisplayCircle(false);
    }

    public void displayLine() {
        changeDisplayCircle(true);
    }

    public void modifColorMult(boolean increase) {
        if (!displayLines) {
            return;
        }
        if (increase) {
            colorMultiplier = 2f;
            color.a = 2f;
        } else {
            colorMultiplier = 0.5f;
            color.a = 0.5f;

        }
        circleMat.setColor("Color", color);
        circleText.setColor(color);
    }

    public String toString() {
        return name;
    }

    public Vector3f getLocalTranslation() {
        return node.getLocalTranslation();
    }

    public Vector3f getWorldTranslation() {
        return node.getWorldTranslation();
    }

    public Vector2f getScreenCoordinates() {
        Vector3f v = cam.getScreenCoordinates(getWorldTranslation());
        return new Vector2f(v.x, v.y);
    }

    public boolean collision(Vector3f depart, Vector3f directionProjectile, float multiplierZone) {
        Vector3f directionProjectileNormalize = directionProjectile.normalize();
        Vector3f position = getWorldTranslation();
        Vector3f planetToDepart = position.subtract(depart);
        float radius = scaleSize * multiplierZone;
        float b = 2 * directionProjectileNormalize.dot(planetToDepart);
        float c = planetToDepart.dot(planetToDepart) - (radius * radius);

        return b * b - 4 * c >= 0;

    }

    private Geometry createCircle() {
        int samples = circleDistance * 10;
        Mesh mesh = new Mesh();
        Vector3f[] vertices = new Vector3f[samples];

        for (int i = 0; i < samples; i++) {
            float angle = (float) (i * 2 * Math.PI / samples);
            vertices[i] = new Vector3f(circleDistance * (float) Math.cos(angle),
                    circleDistance * (float) Math.sin(angle), 0);
        }
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setMode(Mesh.Mode.LineLoop);
        mesh.updateBound();

        Geometry geom = new Geometry("Circle_" + name, mesh);
        circleMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        circleMat.setColor("Color", color.mult(colorMultiplier));
        geom.setMaterial(circleMat);
        geom.setQueueBucket(RenderQueue.Bucket.Gui);

        this.circleText = font.createLabel(name);
        this.circleText.setColor(color.mult(colorMultiplier));
        this.circleText.setSize(textSize);
        guiNode.attachChild(this.circleText);

        return geom;
    }

    public boolean collisionCircle(Body b) {
        Vector2f screenPos2d = b.getScreenCoordinates();
        Vector2f screenPos2dThis = getScreenCoordinates();
        return screenPos2d.distance(screenPos2dThis) < circleDistance * 2.5;
    }

    public boolean isPrimaryClickable() {
        return false;
    }

    public void updateisClickable() {
        // if (CameraTool.bodies.getCurrentValue().equals(this)) {
        // isClickable = false;
        // return;
        // }
        if (isPrimaryClickable()) {
            isClickable = false;
            return;
        }
        if (!isOnScreen()) {
            isClickable = false;
            return;
        }
        isClickable = true;
    }

    public void removeCircle() {
        if (!actualDisplayCircle) {
            return;
        }
        actualDisplayCircle = false;
        circleGeo.setCullHint(Spatial.CullHint.Always);
        circleText.setText("");
    }

    public void displayCircle() {
        circleGeo.setLocalTranslation(cam.getScreenCoordinates(getWorldTranslation()));
        circleText
                .setLocalTranslation(circleGeo.getLocalTranslation().add(circleDistance, circleDistance + textSize, 0));
        if (actualDisplayCircle) {
            return;
        }
        actualDisplayCircle = true;
        circleGeo.setCullHint(Spatial.CullHint.Never);
        circleText.setText(name);
    }

    public boolean isPrimaryDisplayed() {
        return false;
    }

    public boolean isOnScreen() {
        return cam.getDirection().dot(getWorldTranslation().subtract(cam.getLocation())) > 0;
    }

    public boolean isFarFromCam() {
        return cam.getLocation().distance(getWorldTranslation()) > reference.getRadius() * 50;
    }

    public void changeDisplayCircle(boolean change) {
        displayCircle = change;
        if (!displayCircle && !actualDisplayCircle) {
            return;
        }

        if (isPrimaryDisplayed()) {
            shouldBeDisplayed = false;
            return;
        }
        if (!isOnScreen()) {
            shouldBeDisplayed = false;
            return;
        }

        if (!isFarFromCam()) {
            shouldBeDisplayed = false;
            return;
        }
        if (change) {
            shouldBeDisplayed = true;
        } else {
            shouldBeDisplayed = false;
        }
    }

    public void shouldDisplayCircle() {
        if (!displayCircle) {
            changeDisplayCircle(false);
            return;
        }
        changeDisplayCircle(true);
        if (!actualDisplayCircle) {
            return;
        }

    }

    public void updateCircle(boolean reel) {
        if (shouldBeDisplayed && reel) {
            displayCircle();
        } else {
            removeCircle();
        }
    }

    public void update(double time) {
        rotation(time);
        updateisClickable();
        shouldDisplayCircle();
        planets.values().forEach(planet -> planet.update(time));
    }
}
