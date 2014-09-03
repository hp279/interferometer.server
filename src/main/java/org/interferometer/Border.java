package org.interferometer;

import java.util.Iterator;
import java.util.LinkedList;

import org.interferometer.StripsInfo.PixelType;

/**
 * Последовательность точек, соединённая в границу. Может быть замкнута в кольцо
 * (тогда считаем, что за последней точкой идёт первая). Класс не отслеживает
 * самопересечения и замыкания в кольцо - за этим следит класс StripsInfo.
 */
public class Border implements Comparable<Border> {
    public enum Type {
        Max, Min, Nil, Empty; // псевдограница - её не существует, она нужна
                              // только для поиска

        public StripsInfo.PixelType getMayBeType() {
            switch (this) {
            case Max:
                return PixelType.MayBeMax;
            case Min:
                return PixelType.MayBeMin;
            case Nil:
                return PixelType.MayBeNil;
            default:
                return null;
            }
        }

        public StripsInfo.PixelType getPossibleType() {
            switch (this) {
            case Max:
                return PixelType.PossibleMax;
            case Min:
                return PixelType.PossibleMin;
            case Nil:
                return PixelType.PossibleNil;
            default:
                return null;
            }
        }

        public StripsInfo.PixelType getPossibleBeginType() {
            switch (this) {
            case Max:
                return PixelType.PossibleMaxAndBegin;
            case Min:
                return PixelType.PossibleMinAndBegin;
            case Nil:
                return PixelType.PossibleNilAndBegin;
            default:
                return null;
            }
        }
    }

    Type type;
    LinkedList<IntPoint> pixels;
    StripsInfo field;
    boolean is_ring;

    public Border(StripsInfo field, Type type, IntPoint pt) {
        this.field = field;
        this.type = type;
        this.pixels = new LinkedList<IntPoint>();
        this.pixels.addLast(pt);
        if (this.field != null)
            this.field.setType(pt, type.getPossibleBeginType());
        this.is_ring = false;
    }

    public Border(IntPoint pt) {
        this(null, Type.Empty, pt);
    }

    public Type getType() {
        return type;
    }

    public boolean isEmpty() {
        return this.pixels.isEmpty();
    }

    public boolean isOnlyBegin() {
        return this.pixels.size() == 1;
    }

    public boolean hasBegin(IntPoint pt) {
        return this.pixels.getFirst().equals(pt);
    }

    public IntPoint getFirst() {
        return this.pixels.getFirst();
    }

    public IntPoint getLast() {
        return this.pixels.getLast();
    }

    public IntPoint getPrevLast() {
        if (this.pixels.size() >= 2) {
            Iterator<IntPoint> iterator = this.pixels.descendingIterator();
            iterator.next();
            return iterator.next();
        } else
            return null;
    }

    public void setRing() {
        this.is_ring = true;
    }

    public void addPixel(IntPoint pt) {
        System.out.printf("\n add pixel: (%d, %d)", pt.getX(), pt.getY());
        this.pixels.addLast(pt);
        this.is_ring = false;
        if (this.field != null)
            this.field.setType(pt, type.getPossibleType());
    }

    public void removePixel() {
        if (this.field != null)
            this.field.setType(this.pixels.getLast(), PixelType.Nothing);
        this.pixels.removeLast();
        this.is_ring = false;
    }

    public void addBeginPixel(IntPoint pt) {
        if (this.field != null) {
            this.field.setType(this.pixels.getFirst(), type.getPossibleType());
            this.field.setType(pt, type.getPossibleBeginType());
        }
        this.pixels.addFirst(pt);
        this.is_ring = false;
    }

    public void addBegin(Border b) {
        if (this.field != null)
            field.setType(pixels.getFirst(), type.getPossibleType());
        LinkedList<IntPoint> newpixels = b.pixels;
        newpixels.addAll(this.pixels);
        this.pixels = newpixels;
        this.is_ring = false;
    }

    public void addEnd(Border b) {
        // b.pixels.getFirst();
        // type.getPossibleType();
        if (this.field != null)
            this.field.setType(b.pixels.getFirst(), type.getPossibleType());
        this.pixels.addAll(b.pixels);
        this.is_ring = false;
    }

    public void reverse() {
        LinkedList<IntPoint> newpixels = new LinkedList<IntPoint>();
        Iterator<IntPoint> desc = this.pixels.descendingIterator();
        while (desc.hasNext())
            newpixels.addLast(desc.next());
        this.pixels = newpixels;
        if (this.field != null) {
            field.setType(pixels.getLast(), type.getPossibleType());
            field.setType(pixels.getFirst(), type.getPossibleBeginType());
        }
    }

    public boolean equals(Border arg) {
        return pixels.isEmpty() ? arg.pixels.isEmpty()
                : (!arg.pixels.isEmpty() && pixels.getFirst().equals(
                        arg.pixels.getFirst()));
    }

    public boolean equals(Object o) {
        return equals((Border) o);
    }

    @Override
    public int compareTo(Border arg0) {
        return pixels.isEmpty() ? (arg0.pixels.isEmpty() ? 0 : -1)
                : (arg0.pixels.isEmpty() ? 1 : pixels.getFirst().compareTo(
                        arg0.pixels.getFirst()));
    }
}