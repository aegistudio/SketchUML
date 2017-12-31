package net.aegistudio.sketchuml.path;

import de.dubs.dollarn.PointR;

public class BezierEvaluator {
	private final double x0, xCtrl, x1;
	private final double y0, yCtrl, y1;
	
	public BezierEvaluator(
		double x0, double xCtrl, double x1,
		double y0, double yCtrl, double y1) {
		this.x0 = x0; this.xCtrl = xCtrl; this.x1 = x1;
		this.y0 = y0; this.yCtrl = yCtrl; this.y1 = y1;
	}
	
	public BezierEvaluator(PointR p0, PointR pctrl, PointR p1) {
		this(p0.X, pctrl.X, p1.X, p0.Y, pctrl.Y, p1.Y);
	}
	
	public void evaluate(double t, PointR result) {
		double tP = 1 - t;
		// Calculate bezier curve's position.
		result.X = tP * tP * x0 + 
				2 * tP * t * xCtrl + t * t * x1;
		result.Y = tP * tP * y0 + 
				2 * tP * t * yCtrl + t * t * y1;
	}
	
	public void tangent(double t, PointR result) {
		double tP = 1 - t;
		// Calculate bezier curve's tangent.
		result.X = 2 * tP * (xCtrl - x0)
				+ 2 * t * (x1 - xCtrl);
		result.Y = 2 * tP * (yCtrl - y0)
				+ 2 * t * (y1 - yCtrl);
		result.normalize();
	}
}
