package net.aegistudio.sketchuml.path;

import java.util.List;
import java.util.Vector;

import de.dubs.dollarn.PointR;

public class TrifoldRectPath implements TrifoldPath {
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
		controlPoints.add(null);
		separatePoints.add(new PointR(pointSkewX.X, pointSkewY.Y));
		controlPoints.add(null);
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
		Vector<LinePiece> pathUpperSkew = new Vector<>();
		pathUpperSkew.add(new LinePiece(pointStart, pointUpperSkew));
		pathUpperSkew.add(new LinePiece(pointUpperSkew, pointEnd));
		double varianceUpperSkew = LinePiece.distance( 
				stroke, pathUpperSkew, 0.0, 0.0);
		
		// Test for the lower skewed lines.
		PointR pointLowerSkew = new PointR(pointEnd.X, pointStart.Y);
		Vector<LinePiece> pathLowerSkew = new Vector<>();
		pathLowerSkew.add(new LinePiece(pointStart, pointLowerSkew));
		pathLowerSkew.add(new LinePiece(pointLowerSkew, pointEnd));	
		double varianceLowerSkew = LinePiece.distance( 
				stroke, pathLowerSkew, 0.0, 0.0);
		
		// Articulates according to the variance.
		if(varianceUpperSkew < varianceLowerSkew)
			{	highSkew = true; return varianceUpperSkew; }
		else {	highSkew = false; return varianceLowerSkew; }
	}

	@Override
	public TrifoldRectPath clone() {
		TrifoldRectPath path = new TrifoldRectPath();
		path.highSkew = this.highSkew;
		return path;
	}
	
	@Override
	public boolean equals(Object path) {
		if(!(path instanceof TrifoldRectPath)) return false;
		return this.highSkew == ((TrifoldRectPath)path).highSkew;
	}
}
