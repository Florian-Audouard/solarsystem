package fr.univtln.faudouard595.solarsystem;

import com.jme3.app.SimpleApplication;
import com.jme3.util.SkyFactory;

public class App extends SimpleApplication {

    public static void main(String[] args) {
        App app = new App();
        app.start();
    }

    public App() {

    }

    @Override
    public void simpleInitApp() {
        getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/Sky/Bright/BrightSky.dds",
                SkyFactory.EnvMapType.CubeMap));
    }

}
