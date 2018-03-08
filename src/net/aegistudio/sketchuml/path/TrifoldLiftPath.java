package net.aegistudio.sketchuml.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.path.ParametricLinePiece.Parameter;

public class TrifoldLiftPath implements TrifoldPath {
	/** Whether the lifting's level is horizontal or vertical. */
	public boolean horizontal = true;
	
	/** How many pixels should the path be lifted. */
	public int lift = 0;

	/** Ignore paths that has exceeded the blinded pixel. */
	public static final int BLINDED_PIXEL = 30;
	
	@Override
	public void makePath(PointR pointStart, PointR pointEnd, 
			List<PointR> separatePoints, List<PointR> controlPoints) {
		PointR levelStart = new PointR(pointStart);
		PointR levelEnd = new PointR(pointEnd);
		
		// Calculate the lifted points.
		if(horizontal) {
			if(lift > 0) levelStart.Y = levelEnd.Y = 
				Math.max(levelStart.Y, levelEnd.Y) + lift;
			else levelStart.Y = levelEnd.Y = 
				Math.min(levelStart.Y, levelEnd.Y) + lift;
		}
		else {
			if(lift > 0) levelStart.X = levelEnd.X = 
				Math.max(levelStart.X, levelEnd.X) + lift;
			else levelStart.X = levelEnd.X = 
				Math.min(levelStart.X, levelEnd.X) + lift;			
		}
		
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
	public double articulateAndFitness(Vector<PointR> stroke, 
			Vector<PointR> trainingStroke, int intersectBegin,
			int intersectEnd) {
		if(trainingStroke.size() <= 1) return Double.MAX_VALUE;
		PointR boundMin = new PointR(trainingStroke.get(0));
		PointR boundMax = new PointR(trainingStroke.get(0));
		
		// Find the boundary of the training stroke.
		for(int i = 1; i < trainingStroke.size(); ++ i) {
			PointR pointCurrent = trainingStroke.get(i);
			boundMin.X = Math.min(pointCurrent.X, boundMin.X);
			boundMin.Y = Math.min(pointCurrent.Y, boundMin.Y);
			boundMax.X = Math.max(pointCurrent.X, boundMax.X);
			boundMax.Y = Math.max(pointCurrent.Y, boundMax.Y);
		}
		
		// Commonly used parameters.
		PointR pointBegin = trainingStroke.get(0);
		PointR pointEnd = trainingStroke.get(trainingStroke.size() - 1);
		Parameter paramPointBegin = new ParameterConstant(pointBegin);
		Parameter paramPointEnd = new ParameterConstant(pointEnd);
		Parameter paramHLiftBegin = new ParameterLift(
				false, true, pointBegin, pointEnd);
		Parameter paramHLiftEnd = new ParameterLift(
				false, false, pointBegin, pointEnd);
		Parameter paramVLiftBegin = new ParameterLift(
				true, true, pointBegin, pointEnd);
		Parameter paramVLiftEnd = new ParameterLift(
				true, false, pointBegin, pointEnd);
		
		// Construct horizontal line strides.
		Vector<ParametricLinePiece> stripHorizontal = new Vector<>();
		stripHorizontal.add(new ParametricLinePiece(
				paramPointBegin, paramHLiftBegin));
		stripHorizontal.add(new ParametricLinePiece(
				paramHLiftBegin, paramHLiftEnd));
		stripHorizontal.add(new ParametricLinePiece(
				paramHLiftEnd, paramPointEnd));
		
		// Construct vertical line strides.
		Vector<ParametricLinePiece> stripVertical = new Vector<>();
		stripVertical.add(new ParametricLinePiece(
				paramPointBegin, paramVLiftBegin));
		stripVertical.add(new ParametricLinePiece(
				paramVLiftBegin, paramVLiftEnd));
		stripVertical.add(new ParametricLinePiece(
				paramVLiftEnd, paramPointEnd));
		
		// Try perform reduction and record the results.
		List<Vector<ParametricLinePiece>> linePieces = new ArrayList<>();
		List<Double> lineParameters = new ArrayList<>();
		List<Double> lineVariances = new ArrayList<>();
		double[] lineParameter = new double[1];
		
		// The points for comparing.
		PointR pointMin = new PointR();
		PointR pointMax = new PointR();
		pointMin.X = Math.min(pointBegin.X, pointEnd.X);
		pointMax.X = Math.max(pointBegin.X, pointEnd.X);
		pointMin.Y = Math.min(pointBegin.Y, pointEnd.Y);
		pointMax.Y = Math.max(pointBegin.Y, pointEnd.Y);
		
		// Add the descending results to the list.
		if((lineParameter[0] = boundMin.X - pointMin.X) < - BLINDED_PIXEL) 
			performDescent(trainingStroke, linePieces, lineParameters, 
					lineVariances, lineParameter, stripVertical);
		if((lineParameter[0] = boundMin.Y - pointMin.Y) < - BLINDED_PIXEL) 
			performDescent(trainingStroke, linePieces, lineParameters, 
					lineVariances, lineParameter, stripHorizontal);
		if((lineParameter[0] = boundMax.X - pointMax.X) > + BLINDED_PIXEL) 
			performDescent(trainingStroke, linePieces, lineParameters, 
					lineVariances, lineParameter, stripVertical);
		if((lineParameter[0] = boundMax.Y - pointMax.Y) > + BLINDED_PIXEL) 
			performDescent(trainingStroke, linePieces, lineParameters, 
					lineVariances, lineParameter, stripHorizontal);
		
		// Find the minimal variance and return the points.
		if(lineVariances.size() == 0) return Double.MAX_VALUE;
		double minVariance = lineVariances.get(0); int minIndex = 0;
		for(int i = 1; i < lineVariances.size(); ++ i)
			if(lineVariances.get(i) < minVariance) {
				minIndex = i; minVariance = lineVariances.get(i);
			}
		Vector<ParametricLinePiece> selectedPiece = linePieces.get(minIndex);
		Vector<LinePiece> minimalLinePieces = new Vector<>();
		lift = (int)(lineParameter[0] = lineParameters.get(minIndex));
		horizontal = selectedPiece == stripHorizontal;
		selectedPiece.stream().map(p -> p.evaluate(lineParameter))
			.forEach(minimalLinePieces::add);
		return LinePiece.distance(stroke, minimalLinePieces, .0, .3);
	}
	
	private void performDescent(Vector<PointR> trainingStroke,
		List<Vector<ParametricLinePiece>> linePieces,
		List<Double> lineParameters, List<Double> lineVariances, 
		double[] lineParameter, Vector<ParametricLinePiece> strip) {
		
		// Perform gradient descent.
		double variance = ParametricLinePiece.gradientDescent(
			lineParameter, trainingStroke, strip, 
			2.0, 1.1, 0.9, .0, .3, 1e-2, 50);
		
		// Add the result to the list.
		if(Math.abs(lineParameter[0]) < BLINDED_PIXEL) return;
		linePieces.add(strip);
		lineParameters.add(lineParameter[0]);
		lineVariances.add(variance);
	}
	
	static class ParameterLift extends ParameterAxial {
		public ParameterLift(boolean selectAxisX, boolean selectBegin, 
				PointR pointBegin, PointR pointEnd) {
			super(selectAxisX, selectBegin, pointBegin, pointEnd);
		}

		@Override
		protected void f(PointR result, double[] vector, 
				PointR begin, PointR end) {
			if(vector[0] > 0) {
				result.X = Math.max(begin.X, end.X) + vector[0];
				result.Y = Math.max(begin.Y, end.Y) + vector[0];
			}
			else {
				result.X = Math.min(begin.X, end.X) + vector[0];
				result.Y = Math.min(begin.Y, end.Y) + vector[0];				
			}
		}

		@Override
		protected void dfdai(PointR result, int i, double[] vector, 
				PointR begin, PointR end) {
			result.X = result.Y = 1;
		}
	}
}
