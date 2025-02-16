package fr.univtln.faudouard595.solarsystem.utils.api;

import com.jme3.math.ColorRGBA;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class DataCreationNeeded {
    private String name;
    private ColorRGBA color;
    private int numberOfMoons;
    private boolean ring;

    public DataCreationNeeded(String name, ColorRGBA color, int numberOfMoons) {
        this(name, color, numberOfMoons, false);
    }

}
