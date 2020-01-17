package ru.nsu.kokunin.networks.task5.utils;

import java.util.TreeMap;

//number of connections
public class TreeMapLimited<K extends Comparable<K>,V> extends TreeMap<K, V> {
    //    private TreeMap<K,V> map  = new TreeMap<>();
    private int capacity;

    public TreeMapLimited(int capacity) {
        super();
        this.capacity = capacity;
    }

    @Override
    public V get(Object key){
        System.out.println("GET FROM CACHE: " + key);
        return super.get(key);
    }

    @Override
    public V put(K key, V value) {
        if(size() >= capacity) {
            remove(firstKey());
        }

        super.put(key, value);
        return value;
    }
}

