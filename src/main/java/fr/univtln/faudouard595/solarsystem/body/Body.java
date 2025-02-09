package fr.univtln.faudouard595.solarsystem.body;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

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
    protected static final String TEXTUREPATH = "Textures/Planet/";
    protected static final String OBJPATH = "Models/Planet/";
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

    public enum TYPE {
        OBJ, SPHERE
    }

    public Body(String name, float size, float rotationPeriod, float rotationInclination, TYPE type, ColorRGBA color) {
        this.name = name;
        this.realSize = size;
        if (reference == null) {
            reference = this;
            this.radius = referenceSize;
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
        // model.setLocalScale();
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

    public void update(double time) {
        // Vector3f position = new Vector3f(0, scaleSize, 0);
        // text.setLocalTranslation(node.getWorldTranslation().add(position));
        // text.setLocalTranslation(new Vector3f(50, 200, 0));
        // text.lookAt(cam.getLocation(), cam.getUp());
        rotation(time);
        planets.values().forEach(planet -> planet.update(time));

    }

    public void switchDisplayLines() {
        planets.values().forEach(planet -> planet.switchDisplayLine());
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

    public void modifColorMult(boolean increase) {
        if (increase) {

            colorMultiplier = 2f;
        } else {
            colorMultiplier = 0.5f;

        }
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

    public boolean collision(Vector3f depart, Vector3f directionProjectile, float multiplierZone) {
        Vector3f directionProjectileNormalize = directionProjectile.normalize();
        Vector3f position = getWorldTranslation();
        Vector3f planetToDepart = position.subtract(depart);
        float radius = scaleSize * multiplierZone;
        float b = 2 * directionProjectileNormalize.dot(planetToDepart);
        float c = planetToDepart.dot(planetToDepart) - (radius * radius);

        return b * b - 4 * c >= 0;

    }
}
