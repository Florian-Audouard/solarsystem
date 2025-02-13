package fr.univtln.faudouard595.solarsystem.utils.controls.button;

import com.jme3.font.BitmapText;
import com.jme3.font.BitmapFont.Align;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;

import fr.univtln.faudouard595.solarsystem.App;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ButtonControl {
    private static float heightFlowButton = 30;
    private static float positionYFlowButton = 30;
    private static BitmapText speedText;
    private static App app;

    public static void init(App app) {
        ButtonControl.app = app;
        MyButton.app = app;
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        speedText = app.font.createLabel("");
        speedText.setSize(32);
        speedText.setColor(ColorRGBA.White);

        log.info("size of text : {}", speedText.getLineWidth());

        app.getGuiNode().attachChild(speedText);

        MyButton pause = MyButton.builder()
                .text("Pause")
                .guiNode(app.getGuiNode())
                .x(app.getCamera().getWidth() / 2)
                .y(positionYFlowButton)
                .preferedSizeWidth(80)
                .preferedSizeHeight(heightFlowButton)
                .build()
                .init();
        pause.addCommand(b -> {
            app.isPause = !app.isPause;
            pause.setText(app.isPause ? "Play" : "⏵");
        });

        MyButton.builder()
                .text(">>")
                .guiNode(app.getGuiNode())
                .x(app.getCamera().getWidth() / 2 + pause.getSize().x / 2 + 30)
                .y(positionYFlowButton)
                .preferedSizeWidth(45)
                .preferedSizeHeight(heightFlowButton)
                .command(e -> app.calcSpeed(-1))
                .build()
                .init();

        MyButton.builder()
                .text("<<")
                .guiNode(app.getGuiNode())
                .x(app.getCamera().getWidth() / 2 - pause.getSize().x / 2 - 30)
                .y(positionYFlowButton)
                .preferedSizeWidth(45)
                .preferedSizeHeight(heightFlowButton)
                .command(e -> app.calcSpeed(1))
                .build()
                .init();

    }

    public static void updateSpeedText() {
        String text = app.isPause ? "Paused" : app.getFormatedSpeed();
        speedText.setText(text);
        speedText.setLocalTranslation(app.getCamera().getWidth() / 2 - speedText.getLineWidth() / 2,
                positionYFlowButton + heightFlowButton + speedText.getLineHeight(), 0);
    }

    public static void update() {
        updateSpeedText();
    }
}
