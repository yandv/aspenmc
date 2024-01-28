package br.com.aspenmc.utils;

import java.util.HashMap;
import java.util.Map;

public class IndexableMap<K, V> extends HashMap<K, V> {

    private final Map<K, Integer> keyIndexMap;

    public IndexableMap() {
        super();
        this.keyIndexMap = new HashMap<>();
    }

    @Override
    public V put(K key, V value) {
        if (!keyIndexMap.containsKey(key)) {
            int index = keyIndexMap.size();

            while (keyIndexMap.containsValue(index)) index++;

            keyIndexMap.put(key, index);
        }
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        keyIndexMap.remove(key);
        return super.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        keyIndexMap.clear();
        super.clear();
    }

    public int indexOf(K key) {
        return keyIndexMap.getOrDefault(key, -1);
    }
}