package fr.univtln.faudouard595.solarsystem.utils.convertionObject;

import java.util.AbstractMap;
import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;

import fr.univtln.faudouard595.solarsystem.space.Planet;
import fr.univtln.faudouard595.solarsystem.utils.file.MyLoadFile;

public class Convert {

    public static Spatial convert(String path, AssetManager assetManager, Node rootNode,
            boolean override, boolean test) {
        String extention = ".obj";
        if (!override && !test && MyLoadFile.fileExists(path + ".j3o")) {
            extention = ".j3o";
        }
        Spatial model = assetManager.loadModel(path + extention);
        rootNode.attachChild(model);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        TextureKey key = new TextureKey(path + "_Color.jpg");
        Texture tex = assetManager.loadTexture(key);
        mat.setTexture("DiffuseMap", tex);
        model.setMaterial(mat);
        if (MyLoadFile.fileExists(path + "_normal.jpg")) {
            Texture normalMap = assetManager.loadTexture(path + "_normal.jpg");
            mat.setTexture("NormalMap", normalMap);
            TangentBinormalGenerator.generate(model);
        }
        Planet.armoniseMat(mat);
        return model;

    }
}
