package net.aegistudio.sketchuml.path;

import de.dubs.dollarn.PointR;

public class BezierEvaluator {
	private final PointR p0, pctrl, p1;
	
	public BezierEvaluator(
		double x0, double xCtrl, double x1,
		double y0, double yCtrl, double y1) {
		this.p0 = new PointR(x0, y0);
		this.pctrl = new PointR(xCtrl, yCtrl);
		this.p1 = new PointR(x1, y1);
	}
	
	public BezierEvaluator(PointR p0, PointR pctrl, PointR p1) {
		this.p0 = new PointR(p0);
		this.pctrl = new PointR(pctrl);
		this.p1 = new PointR(p1);
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
	 * Evaluate the length of the curve.
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
		double dX1 = pctrl.X - p0.X;
		double dX2 = p1.X - pctrl.X;
		double dY1 = pctrl.Y - p0.Y;
		double dY2 = p1.Y - pctrl.Y;
		
		double c1 = dX1 * dX1 + dY1 * dY1;
		double c2 = dX1 * dX2 + dY1 * dY2;
		double c3 = dX2 * dX2 + dY2 * dY2;
		
		double A = c3 - 2 * c2 + c1;
		double B = c2 - c1;
		double C = c1;
		
		double b = B / (2 * A);
		double c = C / A;
		double u = t + b;
		double k = c - b * b;
		
		double su = Math.sqrt(u * u + k);
		double sb = Math.sqrt(b * b + k);
		double sA = Math.sqrt(A);
		
		return sA * (u * su - b * sb + k * Math
				.log(Math.abs((u + su) / (b + sb))));
	}
}
