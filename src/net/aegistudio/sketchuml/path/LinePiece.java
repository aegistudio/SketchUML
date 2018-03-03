package net.aegistudio.sketchuml.path;

import java.awt.geom.Rectangle2D;
import java.util.Vector;

import de.dubs.dollarn.PointR;

/**
 * Stores the precompiled information of a piece of line, so that it 
 * could be used in the later calculation.
 * 
 * @author Haoran Luo
 */
public class LinePiece {
	public final PointR pointBegin, pointEnd;
	public final double pointDistance;
	public final PointR direction;
	
	public LinePiece(PointR lineBegin, PointR lineEnd) {
		this.pointBegin = new PointR(lineBegin); // A
		this.pointEnd = new PointR(lineEnd);     // B
		
		// Calculate and normalize (AB).
		direction = new PointR();
		direction.combine(-1, pointBegin, 1, pointEnd);
		pointDistance = direction.normalize();
	}
	
	public static class DistanceStatus {
		public double distance;
		
		public int status;
		
		public static final int STATUS_ASIDE = 0;
		
		public static final int STATUS_BSIDE = 1;
		
		public static final int STATUS_CENTER = 2;
	}
	
	public void distance(DistanceStatus status, PointR point) {
		PointR temp = new PointR();
		distance(status, temp, point);
	}
	
	public void distance(DistanceStatus status, PointR temp, PointR point) {
		temp.combine(-1, pointBegin, 1, point);
		if(pointDistance == 0) {
			status.distance = temp.modulus();
			status.status = LinePiece.DistanceStatus.STATUS_ASIDE;
			return;
		}
		
		// Calculate AP * direction.
		double dirDotAP = temp.dot(direction);
		double modulusAP = temp.normalize();
		
		// Judge the distance by the value.
		if(dirDotAP < 0) {
			// P should be on the A side.
			status.distance = modulusAP;
			status.status = DistanceStatus.STATUS_ASIDE;
		}
		else if(dirDotAP > pointDistance) {
			// P should be on the B side.
			temp.combine(-1, pointEnd, 1, point);
			status.distance = temp.modulus();
			status.status = DistanceStatus.STATUS_BSIDE;
		}
		else {
			// P is in middle of AB line.
			status.distance = Math.sqrt(Math.abs(
				modulusAP * modulusAP - dirDotAP * dirDotAP));
			status.status = DistanceStatus.STATUS_CENTER;
		}
	}
	
	/**
	 * @param point the source point to calculate distance.
	 * @param lines the line pieces for calculating distance.
	 * @return the index of line piece at which the minimal distance
	 * is reached.
	 */
	public static int distance(DistanceStatus status,
			PointR point, Vector<LinePiece> linePieces) {
		if(linePieces.size() == 0) return -1;
		
		// Initialize local variables.
		DistanceStatus tempStatus = new DistanceStatus();
		PointR tempPoint = new PointR();
		status.distance = Double.MAX_VALUE; 
		int distanceIndex = -1;
		
		// Loop and search for status.
		for(int i = 0; i < linePieces.size(); ++ i) { 
			linePieces.get(i).distance(tempStatus, tempPoint, point);
			
			// Update the status variable if matches.
			if(tempStatus.distance < status.distance) {
				distanceIndex = i;
				status.distance = tempStatus.distance;
				status.status = tempStatus.status;
			}
		}
		
		return distanceIndex;
	}
	
	public static double distance(Vector<PointR> points, 
			Vector<LinePiece> linePieces, 
			double gammaInner, double gammaOuter) {

		// Calculate the bounding box.
		BoundingBox bound = new BoundingBox(points);
		BoundingBox.BoundingStatus left = new BoundingBox.BoundingStatus();
		BoundingBox.BoundingStatus right = new BoundingBox.BoundingStatus();
		BoundingBox.BoundingStatus top = new BoundingBox.BoundingStatus();
		BoundingBox.BoundingStatus bottom = new BoundingBox.BoundingStatus();
		
		// Calculate the elastic potential of the bound.
		double potential = bound.potential(linePieces, 
				left, right, top, bottom, gammaInner, gammaOuter);
		
		// Collect the distance caused by points and the line pieces.
		DistanceStatus status = new DistanceStatus(); 
		double totalDistance = 0.;
		for(int i = 0; i < points.size(); ++ i) {
			distance(status, points.get(i), linePieces);
			totalDistance += status.distance * status.distance;
		}
		
		// The final distance result.
		return totalDistance + potential;
	}
	
	public static class BoxIntersectStatus {
		public static final int BOX_INTERLEAVED = 0;
		public static final int BOX_TOP = 1;
		public static final int BOX_RIGHT = 2;
		public static final int BOX_BOTTOM = 3;
		public static final int BOX_LEFT = 4;
		
		// The status should be a provided constant value.
		public int status;
		
		// The ratio should be in range -1 ~ +1.
		public double ratio; 
		
