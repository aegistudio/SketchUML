package net.aegistudio.sketchuml.path;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

import de.dubs.dollarn.PointR;

public class DefaultPathView implements PathView<DefaultPath> {

	@Override
	public void render(Graphics2D g2d, boolean selected,
			DefaultPath pathObject, LineStyle line, 
			Rectangle2D boundBegin, ArrowStyle arrowBegin,
			Rectangle2D boundEnd, ArrowStyle arrowEnd) {
		
		// Prepare for rendering.
		g2d.setColor(selected? Color.GRAY : Color.BLACK);
		g2d.setStroke(new BasicStroke(selected? 4.0f : 3.0f));
		
		// Collect path object information.
		int numPoints = pathObject.separatePoints.size();
		PointR pointBegin = new PointR(
				boundBegin.getCenterX(), 
				boundBegin.getCenterY());
		PointR pointEnd = new PointR(
				boundEnd.getCenterX(), 
				boundEnd.getCenterY());
		PointR[] points = new PointR[numPoints + 2];
		points[0] = pointBegin;
		points[points.length - 1] = pointEnd;
		for(int i = 0; i < numPoints; ++ i) 
			points[i + 1] = pathObject.separatePoints.get(i);
		
		// Perform drawings.
		QuadCurve2D q2d = new QuadCurve2D.Double();
		for(int i = 0; i < numPoints + 1; ++ i) {
			int xBegin = (int)points[i].X;
			int yBegin = (int)points[i].Y;
			int xEnd = (int)points[i + 1].X;
			int yEnd = (int)points[i + 1].Y;
			
			// Render knot when selected.
			if(selected) {
				g2d.fillRect(xBegin - 3, yBegin - 3, 6, 6);
				g2d.fillRect(xEnd - 3, yEnd - 3, 6, 6);
			}
			
			if(pathObject.controlPoints.size() <= i || 
					pathObject.controlPoints.get(i) == null)
				g2d.drawLine(xBegin, yBegin, xEnd, yEnd);
			else {
				PointR control = pathObject.controlPoints.get(i);
				int xCtrl = (int)control.X; int yCtrl = (int)control.Y;
				q2d.setCurve(xBegin, yBegin, xCtrl, yCtrl, xEnd, yEnd);
				g2d.draw(q2d);
				
				// Render control points when selected.
				if(selected)
					g2d.fillRect(xCtrl - 3, yCtrl - 3, 6, 6);
			}
		}
	}

}
