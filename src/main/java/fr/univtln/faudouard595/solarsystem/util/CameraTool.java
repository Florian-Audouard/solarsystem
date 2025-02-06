package fr.univtln.faudouard595.solarsystem.util;

import java.util.List;

import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

import fr.univtln.faudouard595.solarsystem.Astre.Astre;
import fr.univtln.faudouard595.solarsystem.Astre.Planet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class CameraTool {
    private ChaseCamera chaseCam;
    private Astre astre;
    private List<Astre> astres;
    private int currentAstre = 0;
    private Camera cam;
    private boolean chaseCamActive;

    public CameraTool(Camera cam, Node rootNode, InputManager inputManager) {
        this.cam = cam;
        this.chaseCamActive = true;
        chaseCam = new ChaseCamera(cam, rootNode, inputManager);
        chaseCam.setSmoothMotion(true);
        chaseCam.setLookAtOffset(Vector3f.ZERO);
        chaseCam.setRotationSpeed(3f); // Vitesse de rotation
        chaseCam.setMaxVerticalRotation(Float.MAX_VALUE); // Pas de limite maximale

        chaseCam.setChasingSensitivity(10f);
        chaseCam.setTrailingEnabled(false);

        chaseCam.setUpVector(cam.getUp());
    }

    public void initCam() {
        float minDist = Math.max(astre.getScaleSize() * 2, 2f);
        chaseCam.setDefaultDistance(minDist * 3); // Distance initiale
        chaseCam.setMinDistance(0); // Distance minimale
        chaseCam.setMaxDistance(minDist * 100); // Distance maximale
        chaseCam.setMinDistance(astre.getScaleSize() * 10);
        chaseCam.setMaxDistance(astre.getScaleSize() * 10);
        chaseCam.setSpatial(astre.getNode());
        chaseCam.setZoomSensitivity(FastMath.pow(astre.getScaleSize(), 2f) * 1f);
    }

    public void setAstre(Astre astre) {
        this.astre = astre;
        // chaseCam.setMaxDistance(astre.getScaleSize() * 10000);

        astres = astre.getEveryAstres();
        initCam();

    }

    public void nextAstre() {
        if (astres == null) {
            return;
        }
        if (currentAstre >= astres.size() - 1) {
            currentAstre = 0;
        } else {
            currentAstre++;
        }
        astre = astres.get(currentAstre);
        initCam();
    }

    public void prevAstre() {
        if (astres == null) {
            return;
        }
        if (currentAstre <= 0) {
            currentAstre = astres.size() - 1;
        } else {
            currentAstre--;
        }
        astre = astres.get(currentAstre);
        initCam();
    }

    private void switchMode(boolean change) {
        if (change == chaseCamActive) {
            return;
        }
        chaseCamActive = change;
        chaseCam.setEnabled(chaseCamActive);

    }

    public void update(double time, float speed) {
        // switchMode(Math.max(speed, speed * -1) < 100);
        // if (astre instanceof Planet planet && Math.max(speed, speed * -1) > 100) {
        // cam.lookAt(planet.getPrimary().getModel().getWorldTranslation(),
        // Vector3f.UNIT_Y);
        // cam.setLocation(planet.getPrimary().getModel().getWorldTranslation().add(
        // planet.calcTrajectory(time + 00000, astre.getScaleSize() * 10))
        // .add(new Vector3f(0, planet.getScaleSize(), 0)));
        // log.info("primary : {}",
        // planet.getPrimary().getModel().getWorldTranslation());
        // log.info("earth : {}",
        // astres.get(0).getPlanets().get("Earth").getModel().getWorldTranslation());
        // }
        if (speed > 100) {
            if (astre instanceof Planet planet) {
                cam.lookAt(planet.getPrimary().getModel().getWorldTranslation(), Vector3f.UNIT_Y);

            }
        }
    }

}