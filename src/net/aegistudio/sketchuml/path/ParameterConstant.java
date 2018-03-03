package net.aegistudio.sketchuml.path;

import de.dubs.dollarn.PointR;

public class ParameterConstant implements ParametricLinePiece.Parameter {
	private final PointR point;
	public ParameterConstant(PointR point) {
		this.point = point;
	}
	
	@Override
	public double x(double[] vector) {
		return point.X;
	}

	@Override
	public double y(double[] vector) {
		return point.Y;
	}

	@Override
	public double dxdai(int i, double[] vector) {
		return 0;
	}

	@Override
	public double dydai(int i, double[] vector) {
		return 0;
	}
}
