package net.aegistudio.sketchuml.path;

import java.util.List;
import java.util.Vector;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.path.ParametricLinePiece.Parameter;

public class TrifoldLiftPath extends TrifoldAbstractLiftPath {
	@Override
	public void makePath(
			PointR pointStart, PointR pointEnd,
			PointR levelStart, PointR levelEnd,
			List<PointR> separatePoints, List<PointR> controlPoints) {
		
		// Add the points to the path.
		separatePoints.add(pointStart);
		controlPoints.add(null);
		separatePoints.add(levelStart);
		controlPoints.add(null);
		separatePoints.add(levelEnd);
		controlPoints.add(null);
		separatePoints.add(pointEnd);
	}

	@Override
	protected void stripHorizontal(Vector<ParametricLinePiece> stripHorizontal, 
			Parameter paramPointBegin, Parameter paramPointEnd, 
			Parameter paramHLiftBegin, Parameter paramHLiftEnd) {
		
		stripHorizontal.add(new ParametricLinePiece(
				paramPointBegin, paramHLiftBegin));
		stripHorizontal.add(new ParametricLinePiece(
				paramHLiftBegin, paramHLiftEnd));
		stripHorizontal.add(new ParametricLinePiece(
				paramHLiftEnd, paramPointEnd));
	}

	@Override
	protected void stripVertical(Vector<ParametricLinePiece> stripVertical, 
			Parameter paramPointBegin, Parameter paramPointEnd, 
			Parameter paramVLiftBegin, Parameter paramVLiftEnd) {
		
		stripVertical.add(new ParametricLinePiece(
				paramPointBegin, paramVLiftBegin));
		stripVertical.add(new ParametricLinePiece(
				paramVLiftBegin, paramVLiftEnd));
		stripVertical.add(new ParametricLinePiece(
				paramVLiftEnd, paramPointEnd));
	}
}
