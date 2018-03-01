package net.aegistudio.sketchuml.path;

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
}
