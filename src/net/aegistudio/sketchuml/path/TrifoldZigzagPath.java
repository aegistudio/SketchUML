package net.aegistudio.sketchuml.path;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import de.dubs.dollarn.PointR;

public class TrifoldZigzagPath implements TrifoldPath {
	/**
	 * Indicates whether the zigzag line is horizontal.
	 */
	public boolean horizontal = false;
	
	/**
	 * The interpolation ratio between the beginning point
	 * and the ending point.
	 */
	public double ratio = 0.5;
	
	public static final double BLIND_RATIO = 0.1;

	private PointR center = new PointR();
	
	@Override
	public void makePath(PointR pointStart, PointR pointEnd, 
			List<PointR> separatePoints, List<PointR> controlPoints) {
		// Collect the internal points for zigzag-ing.
		center.interpolate(ratio, pointStart, pointEnd);
		
		// Add the points to the container.
		separatePoints.add(pointStart);
		controlPoints.add(null);
		if(horizontal) {
			separatePoints.add(new PointR(pointStart.X, center.Y));
			controlPoints.add(null);
			separatePoints.add(new PointR(pointEnd.X, center.Y));
		}
		else {
			separatePoints.add(new PointR(center.X, pointStart.Y));
			controlPoints.add(null);
			separatePoints.add(new PointR(center.X, pointEnd.Y));
		}
		controlPoints.add(null);
		separatePoints.add(pointEnd);
	}

	static class ParameterZigzag extends ParameterAxial {

		public ParameterZigzag(boolean selectAxisX, boolean selectBegin, 
				PointR pointBegin, PointR pointEnd) {
			super(selectAxisX, selectBegin, pointBegin, pointEnd);
		}

		@Override
		protected void f(PointR result, double[] vector, 
				PointR begin, PointR end) {
			result.interpolate(vector[0], begin, end);
		}

		@Override
		protected void dfdai(PointR result, int i, double[] vector, 
				PointR begin, PointR end) {
			result.X = end.X - begin.X;
			result.Y = end.Y - begin.Y;
		}
	}
	
	@Override
	public double articulateAndFitness(Vector<PointR> stroke,
			Vector<PointR> trainingStroke,
			int intersectBegin, int intersectEnd) {
		PointR pointStart = trainingStroke.get(0);
		PointR pointEnd = trainingStroke.get(trainingStroke.size() - 1);
		ParameterConstant paramStart = new ParameterConstant(pointStart);
		ParameterConstant paramEnd = new ParameterConstant(pointEnd);
		
		// Test for the upper skewed lines.
		ParameterZigzag paramHStart = new ParameterZigzag(
				false, true, pointStart, pointEnd);
		ParameterZigzag paramHEnd = new ParameterZigzag(
				false, false, pointStart, pointEnd);
		Vector<ParametricLinePiece> pathHorizontal = new Vector<>();
		pathHorizontal.add(new ParametricLinePiece(paramStart, paramHStart));
		pathHorizontal.add(new ParametricLinePiece(paramHStart, paramHEnd));
		pathHorizontal.add(new ParametricLinePiece(paramHEnd, paramEnd));
		double[] variableHorizontal = new double[] {ratio};
		double varianceHorizontal = ParametricLinePiece.gradientDescent(
				variableHorizontal, trainingStroke, pathHorizontal, 
				0.2, 1.1, 0.9, 0., 0.5, 10e-3, 50);
		if(	   variableHorizontal[0] < BLIND_RATIO 
			|| variableHorizontal[0] > 1.0 - BLIND_RATIO)
			varianceHorizontal = Double.MAX_VALUE;
		
		// Test for the lower skewed lines.
		ParameterZigzag paramVStart = new ParameterZigzag(
				true, true, pointStart, pointEnd);
		ParameterZigzag paramVEnd = new ParameterZigzag(
				true, false, pointStart, pointEnd);
		Vector<ParametricLinePiece> pathVertical = new Vector<>();
		pathVertical.add(new ParametricLinePiece(paramStart, paramVStart));
		pathVertical.add(new ParametricLinePiece(paramVStart, paramVEnd));
		pathVertical.add(new ParametricLinePiece(paramVEnd, paramEnd));
		double[] variableVertical = new double[] {ratio};
		double varianceVertical = ParametricLinePiece.gradientDescent(
				variableVertical, trainingStroke, pathVertical, 
				0.2, 1.1, 0.9, 0., 0.5, 10e-3, 50);
		if(	   variableVertical[0] < BLIND_RATIO 
			|| variableVertical[0] > 1.0 - BLIND_RATIO)
				varianceVertical = Double.MAX_VALUE;
		
		// Articulates according to the variance.
		Vector<LinePiece> variancePiece = new Vector<>();
		if(varianceHorizontal < varianceVertical)
			{	horizontal = true; ratio = variableHorizontal[0];
				if(varianceHorizontal == Double.MAX_VALUE)
					return Double.MAX_VALUE;
				pathHorizontal.stream()
					.map(p -> p.evaluate(variableHorizontal))
					.forEach(variancePiece::add); }
		else {	horizontal = false; ratio = variableVertical[0];
				if(varianceVertical == Double.MAX_VALUE)
					return Double.MAX_VALUE;
				pathVertical.stream()
					.map(p -> p.evaluate(variableVertical))
					.forEach(variancePiece::add); }
		return LinePiece.distance(stroke, variancePiece, 0.0, 0.3);
	}

	@Override
	public TrifoldZigzagPath clone() {
		TrifoldZigzagPath path = new TrifoldZigzagPath();
		path.horizontal = this.horizontal;
		path.ratio = this.ratio;
		return path;
	}
	
	@Override
	public boolean equals(Object path) {
		if(!(path instanceof TrifoldZigzagPath)) return false;
		if(this.horizontal != ((TrifoldZigzagPath)path).horizontal) return false;
		return ((TrifoldZigzagPath)path).ratio == this.ratio;
	}
	

	@Override
	public void writePath(DataOutputStream outputStream) throws IOException {
		outputStream.writeByte(this.horizontal? 1 : 0);
		outputStream.writeDouble(this.ratio);
	}

	@Override
	public void readPath(DataInputStream inputStream) throws IOException {
		this.horizontal = inputStream.readByte() == 1;
		this.ratio = inputStream.readDouble();
	}
}