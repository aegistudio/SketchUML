package net.aegistudio.sketchuml.path;

import java.awt.geom.Rectangle2D;
import java.util.List;

import de.dubs.dollarn.PointR;

public interface BezierPath {
	public List<PointR> separatePoints(
			Rectangle2D boundBegin, Rectangle2D boundEnd);
	
	public List<PointR> controlPoints(
			Rectangle2D boundBegin, Rectangle2D boundEnd);
}
