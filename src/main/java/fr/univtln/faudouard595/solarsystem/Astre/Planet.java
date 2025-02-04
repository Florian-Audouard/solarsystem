package fr.univtln.faudouard595.solarsystem.Astre;

import java.util.stream.IntStream;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;

import com.jme3.util.BufferUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@Slf4j
public class Planet extends Astre {
    private float eccentricity;
    private float orbitalPeriod;
    private float sunDistance;
    public static float realSunSize;
    public static Star sun;
    public Mesh orbitMesh;

    public Planet(String name, float size, float sunDistance, float eccentricity, float orbitalPeriod,
            float rotationPeriod) {
        super(name, convertion(size), rotationPeriod);

        this.sunDistance = convertion(sunDistance);
        this.eccentricity = eccentricity;
        this.orbitalPeriod = orbitalPeriod * 60 * 60 * 24;
        orbitMesh = new Mesh();

    }

    public static float convertion(float value) {
        return (value * sun.getSize()) / realSunSize;
    }

    public void generateLine() {
        int numPoints = (int) sun.getSize() + 1;
        Vector3f[] points = new Vector3f[numPoints];
        IntStream.range(0, numPoints - 1).forEach(i -> {
            points[i] = calcTrajectory(i * orbitalPeriod / (numPoints - 1));
        });
        points[numPoints - 1] = points[0];
        orbitMesh.setMode(Mesh.Mode.LineStrip);
        orbitMesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(points));
        orbitMesh.updateBound();
    }

    public void generatePlanet(AssetManager assetManager, Node rootNode) {
        generateAstre(assetManager, rootNode, false);

        generateLine();
        Material lineMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterial.setColor("Color", ColorRGBA.White);
        Geometry orbitGeometry = new Geometry("OrbitLine");
        orbitGeometry.setMesh(orbitMesh);
        orbitGeometry.setMaterial(lineMaterial);
        rootNode.attachChild(orbitGeometry);
        trajectory(0);
        super.rotation(0);
    }

    public Vector3f calcTrajectory(float time) {
        float angle = (2 * FastMath.PI / (orbitalPeriod)) * time;
        float x = FastMath.cos(angle) * sunDistance * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * FastMath.cos(angle));
        float z = FastMath.sin(angle) * sunDistance * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * FastMath.cos(angle));
        return new Vector3f(x, 0, z);
    }

    public void drawNewOrbit() {

    }

    public void trajectory(float time) {
        super.getModel().setLocalTranslation(calcTrajectory(time));

    }

    public void update(float time) {
        super.update(time);
        trajectory(time);
    }

}
