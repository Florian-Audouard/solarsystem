package fr.univtln.faudouard595.solarsystem.space;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.BufferUtils;
import com.jme3.util.TangentBinormalGenerator;

import fr.univtln.faudouard595.solarsystem.App;
import fr.univtln.faudouard595.solarsystem.utils.file.MyLoadFile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public abstract class Body {
    protected String name;
    private float radius;
    private double realSize;
    private float scaleRadius;
    private Map.Entry<Float, Integer> mass;
    private Map.Entry<Float, Integer> volume;
    private double density;
    private double gravity;
    private Optional<String> discoveredBy;
    private Optional<String> discoveryDate;
    private Optional<String> alternativeName;

    private Spatial model;
    private Node node;
    private Node rotationOrbitalNode;
    private Map<String, Planet> planets;
    private double rotationPeriod;
    protected static final String TEXTUREPATH = "Textures/Body/";
    protected static final String OBJPATH = "Models/Body/";
    public float scaleMultiplier;
    protected float saveScaleMultiplier;
    private ColorRGBA color;
    private float colorMultiplier;
    public static Node guiNode;
    private BitmapText text;
    public static Camera cam;
    private float rotationInclination;
    public static Body reference;
    public static float referenceSize;
    protected Material circleMat;
    protected Geometry circleGeo;
    public static App app;
    public static int circleDistance = 10;
    public boolean displayCircle = false;
    public boolean actualDisplayCircle = false;
    protected boolean displayLines;
    protected BitmapText circleText;
    private int textSize = 20;
    protected boolean isClickable = false;
    protected boolean shouldBeDisplayed = true;
    protected static final NumberFormat formatter = NumberFormat.getInstance(Locale.FRENCH);
    private Node planetNode;
    private List<AsteroidBelt> belts = new ArrayList<>();
    protected static float scalePlanet = 1;
    private Node parentNode;

    public static boolean dynamicScale = true;
    public static float closeScale = 10f;
    public static float closeScaleEarth = 50f;
    public static int sphereSample = 32;
    public static int nightSphereSample = 256;
    public static int sphereRadius = 50;
    public static boolean enableNightTexture = true;
    public static boolean enableAtmosphere = true;
    public static boolean enableCloud = true;

    public Body(Node parentNode, String name, double size, float rotationPeriod, float rotationInclination,
            Map.Entry<Float, Integer> mass, Map.Entry<Float, Integer> volume, double density, double gravity,
            Optional<String> discoveredBy, Optional<String> discoveryDate, Optional<String> alternativeName,
            ColorRGBA color) {
        this.parentNode = parentNode;
        this.name = name;
        this.realSize = size;
        if (reference == null) {
            reference = this;
            this.radius = referenceSize;
        } else {
            this.radius = convertion(size);
        }
        this.scaleRadius = this.radius;
        this.rotationPeriod = rotationPeriod * 60 * 60;
        this.rotationInclination = rotationInclination * FastMath.DEG_TO_RAD;
        this.planets = new HashMap<>();
        this.node = new Node(name);
        this.scaleMultiplier = 1;
        this.saveScaleMultiplier = 1;
        this.color = color;
        this.rotationOrbitalNode = new Node(name + "_rotationOrbital");
        this.planetNode = new Node(name + "_planet");
        this.colorMultiplier = 0.5f;
        this.displayLines = true;
        this.discoveredBy = discoveredBy;
        this.discoveryDate = discoveryDate;
        this.alternativeName = alternativeName;
        this.mass = mass;
        this.volume = volume;
        this.density = density;
        this.gravity = gravity;
    }

    public static float convertion(double value) {
        return (float) ((value * reference.getRadius()) / reference.getRealSize());
    }

    public static long inverseConvertion(float value) {
        return (long) (((double) value * reference.getRealSize()) / reference.getRadius());
    }

    public float calcObjSize() {
        if (model == null) {
            return 1;
        }
        BoundingVolume worldBound = model.getWorldBound();
        if (worldBound instanceof BoundingBox) {
            BoundingBox box = (BoundingBox) worldBound;
            Vector3f min = box.getMin(null);
            Vector3f max = box.getMax(null);
            Vector3f size = max.subtract(min);
            return (size.x + size.y + size.z) / 3;
        }
        if (model instanceof Geometry g) {
            Sphere sphere = (Sphere) g.getMesh();
            return sphere.getRadius();
        }
        return 1;
    }

    public abstract Material generateMat();

    public void generateBody() {
        log.info("Generate body : {} , radius : {}", name, scaleRadius);
        String objPath = "Models/Body/" + name + "/" + name + ".j3o";
        if (MyLoadFile.fileExists(objPath)) {
            this.model = app.getAssetManager().loadModel(objPath);
            float objSize = calcObjSize();
            model.setLocalScale(scaleRadius / objSize);
        } else {
            int customSphereSample = sphereSample;
            if (enableNightTexture && name.equals("Earth")) {
                customSphereSample = nightSphereSample;
            }
            Sphere sphere = new Sphere(customSphereSample, customSphereSample, sphereRadius);
            sphere.setTextureMode(Sphere.TextureMode.Projected);
            this.model = new Geometry("Sphere_" + name, sphere);
            model.setLocalScale(scaleRadius / sphereRadius);
            Material mat = generateMat();
            if (mat.getParam("NormalMap") != null) {
                TangentBinormalGenerator.generate(model);
            }
            model.setMaterial(mat);
        }
        model.setCullHint(Spatial.CullHint.Never);
        model.setShadowMode(ShadowMode.CastAndReceive);

        planetNode.attachChild(model);
        planetNode
                .setLocalRotation(new Quaternion().fromAngles(FastMath.DEG_TO_RAD * (-90f),
                        0, 0));
        rotationOrbitalNode.attachChild(planetNode);

        node.attachChild(rotationOrbitalNode);
        parentNode.attachChild(node);
        circleGeo = createCircle();
        guiNode.attachChild(circleGeo);
    }

    public float calcRoatation(double time) {
        double rotationSpeed = FastMath.TWO_PI / rotationPeriod;
        return (float) ((rotationSpeed * time) % FastMath.TWO_PI);
    }

    public void rotation(double time) {
        if (rotationPeriod == 0) {
            return;
        }
        Quaternion rotation = new Quaternion().fromAngles(
                0,
                0,
                calcRoatation(time));

        model.setLocalRotation(rotation);

    }

    public void scale(float scaleMultiplier) {
        if (scaleMultiplier == this.scaleMultiplier) {
            return;
        }
        this.scaleMultiplier = scaleMultiplier;
        node.setLocalScale(scaleMultiplier);
        scaleRadius = radius * node.getWorldScale().x;
        planets.values().forEach(planet -> planet.setScaleRadius(planet.getRadius() * node.getWorldScale().x));

    }

    public Planet addPlanet(String name, float size, double semimajorAxis, float eccentricity, float orbitalPeriod,
            float rotationPeriod,
            Map.Entry<Float, Integer> mass, Map.Entry<Float, Integer> volume, double density, double gravity,
            Optional<String> discoveredBy, Optional<String> discoveryDate, Optional<String> alternativeName,
            double escapeVelocity,
            float orbitalInclination, float rotationInclination, float longAscNode,
            float argPeriapsis, float mainAnomaly, ColorRGBA lineColor) {
        Planet planet = new Planet(name, size, semimajorAxis, eccentricity, orbitalPeriod, rotationPeriod,
                mass, volume, density, gravity,
                discoveredBy, discoveryDate, alternativeName, escapeVelocity,
                orbitalInclination, rotationInclination, longAscNode,
                argPeriapsis, mainAnomaly, this,
                lineColor);
        planets.put(name, planet);
        return planet;
    }

    public Planet getPlanet(String name) {
        return planets.get(name);
    }

    public void scalePlanets(float scaleMultiplier) {
        planets.values().forEach(planet -> planet.scale(scaleMultiplier));
    }

    public void changeDistancePlanets(float distanceMultiplier) {
        planets.values().forEach(planet -> planet.changeDistance(planet.getDistanceMultiplier() * distanceMultiplier));
    }

    public void switchDisplayLine() {
        displayLines = !displayLines;
        if (displayLines) {
            displayLine();
        } else {
            removeLine();
        }
    }

    public void switchDisplayLines() {
        switchDisplayLine();
        planets.values().forEach(planet -> planet.switchDisplayLines());
    }

    public List<Body> getEveryBodies() {
        List<Body> bodies = new ArrayList<>();
        bodies.add(this);

        planets.values().stream().sorted((p1, p2) -> p1.getRealSemimajorAxis() - p2.getRealSemimajorAxis())
                .forEach(planet -> bodies.addAll(planet.getEveryBodies()));
        return bodies;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Body body) {
            return this.name.equals(body.getName());
        }
        return false;
    }

    public void removeLine() {
        changeDisplayCircle(false);
    }

    public void displayLine() {
        changeDisplayCircle(true);
    }

    public void modifColorMult(boolean increase) {
        if (!displayLines) {
            return;
        }
        if (increase) {
            colorMultiplier = 1f;
        } else {
            colorMultiplier = 0.5f;
        }
        circleMat.setColor("Color", color.mult(colorMultiplier));
        circleText.setColor(color.mult(colorMultiplier));
    }

    public String toString() {
        return name;
    }

    public Vector3f getLocalTranslation() {
        return node.getLocalTranslation();
    }

    public Vector3f getWorldTranslation() {
        return node.getWorldTranslation();
    }

    public Vector2f getScreenCoordinates() {
        Vector3f v = cam.getScreenCoordinates(getWorldTranslation());
        return new Vector2f(v.x, v.y);
    }

    public boolean collision(Vector3f depart, Vector3f directionProjectile, float multiplierZone) {
        Vector3f directionProjectileNormalize = directionProjectile.normalize();
        Vector3f position = getWorldTranslation();
        Vector3f planetToDepart = position.subtract(depart);
        float radius = scaleRadius * multiplierZone;
        float b = 2 * directionProjectileNormalize.dot(planetToDepart);
        float c = planetToDepart.dot(planetToDepart) - (radius * radius);

        return b * b - 4 * c >= 0;

    }

    private Geometry createCircle() {
        int samples = circleDistance * 10;
        Mesh mesh = new Mesh();
        Vector3f[] vertices = new Vector3f[samples];

        for (int i = 0; i < samples; i++) {
            float angle = (float) (i * 2 * Math.PI / samples);
            vertices[i] = new Vector3f(circleDistance * (float) Math.cos(angle),
                    circleDistance * (float) Math.sin(angle), 0);
        }
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setMode(Mesh.Mode.LineLoop);
        mesh.updateBound();

        Geometry geom = new Geometry("Circle_" + name, mesh);
        circleMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        circleMat.setColor("Color", color.mult(colorMultiplier));
        geom.setMaterial(circleMat);
        geom.setQueueBucket(RenderQueue.Bucket.Gui);

        this.circleText = app.font.createLabel(name);
        this.circleText.setColor(color.mult(colorMultiplier));
        this.circleText.setSize(textSize);
        guiNode.attachChild(this.circleText);

        return geom;
    }

    public boolean collisionCircle(Body b) {
        Vector2f screenPos2d = b.getScreenCoordinates();
        Vector2f screenPos2dThis = getScreenCoordinates();
        return screenPos2d.distance(screenPos2dThis) < circleDistance * 2.5;
    }

    public boolean isPrimaryClickable() {
        return false;
    }

    public boolean isTooSmall() {
        return scaleRadius < 1;
    }

    public void updateisClickable() {
        boolean primaryClickable = isPrimaryClickable();
        if (primaryClickable) {
            isClickable = false;
            return;
        }
        if (!isOnScreen()) {
            isClickable = false;
            return;
        }
        // if (isTooSmall()) {
        // isClickable = false;
        // return;
        // }
        if (!primaryClickable && !isFarFromCam()) {
            isClickable = false;
            return;
        }
        isClickable = true;
    }

    public void removeCircle() {
        if (!actualDisplayCircle) {
            return;
        }
        actualDisplayCircle = false;
        circleGeo.setCullHint(Spatial.CullHint.Always);
        circleText.setText("");
    }

    public void displayCircle() {
        circleGeo.setLocalTranslation(cam.getScreenCoordinates(getWorldTranslation()));
        circleText
                .setLocalTranslation(circleGeo.getLocalTranslation().add(circleDistance, circleDistance + textSize, 0));
        if (actualDisplayCircle) {
            return;
        }
        actualDisplayCircle = true;
        circleGeo.setCullHint(Spatial.CullHint.Never);
        circleText.setText(name);
    }

    public boolean isPrimaryDisplayed() {
        return false;
    }

    public boolean isOnScreen() {
        return cam.getDirection().dot(getWorldTranslation().subtract(cam.getLocation())) > 0;
    }

    public float get2dRadius() {

        Vector2f screenPos2d = getScreenCoordinates();
        Vector3f radiusPos = cam.getScreenCoordinates(getWorldTranslation().add(cam.getLeft().mult(-scaleRadius)));
        Vector2f screenPos2dRadius = new Vector2f(radiusPos.x, radiusPos.y);
        return screenPos2d.distance(screenPos2dRadius);
    }

    public boolean isFarFromCam() {
        return get2dRadius() * 4 < circleDistance;
    }

    public void changeDisplayCircle(boolean change) {
        displayCircle = change;
        if (!displayCircle && !actualDisplayCircle) {
            return;
        }

        if (isPrimaryDisplayed()) {
            shouldBeDisplayed = false;
            return;
        }
        if (!isOnScreen()) {
            shouldBeDisplayed = false;
            return;
        }

        if (!isFarFromCam()) {
            shouldBeDisplayed = false;
            return;
        }
        if (change) {
            shouldBeDisplayed = true;
        } else {
            shouldBeDisplayed = false;
        }
    }

    public void shouldDisplayCircle() {
        if (!displayCircle) {
            changeDisplayCircle(false);
            return;
        }
        changeDisplayCircle(true);
        if (!actualDisplayCircle) {
            return;
        }

    }

    public void updateCircle(boolean reel) {
        if (shouldBeDisplayed && reel) {
            displayCircle();
        } else {
            removeCircle();
        }
    }

    public void scaleWhenSelected() {
    }

    public void scaleWhenSelected(float scale) {
    }

    public void update(double time) {
        rotation(time);
        updateisClickable();
        shouldDisplayCircle();
        scaleWhenSelected();
        planets.values().forEach(planet -> planet.update(time));
        planets.values().forEach(planet -> planet.updateOrbit());
        belts.forEach(belt -> belt.update(time));

    }

    public String displayInformation() {
        StringBuilder res = new StringBuilder();
        res.append(String.format("Radius : %s km\n", formatter.format(Math.round(realSize))));
        res.append(String.format("Mass : %.3f kg^%d\n", mass.getKey(), mass.getValue()));
        if (volume.getValue() != 0) {
            res.append(String.format("Volume : %.3f km^%d\n", volume.getKey(), volume.getValue()));
        }
        res.append(String.format("Density : %.3f g/cm^3\n", density));
        if (gravity != 0) {
            res.append(String.format("Gravity : %.3f m/s^2\n", gravity));
        }
        discoveredBy.ifPresent(discover -> res.append("Discovered by : ").append(discover).append("\n"));
        discoveryDate.ifPresent(discover -> res.append("Discovery date : ").append(discover).append("\n"));
        alternativeName.ifPresent(discover -> res.append("Alternative name : ").append(discover).append("\n"));

        return res.toString();
    }

    public void addAsteroidBelt(double rotation, float innerRadius, float outerRadius, float heightVariation,
            float sizeOfAsteroid,
            int numObjects) {
        AsteroidBelt belt = new AsteroidBelt(rotation).generateAsteroidBelt(innerRadius, outerRadius, heightVariation,
                sizeOfAsteroid,
                numObjects);
        belts.add(belt);
        this.node.attachChild(belt.getBeltNode());

    }

    public void displayWhenSelected() {
        // log.info("name : {} ,Position : {},planetPos : {}",
        // name,getWorldTranslation() , model.getWorldTranslation());
    }

}
