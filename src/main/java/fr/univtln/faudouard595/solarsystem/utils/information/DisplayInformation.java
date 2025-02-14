package fr.univtln.faudouard595.solarsystem.utils.information;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.QuadBackgroundComponent;

import fr.univtln.faudouard595.solarsystem.App;
import fr.univtln.faudouard595.solarsystem.body.Body;
import fr.univtln.faudouard595.solarsystem.utils.controls.camera.CameraTool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisplayInformation {
    private static Body currentBody;
    private static Label label;
    private static Panel labelBackground;
    public static App app;
    private static float paddingWidth = 10;

    public static void init() {
        label = new Label("");
        // float width = app.getCamera().getWidth() / 4;
        // float height = app.getCamera().getHeight() / 4;
        // label.setPreferredSize(new Vector3f(width, app.getCamera().getHeight() / 4,
        // 0));
        // label.setLocalTranslation(50, app.getCamera().getHeight() - 50, 0);
        label.setFontSize(20);
        label.setLocalTranslation(paddingWidth, 0, 0);
        label.setFont(app.font);
        labelBackground = new Panel();
        QuadBackgroundComponent background = new QuadBackgroundComponent();
        background.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.1f)); // Set background color
        labelBackground.setBackground(background);
        calculateBackGround();
        // Attach label to the panel
        labelBackground.attachChild(label);
        app.getGuiNode().attachChild(labelBackground);
    }

    public static void calculateBackGround() {
        float width = label.getPreferredSize().x;
        float height = label.getPreferredSize().y;
        labelBackground.setPreferredSize(new Vector3f(width + paddingWidth * 2, height, 0));
        labelBackground.setLocalTranslation(50, app.getCamera().getHeight() - 50, 0);

    }

    public static void display() {
        label.setText(currentBody.displayInformation());
        calculateBackGround();
    }

    public static void onChange() {
        if (currentBody == null || !currentBody.equals(CameraTool.bodies.getCurrentValue())) {
            currentBody = CameraTool.bodies.getCurrentValue();
            display();
        }
    }

    public static void update() {
        onChange();
    }
}
