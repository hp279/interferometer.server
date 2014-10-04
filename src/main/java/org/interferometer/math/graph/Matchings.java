package org.interferometer.math.graph;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;

/** Алгоритмы нахождения оптимальных паросочетаний и покрытий */
public class Matchings {
    /** Максимальное паросочетание записывает в result */
    public static <V, E, N extends Number> void getMaxMatching(Graph<V, E> graph, Graph<V, E> result) {
        // пока что действуем тупо: соединяем каждую точку с произвольной
           for(V v1: graph.getVertices()) {
               V maxv = null;
               E maxedge = null;
               for(V v2: graph.getVertices()) {
                   E edge = graph.findEdge(v1, v2);
                   if(edge != null) {
                       maxv = v2;
                       maxedge = edge;
                       break;
                   }
               }
               if(maxv != null)
                   result.addEdge(maxedge, v1, maxv);
           }
           // TODO: сделать поиск максимального паросочетания
       }
    
    /** Паросочетание максимальной стоимости записывает в result */
    public static <V, E, N extends Number> void getMaxMatching(Graph<V, E> graph, Transformer<E, N> costs, Graph<V, E> result) {
        System.out.printf("\bGraph has %d edges.", graph.getEdgeCount());
     // пока что действуем тупо: соединяем каждую точку с той, которая максимальна по стоимости ребра
        for(V v1: graph.getVertices()) {
            if(result.degree(v1) > 0) // вершина уже занята
                continue;
            V maxv = null;
            double cost,
                   maxcost =  Double.NEGATIVE_INFINITY;
            E maxedge = null;
            for(V v2: graph.getVertices()) {
                if(result.degree(v2) > 0) // вершина уже занята
                    continue;
                E edge = graph.findEdge(v1, v2);
                cost = costs.transform(edge).doubleValue();
                if(cost > maxcost) {
                    maxv = v2;
                    maxcost = cost;
                    maxedge = edge;
                }
            }
            if(maxv != null) {
                result.addEdge(maxedge, v1, maxv);
                System.out.print('+');
            }
   
        }
        // TODO: сделать поиск максимального паросочетания
    }
}