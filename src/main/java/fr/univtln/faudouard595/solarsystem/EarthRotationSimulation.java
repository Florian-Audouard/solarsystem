package fr.univtln.faudouard595.solarsystem;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.PointLight;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

import de.lessvoid.nifty.tools.LinearInterpolator.Point;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;

public class EarthRotationSimulation extends SimpleApplication {

    float time = 0;
    // Période de rotation en secondes (24 heures = 86 400 secondes)
    private static final float rotationPeriod = 86400f;

    // Inclinaison de l'axe de rotation (en degrés)
    private static final float rotationInclination = 23.4393f;

    // L'objet Terre
    private Geometry earth;

    public static void main(String[] args) {
        EarthRotationSimulation app = new EarthRotationSimulation();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Créer une sphère pour représenter la Terre
        Sphere sphere = new Sphere(30, 30, 1f); // 30 segments et rayon 1
        earth = new Geometry("Earth", sphere);

        // Appliquer un matériau (couleur bleu, comme l'eau de la Terre)
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Planet/Earth.jpg"));
        earth.setMaterial(mat);
        Node node = new Node();
        // Ajouter la Terre à la scène
        node.attachChild(earth);
        rootNode.attachChild(node);
        float angleRotation = (2 * FastMath.PI * (time / rotationPeriod));

        float inclination = FastMath.DEG_TO_RAD * (rotationInclination - 90);
        Quaternion q = new Quaternion().fromAngles(inclination, 0, 0f);
        node.setLocalRotation(q);

        PointLight sun = new PointLight();
        sun.setColor(new com.jme3.math.ColorRGBA(1f, 1f, 1f, 1.0f));
        sun.setPosition(new Vector3f(-100, 0, 0));
        rootNode.addLight(sun);
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.1f));
        rootNode.addLight(al);
    }

    @Override
    public void simpleUpdate(float tpf) {
        float speed = 20000f;
        time += tpf * speed;
        float angleRotation = (2 * FastMath.PI * (time / rotationPeriod));

        float inclination = FastMath.DEG_TO_RAD * (23.5f); // Inclinaison de 23.5°
        Quaternion finalRotation = new Quaternion().fromAngles(0, 0, angleRotation); // Rotation sur Y

        earth.setLocalRotation(finalRotation);

    }
}
