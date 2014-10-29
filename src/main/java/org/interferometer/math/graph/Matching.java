package org.interferometer.math.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;

import org.interferometer.util.Pair;
import org.interferometer.math.graph.Utils;
import org.interferometer.math.graph.iterator.GraphDepthIterator;

/** Паросочетание в графе
 *  Граф может быть двух видов:
 *  1. Полный со стоимостями
 *  2. Неполный, так что паросочетание должно быть его подграфом. */
public class Matching<V, E> implements Cloneable {
    class SetUtils extends org.interferometer.math.graph.set.Utils {
    }
    
    Graph<V, E> matching;
    GraphFactory<V, E> graph_factory; // нужно во всяких вспомогательных операциях
    
    public Matching(Collection<V> vertices, GraphFactory<V, E> factory) {
        matching = SetUtils.makeVoid(vertices, factory);
        graph_factory = factory;
    }
    
    private Matching(Matching<V, E> copy) {
        matching = SetUtils.copy(copy.matching, copy.graph_factory);
        graph_factory = copy.graph_factory;
    }
    public Matching<V, E> clone() {
        return new Matching<V, E>(this);
    }
    
    public Graph<V, E> getMatching() {
        return matching;
    }
    
    /** Экспонированная вершина - не принадлежащая ни одному паросочетанию */
    public boolean isExposed(V vertex) {
        return matching.degree(vertex) == 0;
    }
    
    /** Вторая вершина в паре */
    public V getAnotherVertex(V vertex) {
        return matching.getNeighbors(vertex).iterator().next();
    }
    
    /** Количество ребер в паросочетании */
    public int getPower() {
        return matching.getEdgeCount();
    }

    /** Стоимость паросочетания */
    public double getCost(Transformer<E, Double> costs) {
        Iterator<E> itr = matching.getEdges().iterator();
        if(!itr.hasNext())
            return 0;
        double result = costs.transform(itr.next());
        while(itr.hasNext())
            result += costs.transform(itr.next());
        return result;
    }
    
    /** Находит 2 экспонированные вершины (если столько нет - 1 или 0) */
    private Pair<V, V> getExposed() {
        V first = null,
          second = null;
        for(Iterator<V> itr = matching.getVertices().iterator(); itr.hasNext() && second == null;) {
            V current = itr.next();
            if(isExposed(current))
                if(first == null)
                    first = current;
                else
                    second = current;
        }
        return Pair.make(first, second);
    }
    
