package fr.univtln.faudouard595.solarsystem.space;

import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LightControl;
import com.jme3.scene.shape.Sphere;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Star extends Body {
    private ParticleEmitter flame;
    private static final int COUNT_FACTOR = 50;
    private static final float COUNT_FACTOR_F = 1f;
    private static final boolean POINT_SPRITE = false;

    private static final Type EMITTER_TYPE = POINT_SPRITE ? Type.Point : Type.Triangle;

    public Star(String name, float size, float rotationPeriod, float rotationInclination, TYPE type, ColorRGBA color) {
        super(name, size, rotationPeriod, rotationInclination, type, color);

    }

    public void generateHalo() {
        Material haloMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        haloMaterial.setColor("Color", ColorRGBA.Yellow);
        haloMaterial.setFloat("GlowMap", 2f); // Apply a slight glow effect

        // Create a larger sphere to represent the corona
        Sphere halo = new Sphere(sphereSample, sphereSample, sphereRadius); // Make it larger than the sun
        Geometry haloGeometry = new Geometry("Halo", halo);
        haloGeometry.setMaterial(haloMaterial);
        super.getNode().attachChild(haloGeometry);
    }

    public Material generateMat() {
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap",
                app.getAssetManager()
                        .loadTexture(TEXTUREPATH + super.getName() + "/" + super.getName() + "_Color.jpg"));
        mat.setFloat("GlowMap", 5f);
        return mat;
    }

    public void generateBody(Node rootNode, ViewPort viewPort) {
        super.generateBody(rootNode);
        PointLight sunLight = new PointLight();
        sunLight.setPosition(super.getNode().getWorldTranslation());
        sunLight.setColor(ColorRGBA.White);
        super.getNode().addLight(sunLight);
        LightControl lightControl = new LightControl(sunLight);
        super.getNode().addControl(lightControl);
        displayLine();
        circleGeo.setCullHint(Spatial.CullHint.Always);
        circleText.setText("");
        createFlame(super.getScaleRadius());
    }

    private void createFlame(float radius) {
        flame = new ParticleEmitter("Flame", EMITTER_TYPE, 32 * COUNT_FACTOR);
        flame.setSelectRandomImage(true);
        flame.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (1f / COUNT_FACTOR_F)));
        flame.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        flame.setStartSize(radius / 2);
        flame.setEndSize(radius);
        flame.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        flame.setParticlesPerSec(0);
        flame.setGravity(0, 0, 0);
        flame.setLowLife(0f);
        flame.setHighLife(5f);
        flame.getParticleInfluencer().setInitialVelocity(new Vector3f(0, radius / 4, 0));

        flame.getParticleInfluencer().setVelocityVariation(1f);
        flame.setImagesX(2);
        flame.setImagesY(2);
        Material mat = new Material(Body.app.getAssetManager(),
                "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture",
                Body.app.getAssetManager().loadTexture("Effects/Explosion/flame.png"));
        mat.setBoolean("PointSprite", POINT_SPRITE);
        flame.setMaterial(mat);
        super.getNode().attachChild(flame);
    }

    @Override
    public void update(double time) {
        super.update(time);
        flame.emitAllParticles();
    }

}