package net.aegistudio.sketchuml.path;

import de.dubs.dollarn.PointR;

public class BezierEvaluator {
	private final PointR p0, pctrl, p1;
	
	// Used when calculate length and differential.
	protected double A, B, C, b, c, k, sb, sA;
	
	public BezierEvaluator(
		double x0, double xCtrl, double x1,
		double y0, double yCtrl, double y1) {
		this.p0 = new PointR(x0, y0);
		this.pctrl = new PointR(xCtrl, yCtrl);
		this.p1 = new PointR(x1, y1);
		
		this.calculateConstants();
	}
	
	public BezierEvaluator(PointR p0, PointR pctrl, PointR p1) {
		this.p0 = new PointR(p0);
		this.pctrl = new PointR(pctrl);
		this.p1 = new PointR(p1);
		
		this.calculateConstants();
	}
	
	private void calculateConstants() {
		double dX1 = pctrl.X - p0.X;
		double dX2 = p1.X - pctrl.X;
		double dY1 = pctrl.Y - p0.Y;
		double dY2 = p1.Y - pctrl.Y;
		
		double c1 = dX1 * dX1 + dY1 * dY1;
		double c2 = dX1 * dX2 + dY1 * dY2;
		double c3 = dX2 * dX2 + dY2 * dY2;
		
		A = c3 - 2 * c2 + c1;
		B = c2 - c1;
		C = c1;
		
		b = B / (2 * A);
		c = C / A;
		k = c - b * b;
		
		sb = Math.sqrt(b * b + k);
		sA = Math.sqrt(A);
	}
	
	public void evaluate(double t, PointR result) {
		double tP = 1 - t;
		
		// Calculate bezier curve's position.
		result.combine(tP * tP, p0, 2 * tP * t, pctrl);
		result.combine(1, result, t * t, p1);
	}
	
	public void tangent(double t, PointR result) {
		double tP = 1 - t;
		
		// Calculate bezier curve's tangent.
		result.combine(- 2 * tP, p0, 2 * t, p1);
		result.combine(1, result, 2 * (tP - t), pctrl);
		result.normalize();
	}
	
	/**
	 * Evaluate the length of the curve P(0) to P(t).
	 * 
	 * The calculation is based on the integral:
	 * ArcLen(P(t)) = lim(Dt -> 0) |P(t+Dt) - P(t)|
	 * = lim(Dt -> 0) |(P(t+Dt) - P(t)) / Dt| |Dt| 
	 * = integral_t(0, 1) |P'(t)| dt
	 * 
	 * The integral is based on a quadratic bezier.
	 * 
	 * Reference:
	 * https://en.wikipedia.org/wiki/B%C3%A9zier_curve
	 * https://en.wikipedia.org/wiki/Arc_length
	 * https://stackoverflow.com/questions/11854907/calculate-the-length-of-a-segment-of-a-quadratic-bezier}
	 * 
	 * @return the length of current piece of bezier curve.
	 */
	public double length(double t) {
		// We perform a series of variable substitution here.
		double u = t + b;
		double su = Math.sqrt(u * u + k);
		
		return sA * (u * su - b * sb + k * Math
				.log(Math.abs((u + su) / (b + sb))));
	}
	
	/**
	 * Evaluate the derivative of length with parameter t.
	 * 
	 * @param t the curve parameter
	 * @return the derivative of length of current piece of bezier curve.
	 */
	public double derivativeLength(double t) {
		double u = t + b;
		double su = Math.sqrt(u * u + k);
		double dsu = u / su;
		
		return sA * (su + u * dsu + k * ((1 + dsu) / (u + su)));
	}
	
	/**
	 * Solve the equation at which l(c) = t * l(1)
	 * @param t the parameter.
	 * @param maxIteration when will solution stop.
	 * @param epsilon at which precision will solution stop.
	 * @return the control parameter.
	 */
	public double solveLengthEquation(double t, 
			int maxIteration, double epsilon) {
		double result = t;
		double length1d2 = t * length(1);
		
		double distance = length(result) - length1d2;
		if(Math.abs(distance) < epsilon) return result;
		double newResult = t; int iteration = 0;
		double newDistance = 0.0;
		
		do {
			result = newResult;
			newResult = result - (length(result) 
					- length1d2) / derivativeLength(result);
			newDistance = length(newResult) - length1d2;
			++ iteration;
		}
		while(Math.abs(newDistance) > 
			epsilon && iteration < maxIteration);
		
		return newResult;
	}
}
