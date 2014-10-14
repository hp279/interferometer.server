package org.interferometer.math.graph.set;

import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.graph.Graph;

class InGraphEdgePredicate<V, E> implements Predicate<E> {
    Graph<V, E> graph;
    
    public InGraphEdgePredicate(Graph<V, E> graph) {
        this.graph = graph;
    }

    @Override
    public boolean evaluate(E arg0) {
        return graph.containsEdge(arg0);
    }
    
}