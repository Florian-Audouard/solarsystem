package fr.univtln.faudouard595.solarsystem.space;

import static fr.univtln.faudouard595.solarsystem.space.Body.formatter;

import java.io.File;
import java.util.List;
import java.util.stream.IntStream;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;

import fr.univtln.faudouard595.solarsystem.ui.controls.camera.CameraTool;
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
    private int realSemimajorAxis;
    private Body primary;
    private float distanceMultiplier;
    public static float realSunSize;
    private Mesh orbitMesh;
    private float currentAngle;
    private Material lineMaterial;
    private Geometry orbitGeometry;
    private float orbitalInclination;
    private boolean ring;
    private float longAscNode;
    private float argPeriapsis;
    private float meanAnomaly;

    public Planet(String name, double size, double semimajorAxis, float eccentricity, float orbitalPeriod,
            float rotationPeriod, float orbitalInclination, float rotationInclination, float longAscNode,
            float argPeriapsis, float mainAnomaly, Body primary, TYPE type,
            ColorRGBA color, boolean ring) {
        super(name, size, rotationPeriod, rotationInclination, type, color);

        this.semimajorAxis = convertion(semimajorAxis);
        this.realSemimajorAxis = (int) semimajorAxis;
        this.eccentricity = eccentricity;
        this.orbitalPeriod = orbitalPeriod * 60 * 60 * 24;
        this.orbitalInclination = orbitalInclination * FastMath.DEG_TO_RAD;
        this.orbitMesh = new Mesh();
        distanceMultiplier = 1;
        this.primary = primary;
        this.ring = ring;
        this.longAscNode = longAscNode * FastMath.DEG_TO_RAD;
        this.argPeriapsis = argPeriapsis * FastMath.DEG_TO_RAD;
        this.meanAnomaly = mainAnomaly * FastMath.DEG_TO_RAD;
        if (orbitalInclination != 0) {
            super.getNode().setLocalRotation(new Quaternion().fromAngles(
                    (super.getRotationInclination()),
                    0, 0));
        } else {
            super.getRotationNode()
                    .setLocalRotation(new Quaternion().fromAngles((super.getRotationInclination()),
                            0, 0));
        }
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
        String texturePath = Body.TEXTUREPATH + Body.planetTexture + "/" + super.getName() + ".jpg";
        File f = new File("src/main/resources/" + texturePath);
        if (!f.exists()) {
            String randomTexture = app.ASTEROID_MODELS.get(app.random.nextInt(app.ASTEROID_MODELS.size()));
            texturePath = Body.TEXTUREPATH + "Low/" + randomTexture + ".jpg";
        }
        mat.setTexture("DiffuseMap", app.getAssetManager().loadTexture(texturePath));

        return mat;

    }

    private Geometry createPlanetRings() {
        int segments = 64;
        float innerRadius = super.getRadius() * 1.236145f;
        float outerRadius = super.getRadius() * 2.27318f;

        Mesh ringMesh = new Mesh();
        Vector3f[] vertices = new Vector3f[segments * 2];
        Vector2f[] texCoords = new Vector2f[segments * 2];
        int[] indices = new int[segments * 6];

        for (int i = 0; i < segments; i++) {
            float angle = (float) (i * Math.PI * 2 / segments);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            // Positions des sommets
            vertices[i * 2] = new Vector3f(innerRadius * cos, 0, innerRadius * sin);
            vertices[i * 2 + 1] = new Vector3f(outerRadius * cos, 0, outerRadius * sin);

            // Mapping UV correct :
            texCoords[i * 2] = new Vector2f(0, i / (float) segments); // Intérieur (gauche de l’image)
            texCoords[i * 2 + 1] = new Vector2f(1, i / (float) segments); // Extérieur (droite de l’image)

            int next = (i + 1) % segments;

            // Triangles pour le disque
            indices[i * 6] = i * 2;
            indices[i * 6 + 1] = next * 2;
            indices[i * 6 + 2] = i * 2 + 1;

            indices[i * 6 + 3] = i * 2 + 1;
            indices[i * 6 + 4] = next * 2;
            indices[i * 6 + 5] = next * 2 + 1;
        }

        ringMesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        ringMesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        ringMesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
        ringMesh.updateBound();

        Geometry ringGeo = new Geometry(name + "_Ring", ringMesh);
        Material ringMat = new Material(Body.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        Texture ringTexture = Body.app.getAssetManager().loadTexture(TEXTUREPATH + Body.planetTexture + "/Ring.png");
        ringTexture.setWrap(Texture.WrapMode.EdgeClamp); // Empêche la répétition

        ringMat.setTexture("ColorMap", ringTexture);
        ringMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        ringMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        ringGeo.setMaterial(ringMat);
        ringGeo.setQueueBucket(RenderQueue.Bucket.Transparent);

        return ringGeo;
    }

    public void generateRing() {
        if (!ring) {
            return;
        }
        Geometry ringGeo = createPlanetRings();
        super.getNode().attachChild(ringGeo);
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
        generateRing();
    }

    public float getAngle(double time) {
        return (float) (((FastMath.TWO_PI / (orbitalPeriod)) * time) % FastMath.TWO_PI);
    }

    public Vector3f calcTrajectory(double time, float add) {
        float workingDistance = super.getScaleSize() + primary.getScaleSize()
                + add + ((semimajorAxis) * distanceMultiplier);
        float angle = -getAngle(time);
        float x = FastMath.cos(angle) * workingDistance * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * FastMath.cos(angle));
        float z = FastMath.sin(angle) * workingDistance * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * FastMath.cos(angle));
        float y = 0;
        y = FastMath.sin(orbitalInclination) * z;
        z = FastMath.cos(orbitalInclination) * z;
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
        if (CameraTool.bodies.getCurrentValue() instanceof Planet focusPlanet) {
            return !focusPlanet.getPrimary().equals(primary) && !focusPlanet.equals(primary)
                    && !primary.equals(reference);
        }
        return (!CameraTool.bodies.getCurrentValue().equals(primary))
                && !primary.equals(reference);
    }

    public float getDistanceFromPrimary() {
        return primary.getWorldTranslation().distance(super.getWorldTranslation());
    }

    public long getActualDistanceFromPrimary() {
        return (long) inverseConvertion(getDistanceFromPrimary());
    }

    @Override
    public String displayInformation() {
        String stringRealSemimajorAxis = formatter.format(getActualDistanceFromPrimary());
        String res = String.format("""
                %s
                Distance from %s : %s km
                """, super.displayInformation(), primary.getName(), stringRealSemimajorAxis);
        return res;
    }

}
