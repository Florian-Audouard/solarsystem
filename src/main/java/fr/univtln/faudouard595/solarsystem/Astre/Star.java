package fr.univtln.faudouard595.solarsystem.Astre;

import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.LightControl;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.PointLightShadowRenderer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Star extends Astre {
    private Vector3f lastPosition;
    private PointLight sunLight;

    public Star(String name, float size, float rotationPeriod) {
        super(name, size, rotationPeriod);
        sunLight = new PointLight();
    }

    public void generateStar(Node rootNode, ViewPort viewPort) {
        generateAstre(rootNode, true);
        sunLight.setPosition(super.getNode().getWorldTranslation());
        sunLight.setColor(ColorRGBA.White);
        super.getNode().addLight(sunLight);
        lastPosition = super.getNode().getWorldTranslation().clone();
        LightControl lightControl = new LightControl(sunLight);
        super.getNode().addControl(lightControl);

        PointLightShadowRenderer shadowRenderer = new PointLightShadowRenderer(assetManager, 1024);
        shadowRenderer.setLight(sunLight);
        shadowRenderer.setShadowIntensity(0f);
        viewPort.addProcessor(shadowRenderer);

    }

}
