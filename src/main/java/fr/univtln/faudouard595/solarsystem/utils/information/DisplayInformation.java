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
    private static Label nameLabel;
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
        nameLabel = new Label("");
        nameLabel.setFontSize(30);
        nameLabel.setFont(app.font);

        label.setFontSize(20);
        label.setLocalTranslation(paddingWidth, -50, 1);
        label.setColor(ColorRGBA.White);
        label.setFont(app.font);
        labelBackground = new Panel();
        QuadBackgroundComponent background = new QuadBackgroundComponent();
        background.setColor(new ColorRGBA(27f / 255, 25f / 255, 27f / 255, 0.2f)); // Set background color
        labelBackground.setBackground(background);
        float paddingBackground = 50f;
        labelBackground.setPreferredSize(
                new Vector3f(app.getCamera().getWidth() / 4 - paddingBackground,
                        app.getCamera().getHeight() - paddingBackground * 2, 0));
        labelBackground.setLocalTranslation(paddingBackground, app.getCamera().getHeight() - paddingBackground, 0);
        // Attach label to the panel
        labelBackground.attachChild(nameLabel);
        labelBackground.attachChild(label);
        app.getGuiNode().attachChild(labelBackground);
    }

    public static void display() {
        nameLabel.setText(currentBody.getName());
        nameLabel.setColor(currentBody.getColor());
        label.setText(currentBody.displayInformation());
        nameLabel.setLocalTranslation(labelBackground.getPreferredSize().x / 2 - nameLabel.getPreferredSize().x / 2, 0,
                1);
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
