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
import java.nio.FloatBuffer;
import java.util.Random;

public class KuiperBelt extends SimpleApplication {
    private static final int NUM_OBJECTS = 10000;
    private static final float INNER_RADIUS = 500f;
    private static final float OUTER_RADIUS = 1000f;
    private static final float HEIGHT_VARIATION = 30f;

    public static void main(String[] args) {
        KuiperBelt app = new KuiperBelt();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Node belt = generateAsteroidBelt(INNER_RADIUS, OUTER_RADIUS, HEIGHT_VARIATION, NUM_OBJECTS);
        rootNode.attachChild(belt);
        flyCam.setMoveSpeed(100);
        PointLight pl = new PointLight();
        pl.setPosition(new Vector3f(0, 0, 0));
        pl.setColor(com.jme3.math.ColorRGBA.White);
        rootNode.addLight(pl);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.1f));
        rootNode.addLight(al);

    }

    private Node generateAsteroidBelt(float innerRadius, float outerRadius, float heightVariation, int numObjects) {
        Node beltNode = new Node("KuiperBelt");
        Random random = new Random();
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Diffuse", new ColorRGBA(new Vector3f(192 / 255, 184 / 255, 171 / 255)).mult(0.5f));
        material.setColor("Specular", new ColorRGBA(1f, 1f, 1f, 1f).mult(0.2f));
        material.setColor("Ambient", ColorRGBA.Gray);
        material.setFloat("Shininess", 12f);

        for (int i = 0; i < NUM_OBJECTS; i++) {
            float radius = INNER_RADIUS + random.nextFloat() * (OUTER_RADIUS - INNER_RADIUS);
            float angle = random.nextFloat() * FastMath.TWO_PI;
            float x = radius * FastMath.cos(angle);
            float y = (random.nextFloat() - 0.5f) * HEIGHT_VARIATION; // Some vertical displacement
            float z = radius * FastMath.sin(angle);

            Vector3f position = new Vector3f(x, y, z);
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
