package fr.univtln.faudouard595.solarsystem.utils.convertionObject;

import java.io.File;
import java.util.Map;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.AmbientLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;

import fr.univtln.faudouard595.solarsystem.utils.file.MyLoadFile;

public class ConvertAsteroid extends SimpleApplication {

    public static void main(String[] args) {
        ConvertAsteroid app = new ConvertAsteroid();
        AppSettings settings = new AppSettings(true);

        settings.setFrameRate(60);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        boolean test = false;
        boolean override = false;
        flyCam.setMoveSpeed(10);
        for (int i = 1; i < 11; i++) {
            String name = "Asteroid";
            String path = "Models/Asteroid/Asteroid" + i + "/" + name;
            Spatial model = Convert.convert(path, assetManager, rootNode, override, test, i);

            if (override || !test && MyLoadFile.fileExists(path + ".j3o")) {
                try {
                    BinaryExporter.getInstance().save(model, new File("src/main/resources/" + path + ".j3o"));
                } catch (Exception e) {
                }
            }
        }

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.1f));
        rootNode.addLight(al);
        PointLight pl = new PointLight();
        pl.setPosition(new Vector3f(0, 0, 0));
        pl.setColor(ColorRGBA.White);
        rootNode.addLight(pl);
        // cam.lookAt(model.getWorldTranslation(), Vector3f.UNIT_Y);

    }

    @Override
    public void simpleUpdate(float tpf) {
    }
}
