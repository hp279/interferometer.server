package org.interferometer.math.graph;

import java.util.Iterator;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class Utils {
    /** Зная одну вершину, находим вторую */
    static public <V, E> V getAnotherVertex(Graph<V, E> graph, E edge, V vertex1) {
        Pair<V> endpoints = graph.getEndpoints(edge);
        return (endpoints.getFirst().equals(vertex1)) ? endpoints.getSecond()
                : endpoints.getFirst();
    }

    static public <V, E> E getAnyIncidentEdge(Graph<V, E> graph, V vertex) {
        Iterator<E> itr = graph.getIncidentEdges(vertex).iterator();
        return itr.next();
    }

    /** Для вершин степени 2: зная одну инцидентную дугу, находим вторую */
    static public <V, E> E getAnotherIncidentEdge(Graph<V, E> graph, V vertex,
            E edge1) {
        Iterator<E> itr = graph.getIncidentEdges(vertex).iterator();
        E edge2 = itr.next();
        if (edge2 == edge1)
            edge2 = itr.next();
        return edge2;
    }

    static public <V, E> Graph<V, E> copy(Graph<V, E> src, Graph<V, E> dest) {
        for (V v : src.getVertices())
            dest.addVertex(v);
        for (E e : src.getEdges()) {
            Pair<V> vertices = src.getEndpoints(e);
            dest.addEdge(e, vertices.getFirst(), vertices.getSecond());
        }
        return dest;
    }

    static public <V, E> Graph<V, E> setUnion(Graph<V, E> src, Graph<V, E> dest) {
        copy(src, dest);
        return dest;
    }

    static public <V, E> Graph<V, E> setIntersection(Graph<V, E> src,
            Graph<V, E> dest) {
        for (E e : dest.getEdges())
            if (!src.containsEdge(e))
                dest.removeEdge(e);
        for (V v : dest.getVertices())
            if (!src.containsVertex(v))
                dest.removeVertex(v);
        return dest;
    }

    static public <V, E> Graph<V, E> setDifference(Graph<V, E> src,
            Graph<V, E> dest) {
        for (E e : dest.getEdges())
            if (src.containsEdge(e))
                dest.removeEdge(e);
        for (V v : dest.getVertices())
            if (src.containsVertex(v))
                dest.removeVertex(v);
        return dest;
    }
}