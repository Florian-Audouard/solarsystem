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
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

import groovyjarjarpicocli.CommandLine.Help.Ansi.Text;

public class Convert extends SimpleApplication {
    Spatial model;

    public static void main(String[] args) {
        Convert app = new Convert();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        String name = "Kuiper.j3o";
        String path = "Models/" + name;

        model = assetManager.loadModel(path);

        // model.setMaterial(mat);
        model.setLocalScale(0.0005f);
        rootNode.attachChild(model);
        // model.setLocalTranslation(new Vector3f(0, 0, 40));

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.1f));
        rootNode.addLight(al);
        PointLight pl = new PointLight();
        pl.setPosition(new Vector3f(0, 0, 0));
        pl.setColor(ColorRGBA.White);
        rootNode.addLight(pl);
        cam.lookAt(model.getWorldTranslation(), Vector3f.UNIT_Y);
        // try {
        // BinaryExporter.getInstance().save(model, new File("Models/Kuiper.j3o"));
        // } catch (Exception e) {
        // // TODO: handle exception
        // }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (model != null)
            model.rotate(0, 0.001f, 0);
    }
}
