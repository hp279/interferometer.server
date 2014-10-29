package org.interferometer.util;

public class Pair<T1, T2> {
    // TODO: private
    public T1 first;
    public T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
    
    public T1 getFirst() {
        return first;
    }
    
    public T2 getSecond() {
        return second;
    }

    public static <T1, T2> Pair<T1, T2> make(T1 first, T2 second) {
        return new Pair<T1, T2>(first, second);
    }

    public String toString() {
        return String.format("(%s, %s)", first.toString(), second.toString());
    }

    public boolean equals(Pair<T1, T2> p) {
        return first.equals(p.first) && second.equals(p.second);
    }

    public boolean equals(Object o) {
        return (o != null) && this.equals((Pair<T1, T2>) o);
    }

    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }
}