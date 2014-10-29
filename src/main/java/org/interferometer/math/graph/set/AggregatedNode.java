package org.interferometer.math.graph.set;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class AggregatedNode<V> {
    boolean is_set;
    V vertex;
    Set<V> vertices;
    
    AggregatedNode(Class<Set<?>> set_class) {
        is_set = true;
        try {
            this.vertices = (Set<V>)set_class.newInstance();            
        } catch(Exception e) {
            this.vertices = new HashSet<V>();
        }
    }
    
    public AggregatedNode(V vertex) {
        is_set = false;
        this.vertex = vertex;
    }
    
    public AggregatedNode(Set<V> vertices) {
        is_set = true;
        this.vertices = vertices;
    }
    
    public static<V> AggregatedNode<V> createUnion(Set<AggregatedNode<V>> vertices) {
        AggregatedNode<V> result = new AggregatedNode<V>((Class<Set<?>>)vertices.getClass());
        for(AggregatedNode<V> node: vertices)
            result.vertices.addAll(node.getVertices());
        return result;
    }
    
    public boolean isAggregated() {
        return is_set;
    }
    
    public Set<V> getVertices() {
        return is_set? vertices : Collections.singleton(vertex);
    }
    
    public boolean contains(V v) {
        return is_set? vertices.contains(v) : vertex.equals(v);
    }
    
    /** Возвращает множество "первичных" (не агрегированных) вершин */
    public static<V> Set<AggregatedNode<V>> getFirstSet(Set<V> set) {
        Set<AggregatedNode<V>> result;
        try {
            result = set.getClass().newInstance();            
        } catch(Exception e) {
            result = new HashSet<AggregatedNode<V>>();
        }
        for(V v: set)
            result.add(new AggregatedNode<V>(v));
        return result;
    }

}