package fr.univtln.faudouard595.solarsystem.utils.test;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;

public class NormalMapTest extends SimpleApplication {
    Geometry model;

    public static void main(String[] args) {
        NormalMapTest app = new NormalMapTest();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(10);
        String planet = "Mars";
        String path = "Textures/Body/" + planet + "/" + planet;
        Sphere sphere = new Sphere(32, 32, 5);
        sphere.setTextureMode(Sphere.TextureMode.Projected);
        model = new Geometry("Sphere", sphere);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Specular", new ColorRGBA(1f, 1f, 1f, 1f).mult(0.2f));
        mat.setColor("Ambient", ColorRGBA.Gray);
        mat.setFloat("Shininess", 12f);
        mat.setTexture("DiffuseMap", assetManager.loadTexture(path + "_Color.jpg"));

        Texture normalMap = assetManager.loadTexture(path + "_Normal.jpg");
        mat.setTexture("NormalMap", normalMap);
        TangentBinormalGenerator.generate(model);

        model.setMaterial(mat);
        model.setLocalRotation(new Quaternion().fromAngles(-90 * FastMath.DEG_TO_RAD, 0, 0));
        model.setShadowMode(ShadowMode.CastAndReceive);

        model.setLocalTranslation(new Vector3f(10, 0, 0));
        rootNode.attachChild(model);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.1f));
        rootNode.addLight(al);
        PointLight pl = new PointLight();
        pl.setPosition(new Vector3f(0, 0, 0));
        pl.setColor(ColorRGBA.White);
        rootNode.addLight(pl);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (model != null) {
            model.rotate(0, 0, 0.0001f);
        }
    }

}
