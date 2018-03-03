package net.aegistudio.sketchuml.path;

import de.dubs.dollarn.PointR;

public abstract class ParameterAxial implements ParametricLinePiece.Parameter {
	private final boolean selectAxisX, selectBegin;
	private final PointR pointBegin, pointEnd;
	private final PointR pointTemp;
	
	public ParameterAxial(boolean selectAxisX, boolean selectBegin,
			PointR pointBegin, PointR pointEnd) {
		this.pointTemp = new PointR();
		this.selectAxisX = selectAxisX; this.selectBegin = selectBegin;
		this.pointBegin = pointBegin; this.pointEnd = pointEnd;
	}
	
	protected abstract void f(PointR result, 
			double[] vector, PointR begin, PointR end);
	
	protected abstract void dfdai(PointR result, 
			int i, double[] vector, PointR begin, PointR end);
	
	@Override
	public double x(double[] vector) {
		if(selectAxisX) {
			f(pointTemp, vector, pointBegin, pointEnd);
			return pointTemp.X;
		}
		return selectBegin? pointBegin.X : pointEnd.X;
	}

	@Override
	public double y(double[] vector) {
		if(!selectAxisX) {
			f(pointTemp, vector, pointBegin, pointEnd);
			return pointTemp.Y;
		}
		return selectBegin? pointBegin.Y : pointEnd.Y;
	}

	@Override
	public double dxdai(int i, double[] vector) {
		if(selectAxisX) {
			dfdai(pointTemp, i, vector, pointBegin, pointEnd);
			return pointTemp.Y;
		}
		return selectBegin? pointBegin.Y : pointEnd.Y;
	}

	@Override
	public double dydai(int i, double[] vector) {
		if(!selectAxisX) {
			dfdai(pointTemp, i, vector, pointBegin, pointEnd);
			return pointTemp.Y;
		}
		return selectBegin? pointBegin.Y : pointEnd.Y;
	}
}
