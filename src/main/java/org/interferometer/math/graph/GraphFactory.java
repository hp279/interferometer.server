package org.interferometer.math.graph;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.Graph;

/** Создание графов определённого типа */
public interface GraphFactory<V, E> extends Factory<Graph<V, E>>
{
    public Graph<V, E> create(Graph<V, E> pattern);
    
    public static interface EdgeFactory<V, E> {
        public E create(V v1, V v2);
        public E create(E pattern);
    }
    
    public static class GraphEdgeFactory<V, E> implements EdgeFactory<V, E> {
        Graph<V, E> graph;
        
        public GraphEdgeFactory(Graph<V, E> graph) {
            this.graph = graph;
        }
        
        @Override
        public E create(V v1, V v2) {
            return graph.findEdge(v1, v2);
        }

        @Override
        public E create(E pattern) {
            return graph.containsEdge(pattern)? pattern : null;
        }
        
    }
}