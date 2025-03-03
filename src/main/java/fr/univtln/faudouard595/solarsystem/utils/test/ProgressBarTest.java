package fr.univtln.faudouard595.solarsystem.utils.test;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.BaseStyles;

public class ProgressBarTest extends SimpleApplication {

    private ProgressBar progressBar;
    private float progress = 0.0f;

    public static void main(String[] args) {
        ProgressBarTest app = new ProgressBarTest();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Initialize Lemur
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        // Create a container for layout
        Container container = new Container();
        container.setLocalTranslation(100, cam.getHeight() - 50, 0);
        guiNode.attachChild(container);

        // Create the progress bar
        progressBar = new ProgressBar();
        progressBar.setPreferredSize(new com.jme3.math.Vector3f(200, 20, 1));
        progressBar.getValueIndicator().setBackground(new QuadBackgroundComponent(new ColorRGBA(0.2f, 0.5f, 0.9f, 1f)));
        progressBar.setProgressValue(0.0f); 

        // Add it to the container
        container.addChild(progressBar);
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Simulate progress
        progress += 0.1f; // Increment slowly

        // Update the progress bar
        progressBar.setProgressValue(progress);
    }
}
