package fr.univtln.faudouard595.solarsystem.ui.information;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.QuadBackgroundComponent;

import fr.univtln.faudouard595.solarsystem.App;
import fr.univtln.faudouard595.solarsystem.space.Body;
import fr.univtln.faudouard595.solarsystem.ui.controls.button.MyButton;
import fr.univtln.faudouard595.solarsystem.ui.controls.camera.CameraTool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisplayInformation {
    private static Body currentBody;
    private static Label label;
    private static Label nameLabel;
    private static Panel labelBackground;
    public static App app;
    private static float paddingWidth = 10;
    private static Node informationNode;
    public static boolean isInformationDisplayed = false;

    private static Node helpNode;
    public static boolean isHelpDisplayed = false;
    private static String helpText = """
            - H : Toggle help
            - I : Toggle information
            - Space : Pause / Play
            - Arrow left : Previous body
            - Arrow right : Next body
            - F1 : Slow down
            - F2 : Speed up
            - F3 : Switch camera
            """;

    private static void initHelp() {
        helpNode = new Node("HelpNode");

        Label helpLabel = new Label(helpText);
        helpLabel.setColor(ColorRGBA.White);
        helpLabel.setFont(app.font);
        helpLabel.setFontSize(32);
        QuadBackgroundComponent background = new QuadBackgroundComponent();
        background.setColor(new ColorRGBA(27f / 255, 25f / 255, 27f / 255, 0.15f));
        Panel helpBackground = new Panel();
        helpBackground.attachChild(helpLabel);
        helpBackground.setBackground(background);
        int margin = 10;
        helpBackground.setPreferredSize(
                new Vector3f(helpLabel.getPreferredSize().x + margin * 2, helpLabel.getPreferredSize().y,
                        0));
        helpLabel.setLocalTranslation(margin, -margin, 1);
        int paddingBackground = 20;
        helpBackground.setLocalTranslation(
                app.getCamera().getWidth() - helpBackground.getPreferredSize().x - paddingBackground,
                helpBackground.getPreferredSize().y + paddingBackground, 0);

        helpNode.attachChild(helpBackground);
    }

    public static void init() {
        informationNode = new Node("InformationNode");
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
        informationNode.attachChild(labelBackground);
        MyButton.builder()
                .text("⏪")
                .guiNode(informationNode)
                .x(paddingBackground + 25 + 5)
                .y(app.getCamera().getHeight() - paddingBackground - nameLabel.getPreferredSize().y / 2)
                .preferedSizeWidth(50)
                .preferedSizeHeight(45)
                .fontSize(30)
                .actionFunction(() -> CameraTool.prevMainBody())
                .build()
                .init();
        MyButton.builder()
                .text("◀️")
                .guiNode(informationNode)
                .x(paddingBackground + 25 + 5 + 50)
                .y(app.getCamera().getHeight() - paddingBackground - nameLabel.getPreferredSize().y / 2)
                .preferedSizeWidth(50)
                .preferedSizeHeight(45)
                .fontSize(30)
                .actionFunction(() -> CameraTool.prevBody())
                .build()
                .init();
        MyButton nextMainButton = MyButton.builder()
                .text("⏩")
                .guiNode(informationNode)
                .x(paddingBackground + app.getCamera().getWidth() / 4 - paddingBackground - 25 - 5)
                .y(app.getCamera().getHeight() - paddingBackground - nameLabel.getPreferredSize().y / 2)
                .preferedSizeWidth(50)
                .preferedSizeHeight(45)
                .fontSize(30)
                .build()
                .init();
        nextMainButton.setActionFunction(() -> CameraTool.nextMainBody());
        MyButton nextButton = MyButton.builder()
                .text("▶️")
                .guiNode(informationNode)
                .x(paddingBackground + app.getCamera().getWidth() / 4 - paddingBackground - 25 - 5 - 50)
                .y(app.getCamera().getHeight() - paddingBackground - nameLabel.getPreferredSize().y / 2)
                .preferedSizeWidth(50)
                .preferedSizeHeight(45)
                .fontSize(30)
                .build()
                .init();
        nextButton.setActionFunction(() -> CameraTool.nextBody());

        initHelp();

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

    public static void disableInformation() {
        app.getMyGuiNode().detachChild(informationNode);
    }

    public static void enableInformation() {
        app.getMyGuiNode().attachChild(informationNode);
    }

    public static void switchDisplayInformation() {
        if (isInformationDisplayed) {
            disableInformation();
        } else {
            enableInformation();
        }
        isInformationDisplayed = !isInformationDisplayed;
    }

    public static void disableHelp() {
        app.getMyGuiNode().detachChild(helpNode);
    }

    public static void enableHelp() {
        app.getMyGuiNode().attachChild(helpNode);
    }

    public static void switchHelpInformation() {
        if (isHelpDisplayed) {
            disableHelp();
        } else {
            enableHelp();
        }
        isHelpDisplayed = !isHelpDisplayed;
    }

    public static void update() {
        onChange();
    }
}
