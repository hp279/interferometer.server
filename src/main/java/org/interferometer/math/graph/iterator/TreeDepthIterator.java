package org.interferometer.math.graph.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import edu.uci.ics.jung.graph.Tree;

/** Итератор для обхода дерева в глубину */
public class TreeDepthIterator<V, E> extends GraphInputIterator<V> {
    Tree<V, E> tree;
    
    public enum Type {
        Prefix,
        Postfix
    }
    Type type;
    
    Stack<Iterator<V>> stack;
    
 /*   static class TreeTransformer<V, E> implements Transformer<V, Iterator<V>> {
        Tree<V, E> tree;
        public TreeTransformer(Tree<V, E> tree) {
            this.tree = tree;
        }
        
        @Override
        public Iterator<V> transform(V arg0) {
            return tree.getChildren(arg0).iterator();
        }           
    }*/
    
    @Override
    public void init() {
        this.stack.clear();
        current = getBegin();
        if(type == Type.Postfix)
            while(canForward())
                forward();
    }    
        
    public TreeDepthIterator(Tree<V, E> tree, Type type) {
        super(tree.getRoot());
        this.tree = tree;
        this.type = type;
        this.stack = new Stack<Iterator<V>>();
        init();
    }
        
    public int getDepth() {
        return stack.size();
    }
    public boolean isRoot() {
        return stack.isEmpty();
    }
    public boolean canForward() {
        return tree.outDegree(current) > 0;
    }
    public boolean isLeaf() {
        return tree.outDegree(current) == 0;
    }
    public boolean canRight() {
        return stack.lastElement().hasNext();
    }
    
    public void backward() {
        stack.pop();
        current = tree.getParent(current);
    }
    public void forward() {
        Iterator<V> itr = tree.getChildren(current).iterator();
        stack.push(itr);
        current = itr.next();
    }
    public void right() {
        current = stack.lastElement().next();
    }
    
    public void advance() {
        switch(type) {
        case Prefix:
            prefixDepthAdvance();
            break;
        case Postfix:
            postfixDepthAdvance();
            break;
        default:
            break;
        }
    }

    /*@Override
    public void remove() {
        tree.removeVertex(current); // удаляется с поддеревом????????????????????
    }*/
    
    public List<V> getPath() {
        List<V> result = new LinkedList<V>();
        V vertex = current;
        while(vertex != null) {
            result.add(0, vertex);
            vertex = tree.getParent(vertex);
        }
        return result;
    }
    
    public V nextLeaf() {
        if(!hasNext())
            return null;
        V result = getValue();
        do 
            next();
        while(hasNext() && !isLeaf());
        return result;
    }
}