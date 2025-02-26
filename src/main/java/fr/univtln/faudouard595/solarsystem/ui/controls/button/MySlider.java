package fr.univtln.faudouard595.solarsystem.ui.controls.button;

import com.jme3.math.Vector3f;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.Slider;

import fr.univtln.faudouard595.solarsystem.App;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
public class MySlider implements Updatable {
    @Builder.Default
    private Slider slider = new Slider(Axis.X);
    public static App app;

    public MySlider init() {
        slider.setPreferredSize(new Vector3f(200, 20, 0));
        slider.setLocalTranslation(50, 50, 0);
        DefaultRangedValueModel model = new DefaultRangedValueModel(0, 100, 50);
        slider.setModel(model);
        app.getGuiNode().attachChild(slider);
        return this;
    }

    public void update() {
        log.info("Slider value: " + slider.getModel().getValue());
    }
}
