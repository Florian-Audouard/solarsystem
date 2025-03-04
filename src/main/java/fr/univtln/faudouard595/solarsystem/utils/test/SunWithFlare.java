package fr.univtln.faudouard595.solarsystem.utils.test;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

public class SunWithFlare extends SimpleApplication {

    private Node sunNode;
    private ParticleEmitter flame;
    private static final int COUNT_FACTOR = 10;
    private static final float COUNT_FACTOR_F = 1f;
    private static final boolean POINT_SPRITE = false;

    private static final Type EMITTER_TYPE = POINT_SPRITE ? Type.Point : Type.Triangle;

    public static void main(String[] args) {
        SunWithFlare app = new SunWithFlare();
        app.start(); // Starts the application
    }

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(50); // Increase the camera movement speed
        cam.setFrustumFar(50000000);

        float radius = 500f;

        createSun(radius);
        createFlame(radius);

        addBloomEffect();
        cam.setLocation(new Vector3f(0, 0, 2000)); // Move the camera back a bit
    }

    private void createSun(float radius) {
        // Load the sun texture
        Texture sunTexture = assetManager.loadTexture("Textures/Body/Sun/Sun_Color.jpg"); // Correct texture path

        // Create the sun material and apply the texture
        Material sunMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sunMaterial.setTexture("ColorMap", sunTexture);

        // Use Bloom effect for glow instead of GlowMap (Unshaded does not support
        // GlowMap directly)
        // Set a placeholder glow intensity via the Bloom Filter effect later
        // Create the geometry for the sun
        Geometry sunGeometry = new Geometry("Sun", new Sphere(30, 30, radius)); // Size of the sun
        sunGeometry.setMaterial(sunMaterial);

        // Create a node to hold the sun and attach it to the root node
        sunNode = new Node("SunNode");
        sunNode.attachChild(sunGeometry);
        rootNode.attachChild(sunNode);

        // Add particle system for solar flare effect
    }

    private void createFlame(float radius) {
        flame = new ParticleEmitter("Flame", EMITTER_TYPE, 32 * COUNT_FACTOR);
        flame.setSelectRandomImage(true);
        flame.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (1f / COUNT_FACTOR_F)));
        flame.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        flame.setStartSize(radius * 1f);
        flame.setEndSize(radius * 1.2f);
        flame.setShape(new EmitterSphereShape(Vector3f.ZERO, 1));
        flame.setParticlesPerSec(0);
        flame.setGravity(0, 0, 0);
        flame.setLowLife(1f);
        flame.setHighLife(5f);
        flame.getParticleInfluencer().setInitialVelocity(new Vector3f(100, 100, 100));
        flame.getParticleInfluencer().setVelocityVariation(1);
        flame.setImagesX(2);
        flame.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        mat.setBoolean("PointSprite", POINT_SPRITE);
        flame.setMaterial(mat);
        rootNode.attachChild(flame);
    }

    private void addBloomEffect() {
        // Create and add Bloom Filter for lens flare effect
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bloom = new BloomFilter();
        bloom.setBloomIntensity(2.5f); // Adjust intensity of bloom
        bloom.setBlurScale(2.0f); // Adjust blur size for the bloom effect

        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp); // Add Bloom effect to the viewport
    }

    @Override
    public void simpleUpdate(float tpf) {
        // You can update any properties of the flare or sun effect here if needed
        flame.emitAllParticles();

    }
}
