package fr.univtln.faudouard595.solarsystem.space;

import java.util.Map;
import java.util.Optional;
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
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;

import fr.univtln.faudouard595.solarsystem.ui.controls.camera.CameraTool;
import fr.univtln.faudouard595.solarsystem.utils.file.MyLoadFile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class Planet extends Body {
    private Optional<Spatial> nightModel;
    private Optional<Spatial> cloudModel;
    private Optional<Spatial> atmospherModel;
    private Node nightNode;
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
    private float longAscNode;
    private float argPeriapsis;
    private float meanAnomaly;
    private double escapeVelocity;

    public Planet(String name, double size, double semimajorAxis, float eccentricity, float orbitalPeriod,
            float rotationPeriod,
            Map.Entry<Float, Integer> mass, Map.Entry<Float, Integer> volume, double density, double gravity,
            Optional<String> discoveredBy, Optional<String> discoveryDate, Optional<String> alternativeName,
            double escapeVelocity,
            float orbitalInclination, float rotationInclination, float longAscNode,
            float argPeriapsis, float mainAnomaly, Body primary,
            ColorRGBA color) {
        super(primary.getNode(), name, size, rotationPeriod, rotationInclination,
                mass, volume, density, gravity, discoveredBy, discoveryDate, alternativeName, color);

        this.semimajorAxis = convertion(semimajorAxis);
        this.realSemimajorAxis = (int) semimajorAxis;
        this.eccentricity = eccentricity;
        this.orbitalPeriod = orbitalPeriod * 60 * 60 * 24;
        this.orbitalInclination = orbitalInclination * FastMath.DEG_TO_RAD;
        this.orbitMesh = new Mesh();
        distanceMultiplier = 1;
        this.primary = primary;
        this.longAscNode = longAscNode * FastMath.DEG_TO_RAD;
        this.argPeriapsis = argPeriapsis * FastMath.DEG_TO_RAD;
        this.meanAnomaly = mainAnomaly * FastMath.DEG_TO_RAD;
        if (orbitalInclination != 0) {
            super.getNode().setLocalRotation(new Quaternion().fromAngles(
                    (super.getRotationInclination()),
                    0, 0));
        } else {
            super.getRotationOrbitalNode()
                    .setLocalRotation(new Quaternion().fromAngles((super.getRotationInclination()),
                            0, 0));
        }
        this.nightModel = Optional.empty();
        this.cloudModel = Optional.empty();
        this.atmospherModel = Optional.empty();
        this.escapeVelocity = escapeVelocity;
    }

    public void generateLine() {
        int numPoints = (int) 3000;
        Vector3f[] points = new Vector3f[numPoints];
        IntStream.range(0, numPoints).forEach(i -> {
            points[i] = calcTrajectory(i * orbitalPeriod / (numPoints - 1));
        });

        orbitMesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(points));
        orbitMesh.updateBound();
    }

    public static void armoniseMat(Material mat) {
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Specular", new ColorRGBA(1f, 1f, 1f, 1f).mult(0.2f));
        mat.setColor("Ambient", ColorRGBA.White.mult(0.5f));
        mat.setFloat("Shininess", 6f);
    }

    public Material generateMat() {
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        armoniseMat(mat);
        String texturePath = Body.TEXTUREPATH + super.getName() + "/" + super.getName() + "_Color.jpg";
        if (!MyLoadFile.fileExists(texturePath)) {
            String randomTexture = app.ASTEROID_MODELS.get(app.random.nextInt(app.ASTEROID_MODELS.size()));
            texturePath = Body.TEXTUREPATH + randomTexture + "_Color.jpg";
        }
        mat.setTexture("DiffuseMap", app.getAssetManager().loadTexture(texturePath));
        String normalPath = Body.TEXTUREPATH + super.getName() + "/" + super.getName() + "_Normal.jpg";
        if (MyLoadFile.fileExists(normalPath)) {
            Texture normalMap = app.getAssetManager().loadTexture(normalPath);
            mat.setTexture("NormalMap", normalMap);
        }
        return mat;
    }

    public Geometry generateTransparentSphere(String texturePath, float transparency) {
        Sphere sphere = new Sphere(nightSphereSample, nightSphereSample, sphereRadius);
        sphere.setTextureMode(Sphere.TextureMode.Projected);
        Geometry transparent = new Geometry("Sphere_" + name, sphere);
        transparent.setLocalScale(getScaleRadius() / sphereRadius);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", app.getAssetManager().loadTexture(texturePath));
        mat.setColor("Color", new ColorRGBA(1, 1, 1, transparency));
        transparent.setQueueBucket(RenderQueue.Bucket.Transparent);
        mat.setTransparent(true);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        transparent.setMaterial(mat);
        return transparent;

    }

    public Node attachCorrectNode(Spatial spatial) {
        Node panetInclinaison = new Node("PlanetInclinaison");
        panetInclinaison
                .setLocalRotation(new Quaternion().fromAngles((super.getRotationInclination()),
                        0, 0));
        panetInclinaison.attachChild(spatial);
        Node finalNode = new Node("FinalNode");
        finalNode.setLocalRotation(new Quaternion().fromAngles(FastMath.DEG_TO_RAD * (-90f),
                0, 0));
        finalNode.attachChild(panetInclinaison);
        return finalNode;
    }

    public void generateNightModel() {
        if (!enableNightTexture) {
            return;
        }
        String texturePath = Body.TEXTUREPATH + super.getName() + "/" + super.getName() + "_Night.png";
        if (!MyLoadFile.fileExists(texturePath)) {
            return;
        }

        Spatial nighSpatial = generateTransparentSphere(texturePath, 1.5f);
        nightModel = Optional.of(nighSpatial);
        nightNode = attachCorrectNode(nighSpatial);
        super.getNode().attachChild(nightNode);
    }

    public void generateAtmosphere() {
        if (!enableAtmosphere) {
            return;
        }
        String texturePath = Body.TEXTUREPATH + super.getName() + "/" + super.getName() + "_Atmosphere.jpg";
        if (!MyLoadFile.fileExists(texturePath)) {
            return;
        }
        Spatial atmosphere = generateTransparentSphere(texturePath, 0.05f);
        atmosphere.scale(1.05f);
        atmospherModel = Optional.of(atmosphere);
        Node atmosphereNode = attachCorrectNode(atmosphere);
        super.getNode().attachChild(atmosphereNode);
    }

    public void generateCloud() {
        if (!enableCloud) {
            return;
        }
        String texturePath = Body.TEXTUREPATH + super.getName() + "/" + super.getName() + "_Cloud.png";
        if (!MyLoadFile.fileExists(texturePath)) {
            return;
        }
        Spatial cloud = generateTransparentSphere(texturePath, 0.5f);
        cloudModel = Optional.of(cloud);
        cloud.scale(1.03f);
        Node cloudNode = attachCorrectNode(cloud);
        super.getNode().attachChild(cloudNode);
    }

    private Geometry createPlanetRings(String texturePath) {
        int segments = 64;
        float innerRadius = super.getScaleRadius() * 1.236145f;
        float outerRadius = super.getScaleRadius() * 2.27318f;

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
        Texture ringTexture = Body.app.getAssetManager()
                .loadTexture(texturePath);

        ringMat.setTexture("ColorMap", ringTexture);

        ringMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        ringMat.getAdditionalRenderState().setDepthWrite(true);
        ringMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        ringGeo.setMaterial(ringMat);
        ringGeo.setQueueBucket(RenderQueue.Bucket.Transparent);

        return ringGeo;
    }

    public void generateRing() {
        String texturePath = Body.TEXTUREPATH + super.getName() + "/Ring_Color.jpg";
        if (!MyLoadFile.fileExists(texturePath)) {
            return;
        }
        Geometry ringGeo = createPlanetRings(texturePath);
        super.getNode().attachChild(ringGeo);
    }

    @Override
    public void generateBody() {

        if (!primary.equals(reference)) {
            semimajorAxis *= Body.scalePlanet;
        }
        super.generateBody();
        generateNightModel();
        generateAtmosphere();
        generateCloud();
        generateLine();
        lineMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        orbitGeometry = new Geometry("OrbitLine");
        orbitMesh.setMode(Mesh.Mode.LineLoop);
        orbitGeometry.setMesh(orbitMesh);
        orbitGeometry.setMaterial(lineMaterial);
        getParentNode().attachChild(orbitGeometry);
        trajectory(0);
        super.rotation(0);
        displayLine();
        circleGeo.setCullHint(Spatial.CullHint.Always);
        circleText.setText("");
        generateRing();

    }

    @Override
    public void rotation(double time) {
        super.rotation(time);
        Quaternion rotation = new Quaternion().fromAngles(
                0,
                0,
                calcRoatation(time));
        nightModel.ifPresent(spatial -> spatial.setLocalRotation(rotation));
        cloudModel.ifPresent(spatial -> spatial.setLocalRotation(rotation));
        atmospherModel.ifPresent(spatial -> spatial.setLocalRotation(rotation));
    }

    public float getAngle(double time) {
        return (float) (((FastMath.TWO_PI / (orbitalPeriod)) * time) % FastMath.TWO_PI);
    }

    public Vector3f calcTrajectory(double time, float add) {
        float workingDistance = add + ((semimajorAxis) * distanceMultiplier);
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

    public void changeDistance(float distanceMultiplier) {
        super.changeDistancePlanets(distanceMultiplier);
        this.distanceMultiplier = distanceMultiplier;
        generateLine();
    }

    public void trajectory(double time) {
        Vector3f position = calcTrajectory(time);
        currentAngle = getAngle(time);
        super.getNode().setLocalTranslation(position);
        if (!nightModel.isPresent()) {
            return;
        }
        Vector3f positionNight = position.normalize().mult(0.05f);
        nightNode.setLocalTranslation(positionNight);
    }

    public void updateOrbit() {
        if (saveScaleMultiplier == scaleMultiplier &&
                primary.saveScaleMultiplier == primary.scaleMultiplier) {
            return;
        }

        saveScaleMultiplier = scaleMultiplier;
        generateLine();
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
        if (primary.equals(reference)) {
            return primary.getWorldTranslation().distance(super.getWorldTranslation());
        }
        return primary.getWorldTranslation().distance(super.getWorldTranslation()) / (getScaleRadius() / getRadius());
    }

    public long getActualDistanceFromPrimary() {
        return (long) inverseConvertion(getDistanceFromPrimary());
    }

    
    @Override
    public String displayInformation() {
        StringBuilder res = new StringBuilder();
        res.append(super.displayInformation());
        res.append("Distance from %s : %s km\n".formatted(primary.getName(),
                formatter.format(getActualDistanceFromPrimary())));
        res.append("Orbital Period : %.2f days\n".formatted((orbitalPeriod / (60 * 60 * 24))));
        res.append("Orbital Inclination : %.2f°\n".formatted((orbitalInclination * FastMath.RAD_TO_DEG)));
        res.append("Rotation Inclination : %.2f°\n".formatted((getRotationInclination() * FastMath.RAD_TO_DEG)));
        return res.toString();
    }

    @Override
    public void scaleWhenSelected() {
        if (!Body.dynamicScale) {
            return;
        }
        if (!reference.equals(primary)) {
            return;
        }
        if (app.changeCam || app.getFlyByCamera().isEnabled() || !CameraTool.bodies.getCurrentValue().equals(this)
                && !getPlanets().values().stream()
                        .anyMatch(p -> p.equals(CameraTool.bodies.getCurrentValue()))) {
            scale(1f);
            return;
        }
        float myCloseScale = closeScale;
        if (this.getName().equals("Earth")) {
            myCloseScale = closeScaleEarth;
        }
        float scale = (myCloseScale * CameraTool.minDistance
                / CameraTool.distanceFromBody) / CameraTool.bodies.getCurrentValue().getRadius();
        scale = Math.max(1f, scale);
        scale(scale);

    }

    @Override
    public void scaleWhenSelected(float scale) {
        if (!Body.dynamicScale) {
            return;
        }
        if (!reference.equals(primary)) {
            return;
        }
        if (app.getFlyByCamera().isEnabled() || !CameraTool.bodies.getCurrentValue().equals(this)
                && !getPlanets().values().stream()
                        .anyMatch(p -> p.equals(CameraTool.bodies.getCurrentValue()))) {
            scale(1f);
            return;
        }
        scale = Math.max(1f, scale);
        scale(scale);

    }

    @Override
    public void displayWhenSelected() {
        super.displayWhenSelected();

        // log.info("Earth : {} , NightEarth : {}", getModel().getWorldTranslation(),
        // nightModel.get().getWorldTranslation());
    }

}
