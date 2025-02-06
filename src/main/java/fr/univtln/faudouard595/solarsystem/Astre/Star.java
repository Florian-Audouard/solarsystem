package fr.univtln.faudouard595.solarsystem.Astre;

import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.LightControl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Star extends Astre {

    public Star(String name, float size, float rotationPeriod, TYPE type, ColorRGBA color) {
        super(name, size, rotationPeriod, type, color);
    }

    public Material generateMat() {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture(TEXTUREPATH + super.getName() + ".jpg"));
        return mat;
    }

    public void generateStar(Node rootNode, ViewPort viewPort) {
        generateAstre(rootNode, true);
        PointLight sunLight = new PointLight();
        sunLight.setPosition(super.getNode().getWorldTranslation());
        sunLight.setColor(ColorRGBA.White);
        super.getNode().addLight(sunLight);
        LightControl lightControl = new LightControl(sunLight);
        super.getNode().addControl(lightControl);

    }

}