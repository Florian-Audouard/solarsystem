package fr.univtln.faudouard595.solarsystem.ui.controls.trigger;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;

import fr.univtln.faudouard595.solarsystem.App;
import fr.univtln.faudouard595.solarsystem.ui.controls.camera.CameraTool;
import fr.univtln.faudouard595.solarsystem.ui.controls.updatable.ClassicUpdatable;
import fr.univtln.faudouard595.solarsystem.ui.controls.updatable.Updatable;
import fr.univtln.faudouard595.solarsystem.ui.information.DisplayInformation;

public class TriggerControls {
    private static App app;
    private static InputManager inputManager;
    private static Updatable speedUpdatable;
    private static Updatable speedDownUpdatable;

    public static void init(App app) {
        TriggerControls.app = app;
        inputManager = app.getInputManager();
        initKeys();
    }

    private static void initKeys() {
        inputManager.addMapping("SpeedDown", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addMapping("SpeedUp", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addMapping("SwitchCam", new KeyTrigger(KeyInput.KEY_F3));
        inputManager.addMapping("RemoveLines", new KeyTrigger(KeyInput.KEY_F4));
        inputManager.addMapping("Help", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Information", new KeyTrigger(KeyInput.KEY_I));

        inputManager.addMapping("", new KeyTrigger(KeyInput.KEY_F6));

        inputManager.addMapping("NextAstre", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("PrevAstre", new KeyTrigger(KeyInput.KEY_LEFT));

        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Sprint", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("UltraSprint", new KeyTrigger(KeyInput.KEY_LCONTROL));

        inputManager.addListener(actionListener, "SwitchCam", "RemoveLines", "NextAstre", "PrevAstre", "Pause",
                "SpeedUp", "SpeedDown", "Sprint", "UltraSprint", "Help", "Information");
        inputManager.addListener(analogListener, "");
        speedDownUpdatable = ClassicUpdatable.builder()
                .actionFunction(() -> app.speedList.decreaseSpeed())
                .isAnalog(true)
                .build();
        speedUpdatable = ClassicUpdatable.builder()
                .actionFunction(() -> app.speedList.increaseSpeed())
                .isAnalog(true)
                .build();
    }

    final static private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {

        }
    };

    final static private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Help") && keyPressed) {
                DisplayInformation.switchHelpInformation();
            }
            if (name.equals("Information") && keyPressed) {
                DisplayInformation.switchDisplayInformation();
            }
            if (name.equals("SpeedUp")) {
                speedUpdatable.activateByBoolean(keyPressed);
            }
            if (name.equals("SpeedDown")) {
                speedDownUpdatable.activateByBoolean(keyPressed);
            }
            if (name.equals("SwitchCam") && keyPressed) {
                app.changeCam = true;
            }
            if (keyPressed && name.equals("RemoveLines")) {
                app.getSun().switchDisplayLines();
            }
            if (keyPressed && name.equals("NextAstre")) {
                CameraTool.nextMainBody();
            }
            if (keyPressed && name.equals("PrevAstre")) {
                CameraTool.prevMainBody();
            }
            if (keyPressed && name.equals("Pause")) {
                app.isPause = !app.isPause;
            }
            if (name.equals("Sprint")) {
                if (keyPressed) {
                    app.getFlyByCamera().setMoveSpeed(5_000);

                } else {
                    app.getFlyByCamera().setMoveSpeed(10);

                }
            }

            if (name.equals("UltraSprint")) {
                if (keyPressed) {
                    app.getFlyByCamera().setMoveSpeed(500_000);

                } else {
                    app.getFlyByCamera().setMoveSpeed(10);

                }
            }
        }
    };

    public static void update() {

    }

}
