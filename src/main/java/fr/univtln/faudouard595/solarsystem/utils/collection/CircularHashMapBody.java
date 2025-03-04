package fr.univtln.faudouard595.solarsystem.utils.collection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import fr.univtln.faudouard595.solarsystem.space.Body;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Getter
public class CircularHashMapBody extends CircularHashMap<String, Body> {

    private List<Body> mainBodies;

    public void createMapFromList(List<Body> bodies) {
        super.setMap(new LinkedHashMap<>());
        for (Body body : bodies) {
            super.put(body.getName(), body);
        }
        super.setIterator(super.getKeys().listIterator(1));
        super.setCurrent(bodies.get(0));
    }

    @Override
    public void setCurrentValue(Body newValue) {
        keys = new ArrayList<>(map.keySet());
        int i = keys.indexOf(newValue.getName());
        iterator = keys.listIterator(i);
        if (iterator.hasNext()) {
            iterator.next(); // Avance immédiatement pour éviter le double next()
        }
        current = newValue;
    }

    public Body nextMainBody() {
        Body saveBody = current;
        while (!mainBodies.contains(current) || current.equals(saveBody)) {
            nextValue();
        }
        return current;
    }

    public Body previousMainBody() {
        Body saveBody = current;
        while (!mainBodies.contains(current) || current.equals(saveBody)) {
            prevValue();
        }
        return current;
    }

    public Body previousBody() {
        Body saveBody = current;
        while (current.equals(saveBody)) {
            prevValue();
        }
        return current;
    }

    public Body nextBody() {
        Body saveBody = current;
        while (current.equals(saveBody)) {
            nextValue();
        }
        return current;
    }

}
