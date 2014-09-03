package org.interferometer;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.PrintStream;

import javax.imageio.ImageIO;

import org.interferometer.function.AbstractFunction1;
import org.interferometer.function.AbstractFunction2;
import org.interferometer.function.TableFunction2;
import org.interferometer.linear.DoubleAggregator;
import org.interferometer.linear.TableAction;
import org.ojalgo.function.aggregator.AggregatorFunction;

// F(x) = cos(pi(delta f(x) + ax + b)/lambda)
public class InterferometerRestoreFunction extends TableFunction2 {
    double a, b, lambda;
    double deltaz;

    StripsInfo currentStripsInfo;

    public enum Type {
        ByX, ByY
    }

    private Type type;

    public InterferometerRestoreFunction(Type type, double minx, double maxx,
            double miny, double maxy, int m, int n, double a, double b,
            double lambda, double deltaz) {
        super(minx, maxx, miny, maxy, m, n);
        this.type = type;
        this.a = a;
        this.b = b;
        this.lambda = lambda;
        this.deltaz = deltaz;
        this.currentStripsInfo = null;
    }

    public InterferometerRestoreFunction(Type type, double a, double b,
            double lambda) {
        this(type, 0, 0, 0, 0, 0, 0, a, b, lambda, 1);
    }

    public InterferometerRestoreFunction(Type type, String filename)
            throws Exception {
        this.type = type;
        this.load(filename);
        this.evalParams();
    }

    public double getA() {
        return a;
    }

    public double getLambda() {
        return lambda;
    }

    public double getPeriod() {
        return 2 * lambda / a;
    }

    // коэффициент при x внутри косинуса
    public double getXCoef() {
        return Math.PI * a / lambda;
    }

    /** оцениваем параметры a, b, lambda, не зная их */
    private void evalParams() {
        if (this.currentStripsInfo == null) {
            double h1 = Math.max(Math.max(this.getStepX(), this.getStepY()), 4
                    * deltaz / getXCoef()), h2 = Math.max(
                    Math.max(this.getStepX(), this.getStepY()),
                    Math.sqrt(8 * deltaz) / getXCoef()), e0 = 0.05, e1 = 0.01, e2 = 0.01;
            StripsOptions options = new StripsOptions();
            options.setMaxOptions(h1, h2, e0, e1, e2);
            options.setMinOptions(h1, h2, e0, e1, e2);
            options.setNilOptions(h1, h2, e0, e1, e2);
            this.setStripsInfo(options);
        }
        this.currentStripsInfo.createStrips();
        double sumborders = 0;
        /*
         * this.iterate(new TableAction<TableFunction2>(sumborders) {
         * 
         * @Override public void act(TableFunction2 a, int row, int col) { //
         * TODO Auto-generated method stub
         * 
         * } });
         */
        // TODO: использовать тут AggregatorFunction
        for (int i = 0; i <= getSizeX(); ++i)
            for (int j = 0; j <= getSizeY(); ++j)
                if (hasArgument(getArgument1(i), getArgument2(j)))
                    sumborders += (this.currentStripsInfo.getType(i, j) == StripsInfo.PixelType.Nothing ? 0
                            : 1);
        a = sumborders / ((getSizeX() + 1) * (getSizeY() + 1));
        b = 0;
        lambda = 1;
    }

    /**
     * Определяем максимум и минимум и, исходя из этого, приводим к отрезку
     * [-1;1]
     */
    private void normalize() {
        final double min = this.aggregate(DoubleAggregator.MIN), max = this
                .aggregate(DoubleAggregator.MAX);
        System.out.printf("\nmax=%f, min=%f", max, min);
        AbstractFunction1 normalizer = new AbstractFunction1() {
            public double invoke(double x) {
                return (2 * (x - min) / (max - min)) - 1;
            }
        };
        this.transform(normalizer);
        deltaz = deltaz * 2 / (max - min);
    }

    // загрузка из картинки
    public void load(String filename) throws Exception {
        File f = new File(filename);
        try {
            BufferedImage img = ImageIO.read(f);
            int width = img.getWidth();
            int height = img.getHeight();
            System.out.printf("\nwidth=%d height=%d", width, height);
            // пока что размер пикселя по x и по y будет равен 1 единице длины
            // TODO: подумать, где взять информацию о размере пикселя
            resize(0, height - 1, 0, width - 1, height - 1, width - 1);
            // ColorModel color_model = img.getColorModel();
            // double min = color_model.getColorSpace().getMinValue(0),
            // max = color_model.getColorSpace().getMaxValue(0);
            this.deltaz = 1;// / (max-min) / 256;
            Raster raster = img.getData();
            for (int i = 0; i <= getSizeX(); ++i)
                for (int j = 0; j <= getSizeY(); ++j) {
                    double value = raster.getSampleDouble(j, i, 0);
                    setValue(i, j, value);
                    // TODO: уточнить, из какого бэнда брать значения - или
                    // брать альфу???
                }
            setDefined();
            normalize();
        } catch (Exception ex) {
            setError();
            throw ex;
        }
    }

