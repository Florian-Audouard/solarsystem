package fr.univtln.faudouard595.solarsystem.utils.controls.trigger;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;

import fr.univtln.faudouard595.solarsystem.App;
import fr.univtln.faudouard595.solarsystem.utils.controls.camera.CameraTool;

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
        inputManager.addMapping("ScaleUp", new KeyTrigger(KeyInput.KEY_F3));
        inputManager.addMapping("ScaleDown", new KeyTrigger(KeyInput.KEY_F4));
        inputManager.addMapping("DistanceUp", new KeyTrigger(KeyInput.KEY_F5));
        inputManager.addMapping("DistanceDown", new KeyTrigger(KeyInput.KEY_F6));
        inputManager.addMapping("Test", new KeyTrigger(KeyInput.KEY_F7));
        inputManager.addMapping("nextAstre", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("prevAstre", new KeyTrigger(KeyInput.KEY_LEFT));

        inputManager.addMapping("removeLines", new KeyTrigger(KeyInput.KEY_F8));

        // inputManager.addMapping("lockPlanet", new KeyTrigger(KeyInput.KEY_F9));

        inputManager.addListener(actionListener, "Test", "removeLines", "nextAstre", "prevAstre");
        inputManager.addListener(analogListener, "SpeedUp", "SpeedDown", "ScaleUp", "ScaleDown",
                "DistanceUp", "DistanceDown");
    }

    final static private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {

            if (name.equals("ScaleUp")) {
                app.getSun().scalePlanets(1.01f);
                // app.getSun().scale(1.01f);
            }
            if (name.equals("ScaleDown")) {
                app.getSun().scalePlanets(0.99f);
                // app.getSun().scale(0.99f);
            }
            if (name.equals("DistanceUp")) {
                app.getSun().changeDistancePlanets(1.01f);
            }
            if (name.equals("DistanceDown")) {
                app.getSun().changeDistancePlanets(0.99f);
            }
            if (name.equals("SpeedUp")) {
                app.calcSpeed(-1);
            }
            if (name.equals("SpeedDown")) {
                app.calcSpeed(1);
            }

        }
    };

    final static private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {

            if (name.equals("Test")) {
                // Random rand = new Random();
                // app.getSun().getNode().move(rand.nextFloat(app.getSun()Size * 2) -
                // app.getSun()Size,
                // rand.nextFloat(app.getSun()Size * 2) - app.getSun()Size,
                // rand.nextFloat(app.getSun()Size * 2) - app.getSun()Size);
                ;
            }
            if (keyPressed && name.equals("removeLines")) {
                app.getSun().switchDisplayLines();
            }
            if (keyPressed && name.equals("nextAstre")) {
                CameraTool.nextBody();
            }
            if (keyPressed && name.equals("prevAstre")) {
                CameraTool.prevBody();
            }
        }
    };

}
