package fr.univtln.faudouard595.solarsystem.utils.convertionObject;

import java.io.File;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.AmbientLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

public class Convert extends SimpleApplication {
    Spatial model;

    public static void main(String[] args) {
        Convert app = new Convert();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        String name = "Asteroid";
        String path = "Models/Asteroid4/" + name;

        model = assetManager.loadModel(path + ".obj");

        // model.setMaterial(mat);
        model.setLocalScale(0.5f);
        rootNode.attachChild(model);
        model.setLocalTranslation(new Vector3f(0, 0, 20));
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        TextureKey key = new TextureKey(path + ".png");
        Texture tex = assetManager.loadTexture(key);
        mat.setTexture("DiffuseMap", tex);
        model.setMaterial(mat);
        // Texture normalMap = assetManager.loadTexture("Models/Asteroid1/" + name +
        // "_Normal.png");
        // mat.setTexture("NormalMap", normalMap);
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Specular", new ColorRGBA(1f, 1f, 1f, 1f).mult(0.2f));
        mat.setColor("Ambient", ColorRGBA.Gray);
        mat.setFloat("Shininess", 12f);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.1f));
        rootNode.addLight(al);
        PointLight pl = new PointLight();
        pl.setPosition(new Vector3f(0, 0, 0));
        pl.setColor(ColorRGBA.White);
        rootNode.addLight(pl);
        cam.lookAt(model.getWorldTranslation(), Vector3f.UNIT_Y);
        if (!new File("src/main/resources/" + path).exists()) {
            try {
                BinaryExporter.getInstance().save(model, new File("src/main/resources/" + path + ".j3o"));
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (model != null)
            model.rotate(0, 0.001f, 0);
    }
}
