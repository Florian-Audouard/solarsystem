package fr.univtln.faudouard595.solarsystem.utils.convertionObject;

import java.io.File;
import java.util.Map;

import com.jme3.app.SimpleApplication;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.AmbientLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;

import fr.univtln.faudouard595.solarsystem.utils.file.MyLoadFile;

public class ConvertObject extends SimpleApplication {
    Spatial model;

    public static void main(String[] args) {

        ConvertObject app = new ConvertObject();
        AppSettings settings = new AppSettings(true);

        settings.setFrameRate(60);

        app.start();
    }

    @Override
    public void simpleInitApp() {
        boolean test = false;
        boolean override = false;
        flyCam.setMoveSpeed(10);
        String name = "Phobos";
        String path = "Models/Body/" + name + "/" + name;
        Node myNode = new Node();
        model = Convert.convert(path, assetManager, myNode, override, test, 5);
        if (override || !test && !MyLoadFile.fileExists(path + ".j3o")) {
            try {
                BinaryExporter.getInstance().save(model, new File("src/main/resources/" + path + ".j3o"));
            } catch (Exception e) {
            }
        }
        model.scale(0.05f);
        rootNode.attachChild(myNode);
        Sphere sphereMesh = new Sphere(16, 16, 0.5f); // 16x16 segments, radius 0.5
        Geometry sphereGeom = new Geometry("SmallSphere", sphereMesh);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        sphereGeom.setMaterial(mat);
        sphereGeom.setLocalTranslation(0, 0, 0); // Position at (0,0,0)
        rootNode.attachChild(sphereGeom);


        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.1f));
        rootNode.addLight(al);
        PointLight pl = new PointLight();
        pl.setPosition(new Vector3f(0, 0, 0));
        pl.setColor(ColorRGBA.White);
        rootNode.addLight(pl);
        cam.lookAt(model.getWorldTranslation(), Vector3f.UNIT_Y);

    }

    @Override
    public void simpleUpdate(float tpf) {
        model.rotate(0f, 0.01f, 0f);
    }
}
