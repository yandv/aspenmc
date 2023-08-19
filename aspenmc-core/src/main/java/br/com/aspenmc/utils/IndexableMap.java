package br.com.aspenmc.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class IndexableMap<K, V> extends HashMap<K, V> {

    private final Map<K, Integer> keyIndexMap;

    public IndexableMap() {
        super();
        this.keyIndexMap = new HashMap<>();
    }

    public static void main(String[] args) {
        IndexableMap<String, String> map = new IndexableMap<>();

        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");

        System.out.println(map.indexOf("1"));
        System.out.println(map.indexOf("2"));
        System.out.println(map.indexOf("3"));
        System.out.println(map.indexOf("4"));

        map.remove("2");

        System.out.println(map.indexOf("3"));
        System.out.println(map.indexOf("1"));
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