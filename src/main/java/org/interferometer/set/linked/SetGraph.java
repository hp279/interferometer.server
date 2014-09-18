package org.interferometer.set.linked;

import java.util.Collection;

import org.interferometer.linear.Vector2;
import org.interferometer.set.FlatSet;

import edu.uci.ics.jung.graph.SparseMultigraph;

public class SetGraph<S extends FlatSet> extends SparseMultigraph<S, Vector2>
                                         implements SetPathFinder<S>
{
    SetPathFinder<S> finder;
    
    public SetGraph(SetPathFinder<S> finder)
    {
        this.finder = finder;
    }

    @Override
    public Vector2 getPoint(S set) {
        Collection<S> vertices = this.getVertices();
        if(vertices.isEmpty())
            return null;
        else
        {
            S set1 = vertices.iterator().next();
            return finder.getPoint(set1);
        }
    }

    @Override
    public void bypass(S set, Vector2 begin) {
        // TODO Auto-generated method stub
        // методом обхода в глубину
    }
    /**
     * 
     */
    private static final long serialVersionUID = 1L;	
}