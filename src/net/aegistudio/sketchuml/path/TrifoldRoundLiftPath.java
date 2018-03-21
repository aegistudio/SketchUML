package net.aegistudio.sketchuml.path;

import java.util.List;
import java.util.Vector;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.path.ParametricLinePiece.Parameter;

public class TrifoldRoundLiftPath extends TrifoldAbstractLiftPath {
	@Override
	public void makePath(
			PointR pointStart, PointR pointEnd,
			PointR levelStart, PointR levelEnd,
			List<PointR> separatePoints, List<PointR> controlPoints) {
		
		// Add the points to the path.
		PointR levelIntra = new PointR();
		levelIntra.interpolate(0.5, levelStart, levelEnd);
		separatePoints.add(pointStart);
		controlPoints.add(levelStart);
		separatePoints.add(levelIntra);
		controlPoints.add(levelEnd);
		separatePoints.add(pointEnd);
	}

	@Override
	protected void stripHorizontal(Vector<ParametricLinePiece> stripHorizontal, Parameter paramPointBegin,
			Parameter paramPointEnd, Parameter paramHLiftBegin, Parameter paramHLiftEnd) {
		Parameter paramHLiftIntra = new ParameterInterpolate(
				0.5, paramHLiftBegin, paramHLiftEnd);
		ParametricBezierEvaluator bezierHBegin = new ParametricBezierEvaluator(
				paramPointBegin, paramHLiftBegin, paramHLiftIntra);
		ParametricBezierEvaluator bezierHEnd = new ParametricBezierEvaluator(
				paramHLiftIntra, paramHLiftEnd, paramPointEnd);
		bezierHBegin.proximate(stripHorizontal, 16);
		bezierHEnd.proximate(stripHorizontal, 16);
	}

	@Override
	protected void stripVertical(Vector<ParametricLinePiece> stripVertical, 
			Parameter paramPointBegin, Parameter paramPointEnd, 
			Parameter paramVLiftBegin, Parameter paramVLiftEnd) {
		Parameter paramHLiftIntra = new ParameterInterpolate(
				0.5, paramVLiftBegin, paramVLiftEnd);
		ParametricBezierEvaluator bezierVBegin = new ParametricBezierEvaluator(
				paramPointBegin, paramVLiftBegin, paramHLiftIntra);
		ParametricBezierEvaluator bezierVEnd = new ParametricBezierEvaluator(
				paramHLiftIntra, paramVLiftEnd, paramPointEnd);
		bezierVBegin.proximate(stripVertical, 16);
		bezierVEnd.proximate(stripVertical, 16);
	}

	@Override
	public boolean isCurve() {
		return true;
	}

	@Override
	public boolean isRightAngle() {
		return true;
	}
}
