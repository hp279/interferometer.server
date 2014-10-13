package org.interferometer.util;

import java.util.Collection;
import java.util.Iterator;

//import java.util.Arrays;

public class ArraysEx // extends Arrays ???
{
    /** Находит и удаляет все вхождения, возвращает число вхождений */
    public static <T> int findAndRemove(T array[], T value, int from, int to) {
        int real_from = from;
        for (;; ++from, ++real_from) {
            // System.out.println(real_from);
            // System.out.println(array[real_from]);
            while (real_from < to && array[real_from].equals(value))
                real_from++;
            if (real_from >= to)
                break;
            array[from] = array[real_from];
        }
        return real_from - from;
    }

    public static <T> int findAndRemove(T array[], T value, int to) {
        return findAndRemove(array, value, 0, to);
    }

    public static <T> int findAndRemove(T array[], T value) {
        return findAndRemove(array, value, array.length);
    }

    public static <T> T find(Collection<T> collection, T value) {
        T result;
        for (Iterator<T> itr = collection.iterator(); itr.hasNext();) {
            result = itr.next();
            if (result.equals(value))
                return result;
        }
        return null;
    }
}