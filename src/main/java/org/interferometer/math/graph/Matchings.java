package org.interferometer.math.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

import org.interferometer.math.graph.set.Utils;

/** Алгоритмы нахождения оптимальных паросочетаний и покрытий */
public class Matchings {
    /** Находит произвольное паросочетание, принадлежащее указанному графу.
     * Его можно использовать как начальное в других алгоритмах.
     * Используется жадный алгоритм. */
    public static <V, E> Graph<V, E> getMatching(Graph<V, E> graph, GraphFactory<V, E> maker) {
        Graph<V, E> result = Utils.copyVoid(graph, maker);
        // соединяем каждую точку с произвольной
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
           return result;
    }
    
    /** Находит произвольное паросочетание в полном графе.
     * Его можно использовать как начальное в других алгоритмах.
    */
    public static <V, E> Graph<V, E> getMatching(Set<V> vertices,
                                                 GraphFactory<V, E> maker, 
                                                 GraphFactory.EdgeFactory<V, E> edge_maker) {
        Graph<V, E> result = Utils.makeVoid(vertices, maker);
        // соединяем каждую точку с произвольной
        V prevv = null;
        for(V v: vertices) 
            if(prevv != null)
                result.addEdge(edge_maker.create(prevv, v), prevv, v);
            else
                prevv = v;
        return result;
    }
    
    /** Меняет паросочетание по увеличивающему (аугментальному) пути */
    public static <V, E> void runAugmentalPath(Graph<V, E> matchings, 
                                                V path_begin, V path_end, 
                                                List<E> path,
                                                GraphFactory.EdgeFactory<V, E> edge_maker) {
        V begin = null,
          end = path_begin; // начала и концы текущих паросочетаний
        boolean has = false; // текущее ребро принадлежит паросочетанию
        for(Iterator<E> itr = path.iterator(); itr.hasNext();) {
            E edge = itr.next();
            if(has) {
                Pair<V> vertices = matchings.getEndpoints(edge);
                begin = vertices.getFirst();
                // добавляем предыдущее ребро:
                matchings.addEdge(edge_maker.create(end, begin), end, begin);                
                end = vertices.getSecond();
                // и удаляем текущее:
                matchings.removeEdge(edge);               
                has = false;
            }
            else {
                if(!itr.hasNext())
                // добавляем последнее ребро:
                    matchings.addEdge(edge_maker.create(end, path_end), end, path_end);
                has = true;
            }
        }
    }
    
    /** Максимальное паросочетание в двудольном графе */
    public static <V, E> Graph<V, E> getMaxMatchingBiParty(Graph<V, E> graph, GraphFactory<V, E> maker) {
        Graph<V, E> result = getMatching(graph, maker);        
        // TODO: сделать поиск максимального паросочетания
        return result;
    }
    
    /** Максимальное паросочетание в данном графе */
    public static <V, E> Graph<V, E> getMaxMatching(Graph<V, E> graph, GraphFactory<V, E> maker) {
        Graph<V, E> result = getMatching(graph, maker);        
        // TODO: сделать поиск максимального паросочетания
        return result;
    }
    
    /** Паросочетание максимальной стоимости, принадлежащее данному графу. */
    public static <V, E, N extends Number> Graph<V, E> getMaxMatching(Graph<V, E> graph, 
                                                               Transformer<E, N> costs,
                                                               GraphFactory<V, E> maker) {
        return getMaxMatching(graph.getVertices(), costs, maker, 
                              new GraphFactory.GraphEdgeFactory<V, E>(graph));
      }
    
    /** Паросочетание максимальной стоимости в полном графе. */
    public static <V, E, N extends Number> Graph<V, E> getMaxMatching(Collection<V> vertices, 
                                                               Transformer<E, N> costs,
                                                               GraphFactory<V, E> maker,
                                                               GraphFactory.EdgeFactory<V, E> edge_maker) {
        Graph<V, E> result = Utils.makeVoid(vertices, maker);
     // пока что действуем тупо: соединяем каждую точку с той, которая максимальна по стоимости ребра
        for(V v1: result.getVertices()) {
            if(result.degree(v1) > 0) // вершина уже занята
                continue;
            V maxv = null;
            double cost,
                   maxcost =  Double.NEGATIVE_INFINITY;
            E maxedge = null;
            for(V v2: result.getVertices()) {
                if(result.degree(v2) > 0) // вершина уже занята
                    continue;
                E edge = edge_maker.create(v1, v2);
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
        // TODO: сделать поиск паросочетания максимальной стоимости
        return result;
    }
}