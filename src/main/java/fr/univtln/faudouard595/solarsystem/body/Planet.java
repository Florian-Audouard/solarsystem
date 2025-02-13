package fr.univtln.faudouard595.solarsystem.body;

import java.util.List;
import java.util.stream.IntStream;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;

import com.jme3.util.BufferUtils;

import fr.univtln.faudouard595.solarsystem.utils.controls.camera.CameraTool;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class Planet extends Body {
    private float eccentricity;
    private double orbitalPeriod;
    private float semimajorAxis;
    private Body primary;
    private float distanceMultiplier;
    public static float realSunSize;
    private Mesh orbitMesh;
    private float currentAngle;
    private Material lineMaterial;
    private Geometry orbitGeometry;
    private float orbitalInclination;

    public Planet(String name, float size, double semimajorAxis, float eccentricity, float orbitalPeriod,
            float rotationPeriod, float orbitalInclination, float rotationInclination, Body primary, TYPE type,
            ColorRGBA color) {
        super(name, size, rotationPeriod, rotationInclination, type, color);

        this.semimajorAxis = convertion(semimajorAxis);
        this.eccentricity = eccentricity;
        this.orbitalPeriod = orbitalPeriod * 60 * 60 * 24;
        this.orbitalInclination = orbitalInclination;
        this.orbitMesh = new Mesh();
        distanceMultiplier = 1;
        this.primary = primary;
    }

    public void generateLine() {
        int numPoints = (int) Body.reference.getScaleSize() * 5;
        Vector3f[] points = new Vector3f[numPoints];
        IntStream.range(0, numPoints).forEach(i -> {
            points[i] = calcTrajectory(i * orbitalPeriod / (numPoints - 1));
        });

        orbitMesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(points));
        orbitMesh.updateBound();

    }

    public Material generateMat() {
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Specular", new ColorRGBA(1f, 1f, 1f, 1f).mult(0.2f));
        mat.setColor("Ambient", ColorRGBA.Gray);
        mat.setFloat("Shininess", 12f);
        mat.setTexture("DiffuseMap", app.getAssetManager().loadTexture(Body.TEXTUREPATH + Body.planetTexture + "/" +
                super.getName() + ".jpg"));

        return mat;
    }

    public void generateBody(Node node) {
        super.generateBody(node);
        generateLine();
        lineMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        orbitGeometry = new Geometry("OrbitLine");
        orbitMesh.setMode(Mesh.Mode.LineLoop);
        orbitGeometry.setMesh(orbitMesh);
        orbitGeometry.setMaterial(lineMaterial);
        node.attachChild(orbitGeometry);
        trajectory(0);
        super.rotation(0);
        displayLine();
        circleGeo.setCullHint(Spatial.CullHint.Always);
        circleText.setText("");
    }

    public float getAngle(double time) {
        return (float) ((FastMath.TWO_PI / (orbitalPeriod)) * time) % FastMath.TWO_PI;
    }

    public Vector3f calcTrajectory(double time, float add) {
        float workingDistance = super.getScaleSize() + primary.getScaleSize()
                + add + ((semimajorAxis) * distanceMultiplier);
        float angle = getAngle(time);
        float x = FastMath.cos(angle) * workingDistance * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * FastMath.cos(angle));
        float z = FastMath.sin(angle) * workingDistance * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * FastMath.cos(angle));
        float y = 0;
        float inclinaisonRad = FastMath.DEG_TO_RAD * orbitalInclination;
        y = FastMath.sin(inclinaisonRad) * z;
        z = FastMath.cos(inclinaisonRad) * z;
        return new Vector3f(x, y, z);
    }

    public Vector3f calcTrajectory(double time) {
        return calcTrajectory(time, 0);
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
        currentAngle = getAngle(time);
        super.getNode().setLocalTranslation(position);

    }

    public void update(double time) {
        super.update(time);
        trajectory(time);
    }

    @Override
    public void removeLine() {
        super.removeLine();
        orbitGeometry.setCullHint(Spatial.CullHint.Always);

    }

    @Override
    public void displayLine() {
        super.displayLine();
        orbitGeometry.setCullHint(Spatial.CullHint.Never);
    }

    @Override
    public void modifColorMult(boolean keyPressed) {
        if (!displayLines) {
            return;
        }
        super.modifColorMult(keyPressed);
        lineMaterial.setColor("Color", super.getColor().mult(super.getColorMultiplier()));
    }

    @Override
    public boolean isPrimaryDisplayed() {
        return primary.isActualDisplayCircle() && !primary.equals(reference);
    }

    @Override
    public boolean isPrimaryClickable() {
        return primary.isClickable && !CameraTool.bodies.getCurrentValue().equals(primary)
                && !primary.equals(reference);
    }

    @Override
    public boolean isFarFromCam() {
        return cam.getLocation().distance(getWorldTranslation()) > primary.getRadius() * 5;
    }

}
