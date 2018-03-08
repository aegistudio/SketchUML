package net.aegistudio.sketchuml.path;

import net.aegistudio.sketchuml.path.ParametricLinePiece.Parameter;

public class ParameterInterpolate implements Parameter {
	private final double t, _1_t;
	private final Parameter begin, end;
	public ParameterInterpolate(double t, 
			Parameter begin, Parameter end) {
		this.t = t; this._1_t = 1 - t;
		this.begin = begin;
		this.end = end;
	}
	
	@Override
	public double x(double[] vector) {
		return _1_t * begin.x(vector) + t * end.x(vector);
	}

	@Override
	public double y(double[] vector) {
		return _1_t * begin.y(vector) + t * end.y(vector);
	}

	@Override
	public double dxdai(int i, double[] vector) {
		return _1_t * begin.dxdai(i, vector) + t * end.dxdai(i, vector);
	}

	@Override
	public double dydai(int i, double[] vector) {
		return _1_t * begin.dydai(i, vector) + t * end.dydai(i, vector);
	}
}
