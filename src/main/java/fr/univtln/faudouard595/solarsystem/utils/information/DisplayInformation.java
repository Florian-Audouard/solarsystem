package fr.univtln.faudouard595.solarsystem.utils.information;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.QuadBackgroundComponent;

import fr.univtln.faudouard595.solarsystem.App;
import fr.univtln.faudouard595.solarsystem.body.Body;
import fr.univtln.faudouard595.solarsystem.utils.controls.button.MyButton;
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
        nameLabel = new Label("Sun");
        nameLabel.setFont(app.font);

        label.setLocalTranslation(paddingWidth, -80, 1);
        label.setColor(ColorRGBA.White);
        label.setFont(app.font);
        labelBackground = new Panel();
        QuadBackgroundComponent background = new QuadBackgroundComponent();
        background.setColor(new ColorRGBA(27f / 255, 25f / 255, 27f / 255, 0.15f));
        labelBackground.setBackground(background);
        float paddingBackground = 50f;
        labelBackground.setPreferredSize(
                new Vector3f(app.getCamera().getWidth() / 4 - paddingBackground,
                        app.getCamera().getHeight() - paddingBackground * 2, 0));
        labelBackground.setLocalTranslation(paddingBackground, app.getCamera().getHeight() - paddingBackground, 0);
        labelBackground.attachChild(nameLabel);
        labelBackground.attachChild(label);
        app.getGuiNode().attachChild(labelBackground);
        MyButton prevButton = MyButton.builder()
                .text("⏪")
                .guiNode(app.getGuiNode())
                .x(paddingBackground + 25 + 5)
                .y(app.getCamera().getHeight() - paddingBackground - nameLabel.getPreferredSize().y / 2)
                .preferedSizeWidth(50)
                .preferedSizeHeight(45)
                .fontSize(30)
                .build()
                .init();
        prevButton.setActionFunction(b -> CameraTool.prevBody());

        MyButton nextButton = MyButton.builder()
                .text("⏩")
                .guiNode(app.getGuiNode())
                .x(paddingBackground + app.getCamera().getWidth() / 4 - paddingBackground - 25 - 5)
                .y(app.getCamera().getHeight() - paddingBackground - nameLabel.getPreferredSize().y / 2)
                .preferedSizeWidth(50)
                .preferedSizeHeight(45)
                .fontSize(30)
                .build()
                .init();
        nextButton.setActionFunction(b -> CameraTool.nextBody());
    }

    public static void displayName() {

        nameLabel.setText(currentBody.getName());
        nameLabel.setColor(currentBody.getColor());
        nameLabel.setFontSize(64);
        nameLabel.setLocalTranslation(labelBackground.getPreferredSize().x / 2 - nameLabel.getPreferredSize().x / 2, 0,
                1);

    }

    public static void displayInfo() {
        label.setText(currentBody.displayInformation());
        label.setFontSize(30);
    }

    public static void onChange() {
        if (currentBody == null || !currentBody.equals(CameraTool.bodies.getCurrentValue())) {
            currentBody = CameraTool.bodies.getCurrentValue();
            displayName();
        }
        displayInfo();
    }

    public static void update() {
        onChange();
    }
}
