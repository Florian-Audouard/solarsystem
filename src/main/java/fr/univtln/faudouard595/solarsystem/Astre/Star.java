package fr.univtln.faudouard595.solarsystem.Astre;

import com.jme3.asset.AssetManager;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class Star extends Astre {
    public Star(String name, float size, float rotationPeriod) {
        super(name, size, rotationPeriod);
    }

    public void generateStar(AssetManager assetManager, Node rootNode) {
        generateAstre(assetManager, rootNode, true);
        PointLight pointLight = new PointLight();
        pointLight.setPosition(Vector3f.ZERO);
        pointLight.setColor(ColorRGBA.White);
        pointLight.setRadius(0f);
        rootNode.addLight(pointLight);
    }

}
