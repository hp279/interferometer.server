package org.interferometer.linear;

import java.util.LinkedList;
import java.util.List;

import org.ojalgo.access.Access2D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/** Класс для хранения матриц размерности 2 на 2 */
public final class Store22 implements PhysicalStore<Double>
{
	double a11, a12, a21, a22;
	
	public Store22(double a11, double a12, double a21, double a22)
	{
		this.a11 = a11;
		this.a12 = a12;
		this.a21 = a21;
		this.a22 = a22;
	}

	@Override
	public double doubleValue(int aRow, int aCol) {
		return aRow == 0? (aCol == 0? a11 : (aCol == 1? a12 : (new double[2][2])[aRow][aCol])):
			   (aRow == 1?	(aCol == 0? a11 : (aCol == 1? a12 : (new double[2][2])[aRow][aCol])) :
				   (new double[2][2])[aRow][aCol]);
	}

	@Override
	public int getColDim() {
		return 2;
	}

	@Override
	public int getRowDim() {
		return 2;
	}

	@Override
	public int size() {
		return 4;
	}

	@Override
	public Double get(int aRow, int aCol) {
		return doubleValue(aRow, aCol); 
	}

	@Override
	public double doubleValue(int anInd) {
		return doubleValue(anInd / 2, anInd % 2);
	}

	@Override
	public Double get(int anInd) {
		return doubleValue(anInd);
	}

	@Override
	public Double aggregateAll(Aggregator aVisitor) {
	    final AggregatorFunction<Double> tmpFunction = aVisitor.getPrimitiveFunction();
        this.visitAll(tmpFunction);
        return tmpFunction.getNumber();
	}

	@Override
	public Builder builder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PhysicalStore<Double> conjugate() {
		return transpose();
	}

	@Override
	public PhysicalStore<Double> copy() {
		return new Store22(a11, a12, a21, a22);
	}

	@Override
	public boolean equals(MatrixStore<Double> aStore, NumberContext aCntxt) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Factory getFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMinDim() {
		return 2;
	}

	@Override
	public boolean isLowerLeftShaded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUpperRightShaded() {
		// TODO Auto-generated method stub
		return false;
	}
	
    public boolean isAbsolute(final int aRow, final int aCol) {
        return this.toScalar(aRow, aCol).isAbsolute();
    }

    public boolean isPositive(final int aRow, final int aCol) {
        return this.toScalar(aRow, aCol).isPositive();
    }

    public boolean isReal(final int aRow, final int aCol) {
        return this.toScalar(aRow, aCol).isReal();
    }

    public boolean isZero(final int aRow, final int aCol) {
        return this.toScalar(aRow, aCol).isZero();
    }

	@Override
	public MatrixStore<Double> multiplyLeft(MatrixStore<Double> aStore) {
		Store22 A = (Store22)aStore;
		return new Store22(A.a11 * a11 + A.a12 * a21, 
				A.a11 * a12 + A.a12 * a22,
				A.a21 * a11 + A.a22 * a21,
				A.a21 * a12 + A.a22 * a22);
	}

	@Override
	public MatrixStore<Double> multiplyRight(MatrixStore<Double> aStore) {
		Store22 B = (Store22)aStore;
		return new Store22(a11 * B.a11 + a12 * B.a21, 
						  a11 * B.a12 + a12 * B.a22,
						  a21 * B.a11 + a22 * B.a21,
						  a21 * B.a12 + a22 * B.a22);
	}

	@Override
	public Scalar<Double> toScalar(int aRow, int aCol) {
		// TODO Auto-generated method stub
		return null;
		//return new Scalar<Double>(doubleValue(aRow, aCol));
	}

	@Override
	public PhysicalStore<Double> transpose() {
		return new Store22(a11, a21, a12, a22);
	}

	@Override
	public void visitAll(AggregatorFunction<Double> aVisitor) {
		aVisitor.invoke(a11);
		aVisitor.invoke(a12);
		aVisitor.invoke(a21);
		aVisitor.invoke(a22);
	}

	@Override
	public void visitColumn(int aRow, int aCol, AggregatorFunction<Double> aVisitor) {
		if(aCol == 0)
		{
			if(aRow == 0)
				aVisitor.invoke(a11);
			if(aRow <= 1)
				aVisitor.invoke(a12);
		}
		else if(aCol == 1)
		{
			if(aRow == 0)
				aVisitor.invoke(a21);
			if(aRow <= 1)
				aVisitor.invoke(a22);			
		}
	}

