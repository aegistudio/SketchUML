package net.aegistudio.sketchuml.path;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.dubs.dollarn.PointR;

public class TrifoldProxyPath implements BezierPath {
	public final LinePiece.BoxIntersectStatus statusBegin 
		= new LinePiece.BoxIntersectStatus();
	public final LinePiece.BoxIntersectStatus statusEnd 
		= new LinePiece.BoxIntersectStatus();
	
	/** The path style that is proxied. */
	public TrifoldPath path = new TrifoldLinePath();
	
	/**
	 * Set the internal trifold path.
	 * 
	 * @param path the trifold path implementation to set.
	 */
	public void setPath(TrifoldPath path) {
		if(path == null) path = new TrifoldLinePath();
		else this.path = path;
	}
	
	/** The object created and stores the beginning points. */
	public final PointR pointBegin = new PointR();
	
	/** The object created and stores the ending point. */
	public final PointR pointEnd = new PointR();
	
	/** The container for storing the separating points. */
	public final List<PointR> separatePoints = new ArrayList<>();
	
	/** The container for storing the control points. */
	public final List<PointR> controlPoints = new ArrayList<>();

	/**
	 * Update the internal object's state when the boundary of beginning or
	 * ending object changes.
	 * 
	 * @param boundBegin the boundary of the begining object.
	 * @param boundEnd the boundary of the ending object.
	 */
	private void preparePoints(Rectangle2D boundBegin, Rectangle2D boundEnd) {
		// Prepare the empty container.
		separatePoints.clear();
		controlPoints.clear();
		
		boolean isCenterBegin = isCenter(statusBegin);
		boolean isCenterEnd = isCenter(statusEnd);

		// Calculate the beginning and ending point.
		retrieveObjectPoint(pointBegin, boundBegin, statusBegin);
		retrieveObjectPoint(pointEnd, boundEnd, statusEnd);
		
		// Convert and construct the path.
		path.makePath(pointBegin, pointEnd,
				separatePoints, controlPoints);
		
		// Remove the beginning separate and control points.
		if(isCenterBegin) {
			int index = separatePoints.indexOf(pointBegin);
			if(index >= 0) {
				separatePoints.remove(index);
				//controlPoints.remove(index);
			}
		}
		else controlPoints.add(0, null);
		
		// Remove the ending separate and control points.
		if(isCenterEnd) {
			int index = separatePoints.indexOf(pointEnd);
			if(index >= 0) {
				separatePoints.remove(index);
				//if(index - 1 >= 0)
				//	controlPoints.remove(index - 1);
			}
		}
		else controlPoints.add(null);
	}
	
	@Override
	public List<PointR> separatePoints(
			Rectangle2D boundBegin, Rectangle2D boundEnd) {
		
		preparePoints(boundBegin, boundEnd);
		return separatePoints;
	}

	@Override
	public List<PointR> controlPoints(Rectangle2D boundBegin, 
			Rectangle2D boundEnd) {
		
		preparePoints(boundBegin, boundEnd);
		return controlPoints;
	}

	@Override
	public boolean arrowDirectionOnLine() {
		return true;
	}

	@Override
	public boolean renderInnerLineBegin() {
		return isCenter(statusBegin);
	}

	@Override
	public boolean renderInnerLineEnd() {
		return isCenter(statusEnd);
	}
	
	public static boolean isCenter(LinePiece.BoxIntersectStatus status) {
		return status.status == LinePiece
				.BoxIntersectStatus.BOX_INTERLEAVED;
	}
	
	public static void retrieveObjectPoint(PointR point, Rectangle2D bound, 
			LinePiece.BoxIntersectStatus status) {
		if(isCenter(status)) {
			point.X = bound.getCenterX();
			point.Y = bound.getCenterY();
		}
		else status.retrievePoint(point, bound);
	}
}
