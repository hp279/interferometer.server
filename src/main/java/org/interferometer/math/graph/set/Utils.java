package org.interferometer.math.graph.set;

import java.util.Collection;
import java.util.HashSet;

import org.interferometer.math.graph.GraphFactory;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/** Теоретико-множественные операции над графом.
 * Граф - это пара (множество вершин, множество дуг)*/
public class Utils {      
    /** Создаёт пустой граф с заданными вершинами */
    static public <V, E> Graph<V, E> makeVoid(Collection<V> vertices, GraphFactory<V, E> maker) {
        Graph<V, E> result = maker.create();
        for(V v: vertices)
            result.addVertex(v);
        return result;
    }
    
    /** Создаёт пустой граф с теми же вершинами */
    static public <V, E> Graph<V, E> copyVoid(Graph<V, E> src, GraphFactory<V, E> maker) {
        Graph<V, E> result = maker.create(src);
        return copyVoid(src, result);
    }
    
    static public <V, E> Graph<V, E> copyVoid(Graph<V, E> src, Graph<V, E> result) {
        for(V v: src.getVertices())
            result.addVertex(v);
        return result;
    }

    static public <V, E> Graph<V, E> copy(Graph<V, E> src, GraphFactory<V, E> maker) {
        Graph<V, E> result = copyVoid(src, maker);
        for(E e: src.getEdges()) {
            Pair<V> vertices = src.getEndpoints(e);
            result.addEdge(e, vertices.getFirst(), vertices.getSecond());
        }            
        return result;
    }
    
    static public <V, E> Graph<V, E> getSubGraph(Graph<V, E> graph, Collection<V> vertices,
                                     GraphFactory<V, E> maker) {
        Graph<V, E> result = makeVoid(vertices, maker);
        for(V vertex: vertices) {
            Collection<E> neighbors = graph.getOutEdges(vertex);
            for(E edge: neighbors) {
                V vertex2 = graph.getOpposite(vertex, edge);
                if(vertices.contains(vertex2))
                    result.addEdge(edge, vertex, vertex2);
            }
        }
        return result;
    }
    
    static public <V, E> Graph<V, E> setUnion(Graph<V, E> src, Graph<V, E> dest) {
        copyVoid(src, dest);
        setEdgeUnion(src, dest);
        return dest;
    }
    
    static public <V, E> Graph<V, E> setIntersection(Graph<V, E> src, Graph<V, E> dest) {
        Collection<V> difference = new HashSet<V>();
        difference.addAll(dest.getVertices());
        difference.removeAll(src.getVertices());
        for(V v: difference)
            dest.removeVertex(v);
        setEdgeIntersection(src, dest);
        return dest;
    }
    
    static public <V, E> Graph<V, E> setDifference(Graph<V, E> src, Graph<V, E> dest) {
        for(V v: src.getVertices())
            if(dest.containsVertex(v))
                dest.removeVertex(v);
        return dest;
    }
    
    static public <V, E> Graph<V, E> setEdgeUnion(Graph<V, E> src, Graph<V, E> dest) {
        for(E e: src.getEdges()) {
            Pair<V> vertices = src.getEndpoints(e);
            dest.addEdge(e, vertices.getFirst(), vertices.getSecond());
        } 
        return dest;
    }
    
    static public <V, E> Graph<V, E> setEdgeIntersection(Graph<V, E> src, Graph<V, E> dest) {
        Collection<E> difference = new HashSet<E>();
        difference.addAll(dest.getEdges());
        difference.removeAll(src.getEdges());
        for(E e: difference)
            dest.removeEdge(e);
        return dest;
    }
    
    static public <V, E> Graph<V, E> setEdgeDifference(Graph<V, E> src, Graph<V, E> dest) {
        for(E e: src.getEdges()) 
            if(dest.containsEdge(e))
                dest.removeEdge(e);
        return dest;
    }
    
    static public <V, E> Graph<V, E> createUnion(Graph<V, E> graph1, Graph<V, E> graph2, GraphFactory<V, E> factory) {
        Graph<V, E> result = copy(graph1, factory);
        setUnion(graph2, result);
        return result;
    }
    
    static public <V, E> Graph<V, E> createIntersection(Graph<V, E> graph1, Graph<V, E> graph2, GraphFactory<V, E> factory) {
        Graph<V, E> result = copy(graph1, factory);
        setIntersection(graph2, result);
        return result;
    }
    
    static public <V, E> Graph<V, E> createDifference(Graph<V, E> graph1, Graph<V, E> graph2, GraphFactory<V, E> factory) {
        Graph<V, E> result = copy(graph1, factory);
        setDifference(graph2, result);
        return result;
    }
}