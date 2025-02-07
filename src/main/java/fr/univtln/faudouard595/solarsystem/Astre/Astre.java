package fr.univtln.faudouard595.solarsystem.Astre;

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
public abstract class Astre {
    private String name;
    private float size;
    private float scaleSize;
    private Spatial model;
    private Node node;
    private Map<String, Planet> planets;
    private float rotationPeriod;
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

    public enum TYPE {
        OBJ, SPHERE
    }

    public Astre(String name, float size, float rotationPeriod, TYPE type, ColorRGBA color) {
        this.name = name;
        this.size = size;
        this.scaleSize = size;
        this.rotationPeriod = rotationPeriod * 60 * 60 * 24;
        this.planets = new HashMap<>();
        this.node = new Node(name);
        this.type = type;
        this.scaleMultiplier = 1;
        this.objSize = 1;
        this.color = color;
        this.colorMultiplier = 0.5f;
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

    public void generateAstre(Node rootNode, boolean isSun) {

        if (this.type == TYPE.OBJ) {
            this.model = assetManager.loadModel(OBJPATH + name + ".j3o");
            this.objSize = calcObjSize();

        } else {
            Sphere sphere = new Sphere(32, 32, size / 2);
            sphere.setTextureMode(Sphere.TextureMode.Projected);
            this.model = new Geometry("Sphere_" + name, sphere);
        }
        model.setMaterial(generateMat());
        model.setShadowMode(ShadowMode.CastAndReceive);

        node.attachChild(model);
        rootNode.attachChild(node);
    }

    public void rotation(double time) {
        float rotationSpeed = (FastMath.TWO_PI / rotationPeriod);
        Quaternion q = new Quaternion();
        float rotationX = 0;
        if (type == TYPE.SPHERE) {
            rotationX = -FastMath.HALF_PI;
        }
        q.fromAngles(rotationX, (float) ((rotationSpeed * time) % 360), 0f);
        model.setLocalRotation(q);
    }

    public void scale(float scaleMultiplier) {
        objSize = calcObjSize();
        this.scaleMultiplier *= scaleMultiplier;
        scalePlanets(scaleMultiplier);
        // model.setLocalScale();
        Float newSize = (getSize() / getObjSize()) * this.scaleMultiplier;
        model.setLocalScale(new Vector3f(newSize, newSize, newSize));

        log.info("{}: size: {} ", name, calcObjSize());

    }

    public void addPlanet(String name, float size, float primaryBodyDistance, float eccentricity, float orbitalPeriod,
            float rotationPeriod, TYPE type, ColorRGBA lineColor) {
        Planet planet = new Planet(name, size, primaryBodyDistance, eccentricity, orbitalPeriod, rotationPeriod, this,
                type, lineColor);
        planet.generatePlanet(node);
        planets.put(name, planet);
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

    public List<Astre> getEveryAstres() {
        List<Astre> astres = new ArrayList<>();
        astres.add(this);
        planets.values().forEach(planet -> astres.addAll(planet.getEveryAstres()));
        return astres;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Astre astre) {
            return this.name.equals(astre.getName());
        }
        return false;
    }

    // public int hashCode(Object o) {
    // if (o instanceof Astre astre) {
    // return this.name.hashCode(astre.getName());
    // }
    // return "0".hashCode("1");
    // }

    public void modifColorMult(boolean increase) {
        if (increase) {
            // log.info("modifColorMult , name : {}", name);

            colorMultiplier = 2f;
        } else {
            colorMultiplier = 0.5f;

        }
    }

    public String toString() {
        return name;
    }

}
