package net.aegistudio.sketchuml.path;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.dubs.dollarn.PointR;

public class DefaultPath implements BezierPath {
	public List<PointR> separatePoints = new ArrayList<>();
	
	public List<PointR> controlPoints = new ArrayList<>();

	@Override
	public List<PointR> separatePoints(Rectangle2D boundBegin, 
			Rectangle2D boundEnd) {
		return separatePoints;
	}

	@Override
	public List<PointR> controlPoints(Rectangle2D boundBegin, 
			Rectangle2D boundEnd) {
		return controlPoints;
	}
}
