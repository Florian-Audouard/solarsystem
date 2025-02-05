package fr.univtln.faudouard595.solarsystem.Astre;

import java.util.HashMap;
import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@Slf4j
public abstract class Astre {
    private String name;
    private float size;
    private float scaleSize;
    @ToString.Exclude
    private Spatial model;
    private Node node;
    private Map<String, Planet> planets;
    private float rotationPeriod;
    private static final String TEXTUREPATH = "Textures/Planet/";
    public static AssetManager assetManager;
    public TYPE type;
    public float scaleMultiplier;
    public float objSize;

    public enum TYPE {
        OBJ, SPHERE
    }

    public Astre(String name, float size, float rotationPeriod, TYPE type) {
        this.name = name;
        this.size = size;
        this.scaleSize = size;
        this.rotationPeriod = rotationPeriod * 60 * 60 * 24;
        this.planets = new HashMap<>();
        this.node = new Node(name);
        this.type = type;
        this.scaleMultiplier = 1;
        this.objSize = 1;
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

    public void generateAstre(Node rootNode, boolean isSun) {

        if (this.type == TYPE.OBJ) {
            this.model = assetManager.loadModel(TEXTUREPATH + name + ".j3o");
            this.objSize = calcObjSize();

        } else {
            Sphere sphere = new Sphere(30, 30, size / 2);
            this.model = new Geometry("Sphere_" + name, sphere);
        }
        Material mat;
        String typeMap;
        if (isSun) {
            mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            typeMap = "ColorMap";
        } else {
            mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", ColorRGBA.White);
            mat.setColor("Specular", ColorRGBA.White);
            mat.setFloat("Shininess", 12f);
            typeMap = "DiffuseMap";
        }

        mat.setTexture(typeMap, assetManager.loadTexture(TEXTUREPATH + name + ".jpg"));

        model.setMaterial(mat);
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
        this.scaleMultiplier *= scaleMultiplier;
        scalePlanets(scaleMultiplier);
        model.setLocalScale((getSize() / getObjSize()) * this.scaleMultiplier);
        log.info("{}: size: {} , {}", name, Float.toString((getSize() / getObjSize()) * this.scaleMultiplier),
                calcObjSize());
    }

    public void addPlanet(String name, float size, float primaryBodyDistance, float eccentricity, float orbitalPeriod,
            float rotationPeriod, TYPE type) {
        Planet planet = new Planet(name, size, primaryBodyDistance, eccentricity, orbitalPeriod, rotationPeriod, this,
                type);
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
        rotation(time);
        planets.values().forEach(planet -> planet.update(time));
    }

    public void switchDisplayLines() {
        planets.values().forEach(planet -> planet.switchDisplayLine());
    }

}
