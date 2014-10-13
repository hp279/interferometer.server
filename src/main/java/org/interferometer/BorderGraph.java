package org.interferometer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.interferometer.math.graph.Matchings;
import org.interferometer.math.graph.Utils;
import org.interferometer.util.IntPoint;
import org.interferometer.util.Pair;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class BorderGraph extends
        UndirectedSparseGraph<Border.EdgePoint, Pair<Border, Double>> implements
        Cloneable {
    static IntPoint far_point = new IntPoint(Integer.MAX_VALUE,
            Integer.MAX_VALUE);

    // каждой обычной точке соответствует "дальняя" - соединению с ней
    // соответствует соединение с границей
    static Border.EdgePoint getDualPoint(Border.EdgePoint pt) {
        return new Border.EdgePoint(new IntPoint(Integer.MAX_VALUE
                - pt.first.getX(), Integer.MAX_VALUE - pt.first.getY()),
                pt.second);
    }

    static boolean isFar(Border.EdgePoint pt) {
        return pt.first.getX() > Integer.MAX_VALUE / 2;
    }

    public static Transformer<Pair<Border, Double>, Double> getBordersEvaluation() {
        return new Transformer<Pair<Border, Double>, Double>() {
            @Override
            public Double transform(Pair<Border, Double> arg0) {
                return arg0 != null ? 1e10 - arg0.second : // чтобы количество
                                                           // максимизировалось,
                                                           // а стоимость -
                                                           // минимизировалась
                        // TODO: сделать 1e15 * deltaz
                        Double.NEGATIVE_INFINITY;
            }
        };
    }

    BordersInfo field;
    Border.Type type;

    /** Сразу добавляет все бордеры с поля */
    public BorderGraph(BordersInfo field, Border.Type type, Set<Border> borders) {
        this.field = field;
        this.type = type;
        Iterator<Border> itr = borders.iterator();
        while (itr.hasNext()) {
            Border border = itr.next();
            Border.EdgePoint first = border.getFirstVertex(), last = border
                    .getLastVertex();
            this.addVertex(first);
            if (!isFar(first) && !first.first.equals(last.first))
                // если начальная точка совпадает с конечной, дуальную делаем
                // только для конечной
                this.addVertex(getDualPoint(first));
            this.addVertex(last);
            if (!isFar(last))
                this.addVertex(getDualPoint(last));
            double size = border.getLength();
            addBorder(border, size);
        }
    }

    /** Пустой граф - информацию с поля соберёт потом */
    public BorderGraph(BordersInfo field, Border.Type type) {
        this(field, type, new ListOrderedSet<Border>());
    }

    public void addBorder(Border border, double size) {
        Border.EdgePoint first = border.getFirstVertex(), last = border
                .getLastVertex();
        if (isFar(first))
            this.addEdge(Pair.make(border, size), getDualPoint(last), last);
        else if (isFar(last))
            this.addEdge(Pair.make(border, size), first, getDualPoint(first));
        else
            this.addEdge(Pair.make(border, size), first, last);
    }

    /** Просто копия текущего графа - чтобы делать в ней нужные соединения */
    public BorderGraph clone() {
        BorderGraph result = new BorderGraph(this.field, this.type);
        Utils.copy(this, result);
        return result;
    }

    /** Граф с теми же вершинами, но без дуг. */
    // TODO: сделать interface GraphMaker и перенести в Utils
    public BorderGraph getVoidGraph() {
        BorderGraph result = new BorderGraph(this.field, this.type);
        for (Border.EdgePoint pt : this.getVertices())
            result.addVertex(pt);
        return result;
    }

    /**
     * Копия текущего графа, в которой есть все возможные, но несуществующие
     * соединения
     */
    public BorderGraph getPotentialGraph() {
        BorderGraph result = getVoidGraph();
        /*
         * System.out.printf("\nVertices: "); Border.EdgePoint points[] = new
         * Border.EdgePoint[this.getVertexCount()];
         * this.getVertices().toArray(points); Arrays.sort(points); for(int i=0;
         * i<points.length; ++i) System.out.printf("%s, ", points[i]);
         */
        // TODO: подумать, как избежать дублирования ребер для 1-точечных
        // бордеров
        for (Border.EdgePoint pt1 : this.getVertices())
            if (isFar(pt1)) {
                // System.out.printf("\nFar pt1 = %s", pt1);
                Border.EdgePoint pt2 = getDualPoint(pt1);
                // System.out.printf("\npt2 = dual(pt1) = %s", pt2);
                if (!this.isNeighbor(pt1, pt2)) {
                    Pair<Border, Double> link = this.field.getShortestBorder(
                            pt2.first, this.type);
                    if (link != null)
                        result.addEdge(link, pt2, pt1);
                }
            } else
                for (Border.EdgePoint pt2 : this.getVertices()) {
                    if (isFar(pt2) || pt2.first.compareTo(pt1.first) <= 0
                            || this.isNeighbor(pt1, pt2))
                        continue;
                    Pair<Border, Double> link = this.field.getShortestBorder(
                            pt1.first, pt2.first, this.type);
                    if (link != null)
                        result.addEdge(link, pt1, pt2);
                }
        return result;
    }

    /**
     * Паросочетание, охватывающее максимум вершин и при этом минимальное по
     * суммарной стоимости
     */
    public BorderGraph getMinMatching() {
        BorderGraph result = getVoidGraph();
        BorderGraph potential_graph = this.getPotentialGraph();
        Transformer<Pair<Border, Double>, Double> transformer = getBordersEvaluation();
        Matchings.getMaxMatching(potential_graph, transformer, result);
        Utils.setUnion(this.clone(), result);
        return result;
    }

    /**
     * Возвращает бордер, начинающийся в данной точке, и удаляет её и все
     * остальные из множества
     * 
     * @throws BorderException
     */
    private Border getComponentFromEdge(Border.EdgePoint pt,
            Set<Border.EdgePoint> points) throws BorderException {
        Border.EdgePoint nextpt = pt;
        points.remove(nextpt);
        Pair<Border, Double> current_border = null;
        Border result = null;
        // идём вперёд:
        do {
            current_border = Utils.getAnotherIncidentEdge(this, nextpt,
                    current_border);
            nextpt = Utils.getAnotherVertex(this, current_border, nextpt);
            // System.out.printf("\n nextpt = %s", nextpt.toString());
            if (result == null)
                result = current_border.first
                        .setOrientation(nextpt.first, null);
            else
                result.addEnd(current_border.first, true);
            points.remove(nextpt);
        } while (this.degree(nextpt) == 2);
        if (this.degree(nextpt) > 2)
            throw new BorderException(BorderException.Type.MoreThan2Lines,
                    nextpt.first);
        if (nextpt == pt) // это возможно, если граница состоит из 1 петли
            result.setRing();
        // TODO: проверить, что примыкает к краю области определения функции
        return result;
    }

    /**
     * Возвращает бордер, содержащий данную точку в середине, и удаляет её и все
     * остальные из множества
     * 
     * @throws BorderException
     */
    private Border getComponentFromMiddle(Border.EdgePoint pt,
            Set<Border.EdgePoint> points) throws BorderException {
        Border.EdgePoint nextpt = pt, lastpt = null;
        points.remove(nextpt);
        Pair<Border, Double> current_border = Utils.getAnyIncidentEdge(this,
                nextpt), prev_border = current_border;
        Border result = null;
        // идём вперёд:
        do {
            current_border = Utils.getAnotherIncidentEdge(this, nextpt,
                    current_border);
            nextpt = Utils.getAnotherVertex(this, current_border, nextpt);
            if (result == null)
                result = current_border.first
                        .setOrientation(nextpt.first, null);
            else
                result.addEnd(current_border.first, true);
            points.remove(nextpt);
        } while (this.degree(nextpt) == 2 && nextpt != pt);
        if (nextpt == pt) {
            result.setRing();
            return result;
        }
        if (this.degree(nextpt) > 2)
            throw new BorderException(BorderException.Type.MoreThan2Lines,
                    nextpt.first);
        // TODO: проверить, что примыкает к краю области определения функции
        // идём назад:
        lastpt = nextpt;
        nextpt = pt;
        current_border = prev_border;
        nextpt = Utils.getAnotherVertex(this, current_border, nextpt);
        result.addBegin(current_border.first, true);
        points.remove(nextpt);
        while (this.degree(nextpt) == 2 && nextpt != lastpt) {
            current_border = Utils.getAnotherIncidentEdge(this, nextpt,
                    current_border);
            nextpt = Utils.getAnotherVertex(this, current_border, nextpt);
            result.addBegin(current_border.first, true);
            points.remove(nextpt);
        }
        if (nextpt == lastpt)
            result.setRing();
        if (this.degree(nextpt) > 2)
            throw new BorderException(BorderException.Type.MoreThan2Lines,
                    nextpt.first);
        // TODO: проверить, что примыкает к краю области определения функции
        return result;
    }

    /**
     * Возвращает бордер, содержащий данную точку, и удаляет её и все остальные
     * из множества
     * 
     * @throws BorderException
     */
    private Border getComponent(Border.EdgePoint pt,
            Set<Border.EdgePoint> points) throws BorderException {
        Border result = null;
        System.out.printf("\nDegree of pt: %d", this.degree(pt));
        switch (this.degree(pt)) {
        case 0:
            points.remove(pt);
            return null;
        case 1:
            result = getComponentFromEdge(pt, points);
            break;
        case 2:
            result = getComponentFromMiddle(pt, points);
            break;
        default:
            points.remove(pt);
            throw new BorderException(BorderException.Type.MoreThan2Lines,
                    pt.first);
        }
        return result;
    }

    /**
     * Смыкает бордеры и возвращает
     * 
     * @throws BorderException
     */
    public Set<Border> getBorders() throws BorderException {
        ListOrderedSet<Border> result = new ListOrderedSet<Border>();
        Collection<Border.EdgePoint> points = this.getVertices();
        Set<Border.EdgePoint> points_set = new TreeSet<Border.EdgePoint>();
        points_set.addAll(points);
        while (!points_set.isEmpty()) {
            System.out.printf("\nsize now: %d points", points_set.size());
            Border.EdgePoint pt = points_set.iterator().next();
            // if(points_set.contains(pt))
            // System.out.printf("\npoints set contains %s", pt);
            Border component = this.getComponent(pt, points_set);
            if (component != null) {
                component.setType(this.type);
                result.add(component);
            }
        }
        return result;
    }

    private static final long serialVersionUID = 1L;

}