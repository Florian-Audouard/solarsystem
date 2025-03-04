package fr.univtln.faudouard595.solarsystem.ui.controls.trigger;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;

import fr.univtln.faudouard595.solarsystem.App;
import fr.univtln.faudouard595.solarsystem.ui.controls.camera.CameraTool;

public class TriggerControls {
    private static App app;
    private static InputManager inputManager;

    public static void init(App app) {
        TriggerControls.app = app;
        inputManager = app.getInputManager();
        initKeys();
    }

    private static void initKeys() {
        inputManager.addMapping("SpeedUp", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addMapping("SpeedDown", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addMapping("SwitchCam", new KeyTrigger(KeyInput.KEY_F7));
        inputManager.addMapping("nextAstre", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("prevAstre", new KeyTrigger(KeyInput.KEY_LEFT));

        inputManager.addMapping("removeLines", new KeyTrigger(KeyInput.KEY_F8));

        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Sprint", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("UltraSprint", new KeyTrigger(KeyInput.KEY_LCONTROL));

        inputManager.addListener(actionListener, "SwitchCam", "removeLines", "nextAstre", "prevAstre", "Pause",
                "SpeedUp",
                "SpeedDown", "Sprint", "UltraSprint");
        inputManager.addListener(analogListener, "");
    }

    final static private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {

        }
    };

    final static private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("SpeedUp")) {
                app.speedList.increaseSpeed();
            }
            if (name.equals("SpeedDown")) {
                app.speedList.decreaseSpeed();
            }
            if (name.equals("SwitchCam") && keyPressed) {
                app.changeCam = true;
            }
            if (keyPressed && name.equals("removeLines")) {
                app.getSun().switchDisplayLines();
            }
            if (keyPressed && name.equals("nextAstre")) {
                CameraTool.nextMainBody();
            }
            if (keyPressed && name.equals("prevAstre")) {
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

}
