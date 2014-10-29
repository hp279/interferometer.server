package org.interferometer.math.graph.iterator;

import java.util.List;

import org.interferometer.util.AbstractInputIterator;

/** Абстрактный итератор для обхода графа */
public abstract class GraphInputIterator<V> extends AbstractInputIterator<V> {
    
    protected V begin, current;
    protected GraphInputIterator(V begin) {
        this.begin = begin;
        this.current = begin;
    }
    
    public V getBegin() {
        return begin;
    }
    @Override
    public V getValue() {
        return current;
    }
    /** Чаще всего этого недостаточно, нужны ещё инициализирующие действия */
    public void init() {
        current = begin;
    }
    
    public abstract int getDepth();
    
    public abstract boolean canForward();
    public boolean canBackward() {
        return getDepth() > 0;
    }
    public abstract boolean canRight();
    
    public abstract void forward();
    public abstract void backward();
    public abstract void right();  
    
    public abstract List<V> getPath();
    
    protected void prefixDepthAdvance() {
        if(canForward())
            forward();
        else {
            while(canBackward() && !canRight())
                backward();
            if(!canBackward())
                current = null;
            else
                right();
        }
    }
    
    protected void postfixDepthAdvance() {
        if(canRight()) {
            right();
            while(canForward())
                forward();
        }
        else
            if(!canBackward())
                current = null;
            else
                backward();
    }
}