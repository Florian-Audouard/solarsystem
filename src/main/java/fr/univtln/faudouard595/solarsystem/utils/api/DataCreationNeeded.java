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
    String name;
    ColorRGBA color;

}
