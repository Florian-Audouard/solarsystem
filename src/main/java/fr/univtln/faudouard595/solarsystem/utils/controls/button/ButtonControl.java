package fr.univtln.faudouard595.solarsystem.utils.controls.button;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;

public class ButtonControl {

    public static void init(SimpleApplication app) {
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        // Création d’un bouton
        Button myButton = new Button("Cliquez-moi");
        myButton.setLocalTranslation(new Vector3f(100, 300, 0));

        // Ajout d'une action au clic
        myButton.addClickCommands(source -> System.out.println("Bouton cliqué !"));

        // Ajout à l’interface
        app.getGuiNode().attachChild(myButton);
    }
}
