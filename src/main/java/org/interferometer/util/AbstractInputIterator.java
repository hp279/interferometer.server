package org.interferometer.util;

public abstract class AbstractInputIterator<T> implements InputIterator<T> {
    @Override
    public boolean hasNext() {
        return getValue() != null;
    }

    @Override
    public T next() {
        T result = getValue();
        advance();
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Can`t remove value for input iterator");
    }
}