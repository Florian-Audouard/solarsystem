package fr.univtln.faudouard595.solarsystem.util;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import fr.univtln.faudouard595.solarsystem.Astre.Astre;
import fr.univtln.faudouard595.solarsystem.Astre.Planet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class CameraTool {
    private Camera cam;
    private Planet planet;

    public CameraTool(Camera cam) {
        this.cam = cam;
    }

    public void update(double time) {
        if (planet == null) {
            return;
        }
        Vector3f positionCam = planet.calcTrajectory(time + 00000, -planet.getSize() * 5);
        cam.setLocation(positionCam);
        Vector3f positionAstre = planet.getModel().getWorldTranslation();
        // Vector3f positionPrimary =
        // planet.getPrimary().getModel().getWorldTranslation();
        // Vector3f add = new Vector3f(70, 0, 0);
        // positionPrimary = positionPrimary.add(add);

        cam.lookAt(positionAstre, Vector3f.UNIT_Y);

    }

}