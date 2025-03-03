package fr.univtln.faudouard595.solarsystem.ui.loadingscreen;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;

public class LoadingAppState extends BaseAppState {
    private Node guiNode;
    private Picture loadingImage;

    @Override
    protected void initialize(Application app) {
        guiNode = ((com.jme3.app.SimpleApplication) app).getGuiNode();

        // Create a loading image
        loadingImage = new Picture("Loading Screen");
        loadingImage.setImage(app.getAssetManager(), "Image/loading.jpg", true);
        loadingImage.setWidth(app.getCamera().getWidth());
        loadingImage.setHeight(app.getCamera().getHeight());
        loadingImage.setPosition(0, 0);

        guiNode.attachChild(loadingImage);
    }

    @Override
    protected void cleanup(Application app) {
        guiNode.detachChild(loadingImage);
    }

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}
}
