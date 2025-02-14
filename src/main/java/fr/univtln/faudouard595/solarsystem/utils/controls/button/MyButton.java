package fr.univtln.faudouard595.solarsystem.utils.controls.button;

import java.util.function.Function;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.event.MouseListener;

import fr.univtln.faudouard595.solarsystem.App;
import fr.univtln.faudouard595.solarsystem.utils.controls.camera.CameraTool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@AllArgsConstructor
public class MyButton {
    private Button button;
    private String text;
    private Node guiNode;
    private float x;
    private float y;
    @Builder.Default
    private float preferedSizeWidth = -1;
    @Builder.Default
    private float preferedSizeHeight = -1;
    @Builder.Default
    private ColorRGBA defaultColor = ColorRGBA.White;
    @Builder.Default
    private ColorRGBA hoverColor = ColorRGBA.Gray;
    @Builder.Default
    private ColorRGBA clickColor = ColorRGBA.Blue;
    @Builder.Default
    private Command<Button> actionFunction = b -> {
    };
    @Builder.Default
    private Runnable analogFunction = () -> {
    };
    @Builder.Default
    private int fontSize = 20;

    @Builder.Default
    private int analogSpeed = 6;

    @Builder.Default
    private int currentSpeed = 0;

    public static App app;

    public MyButton init() {
        button = new Button(text);
        Vector3f prefSize = button.getPreferredSize();
        float width = preferedSizeWidth == -1 ? prefSize.x : preferedSizeWidth;
        float height = preferedSizeHeight == -1 ? prefSize.y : preferedSizeHeight;
        button.setFont(app.font);
        button.setFontSize(fontSize); // Set the text size to 20
        button.setPreferredSize(new Vector3f(width, height, prefSize.z));
        button.setTextHAlignment(HAlignment.Center);
        button.setTextVAlignment(VAlignment.Center);
        button.setColor(defaultColor);
        button.addClickCommands(actionFunction);
        button.setLocalTranslation(x - (width / 2), y + height / 2, 0);

        MouseEventControl.addListenersToSpatial(button, new MouseListener() {
            @Override
            public void mouseButtonEvent(MouseButtonEvent event, Spatial target, Spatial capture) {
                if (event.isPressed()) {
                    button.setColor(clickColor);
                    currentSpeed = analogSpeed;
                } else {
                    button.setColor(hoverColor);
                }
            }

            @Override
            public void mouseEntered(MouseMotionEvent event, Spatial target, Spatial capture) {
                CameraTool.cursorSaveButton = true;
                button.setColor(hoverColor);
            }

            @Override
            public void mouseExited(MouseMotionEvent event, Spatial target, Spatial capture) {
                CameraTool.cursorSaveButton = false;
                button.setColor(defaultColor);
            }

            @Override
            public void mouseMoved(MouseMotionEvent event, Spatial target, Spatial capture) {
            }
        });
        guiNode.attachChild(button);
        return this;
    }

    public void setActionFunction(Command<Button> command) {
        button.addClickCommands(command);
    }

    public void setText(String text) {
        button.setText(text);
    }

    public Vector3f getSize() {
        return button.getPreferredSize();
    }

    public void update() {
        if (button.isPressed()) {
            if (currentSpeed >= analogSpeed) {
                currentSpeed = 0;
                analogFunction.run();
            }
            currentSpeed++;
        }
    }

}