	@Override
	public void visitRow(int aRow, int aCol, AggregatorFunction<Double> aVisitor) {
		if(aRow == 0)
		{
			if(aCol == 0)
				aVisitor.invoke(a11);
			if(aCol <= 1)
				aVisitor.invoke(a21);
		}
		else if(aRow == 1)
		{
			if(aCol == 0)
				aVisitor.invoke(a12);
			if(aCol <= 1)
				aVisitor.invoke(a22);			
		}
	}
	
	@Override
	public void visitDiagonal(int aRow, int aCol, AggregatorFunction<Double> aVisitor) {
		if(aRow == 0)
		{
			if(aCol == 0)
			{
				aVisitor.invoke(a11);
				aVisitor.invoke(a22);
			}
			if(aCol == 1)
				aVisitor.invoke(a12);
		}
		else if(aRow == 1)
		{
			if(aCol == 0)
				aVisitor.invoke(a21);
			if(aCol == 1)
				aVisitor.invoke(a22);			
		}
	}

	@Override
	public List<Double> asList() {
		List<Double> result = new LinkedList<Double>();
		result.add(a11);
		result.add(a12);
		result.add(a21);
		result.add(a22);
		return result;
	}

	@Override
	public void caxpy(Double aSclrA, int aColX, int aColY, int aFirstRow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exchangeColumns(int aColA, int aColB) {
		if((aColA == 0 && aColB == 1) || (aColA == 1 && aColB == 0))
		{
			double temp = a11;
			a11 = a21;
			a21 = temp;
			temp = a12;
			a12 = a22;
			a22 = temp;
		}
	}

	@Override
	public void exchangeRows(int aRowA, int aRowB) {
		if((aRowA == 0 && aRowB == 1) || (aRowA == 1 && aRowB == 0))
		{
			double temp = a11;
			a11 = a12;
			a12 = temp;
			temp = a21;
			a21 = a22;
			a22 = temp;
		}
	}

	@Override
	public void fillAll(Double aNmbr) {
		a11 = a12 = a21 = a22 = aNmbr.doubleValue();
	}

	@Override
	public void fillByMultiplying(MatrixStore<Double> aLeftStore,
			MatrixStore<Double> aRightStore) {
		Store22 A = (Store22)aLeftStore,
				B = (Store22)aRightStore;
		a11 = A.a11 * B.a11 + A.a12 * B.a21;
		a12 = A.a11 * B.a12 + A.a12 * B.a22;
		a21 = A.a21 * B.a11 + A.a22 * B.a21;
		a22 = A.a21 * B.a12 + A.a22 * B.a22;
	}

	@Override
	public void fillColumn(int aRow, int aCol, Double aNmbr) {
		if(aCol == 0)
		{
			if(aRow == 0)
				a11 = aNmbr.doubleValue();
			if(aRow <= 1)
				a12 = aNmbr.doubleValue();
		}
		else if(aCol == 1)
		{
			if(aRow == 0)
				a21 = aNmbr.doubleValue();
			if(aRow <= 1)
				a22 = aNmbr.doubleValue();			
		}
	}
	
	@Override
	public void fillRow(int aRow, int aCol, Double aNmbr) {
		if(aRow == 0)
		{
			if(aCol == 0)
				a11 = aNmbr.doubleValue();
			if(aCol <= 1)
				a21 = aNmbr.doubleValue();
		}
		else if(aRow == 1)
		{
			if(aCol == 0)
				a12 = aNmbr.doubleValue();
			if(aCol <= 1)
				a22 = aNmbr.doubleValue();			
		}
	}


	@Override
	public void fillDiagonal(int aRow, int aCol, Double aNmbr) {
		if(aRow == 0)
		{
			if(aCol == 0)
			{
				a11 = aNmbr.doubleValue();
				a22 = aNmbr.doubleValue();
			}
			if(aCol == 1)
				a12 = aNmbr.doubleValue();
		}
		else if(aRow == 1)
		{
			if(aCol == 0)
				a21 = aNmbr.doubleValue();
			if(aCol == 1)
				a22 = aNmbr.doubleValue();			
		}
	}

	@Override
	public void fillMatching(Access2D<? extends Number> aSource2D) {
		a11 = aSource2D.doubleValue(0, 0);
		a12 = aSource2D.doubleValue(0, 1);
		a21 = aSource2D.doubleValue(1, 0);
		a22 = aSource2D.doubleValue(1, 1);
	}

	@Override
	public void fillMatching(MatrixStore<Double> aLeftArg,
			BinaryFunction<Double> aFunc, MatrixStore<Double> aRightArg) {
		Store22 A = (Store22)aLeftArg,
				B = (Store22)aRightArg;
		a11 = aFunc.invoke(A.a11, B.a11);
		a12 = aFunc.invoke(A.a12, B.a12);
		a21 = aFunc.invoke(A.a21, B.a21);
		a22 = aFunc.invoke(A.a22, B.a22);
	}

	@Override
	public void fillMatching(MatrixStore<Double> aLeftArg,
			BinaryFunction<Double> aFunc, Double aRightArg) {
		Store22 A = (Store22)aLeftArg;
		a11 = aFunc.invoke(A.a11, aRightArg.doubleValue());
		a12 = aFunc.invoke(A.a12, aRightArg.doubleValue());
		a21 = aFunc.invoke(A.a21, aRightArg.doubleValue());
		a22 = aFunc.invoke(A.a22, aRightArg.doubleValue());
	}

	@Override
	public void fillMatching(Double aLeftArg, BinaryFunction<Double> aFunc,
			MatrixStore<Double> aRightArg) {
		Store22 B = (Store22)aRightArg;
		a11 = aFunc.invoke(aLeftArg.doubleValue(), B.a11);
		a12 = aFunc.invoke(aLeftArg.doubleValue(), B.a12);
		a21 = aFunc.invoke(aLeftArg.doubleValue(), B.a21);
		a22 = aFunc.invoke(aLeftArg.doubleValue(), B.a22);
	}

	@Override
	public void maxpy(Double aSclrA, MatrixStore<Double> aMtrxX) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modifyAll(UnaryFunction<Double> aFunc) {
		a11 = aFunc.invoke(a11);
		a12 = aFunc.invoke(a12);
		a21 = aFunc.invoke(a21);
		a22 = aFunc.invoke(a22);
	}

	@Override
	public void modifyColumn(int aRow, int aCol, UnaryFunction<Double> aFunc) {
		if(aCol == 0)
		{
			if(aRow == 0)
				a11 = aFunc.invoke(a11);
			if(aRow <= 1)
				a12 = aFunc.invoke(a12);
		}
		else if(aCol == 1)
		{
			if(aRow == 0)
				a21 = aFunc.invoke(a21);
			if(aRow <= 1)
				a22 = aFunc.invoke(a22);			
		}
	}
	
	@Override
	public void modifyRow(int aRow, int aCol, UnaryFunction<Double> aFunc) {
		if(aRow == 0)
		{
			if(aCol == 0)
				a11 = aFunc.invoke(a11);
			if(aCol <= 1)
				a21 = aFunc.invoke(a21);
		}
		else if(aRow == 1)
		{
			if(aCol == 0)
				a12 = aFunc.invoke(a12);
			if(aCol <= 1)
				a22 = aFunc.invoke(a22);			
		}
	}

	@Override
	public void modifyDiagonal(int aRow, int aCol, UnaryFunction<Double> aFunc) {
		if(aRow == 0)
		{
			if(aCol == 0)
			{
				a11 = aFunc.invoke(a11);
				a22 = aFunc.invoke(a22);
			}
			if(aCol == 1)
				a12 = aFunc.invoke(a12);
		}
		else if(aRow == 1)
		{
			if(aCol == 0)
				a21 = aFunc.invoke(a21);
			if(aCol == 1)
				a22 = aFunc.invoke(a22);			
		}
	}

	@Override
	public void modifyOne(int aRow, int aCol, UnaryFunction<Double> aFunc) {
		if(aRow == 0)
		{
			if(aCol == 0)
				a11 = aFunc.invoke(a11);
			else if(aCol == 1)
				a12 = aFunc.invoke(a12);
		}
		else if(aRow == 1)
		{
			if(aCol == 0)
				a21 = aFunc.invoke(a21);
			else if(aCol == 1)
				a22 = aFunc.invoke(a22);
		}
	}

	@Override
	public void raxpy(Double aSclrA, int aRowX, int aRowY, int aFirstCol) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void set(int aRow, int aCol, double aNmbr) {
		if(aRow == 0)
		{
			if(aCol == 0)
				a11 = aNmbr;
			else if(aCol == 1)
				a12 = aNmbr;
		}
		else if(aRow == 1)
		{
			if(aCol == 0)
				a21 = aNmbr;
			else if(aCol == 1)
				a22 = aNmbr;
		}
	}

	@Override
	public void set(int aRow, int aCol, Double aNmbr) {
		set(aRow, aCol, aNmbr.doubleValue());
	}

	@Override
	public void transformLeft(Householder<Double> aTransf, int aFirstCol) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transformLeft(Rotation<Double> aTransf) {
		// TODO Auto-generated method stub
		// aTransf.doubleCosineValue()
		// aTransf.doubleSineValue()
	}

	@Override
	public void transformRight(Householder<Double> aTransf, int aFirstRow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transformRight(Rotation<Double> aTransf) {
		// TODO Auto-generated method stub
		
	}
	
}