package org.interferometer;


// функция, восстановленная из разности
public class RestoredTableFunction2 extends TableFunction2 {
    AbstractFunction cont_modul; // оценка сверху модуля непрерывности

    public RestoredTableFunction2(double minx, double maxx, double miny,
            double maxy, int m, int n, AbstractFunction cont_modul) {
        super(minx, maxx, miny, maxy, m, n);
        this.cont_modul = cont_modul;
    }

    // восстановление функции по сдвигу по с заданным шагом и величиной сдвига
    public void restoreByShift(AbstractFunction2 deltaf_x,
            AbstractFunction2 deltaf_y, double s) {
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    // восстановление функции по двум сдвигам с заданным шагом и величинами
    // сдвигов
    public void restoreByShift(AbstractFunction2 delta1f_x,
            AbstractFunction2 delta1f_y, double s1,
            AbstractFunction2 delta2f_x, AbstractFunction2 delta2f_y, double s2) {
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

}
