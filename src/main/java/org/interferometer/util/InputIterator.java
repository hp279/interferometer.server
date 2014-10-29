package org.interferometer.util;

import java.util.Iterator;

public interface InputIterator<T> extends Iterator<T> {
    public void init();
    public T getValue(); 
    public void advance();
}