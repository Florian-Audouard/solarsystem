package fr.univtln.faudouard595.solarsystem.Astre;

import java.util.HashMap;
import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
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
    @ToString.Exclude
    private Spatial model;
    private Node node;
    private Map<String, Planet> planets;
    private float rotationPeriod;
    private static final String TEXTUREPATH = "Textures/Planet/";
    public static AssetManager assetManager;

    public Astre(String name, float size, float rotationPeriod) {
        this.name = name;
        this.size = size;
        this.rotationPeriod = rotationPeriod * 60 * 60 * 24;
        this.planets = new HashMap<>();
        this.node = new Node(name);
    }

    public void generateAstre(Node rootNode, boolean isSun) {
        Sphere sphere = new Sphere(30, 30, size / 2);
        this.model = new Geometry("Sphere_" + name, sphere);
        Material mat;
        String typeMap;
        if (isSun) {
            mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            typeMap = "ColorMap";
        } else {
            mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            typeMap = "DiffuseMap";
        }
        log.info(TEXTUREPATH + name + ".jpg");
        mat.setTexture(typeMap, assetManager.loadTexture(TEXTUREPATH + name + ".jpg"));
        model.setMaterial(mat);
        node.attachChild(model);
        rootNode.attachChild(node);
    }

    public void rotation(float time) {
        float rotationSpeed = (FastMath.TWO_PI / rotationPeriod);
        Quaternion q = new Quaternion();
        q.fromAngles(-FastMath.HALF_PI, rotationSpeed * time, 0);
        model.setLocalRotation(q);
    }

    public void scale(float scaleMultiplier) {
        scalePlanets(scaleMultiplier);
        model.scale(scaleMultiplier);
    }

    public void addPlanet(String name, float size, float primaryBodyDistance, float eccentricity, float orbitalPeriod,
            float rotationPeriod) {
        Planet planet = new Planet(name, size, primaryBodyDistance, eccentricity, orbitalPeriod, rotationPeriod);
        planet.generatePlanet(node);
        planets.put(name, planet);
    }

    public Planet getPlanet(String name) {
        return planets.get(name);
    }

    public void scalePlanets(float scaleMultiplier) {
        planets.values().forEach(planet -> planet.scale(scaleMultiplier));

    }

    public void changeDistancePlanets(float distanceMultiplier) {
        planets.values().forEach(planet -> planet.changeDistance(planet.getDistanceMultiplier() * distanceMultiplier));
    }

    public void update(float time) {
        rotation(time);
        planets.values().forEach(planet -> planet.update(time));
    }

}
