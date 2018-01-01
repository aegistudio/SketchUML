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
}
