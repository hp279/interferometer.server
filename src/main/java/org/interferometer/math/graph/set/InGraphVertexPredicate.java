package org.interferometer.math.graph.set;

import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.graph.Graph;

class InGraphVertexPredicate<V, E> implements Predicate<V> {
    Graph<V, E> graph;
    
    public InGraphVertexPredicate(Graph<V, E> graph) {
        this.graph = graph;
    }

    @Override
    public boolean evaluate(V arg0) {
        return graph.containsVertex(arg0);
    }
    
}
