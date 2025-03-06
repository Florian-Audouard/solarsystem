package fr.univtln.faudouard595.solarsystem.utils.test;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.*;

public class LemurTextBoxExample extends SimpleApplication {

    public static void main(String[] args) {
        LemurTextBoxExample app = new LemurTextBoxExample();
        app.start();
    }

    public void createBox(String text, ColorRGBA backgroundColor) {

    }

    @Override
    public void simpleInitApp() {
        // Initialize Lemur
        GuiGlobals.initialize(this);

        // Create a container (box) to hold the text
        Container box = new Container();
        guiNode.attachChild(box);
        box.setLocalTranslation(new Vector3f(100, cam.getHeight() - 50, 0));
        box.setPreferredSize(new Vector3f(400, 300, 0));

        // Create a label with limited width
        Label label = new Label("This is a long text that should be constrained within the box.");
        label.setPreferredSize(new Vector3f(150, 0, 1));
        // label.setMaxWidth(50);
        // label.setTextHAlignment(HAlignment.Center); // Align horizontally
        // label.setTextVAlignment(VAlignment.Center); // Align vertically
        Label label2 = new Label("second label");
        // Set a background for the label
        QuadBackgroundComponent background = new QuadBackgroundComponent(ColorRGBA.DarkGray);
        box.setBackground(background);

        // Set padding (Insets) around the label text
        background.setMargin(20, 20);

        // Add label to the box
        box.addChild(label);
        box.addChild(label2);
    }
}
