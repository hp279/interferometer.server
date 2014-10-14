package org.interferometer.math.graph;

import java.util.Iterator;
import edu.uci.ics.jung.graph.Graph;

public class Utils {    
    static public <V, E> E getAnyIncidentEdge(Graph<V, E> graph, V vertex) {
        Iterator<E> itr = graph.getIncidentEdges(vertex).iterator();
        return itr.next();
    }
    
    /** Для вершин степени 2: зная одну инцидентную дугу, находим вторую */
    static public <V, E> E getAnotherIncidentEdge(Graph<V, E> graph, V vertex, E edge1) {
        Iterator<E> itr = graph.getIncidentEdges(vertex).iterator();
        E edge2 = itr.next();
        if(edge2 == edge1)
            edge2 = itr.next();
        return edge2;
    }
}