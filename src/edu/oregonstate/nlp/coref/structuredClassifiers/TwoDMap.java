package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.util.HashMap;

public  class TwoDMap<K,V> {
  private HashMap<K,HashMap<K,V>> map;
  public TwoDMap(){
    map = new HashMap<K, HashMap<K,V>>();
  }
  V put(K key1, K key2, V value){
    HashMap<K,V> inner = map.get(key1);
    if(inner==null){
      inner=new HashMap<K, V>();
      map.put(key1, inner);
    }
    V result = inner.put(key2,value);
    return result;
  }
  public V get(K key1, K key2){
    HashMap<K,V> inner = map.get(key1);
    if(inner==null){
      return null;
    }
    return inner.get(key2);
  }
  public HashMap<K,V> getAll(K key1){
    return map.get(key1);
  }
}
