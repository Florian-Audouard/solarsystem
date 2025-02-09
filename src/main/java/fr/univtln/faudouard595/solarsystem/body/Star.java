package fr.univtln.faudouard595.solarsystem.body;

import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.LightControl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Star extends Body {

    public Star(String name, float size, float rotationPeriod, float rotationInclination, TYPE type, ColorRGBA color) {
        super(name, size, rotationPeriod, rotationInclination, type, color);
    }

    public Material generateMat() {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap",
                assetManager.loadTexture(TEXTUREPATH + Body.planetTexture + "/" + super.getName() + ".jpg"));
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
    }

}