package fr.univtln.faudouard595.solarsystem.utils.test;

import com.jme3.app.SimpleApplication;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Cylinder;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

public class RingWithHoleExample extends SimpleApplication {

    public static void main(String[] args) {
        RingWithHoleExample app = new RingWithHoleExample();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Node ringNode = new Node("RingWithHole");

        // Create the outer ring (larger cylinder)
        Cylinder outerCylinder = new Cylinder(32, 64, 2f, 0.1f, true);
        Geometry outerRing = new Geometry("OuterRing", outerCylinder);
        Material outerMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        outerMaterial.setColor("Color", ColorRGBA.Blue);
        outerRing.setMaterial(outerMaterial);

        // Create the inner cylinder (smaller cylinder to create the hole)
        Cylinder innerCylinder = new Cylinder(32, 64, 1f, 0.2f, true); // Smaller radius (1f)
        Geometry innerRing = new Geometry("InnerRing", innerCylinder);
        Material innerMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        innerMaterial.setColor("Color", ColorRGBA.Black); // Make it black (invisible part)
        innerRing.setMaterial(innerMaterial);

        // Position the inner cylinder at the center of the outer cylinder
        innerRing.setLocalTranslation(0, 0.1f, 0); // No offset needed as both cylinders share the same center

        // Attach the outer and inner parts to the node (subtractive effect)
        ringNode.attachChild(outerRing);
        ringNode.attachChild(innerRing); // Inner ring creates the hole effect

        // Attach the node to the root node
        rootNode.attachChild(ringNode);
    }
}
