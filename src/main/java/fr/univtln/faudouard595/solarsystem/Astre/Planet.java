package fr.univtln.faudouard595.solarsystem.Astre;

import java.util.stream.IntStream;

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
    private float primaryBodyDistance;
    private Astre primary;
    private float distanceMultiplier;
    public static float realSunSize;
    public static Star sun;
    private Mesh orbitMesh;
    private boolean displayLines;

    public Planet(String name, float size, float primaireDistance, float eccentricity, float orbitalPeriod,
            float rotationPeriod, Astre primary, TYPE type) {
        super(name, convertion(size), rotationPeriod, type);

        this.primaryBodyDistance = convertion(primaireDistance);
        this.eccentricity = eccentricity;
        this.orbitalPeriod = orbitalPeriod * 60 * 60 * 24;
        orbitMesh = new Mesh();
        distanceMultiplier = 1;
        displayLines = true;
        this.primary = primary;
    }

    public static float convertion(float value) {
        return (value * sun.getSize()) / realSunSize;
    }

    public void generateLine() {
        if (!displayLines) {
            return;
        }
        int numPoints = (int) (sun.getSize() * 10) + 1;
        Vector3f[] points = new Vector3f[numPoints];
        IntStream.range(0, numPoints - 1).forEach(i -> {
            points[i] = calcTrajectory(i * orbitalPeriod / (numPoints - 1));
        });
        points[numPoints - 1] = points[0];

        orbitMesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(points));
        orbitMesh.updateBound();
    }

    public void generatePlanet(Node node) {
        generateAstre(node, false);
        generateLine();
        Material lineMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterial.setColor("Color", ColorRGBA.White);
        Geometry orbitGeometry = new Geometry("OrbitLine");
        orbitMesh.setMode(Mesh.Mode.LineStrip);
        orbitGeometry.setMesh(orbitMesh);
        orbitGeometry.setMaterial(lineMaterial);
        node.attachChild(orbitGeometry);
        trajectory(0);
        super.rotation(0);
    }

    public Vector3f calcTrajectory(double time) {
        float workingDistance = super.getScaleSize() / 2 + primary.getScaleSize() / 2
                + ((primaryBodyDistance) * distanceMultiplier);
        double angle = (2 * FastMath.PI / (orbitalPeriod)) * time;
        double x = Math.cos(angle) * workingDistance * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * Math.cos(angle));
        double z = Math.sin(angle) * workingDistance * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * Math.cos(angle));
        return new Vector3f((float) x, 0f, (float) z);
    }

    @Override
    public void scale(float scaleMultiplier) {
        super.scale(scaleMultiplier);
        generateLine();
    }

    public void changeDistance(float distanceMultiplier) {
        super.changeDistancePlanets(distanceMultiplier);
        this.distanceMultiplier = distanceMultiplier;
        generateLine();
    }

    public void trajectory(double time) {
        Vector3f position = calcTrajectory(time);
        super.getNode().setLocalTranslation(position);

    }

    public void update(double time) {
        super.update(time);
        trajectory(time);
    }

    public void removeLine() {
        orbitMesh.clearBuffer(VertexBuffer.Type.Position);
    }

    public void switchDisplayLine() {
        displayLines = !displayLines;
        if (displayLines) {
            generateLine();
        } else {
            removeLine();
        }
        super.switchDisplayLines();

    }

}