    public void write(PrintStream out) {
        super.write(out);
        if (currentStripsInfo != null) {
            out.printf("\nS\n");
            currentStripsInfo.write(out);
        }
    }

    public StripsInfo getCurrentStripsInfo() {
        return this.currentStripsInfo;
    }

    public void setStripsInfo(StripsOptions options) {
        this.currentStripsInfo = new StripsInfo(this, options);
        // TODO: сделать поддержку нескольких вариантов располосовки
    }

    public void createStrips() {
        double T = getPeriod();
        double h1 = T / 2, // постепенно будем уменьшать до max(deltax, deltay,
                           // 4*deltaz/getXCoef()
        h2 = T / 2; // постепенно будем уменьшать до max(deltax, deltay,
                    // Math.sqrt(8*deltaz)/getXCoef()
        double e0 = deltaz, // постепенно будем увеличивать до 0.5
        e1 = Math.max(deltaz, 2 * deltaz / (h1 * getXCoef())), // постепенно
                                                               // будем
                                                               // увеличивать
                                                               // до 0.5
        e2 = Math.max(deltaz, 4 * deltaz / (h2 * h2 * getXCoef())); // постепенно
                                                                    // будем
                                                                    // увеличивать
                                                                    // до 0.5
        // TODO: сделать цикл, пока не получим приличные полоски
        StripsOptions options = new StripsOptions();

        h1 = T / 8; // Math.max(Math.max(this.getStepX(), this.getStepY()),
                    // 4*deltaz);
        h2 = Math.max(Math.max(this.getStepX(), this.getStepY()),
                Math.sqrt(8 * deltaz) / getXCoef());
        e0 = deltaz; // 0.5;
        e1 = Math.max(deltaz, 2 * deltaz / (h1 * getXCoef())); // 0.5;
        e2 = 0.5 / getXCoef();
        System.out
                .printf("\nh1=%f h2=%f e0=%f e1=%f e2=%f", h1, h2, e0, e1, e2);
        options.setMaxOptions(h1, h2, e0, e1, e2);

        h1 = T / 8; // Math.max(Math.max(this.getStepX(), this.getStepY()),
                    // 4*deltaz);
        h2 = Math.max(Math.max(this.getStepX(), this.getStepY()),
                Math.sqrt(8 * deltaz) / getXCoef());
        e0 = deltaz * 2; // 0.5
        e1 = 0.5 / getXCoef();
        e2 = 0.5 / getXCoef();
        System.out
                .printf("\nh1=%f h2=%f e0=%f e1=%f e2=%f", h1, h2, e0, e1, e2);
        options.setMinOptions(h1, h2, e0, e1, e2);

        h1 = T / 8; // Math.max(Math.max(this.getStepX(), this.getStepY()),
                    // 4*deltaz);
        h2 = Math.max(Math.max(this.getStepX(), this.getStepY()),
                Math.sqrt(8 * deltaz) / getXCoef());
        e0 = deltaz * 5; // 0.5
        e1 = 0.5 / getXCoef();
        e2 = 0.5 / getXCoef();
        System.out
                .printf("\nh1=%f h2=%f e0=%f e1=%f e2=%f", h1, h2, e0, e1, e2);
        options.setNilOptions(h1, h2, e0, e1, e2);

        this.setStripsInfo(options);
        this.currentStripsInfo.createStrips();
    }

    private double getOriginal(int i, int j) {
        return Utils.acos2(getValue(i, j), currentStripsInfo.getK(i, j))
                * lambda / Math.PI;
    }

    public TableFunction2 restore() {
        if (currentStripsInfo == null)
            createStrips();
        if (currentStripsInfo.getStatus() != StripsInfo.Status.StripsRestored)
            currentStripsInfo.createStrips();
        InterferometerRestoreFunction temp = new InterferometerRestoreFunction(
                this.type, this.getMinX(), this.getMaxX(), this.getMinY(),
                this.getMaxY(), this.getSizeX(), this.getSizeY(), this.a,
                this.b, this.lambda, this.deltaz);
        temp.iterate(new TableAction<TableFunction2>() {
            @Override
            public void act(TableFunction2 a, int row, int col) {
                ((InterferometerRestoreFunction) a).setValue(row, col,
                        InterferometerRestoreFunction.this
                                .getOriginal(row, col));
            }
        });
        TableFunction2 result = new TableFunction2(this.getMinX(),
                this.getMaxX(), this.getMinY(), this.getMaxY(),
                this.getSizeX(), this.getSizeY());
        result.setArea(this.getArea());
        result.assign(temp);
        result.append(new AbstractFunction2() {
            public double invoke(double x, double y) {
                return type == Type.ByX ? (-a * x - b) : (-a * y - b);
            }
        });
        return result;
    }

}