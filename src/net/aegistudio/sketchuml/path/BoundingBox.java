package net.aegistudio.sketchuml.path;

import java.util.Vector;

import de.dubs.dollarn.PointR;

public class BoundingBox {
	public final double pointLeft, pointRight, pointTop, pointBottom;
	
	public BoundingBox(Vector<PointR> points) {
		// Initialize local variables.
		double pointLeft = Double.MAX_VALUE, pointRight = - Double.MAX_VALUE, 
				pointTop = Double.MAX_VALUE, pointBottom = - Double.MAX_VALUE;
		
		// Profile the bounding box of the points.
		for(PointR point : points) {
			double x = point.X, y = point.Y;
			pointLeft = Math.min(x, pointLeft);
			pointRight = Math.max(x, pointRight);
			pointTop = Math.min(y, pointTop);
			pointBottom = Math.max(y, pointBottom);
		}
		
		// Write the profile data to the bounding box.
		this.pointLeft = pointLeft; this.pointRight = pointRight;
		this.pointTop = pointTop; this.pointBottom = pointBottom;
	}
	
	public static class BoundingStatus {
		public int index; public boolean lineStart;
		public double value, tension;
		
		public void update(boolean greater, int i, 
				double value, boolean lineStart) {
			// Calculate the condition.
			boolean shouldUpdate;
			if(greater) shouldUpdate = value > this.value;
			else shouldUpdate = value < this.value;
			
			// Update when matches condition.
			if(shouldUpdate) {
				this.value = value;
				this.index = i;
				this.lineStart = lineStart;
			}
		}
		
		public boolean inner() {
			return tension < 0;
		} 
		
		public double gammaTerm(double gammaInner, double gammaOuter) {
			return inner()? gammaInner : gammaOuter;
		}
		
		public double potential(double gammaInner, double gammaOuter) {
			return gammaTerm(gammaInner, gammaOuter) 
					* tension * tension;
		}
	}
	
	/**
	 * Calculate the total elastic potential from the given 
	 * parameters.
	 *                            
	 * @param linePieces The line pieces to profile.
	 * @param left the object to put the result of left.
	 * @param right the object to put the result of left.
	 * @param top the object to put the result of left.
	 * @param bottom the object to put the result of left.
	 * @param gammaInner tension when the bound is inner.
	 * @param gammaOuter tension when the bound is outer.
	 * @return the total potential of the bounding box.
	 */
	public double potential(Vector<LinePiece> linePieces,
			BoundingStatus left, BoundingStatus right,
			BoundingStatus top, BoundingStatus bottom,
			double gammaInner, double gammaOuter) {
		if(linePieces.size() == 0) return 0;
		
		// Initialize result's condition.
		left.index = right.index = top.index = bottom.index = -1;
		left.value = top.value = Double.MAX_VALUE;
		right.value = bottom.value = - Double.MAX_VALUE;
		 
		// Loop and upate the result's condition.
		for(int i = 0; i < linePieces.size(); ++ i) {
			LinePiece piece = linePieces.get(i);
			double xA = piece.pointBegin.X, yA = piece.pointBegin.Y, 
					xB = piece.pointEnd.X, yB = piece.pointEnd.Y;
			
			left.update(false, i, xA, true);
			left.update(false, i, xB, false);
			right.update(true, i, xA, true);
			right.update(true, i, xB, false);
			top.update(false, i, yA, true);
			top.update(false, i, yB, false);
			bottom.update(true, i, yA, true);
			bottom.update(true, i, yB, false);
		}
		
		// Collect and calculate the tension.
		left.tension = pointLeft - left.value;
		right.tension = right.value - pointRight;
		top.tension = pointTop - top.value;
		bottom.tension = bottom.value - pointBottom;
		
		// Calculate the final results.
		return left.potential(gammaInner, gammaOuter) 
				+ right.potential(gammaInner, gammaOuter) 
				+ top.potential(gammaInner, gammaOuter) 
				+ bottom.potential(gammaInner, gammaOuter);
	}
}
