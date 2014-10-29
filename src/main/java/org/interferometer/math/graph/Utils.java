package org.interferometer.math.graph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;

public class Utils {
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
    
    /** Путь из корня дерева в вершину */
    static public <V, E> List<E> getPathFromRoot(Tree<V, E> tree, V v) {    
        List<E> result = new LinkedList<E>();
        for(int d = tree.getDepth(v); d > 0; d--) {
                result.add(0, tree.getParentEdge(v));
                v = tree.getParent(v);
        }
        return result;
    }
    
    /** Ближайший общий предок */
    static public <V, E> V getNearestCommonPredecessor(Tree<V, E> tree, V v1, V v2) {
        int d1 = tree.getDepth(v1),
            d2 = tree.getDepth(v2);    
        if(d1 > d2)
            while(d1 > d2) {
                v1 = tree.getParent(v1);
                d1--;
            }
        else
            while(d2 > d1) {
                v2 = tree.getParent(v2);
                d2--;
            }
        while(!v1.equals(v2)) {
            v1 = tree.getParent(v1);
            v2 = tree.getParent(v2);
        }                
        return v1;
    }
    
    /** Путь (единственный), соединяющий 2 вершины в дереве */
    static public <V, E> List<E> getPath(Tree<V, E> tree, V v1, V v2) {
        int d1 = tree.getDepth(v1),
            d2 = tree.getDepth(v2);    
        List<E> list1 = new LinkedList<E>(),
                list2 = new LinkedList<E>();
        if(d1 > d2)
            while(d1 > d2) {
                list1.add(tree.getParentEdge(v1));
                v1 = tree.getParent(v1);
                d1--;
            }
        else
            while(d2 > d1) {
                list2.add(0, tree.getParentEdge(v2));
                v2 = tree.getParent(v2);
                d2--;
            }
        while(!v1.equals(v2)) {
            list1.add(tree.getParentEdge(v1));
            list2.add(0, tree.getParentEdge(v2));
            v1 = tree.getParent(v1);
            v2 = tree.getParent(v2);
        }                
        list1.addAll(list2);
        return list1;
    }
}