    /** Находит произвольное паросочетание, принадлежащее указанному графу.
     * Его можно использовать как начальное в других алгоритмах.
     * Используется жадный алгоритм. */
    public void init(Graph<V, E> graph) {
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
               if(maxv != null && !matching.containsEdge(maxedge))
                   matching.addEdge(maxedge, v1, maxv);
           }
    }
    
    /** Находит произвольное паросочетание в полном графе.
     * Его можно использовать как начальное в других алгоритмах.
    */
    public void init(GraphFactory.EdgeFactory<V, E> edge_maker) {
        // соединяем каждую точку с произвольной
        V prevv = null;
        for(V v: matching.getVertices()) 
            if(prevv != null)
                matching.addEdge(edge_maker.create(prevv, v), prevv, v);
            else
                prevv = v;
    }
    
    /** Меняет паросочетание по увеличивающему (аугментальному) пути */
    public void runAugmentalPath(V path_begin, V path_end, List<E> path,
                                 GraphFactory.EdgeFactory<V, E> edge_maker) {
        V begin = null,
          end = path_begin; // начала и концы текущих паросочетаний
        boolean has = false; // текущее ребро принадлежит паросочетанию
        for(Iterator<E> itr = path.iterator(); itr.hasNext();) {
            E edge = itr.next();
            if(has) {
                edu.uci.ics.jung.graph.util.Pair<V> vertices = matching.getEndpoints(edge);
                begin = vertices.getFirst();
                // добавляем предыдущее ребро:
                matching.addEdge(edge_maker.create(end, begin), end, begin);                
                end = vertices.getSecond();
                // и удаляем текущее:
                matching.removeEdge(edge);               
                has = false;
            }
            else {
                if(!itr.hasNext())
                // добавляем последнее ребро:
                    matching.addEdge(edge_maker.create(end, path_end), end, path_end);
                has = true;
            }
        }
    }
    
    /**  Находит венгерское дерево - аугментальное дерево, которое невозможно удлинить
     * Аугментальное дерево: начинается в экспонирующей вершине,
     * все пути из корня - чётной длины и чередующиеся.
     * Если одна вершина паросочетания лежит в аугментальном дереве, то и 2-я лежит.
 */
    private Tree<V, E> getHungaryTreeBiParty(Graph<V, E> graph) {
        Pair<V, V> exposed_pair = getExposed();
        DelegateTree<V, E> tree = null;
        boolean has_augmental_path = false;
        while(exposed_pair.getSecond() != null) {
            V root = exposed_pair.getFirst();
            tree = new DelegateTree<V, E>();
            tree.addVertex(root);
            System.out.printf("\nTree with root %s is initialized", root);
            GraphDepthIterator<V, E> itr = new GraphDepthIterator<V, E>(graph, root);
            V current = itr.getValue(); // в начале лист - это корень
            tree_cycle:
            while(current != null) {
                System.out.printf("\nNow leaf is %s", current);
                itr.advance();
                current = itr.getValue();
                if(current != null) {
                    V leaf = itr.getBack();
                    if(isExposed(current)) {
                        // у нас есть аугментальный путь!
                        tree.addEdge(graph.findEdge(leaf, current), leaf, current);
                        List<E> edge_path = Utils.getPathFromRoot(tree, current);
                        runAugmentalPath(tree.getRoot(), current, edge_path,
                                         new GraphFactory.GraphEdgeFactory<V, E>(graph));
                        System.out.printf("\nAugmental path! Now matching with %d edges.", getPower());
                        has_augmental_path = true; 
                        break tree_cycle;
                    }
                    else {
                        // удлиняем дерево, если это возможно:
                        tree.addEdge(graph.findEdge(leaf, current), leaf, current);
                        V newvertex = getAnotherVertex(current);
                        itr.forwardTo(newvertex);
                        tree.addEdge(graph.findEdge(current, newvertex), current, newvertex);
                        System.out.printf("\nTree is increased: %d edges", tree.getEdgeCount());
                    }
                }
            }
            if(!has_augmental_path)
                return tree;
            exposed_pair = getExposed();
        }
        return tree;
    }
    
    /** Максимальное паросочетание в двудольном графе */
    public void setMaxMatchingBiParty(Graph<V, E> graph) {
        init(graph);
        System.out.printf("\nMatching with %d edges is initialized.", getPower());
        Graph<V, E> hungary_matchings = graph_factory.create(matching);
        Pair<V, V> exposed_pair;
        do {
            // Находим в графе венгерское дерево и вытаскиваем соответствующее паросочетание.
            // Одно вергерское дерево - одна экспонированная вершина.
            // Делаем так, покуда экспонированных вершин не останется.
            Tree<V, E> hungary_tree = getHungaryTreeBiParty(graph);
            System.out.printf("\nHungary tree with %d vertices is extracted!", hungary_tree.getVertexCount());
            Graph<V, E> submatching = SetUtils.getSubGraph(matching, hungary_tree.getVertices(), graph_factory);
            SetUtils.setUnion(submatching, hungary_matchings);
            SetUtils.setDifference(hungary_tree, matching);
            System.out.printf("\n%d vertices left.", matching.getVertexCount());
            exposed_pair = getExposed();            
        }
        while(exposed_pair.getSecond() != null);
        // теперь все эти паросочетания из венгерских деревьев добавляем обратно:
        SetUtils.setUnion(hungary_matchings, matching);
     }
    
    /** Максимальное паросочетание в данном графе */
    public void setMaxMatching(Graph<V, E> graph) {
        init(graph);        
        // TODO: сделать поиск максимального паросочетания
    }
    
    /** Паросочетание максимальной стоимости, принадлежащее данному графу. */
    public <N extends Number> void setMaxMatching(Graph<V, E> graph, 
                                                  Transformer<E, N> costs) {
        setMaxMatching(costs, new GraphFactory.GraphEdgeFactory<V, E>(graph));
      }
    
    /** Паросочетание максимальной стоимости в полном графе. */
    public <N extends Number> void setMaxMatching(Transformer<E, N> costs,
                                                  GraphFactory.EdgeFactory<V, E> edge_maker) {
     // пока что действуем тупо: соединяем каждую точку с той, которая максимальна по стоимости ребра
        for(V v1: matching.getVertices()) {
            if(!isExposed(v1)) // вершина уже занята
                continue;
            V maxv = null;
            double cost, maxcost = Double.NEGATIVE_INFINITY;
            E maxedge = null;
            for(V v2: matching.getVertices()) {
                if(!isExposed(v2)) // вершина уже занята
                    continue;
                E edge = edge_maker.create(v1, v2);
                cost = costs.transform(edge).doubleValue();
                if (cost > maxcost) {
                    maxv = v2;
                    maxcost = cost;
                    maxedge = edge;
                }
            }
            if (maxv != null) {
                matching.addEdge(maxedge, v1, maxv);
                System.out.print('+');
            }
        }
        // TODO: сделать поиск паросочетания максимальной стоимости
    }
}