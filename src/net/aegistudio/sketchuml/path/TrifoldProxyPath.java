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
		
		boolean isCenterBegin = statusBegin.status 
				== LinePiece.BoxIntersectStatus.BOX_INTERLEAVED;
		boolean isCenterEnd = statusBegin.status 
				== LinePiece.BoxIntersectStatus.BOX_INTERLEAVED;

		// Calculate the beginning point.
		if(isCenterBegin) {
			pointBegin.X = boundBegin.getCenterX();
			pointBegin.Y = boundBegin.getCenterY();			
		}
		else statusBegin.retrievePoint(pointBegin, boundBegin);
		
		// Calculate the ending point.
		if(isCenterEnd) {
			pointEnd.X = boundEnd.getCenterX();
			pointEnd.Y = boundEnd.getCenterY();
		}
		else statusEnd.retrievePoint(pointEnd, boundEnd);
		
		// Convert and construct the path.
		path.makePath(pointBegin, pointEnd,
				separatePoints, controlPoints);
		
		// Remove the beginning separate and control points.
		if(isCenterBegin) {
			separatePoints.remove(0);
			controlPoints.remove(0);
		}
		
		// Remove the ending separate and control points.
		if(isCenterEnd) {
			if(separatePoints.size() > 0) 
				separatePoints.remove(separatePoints.size() - 1);
			
			if(controlPoints.size() > 0) 
				controlPoints.remove(controlPoints.size() - 1);
		}
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
		return statusBegin.status == LinePiece
				.BoxIntersectStatus.BOX_INTERLEAVED;
	}

	@Override
	public boolean renderInnerLineEnd() {
		return statusEnd.status == LinePiece
				.BoxIntersectStatus.BOX_INTERLEAVED;
	}
}
