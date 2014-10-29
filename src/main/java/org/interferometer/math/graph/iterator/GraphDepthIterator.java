package org.interferometer.math.graph.iterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import edu.uci.ics.jung.graph.Graph;

/** Итератор для обхода дерева в глубину */
public class GraphDepthIterator<V, E> extends GraphInputIterator<V> {
    Graph<V, E> graph;
    
    class NodePathInfo {
        V node;
        Iterator<V> itr;
        
        public NodePathInfo(V node, Iterator<V> itr) {
            this.node = node;
            this.itr = itr;
        }
        public V getValue() {
            return node;
        }
        public V next() {
            return itr.next();
        }
    }    
    
    Stack<NodePathInfo> stack;
    Map<V, Boolean> labels;
        
    @Override
    public void init() {
        this.stack.clear();
        current = getBegin();
        for(V v: graph.getVertices())
            labels.put(v, false);
    }    
        
    public GraphDepthIterator(Graph<V, E> graph, V begin) {
        super(begin);
        this.graph = graph;
        this.stack = new Stack<NodePathInfo>();
        this.labels = new HashMap<V, Boolean>();
        init();
    }
        
    public int getDepth() {
        return stack.size();
    }
    
    public V getBack() {
        return stack.lastElement().getValue();
    }
    
    public boolean canForward() {
        Iterator<V> itr = graph.getSuccessors(current).iterator();
        while(itr.hasNext())
            if(!labels.get(itr.next()))
                return true;
        return false;
    }
    
    // TODO: подумать, как бы это поэффективнее сделать
    public boolean canRight() {
        if(getDepth() == 0)
            return false;
        Iterator<V> itr = graph.getSuccessors(getBack()).iterator();
        while(!itr.next().equals(current));
        while(itr.hasNext())
            if(!labels.get(itr.next()))
                return true;
        return false;
    }
    
    public void backward() {
        NodePathInfo info = stack.pop();
        current = info.getValue();
    }
    
    public void forward() {
        Iterator<V> itr = graph.getSuccessors(current).iterator();
        stack.push(new NodePathInfo(current, itr));
        do
            current = itr.next();
        while(labels.get(current));
        labels.put(current, true);
    }
    /** @param next вершина, следующая за current */
    public void forwardTo(V next) {
        Iterator<V> itr = graph.getSuccessors(current).iterator();
        stack.push(new NodePathInfo(current, itr));
        current = next;
        labels.put(current, true);
    }
        
    public void right() {
        do
            current = stack.lastElement().next();
        while(labels.get(current));
        labels.put(current, true);
    }
    
    public void advance() {
        prefixDepthAdvance();
    }
    
    public List<V> getPath() {
        ArrayList<V> result = new ArrayList<V>(stack.size() + 1);
        for(int i=0; i<stack.size(); ++i)
            result.add(i, stack.elementAt(i).getValue());
        result.add(current);
        return result;
    }

    /*@Override
    public void remove() {
        tree.removeVertex(current); // удаляется с поддеревом????????????????????
    }*/
}