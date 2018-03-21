package net.aegistudio.sketchuml.path;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import de.dubs.dollarn.PointR;

public class TrifoldRoundRectPath implements TrifoldPath {
	/**
	 * Indicates whether the line is skewed high. If so, the start
	 * point provides the Y and the ending point provides the X.
	 */
	public boolean highSkew = false;

	@Override
	public void makePath(PointR pointStart, PointR pointEnd, 
			List<PointR> separatePoints, List<PointR> controlPoints) {
		// Collect the X and Y points for skewing.
		PointR pointSkewX = highSkew? pointStart : pointEnd;
		PointR pointSkewY = highSkew? pointEnd : pointStart;
		
		// Add the points to the container.
		separatePoints.add(pointStart);
		controlPoints.add(new PointR(pointSkewX.X, pointSkewY.Y));
		separatePoints.add(pointEnd);
	}

	@Override
	public double articulateAndFitness(Vector<PointR> stroke,
			Vector<PointR> trainingStroke,
			int intersectBegin, int intersectEnd) {
		PointR pointStart = stroke.get(0);
		PointR pointEnd = stroke.get(stroke.size() - 1);
		
		// Test for the upper skewed lines.
		PointR pointUpperSkew = new PointR(pointStart.X, pointEnd.Y);
		BezierEvaluator bezierUpper = new BezierEvaluator(
				pointStart, pointUpperSkew, pointEnd);
		Vector<LinePiece> pathUpperSkew = LinePiece
				.quantizeBezier(bezierUpper, 16);
		double varianceUpperSkew = LinePiece.distance( 
				stroke, pathUpperSkew, 0.0, 0.0);
		
		// Test for the lower skewed lines.
		PointR pointLowerSkew = new PointR(pointEnd.X, pointStart.Y);
		BezierEvaluator bezierLower = new BezierEvaluator(
				pointStart, pointLowerSkew, pointEnd);
		Vector<LinePiece> pathLowerSkew = LinePiece
				.quantizeBezier(bezierLower, 16);
		double varianceLowerSkew = LinePiece.distance( 
				stroke, pathLowerSkew, 0.0, 0.0);
		
		// Articulates according to the variance.
		if(varianceUpperSkew < varianceLowerSkew)
			{	highSkew = true; return varianceUpperSkew; }
		else {	highSkew = false; return varianceLowerSkew; }
	}

	@Override
	public TrifoldRoundRectPath clone() {
		TrifoldRoundRectPath path = new TrifoldRoundRectPath();
		path.highSkew = this.highSkew;
		return path;
	}
	
	@Override
	public boolean equals(Object path) {
		if(!(path instanceof TrifoldRoundRectPath)) return false;
		return this.highSkew == ((TrifoldRoundRectPath)path).highSkew;
	}

	@Override
	public void writePath(DataOutputStream outputStream) throws IOException {
		outputStream.writeByte(this.highSkew? 1 : 0);
	}

	@Override
	public void readPath(DataInputStream inputStream) throws IOException {
		this.highSkew = inputStream.readByte() == 1;
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
