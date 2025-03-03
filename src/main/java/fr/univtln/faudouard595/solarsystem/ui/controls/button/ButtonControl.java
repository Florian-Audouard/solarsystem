package fr.univtln.faudouard595.solarsystem.ui.controls.button;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.jme3.font.BitmapText;
import com.jme3.font.BitmapFont.Align;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.Slider;
import fr.univtln.faudouard595.solarsystem.App;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ButtonControl {
    private static float heightFlowButton = 45;
    private static float widthFlowButton = 50;
    private static float positionYFlowButton = 80;
    private static int controlButtonTextSize = 30;
    private static int timeTextY = 10;
    private static BitmapText speedText;
    private static App app;
    private static List<Updatable> updatables = new ArrayList<>();
    public static boolean actualPauseTimeText = false;
    private static BitmapText timeText;

    public static void init(App app) {
        ButtonControl.app = app;
        MyButton.app = app;
        MySlider.app = app;

        speedText = app.font.createLabel("");
        speedText.setSize(32);
        speedText.setColor(ColorRGBA.White);
        app.getMyGuiNode().attachChild(speedText);

        // pause.setText(app.isPause ? "⏵" : "⏸");
        MyButton pause = MyButton.builder()
                .text("⏸")
                .guiNode(app.getMyGuiNode())
                .x(app.getCamera().getWidth() / 2)
                .y(positionYFlowButton)
                .preferedSizeWidth(widthFlowButton)
                .preferedSizeHeight(heightFlowButton)
                .fontSize(controlButtonTextSize)
                .actionFunction(b -> app.isPause = !app.isPause)
                .build()
                .init();
        pause.setUpdateFunction(() -> pause.setText(app.isPause ? "⏵" : "⏸"));
        updatables.add(pause);
        updatables.add(MyButton.builder()
                .text("⏩")
                .guiNode(app.getMyGuiNode())
                .x(app.getCamera().getWidth() / 2 + pause.getSize().x / 2 + 30)
                .y(positionYFlowButton)
                .preferedSizeWidth(widthFlowButton)
                .preferedSizeHeight(heightFlowButton)
                .fontSize(controlButtonTextSize)
                .analogFunction(() -> app.speedList.increaseSpeed())
                .build()
                .init());
        updatables.add(MyButton.builder()
                .text("⏪")
                .guiNode(app.getMyGuiNode())
                .x(app.getCamera().getWidth() / 2 - pause.getSize().x / 2 - 30)
                .y(positionYFlowButton)
                .preferedSizeWidth(widthFlowButton)
                .preferedSizeHeight(heightFlowButton)
                .fontSize(controlButtonTextSize)
                .analogFunction(() -> app.speedList.decreaseSpeed())
                .build()
                .init());
        MyButton lineDisplay = MyButton.builder()
                .text("✦")
                .guiNode(app.getMyGuiNode())
                .x(app.getCamera().getWidth() - 50)
                .y(app.getCamera().getHeight() - 50)
                .preferedSizeWidth(widthFlowButton)
                .preferedSizeHeight(heightFlowButton)
                .fontSize(controlButtonTextSize)
                .build()
                .init();
        lineDisplay.setActionFunction(b -> {
            app.getSun().switchDisplayLines();
            lineDisplay.setText(app.getSun().isDisplayLines() ? "✦" : "✧");
        });
        timeText = app.font.createLabel("");
        timeText.setSize(40);
        timeText.setColor(ColorRGBA.White);
        app.getMyGuiNode().attachChild(timeText);
    }

    public static void updateSpeedText() {
        if (actualPauseTimeText && app.isPause) {
            return;
        }
        if (actualPauseTimeText != app.isPause) {
            if (app.isPause) {
                speedText.setColor(ColorRGBA.Orange);
            } else {
                speedText.setColor(ColorRGBA.White);

            }
        }
        String text = app.isPause ? "Paused" : app.speedList.getFormatedSpeed();
        speedText.setText(text);
        speedText.setLocalTranslation(app.getCamera().getWidth() / 2 - speedText.getLineWidth() / 2,
                positionYFlowButton + heightFlowButton + speedText.getLineHeight() - 15, 0);
    }

    public static void updateButtons() {
        updatables.forEach(Updatable::update);
    }

    public static void updateTextDate() {

        if (actualPauseTimeText && app.isPause) {
            return;
        }
        if (actualPauseTimeText != app.isPause) {
            if (app.isPause) {
                timeText.setColor(ColorRGBA.Orange);
            } else {
                timeText.setColor(ColorRGBA.White);

            }
        }
        Instant instant = Instant.ofEpochSecond((long) (app.time - app.startOfUniver));
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDate = dateTime.format(formatter);
        timeText.setText(formattedDate);
        timeText.setLocalTranslation(app.getCamera().getWidth() / 2 - timeText.getLineWidth() / 2,
                timeTextY + timeText.getLineHeight(), 0);
        actualPauseTimeText = app.isPause;
    }

    public static void update() {
        updateSpeedText();
        updateButtons();
        updateTextDate();

    }
}
