package fr.univtln.faudouard595.solarsystem.util.map;

import java.util.*;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Getter
public abstract class CircularHashMap<K, V> {
    protected LinkedHashMap<K, V> map = new LinkedHashMap<>();
    protected ListIterator<K> iterator;
    protected List<K> keys;
    protected V current;

    public void put(K key, V value) {

        if (!map.containsKey(key)) {
            map.put(key, value);
            resetIterator();
        } else {
            map.put(key, value); // Mise à jour de la valeur
        }
    }

    public abstract void createMapFromList(List<V> list);

    private void resetIterator() {
        iterator = new ArrayList<>(map.keySet()).listIterator();
        keys = new ArrayList<>(map.keySet());
    }

    public Collection<V> getValues() {
        return map.values();
    }

    public void forEach(Consumer<? super V> consumer) {
        getValues().forEach(consumer);
    }

    public Map.Entry<K, V> next() {
        if (!iterator.hasNext())
            resetIterator(); // Si fin, recommencer au début
        K key = iterator.next();
        return new AbstractMap.SimpleEntry<>(key, map.get(key));
    }

    public Map.Entry<K, V> prev() {
        if (!iterator.hasPrevious()) { // Si début, repositionner à la fin
            keys = new ArrayList<>(map.keySet());
            iterator = keys.listIterator(keys.size()); // Positionné après le dernier
        }
        K key = iterator.previous();
        return new AbstractMap.SimpleEntry<>(key, map.get(key));
    }

    public V nextValue() {
        current = this.next().getValue();
        log.info("{}", current);
        return current;
    }

    public V prevValue() {
        current = this.prev().getValue();
        return current;
    }

    public V getCurrentValue() {
        return current;
    }

    public abstract void setCurrentValue(V newValue);

    public K nextKey() {
        return this.next().getKey();
    }

    public K prevKey() {
        return this.prev().getKey();
    }

}
