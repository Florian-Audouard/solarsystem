package fr.univtln.faudouard595.solarsystem.util;

import java.util.List;

import com.jme3.input.ChaseCamera;
import com.jme3.math.FastMath;

import fr.univtln.faudouard595.solarsystem.Astre.Astre;
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

    public CameraTool(ChaseCamera chaseCam) {
        this.chaseCam = chaseCam;
    }

    public void initCam() {
        float minDist = Math.max(astre.getScaleSize() * 2, 2f);
        chaseCam.setDefaultDistance(minDist * 3); // Distance initiale
        chaseCam.setMinDistance(0); // Distance minimale
        chaseCam.setSpatial(astre.getNode());
        chaseCam.setZoomSensitivity(FastMath.pow(astre.getScaleSize(), 2f) * 1f);
    }

    public void setAstre(Astre astre) {
        this.astre = astre;
        chaseCam.setMaxDistance(astre.getScaleSize() * 10000);

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

    public void update(float speed) {
        // chaseCam.setChasingSensitivity(speed * 10);
        // chaseCam.setTrailingRotationInertia(speed * 10);
    }

}