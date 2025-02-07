package fr.univtln.faudouard595.solarsystem.util.map;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import fr.univtln.faudouard595.solarsystem.Astre.Astre;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CircularHashMapAstre extends CircularHashMap<String, Astre> {

    public void createMapFromList(List<Astre> astres) {
        super.setMap(new LinkedHashMap<>());
        for (Astre astre : astres) {
            super.put(astre.getName(), astre);
        }
        super.setIterator(super.getKeys().listIterator(1));
        super.setCurrent(astres.get(0));
    }

    @Override
    public void setCurrentValue(Astre newValue) {
        keys = new ArrayList<>(map.keySet());
        int i = keys.indexOf(newValue.getName());
        iterator = keys.listIterator(i);
        if (iterator.hasNext()) { 
            iterator.next(); // Avance immédiatement pour éviter le double next()
        }
        current = newValue;
    }

}
