package fr.univtln.faudouard595.solarsystem.ui.loadingscreen;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.material.Material;

@Slf4j
public class LoadingAppState extends BaseAppState {
    private Node guiNode;
    private Picture background;
    private Geometry progressBar;
    private int progress = 0;
    private int total = 100;
    private float maxWidth;
    private BitmapText loadingText;
    private BitmapFont font;
    @Getter
    private static LoadingAppState instance;


    public LoadingAppState(BitmapFont font){
        this.font = font;
    }


    public static void createInstance(BitmapFont font){
        instance = new LoadingAppState(font);
    }

    public static void updateProgress(){
        instance.updateProgressInstance();
    }


    @Override
    protected void initialize(Application app) {
        guiNode = ((com.jme3.app.SimpleApplication) app).getGuiNode();

        // Background Image (Optional)
        // background = new Picture("Loading Screen");
        // background.setImage(app.getAssetManager(), "Image/loading.jpg", true);
        // background.setWidth(app.getCamera().getWidth());
        // background.setHeight(app.getCamera().getHeight());
        // background.setPosition(0, 0);
        // guiNode.attachChild(background);

        // Progress Bar Background
        float barWidth = app.getCamera().getWidth() * 0.5f;
        float barHeight = 20f;
        float x = (app.getCamera().getWidth() - barWidth) / 2;
        float y = app.getCamera().getHeight() * 0.2f;

        Geometry progressBarBackground = createQuad(app, barWidth, barHeight, ColorRGBA.Gray);
        progressBarBackground.setLocalTranslation(x, y, 0);
        guiNode.attachChild(progressBarBackground);

        // Progress Bar (Red)
        progressBar = createQuad(app, 0, barHeight, ColorRGBA.Red);
        progressBar.setLocalTranslation(x, y, 1);
        guiNode.attachChild(progressBar);

        maxWidth = barWidth;

        // Loading Text
        loadingText = new BitmapText(font);
        loadingText.setSize(24);
        loadingText.setColor(ColorRGBA.White);
        loadingText.setText("Loading... 0%");
        loadingText.setLocalTranslation(x, y + 60, 2);
        guiNode.attachChild(loadingText);
    }

    @Override
    protected void cleanup(Application app) {
        guiNode.detachAllChildren();
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }


    public static void init(int total){
        instance.initInstance(total);
    }

    private void initInstance(int total){
        this.total = total;
        this.progress = 0;
    }

    private void updateProgressInstance() {

        float val = progress/ total;
        log.debug("{}",val);
        progressBar.setLocalScale(val * maxWidth, 1, 1);
        loadingText.setText("Loading... " + (int) (val * 100) + "%");
    }

    private Geometry createQuad(Application app, float width, float height, ColorRGBA color) {
        Quad quad = new Quad(width, height);
        Geometry geom = new Geometry("Quad", quad);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        return geom;
    }
}
