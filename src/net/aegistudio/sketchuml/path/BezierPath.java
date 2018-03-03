package net.aegistudio.sketchuml.path;

import java.awt.geom.Rectangle2D;
import java.util.List;

import de.dubs.dollarn.PointR;

/**
 * Represents a poly-line path consists of straight line 
 * piece and bezier piece. 
 * 
 * The path could change the internal separating and control 
 * point according to the position and boundary of its 
 * connecting objects.
 * 
 * @author Haoran Luo
 */
public interface BezierPath {
	/**
	 * Retrieve the separating points on the path.
	 * 
	 * @param boundBegin the boundary of the beginning object.
	 * @param boundEnd the boundary of the ending object.
	 * @return the separating points.
	 */
	public List<PointR> separatePoints(
			Rectangle2D boundBegin, Rectangle2D boundEnd);
	
	/**
	 * Retrieve the control points on the path.
	 * 
	 * @param boundBegin the boundary of the beginning object.
	 * @param boundEnd the boundary of the ending object.
	 * @return the control points.
	 */
	public List<PointR> controlPoints(
			Rectangle2D boundBegin, Rectangle2D boundEnd);
	
	/**
	 * @return should calculate the arrow direction according
	 * to the line's tangent instead of the intersection point
	 * on the objects. 
	 */
	public boolean arrowDirectionOnLine();
	
	/**
	 * @return should the inner line inside the beginning box be rendered.
	 */
	public boolean renderInnerLineBegin();
	
	/**
	 * @return should the inner line inside the ending box be rendered.
	 */
	public boolean renderInnerLineEnd();
}
