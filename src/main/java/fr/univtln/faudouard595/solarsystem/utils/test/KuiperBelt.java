package fr.univtln.faudouard595.solarsystem.utils.test;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.FloatBuffer;

import java.util.Random;

public class KuiperBelt extends SimpleApplication {
    private static final String[] ASTEROID_MODELS = { "Ceres", "Haumea", "Eris" };

    public static void main(String[] args) {
        KuiperBelt app = new KuiperBelt();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        int NUM_OBJECTS = 10000;
        float INNER_RADIUS = 500f;
        float OUTER_RADIUS = 1000f;
        float HEIGHT_VARIATION = 30f;
        Node belt = generateAsteroidBelt(INNER_RADIUS, OUTER_RADIUS, HEIGHT_VARIATION, NUM_OBJECTS);
        rootNode.attachChild(belt);
        flyCam.setMoveSpeed(100);
        PointLight pl = new PointLight();
        pl.setPosition(new Vector3f(0, 0, 0));
        pl.setColor(ColorRGBA.White);
        rootNode.addLight(pl);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.1f));
        rootNode.addLight(al);

    }

    private Node generateAsteroidBelt(float innerRadius, float outerRadius, float heightVariation, int numObjects) {
        Node beltNode = new Node("KuiperBelt");
        Random random = new Random();
        List<Material> materials = new ArrayList<>();
        for (String model : ASTEROID_MODELS) {
            Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            material.setBoolean("UseMaterialColors", true);
            material.setColor("Diffuse", ColorRGBA.White);
            material.setColor("Specular", new ColorRGBA(1f, 1f, 1f, 1f).mult(0.2f));
            material.setColor("Ambient", ColorRGBA.Gray);
            material.setFloat("Shininess", 12f);
            material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Body/Low/" + model + ".jpg"));
            materials.add(material);
        }

        for (int i = 0; i < numObjects; i++) {
            float radius = innerRadius + random.nextFloat() * (outerRadius - innerRadius);
            float angle = random.nextFloat() * FastMath.TWO_PI;
            float x = radius * FastMath.cos(angle);
            float y = (random.nextFloat() - 0.5f) * heightVariation; // Some vertical displacement
            float z = radius * FastMath.sin(angle);

            Vector3f position = new Vector3f(x, y, z);
            Material material = materials.get(random.nextInt(materials.size()));
            Geometry asteroid = createIrregularAsteroid(position, material, random);
            beltNode.attachChild(asteroid);
        }
        return beltNode;
    }

    private Geometry createIrregularAsteroid(Vector3f position, Material material, Random random) {

        Sphere sphere = new Sphere(12, 12, 1f);
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
}
