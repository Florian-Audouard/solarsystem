package fr.univtln.faudouard595.solarsystem.Astre;

import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Star extends Astre {
    private Vector3f lastPosition;
    private PointLight pointLight;

    public Star(String name, float size, float rotationPeriod) {
        super(name, size, rotationPeriod);
        pointLight = new PointLight();
    }

    public void generateStar(Node rootNode) {
        generateAstre(rootNode, true);
        pointLight.setPosition(super.getNode().getWorldTranslation());
        pointLight.setColor(ColorRGBA.White);
        pointLight.setRadius(0f);
        super.getNode().addLight(pointLight);
        lastPosition = super.getNode().getWorldTranslation().clone();
    }

    @Override
    public void update(float time) {
        super.update(time);
        Vector3f currentPosition = super.getNode().getWorldTranslation();
        if (!currentPosition.equals(lastPosition)) {
            pointLight.setPosition(currentPosition.clone());
            lastPosition.set(currentPosition.clone());
        }
    }

}
