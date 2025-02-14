package fr.univtln.faudouard595.solarsystem.utils.controls.button;

import java.util.ArrayList;
import java.util.List;

import com.jme3.font.BitmapText;
import com.jme3.font.BitmapFont.Align;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;

import fr.univtln.faudouard595.solarsystem.App;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ButtonControl {
    private static float heightFlowButton = 45;
    private static float widthFlowButton = 50;
    private static float positionYFlowButton = 50;
    private static int controlButtonTextSize = 30;
    private static BitmapText speedText;
    private static App app;
    private static List<MyButton> buttons = new ArrayList<>();

    public static void init(App app) {
        ButtonControl.app = app;
        MyButton.app = app;
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        speedText = app.font.createLabel("");
        speedText.setSize(32);
        speedText.setColor(ColorRGBA.White);

        app.getGuiNode().attachChild(speedText);

        MyButton pause = MyButton.builder()
                .text("⏸")
                .guiNode(app.getGuiNode())
                .x(app.getCamera().getWidth() / 2)
                .y(positionYFlowButton)
                .preferedSizeWidth(widthFlowButton)
                .preferedSizeHeight(heightFlowButton)
                .fontSize(controlButtonTextSize)
                .build()
                .init();
        pause.setActionFunction(b -> {
            app.isPause = !app.isPause;
            pause.setText(app.isPause ? "⏵" : "⏸");
        });
        buttons.add(pause);
        buttons.add(MyButton.builder()
                .text("⏩")
                .guiNode(app.getGuiNode())
                .x(app.getCamera().getWidth() / 2 + pause.getSize().x / 2 + 30)
                .y(positionYFlowButton)
                .preferedSizeWidth(widthFlowButton)
                .preferedSizeHeight(heightFlowButton)
                .fontSize(controlButtonTextSize)
                .analogFunction(() -> app.speedList.increaseSpeed())
                .build()
                .init());
        buttons.add(MyButton.builder()
                .text("⏪")
                .guiNode(app.getGuiNode())
                .x(app.getCamera().getWidth() / 2 - pause.getSize().x / 2 - 30)
                .y(positionYFlowButton)
                .preferedSizeWidth(widthFlowButton)
                .preferedSizeHeight(heightFlowButton)
                .fontSize(controlButtonTextSize)
                .analogFunction(() -> app.speedList.decreaseSpeed())
                .build()
                .init());

    }

    public static void updateSpeedText() {
        String text = app.isPause ? "Paused" : app.speedList.getFormatedSpeed();
        speedText.setText(text);
        speedText.setLocalTranslation(app.getCamera().getWidth() / 2 - speedText.getLineWidth() / 2,
                positionYFlowButton + heightFlowButton + speedText.getLineHeight(), 0);
    }

    public static void updateButtons() {
        buttons.forEach(MyButton::update);
    }

    public static void update() {
        updateSpeedText();
        updateButtons();
    }
}