		/** Attempt to test if the point is on the boundary. */
		public boolean attemptBound(PointR point, 
				int left, int right, int top, int bottom) {
			double ratio = 0.0;
			
			// The point is either on the top or on the bottom.
			if(left <= point.intX() && point.intX() <= right
					&& status == BOX_INTERLEAVED) {
				int distance = right - left;
				if(distance == 0) distance = 1;
				
				ratio = (1.0 * point.intX() - left) / distance;
				if(point.intY() == top) status = BOX_TOP;
				else if(point.intY() == bottom) status = BOX_BOTTOM;
			}
			
			// The point is either on the left or on the right.
			if(top <= point.intY() && point.intY() <= bottom
					&& status == BOX_INTERLEAVED) {
				int distance = bottom - top;
				if(distance == 0) distance = 1;
				
				ratio = (1.0 * point.intY() - top) / distance;
				if(point.intX() == left) status = BOX_LEFT;
				else if(point.intX() == right) status = BOX_RIGHT;
			}
			
			// Collect the evaluated status and return result.
			if(status != BOX_INTERLEAVED) {
				this.ratio = 2 * ratio - 1;
				return true;
			}
			else return false;
		}
		
		public void retrievePoint(PointR result, Rectangle2D bound) {
			// Fall-back solution for interleaved line.
			if(status == BOX_INTERLEAVED) {
				result.X = bound.getCenterX();
				result.Y = bound.getCenterY();
				return;
			}
			
			// Initialize local variables.
			PointR start = new PointR(), end = new PointR();
			
			// Perform selection of points.
			boolean selected = false;
			switch(status) {
				case BOX_RIGHT:
					if(!selected) {
						start.X = end.X = bound.getMaxX();
						selected = true;
					}
				case BOX_LEFT:
					if(!selected) {
						start.X = end.X = bound.getMinX();
						selected = true;
					}
					
				// Collected case for both left and right.
				start.Y = bound.getMinY();
				end.Y = bound.getMaxY();
				break;
				
				case BOX_TOP:
					if(!selected) {
						start.Y = end.Y = bound.getMinY();
						selected = true;
					}
				case BOX_BOTTOM:
					if(!selected) {
						start.Y = end.Y = bound.getMaxY();
						selected = true;
					}
					
				// Collected case for both top and bottom.
				start.X = bound.getMinX();
				end.X = bound.getMaxX();
				break;
			}
			
			// Perform interpolation on points.
			result.interpolate(ratio * 0.5 + 0.5, start, end);
		}
		
		public boolean distancedAttempt(LinePiece piece, double distance,
				int left, int right, int top, int bottom) {
			if(this.status != BOX_INTERLEAVED) return true;
			if(distance < 0) return false;
			if(distance >= piece.pointDistance) return false;
			PointR interpolateObject = new PointR();
			interpolateObject.combine(1, piece.pointBegin, 
					distance, piece.direction);
			return attemptBound(interpolateObject, left, right, top, bottom);
		}
	}
	
	/**
	 * Try to find how does the line piece specified intersects with 
	 * the specified bounding box.
	 * 
	 * @param status the result to store the intersection status.
	 * @param box the provided bounding box.
	 */
	public void intersectBox(BoxIntersectStatus status, Rectangle2D box) {
		status.status = BoxIntersectStatus.BOX_INTERLEAVED;
		int left = (int)Math.floor(box.getMinX());
		int right = (int)Math.ceil(box.getMaxX());
		int top = (int)Math.floor(box.getMinY());
		int bottom = (int)Math.floor(box.getMaxY());
		
		// Primary test for whether the beginning point or the ending point
		// is on the bounding box.
		if(status.attemptBound(pointBegin, left, right, top, bottom)) return;
		if(status.attemptBound(pointEnd, left, right, top, bottom)) return;
		
		// Either the beginning point or the ending point should be 
		// outside the box. We use exclusive-or (xor) here.
		if(!(this.pointBegin.inside(box) ^ this.pointEnd.inside(box))) return;
		
		// Intersection with the bound of the bounding box.
		if(direction.X != 0) {
			// Test for the left boundary.
			double distanceLeft = (left - pointBegin.X) / direction.X;
			if(status.distancedAttempt(this, distanceLeft, 
					left, right, top, bottom)) return;
			
			// Test for the right boundary.
			double distanceRight = (right - pointBegin.X) / direction.X;
			if(status.distancedAttempt(this, distanceRight, 
					left, right, top, bottom)) return;
		}
		
		if(direction.Y != 0) {
			// Test for the top boundary.
			double distanceTop = (top - pointBegin.Y) / direction.Y;
			if(status.distancedAttempt(this, distanceTop, 
					left, right, top, bottom)) return;
			
			// Test for the bottom boundary.
			double distanceBottom = (bottom - pointBegin.Y) / direction.Y;
			if(status.distancedAttempt(this, distanceBottom, 
					left, right, top, bottom)) return;
		}
	}
}
