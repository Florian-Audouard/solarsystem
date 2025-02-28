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
import com.jme3.util.TangentBinormalGenerator;

public class Convert extends SimpleApplication {

    public static void main(String[] args) {
        Convert app = new Convert();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        boolean test = false;
        boolean override = false;
        flyCam.setMoveSpeed(10);
        for (int i = 1; i < 11; i++) {
            String name = "Asteroid";
            String path = "Models/Asteroid" + i + "/" + name;
            String extention = ".obj";
            if (!override && !test && new File("src/main/resources/" + path + ".j3o").exists()) {
                extention = ".j3o";
            }
            Spatial model = assetManager.loadModel(path + extention);
            model.setLocalScale(0.5f);
            rootNode.attachChild(model);
            model.setLocalTranslation(new Vector3f(i * 3, 0, 0));
            Material mat = new Material(assetManager,
                    "Common/MatDefs/Light/Lighting.j3md");
            TextureKey key = new TextureKey(path + ".jpg");
            Texture tex = assetManager.loadTexture(key);
            mat.setTexture("DiffuseMap", tex);
            model.setMaterial(mat);
            Texture normalMap = assetManager.loadTexture(path + "_normal.jpg");
            mat.setTexture("NormalMap", normalMap);
            TangentBinormalGenerator.generate(model);

            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", ColorRGBA.White);
            mat.setColor("Specular", new ColorRGBA(1f, 1f, 1f, 1f).mult(0.2f));
            mat.setColor("Ambient", ColorRGBA.Gray);
            mat.setFloat("Shininess", 12f);
            if (override || !test && !new File("src/main/resources/" + path + ".j3o").exists()) {
                try {
                    BinaryExporter.getInstance().save(model, new File("src/main/resources/" + path + ".j3o"));
                } catch (Exception e) {
                }
            }
        }

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.1f));
        rootNode.addLight(al);
        PointLight pl = new PointLight();
        pl.setPosition(new Vector3f(0, 0, 0));
        pl.setColor(ColorRGBA.White);
        rootNode.addLight(pl);
        // cam.lookAt(model.getWorldTranslation(), Vector3f.UNIT_Y);

    }

    @Override
    public void simpleUpdate(float tpf) {
    }
}
