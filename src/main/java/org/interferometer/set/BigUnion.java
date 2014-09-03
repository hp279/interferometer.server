package org.interferometer.set;

import java.util.Iterator;
import java.util.LinkedList;

public class BigUnion extends FlatSet {
    LinkedList<FlatSet> A;
    boolean disjoint;

    public BigUnion() {
        this.A = new LinkedList<FlatSet>();
        this.disjoint = true;
    }

    public void add(FlatSet a, boolean disjoint) {
        this.A.add(a);
        this.disjoint = this.disjoint && disjoint;
    }

    public boolean has(double x, double y) {
        for (Iterator<FlatSet> i = A.iterator(); i.hasNext();)
            if (i.next().has(x, y))
                return true;
        return false;
    }

    public FlatSet getSet(int i) {
        return A.get(i);
    }

    /** объединение непересекающихся множеств */
    public boolean isDisjoint() {
        return disjoint;
    }
}