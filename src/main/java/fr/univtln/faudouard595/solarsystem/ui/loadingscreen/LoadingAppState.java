package fr.univtln.faudouard595.solarsystem.ui.loadingscreen;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import com.simsilica.lemur.ProgressBar;
import com.simsilica.lemur.component.QuadBackgroundComponent;

import fr.univtln.faudouard595.solarsystem.App;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.jme3.math.ColorRGBA;

@Slf4j
public class LoadingAppState {
    private String backgroundPath = "Interface/Background_Loading.jpg";
    // private String backgroundPath = "Textures/Sky/StarSky.jpg";
    private Node guiNode;
    private Picture background;
    private ProgressBar progressBar;
    private int progress = 0;
    private int total = 100;
    private BitmapText loadingText;
    private BitmapFont font;
    @Getter
    private static LoadingAppState instance;

    public LoadingAppState(BitmapFont font) {
        this.font = font;
    }

    public static void createInstance(BitmapFont font) {
        instance = new LoadingAppState(font);
    }

    public static void updateProgress(String text) {
        instance.updateProgressInstance(text);
    }

    public static void initialize(App app) {
        instance.initializeInstance(app);
    }

    public static void initBackground(App app) {
        instance.initBackgroundInstance(app);
    }

    public static void initBar(App app) {
        instance.initBarInstance(app);
    }

    private void initBackgroundInstance(App app) {

        background = new Picture("Loading Screen");
        background.setImage(app.getAssetManager(), backgroundPath, false);
        background.setWidth(app.getCamera().getWidth());
        background.setHeight(app.getCamera().getHeight());
        background.setPosition(0, 0);
        guiNode.attachChild(background);

    }

    private void initBarInstance(App app) {
        float barWidth = app.getCamera().getWidth() * 0.5f;
        float barHeight = 20f;
        float x = (app.getCamera().getWidth() - barWidth) / 2;
        float y = app.getCamera().getHeight() * 0.1f;
        progressBar = new ProgressBar();
        progressBar.setPreferredSize(new com.jme3.math.Vector3f(barWidth, barHeight, 1));
        progressBar.getValueIndicator().setBackground(new QuadBackgroundComponent(new ColorRGBA(0.2f, 0.5f, 0.9f, 1f)));
        progressBar.setProgressValue(0.0f);
        progressBar.setLocalTranslation(x, y, 0);
        guiNode.attachChild(progressBar);

        // Loading Text
        int fontSize = 30;
        loadingText = new BitmapText(font);
        loadingText.setSize(fontSize);
        loadingText.setColor(ColorRGBA.White);
        loadingText.setText("Loading... 0%");
        loadingText.setLocalTranslation(x, y + fontSize + 5, 0);
        guiNode.attachChild(loadingText);
    }

    private void initializeInstance(App app) {
        guiNode = app.getLoadingNode();

    }

    public static void cleanUp() {
        instance.cleanupInstance();
    }

    protected void cleanupInstance() {
        guiNode.detachAllChildren();
    }

    public static void init(int total) {
        instance.initInstance(total);
    }

    private void initInstance(int total) {
        this.total = total;
        this.progress = -1;
    }

    private void updateProgressInstance(String text) {
        progress++;
        float val = ((float) Math.round((float) (progress) / total * 1000)) / 10;
        progressBar.setProgressValue(val);
        text = text.replaceFirst("%s", val + "%");
        loadingText.setText(text);
    }
}
