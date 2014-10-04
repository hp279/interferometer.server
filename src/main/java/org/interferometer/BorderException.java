package org.interferometer;

import org.interferometer.util.IntPoint;

/** Ошибки, возникающие при построении границ */
public class BorderException extends Exception {
    public enum Type {
        NotEdge, // не замыкается и не упирается в границу области определения
        MoreThan2Lines, // в одном месте сходится больше 2 линий
        IntersectsWithThisType, // пересекается с границей того же типа
        IntersectsWithAnotherType; // пересекается с границей другого типа
        
        public String toString() {
            switch(this) {
            case NotEdge:
                return "не замыкается и не упирается в границу области определения";
            case MoreThan2Lines:
                return "в одном месте сходится больше 2 линий";
            case IntersectsWithThisType:
                return "пересекается с границей того же типа";
            case IntersectsWithAnotherType:
                return "пересекается с границей другого типа";
            default:
                return null;            
            }
        }
    }
    
    private Type type;
    private IntPoint pt;
    
    public BorderException(Type type, IntPoint point) {
        this.type = type;
        this.pt = point;
    }
    
    public String getMessage() {
        return String.format("Ошибка границы в точке %s: %s", this.pt.toString(), this.type.toString());
    }
    
    private static final long serialVersionUID = 1L;
}