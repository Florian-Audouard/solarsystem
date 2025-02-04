package fr.univtln.faudouard595.solarsystem;

import java.util.Vector;
import java.util.stream.IntStream;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.BufferUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@Slf4j
public class Planet {
    private String name;
    private float size;
    private float eccentricity;
    private float orbitalPeriod;
    private float sunDistance;
    private float rotationPeriod;
    @ToString.Exclude
    private Spatial model;
    private static final String TEXTUREPATH = "Textures/Planet/";
    private static final float realSunSize = 1_392_700;
    public static float sunSize;

    public Planet(String name, float size, float sunDistance, float eccentricity, float orbitalPeriod,
            float rotationPeriod) {
        this.name = name;
        this.size = convertion(size);

        this.sunDistance = convertion(sunDistance);
        this.eccentricity = eccentricity;
        this.orbitalPeriod = orbitalPeriod * 60 * 60 * 24;
        this.rotationPeriod = rotationPeriod * 60 * 60 * 24;
    }

    public static float convertion(float value) {
        return (value * sunSize) / realSunSize;
    }

    public void generatePlanet(AssetManager assetManager, Node rootNode) {
        Sphere sphere = new Sphere(30, 30, size / 2);
        model = new Geometry("Sphere", sphere);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        // mat.setTexture("NormalMap", assetManager.loadTexture(TEXTUREPATH + name + "/"
        // + name + "_normal.jpg"));
        mat.setTexture("DiffuseMap", assetManager.loadTexture(TEXTUREPATH + name +
                "/" + name + ".jpg"));
        mat.setFloat("Shininess", 32f);
        model.setMaterial(mat);

        rootNode.attachChild(model);

        int numPoints = (int) sunSize + 1;
        Vector3f[] points = new Vector3f[numPoints];
        IntStream.range(0, numPoints - 1).forEach(i -> {
            points[i] = calcTrajectory(i * orbitalPeriod / (numPoints - 1));
        });
        points[numPoints - 1] = points[0];

        Mesh orbitMesh = new Mesh();
        orbitMesh.setMode(Mesh.Mode.LineStrip);
        orbitMesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(points));
        orbitMesh.updateBound();
        Material lineMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterial.setColor("Color", ColorRGBA.White);
        Geometry orbitGeometry = new Geometry("OrbitLine");
        orbitGeometry.setMesh(orbitMesh);
        orbitGeometry.setMaterial(lineMaterial);
        rootNode.attachChild(orbitGeometry);
        trajectory(0);
        rotation(0);
    }

    public Vector3f calcTrajectory(float time) {
        float angle = (2 * FastMath.PI / (orbitalPeriod)) * time;
        float x = FastMath.cos(angle) * sunDistance * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * FastMath.cos(angle));
        float z = FastMath.sin(angle) * sunDistance * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * FastMath.cos(angle));
        return new Vector3f(x, 0, z);
    }

    public void trajectory(float time) {
        model.setLocalTranslation(calcTrajectory(time));

    }

    public void rotation(float time) {
        float rotationSpeed = (FastMath.TWO_PI / rotationPeriod);
        Quaternion q = new Quaternion();
        q.fromAngles(FastMath.HALF_PI, rotationSpeed * time, 0);
        model.setLocalRotation(q);
    }

    public void update(float time) {
        trajectory(time);
        rotation(time);
    }

}
