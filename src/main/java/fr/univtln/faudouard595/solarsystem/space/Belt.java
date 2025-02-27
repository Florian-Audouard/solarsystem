package fr.univtln.faudouard595.solarsystem.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class Belt {
    private Node beltNode;
    private double rotationPeriod;
    private static int numberOfAsteroidsModel = 10;
    private static List<Spatial> asteroids = new ArrayList<>();
    public Random random = new Random();

    public Belt(double rotationPeriod) {
        this.beltNode = new Node("AsteroidBelt");
        ;
        this.rotationPeriod = rotationPeriod;
    }

    public static float calcObjSize(Spatial model) {
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

    public static void initModel(AssetManager assetManager) {
        for (int i = 1; i < numberOfAsteroidsModel + 1; i++) {
            String name = "Asteroid";
            String path = "Models/Asteroid" + i + "/" + name;
            String extention = ".j3o";
            Spatial model = assetManager.loadModel(path + extention);

            asteroids.add(model);
            model.setUserData("MeanSize", calcObjSize(model));
        }
    }

    private float randomFloatBetween(float min, float max) {
        return min + (max - min) * (float) Math.random();
    }

    public Belt generateAsteroidBelt(float innerRadius, float outerRadius, float heightVariation,
            float sizeOfAsteroid,
            int numObjects) {

        for (int i = 0; i < numObjects; i++) {
            float radius = innerRadius + random.nextFloat() * (outerRadius - innerRadius);
            float angle = random.nextFloat() * FastMath.TWO_PI;
            float x = radius * FastMath.cos(angle);
            float y = (random.nextFloat() - 0.5f) * heightVariation; // Some vertical displacement
            float z = radius * FastMath.sin(angle);

            Vector3f position = new Vector3f(x, y, z);

            Spatial asteroid = createIrregularAsteroid(position, random, sizeOfAsteroid);
            beltNode.attachChild(asteroid);
        }
        return this;
    }

    private Spatial createIrregularAsteroid(Vector3f position, Random random,
            float sizeOfAsteroid) {
        Spatial model = asteroids.get(random.nextInt(asteroids.size())).clone(true);
        Quaternion randomRotation = new Quaternion();
        randomRotation.fromAngles(random.nextFloat() * FastMath.TWO_PI, random.nextFloat() * FastMath.TWO_PI,
                random.nextFloat() * FastMath.TWO_PI);
        model.setLocalRotation(randomRotation);
        float meanSize = model.getUserData("MeanSize");
        float scale = (float) sizeOfAsteroid / meanSize;
        float randomScale = randomFloatBetween(scale / 4, scale);
        model.scale(randomScale);
        model.setLocalTranslation(position);
        return model;
    }

    public void rotation(double time) {
        if (rotationPeriod == 0) {
            return;
        }
        double rotationSpeed = FastMath.TWO_PI / rotationPeriod;
        float rotationAngle = (float) ((rotationSpeed * time) % FastMath.TWO_PI);
        Quaternion rotation = new Quaternion().fromAngles(
                0,
                rotationAngle,
                0);

        beltNode.setLocalRotation(rotation);

    }

    public void update(double time) {
        rotation(time);
    }
}
