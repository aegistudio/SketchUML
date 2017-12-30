package net.aegistudio.sketchuml.path;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

import de.dubs.dollarn.PointR;

public class DefaultPathView implements PathView<DefaultPath> {
	public static final float[][] DASH_DECORATION= { 
			/*DASH*/{10.f}, /*DOT*/{2.f}, /*DASHDOT*/{10.f, 4.0f, 4.f} };
	public static final float ARROW_ZONAL = 20;
	public static final float ARROW_HORIZONTAL = 7;
	
	private void intersectBox(Rectangle2D rect, PointR outPoint,
			PointR resultDirection, PointR resultIntersection) {
		
		// Difference and normalization.
		resultDirection.X = outPoint.X - rect.getCenterX();
		resultDirection.Y = outPoint.Y - rect.getCenterY();
		double modulus = Math.sqrt(
				resultDirection.X * resultDirection.X +
				resultDirection.Y * resultDirection.Y);
		resultDirection.X /= modulus; 
		resultDirection.Y /= modulus;
		
		// Calculate vector length.
		double widthRatio = Math.abs(rect.getWidth() 
				/ (2 * resultDirection.X));
		double heightRatio = Math.abs(rect.getHeight() 
				/ (2 * resultDirection.Y));
		double vectorLength = 
				resultDirection.X == 0? heightRatio:
				resultDirection.Y == 0? widthRatio:
				Math.min(widthRatio, heightRatio);
		
		// Output result intersection.
		resultIntersection.X = rect.getCenterX() 
				+ vectorLength * resultDirection.X;
		resultIntersection.Y = rect.getCenterY() 
				+ vectorLength * resultDirection.Y;
	}
	
	private void intersectBezier(Rectangle2D rect, QuadCurve2D bezier,
			PointR resultDirection, PointR resultIntersection) {
		
		// The control point coordinates.
		double p1X = bezier.getCtrlX();
		double p1Y = bezier.getCtrlY();
		double p0X = bezier.getX1();
		double p0Y = bezier.getY1();
		double p2X = bezier.getX2();
		double p2Y = bezier.getY2();
		
		for(double t = 0.0; t <= 1.0; t += 0.01) {
			double x = (1 - t) * (1 - t) * p0X + 
					2 * (1 - t) * t * p1X + t * t * p2X;
			double y = (1 - t) * (1 - t) * p0Y + 
					2 * (1 - t) * t * p1Y + t * t * p2Y;
			
			if(!rect.contains(x, y)) {
				// Calculate intersection.
				resultIntersection.X = x;
				resultIntersection.Y = y;
				
				// Calculate bezier curve's tangent.
				resultDirection.X = 2 * (1 - t) * (p1X - p0X)
						+ 2 * t * (p2X - p1X);
				resultDirection.Y = 2 * (1 - t) * (p1Y - p0Y)
						+ 2 * t * (p2Y - p1Y);
				double modulus = Math.sqrt(
						resultDirection.X * resultDirection.X +
						resultDirection.Y * resultDirection.Y);
				resultDirection.X /= modulus; 
				resultDirection.Y /= modulus;
				
				return;
			}
		}
	}
	
	@Override
	public void render(Graphics2D g2d, boolean selected,
			DefaultPath pathObject, LineStyle line, 
			Rectangle2D boundBegin, ArrowStyle arrowBegin,
			Rectangle2D boundEnd, ArrowStyle arrowEnd) {
		
		// Prepare for rendering.
		g2d.setColor(selected? Color.GRAY : Color.BLACK);
		
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
		
		// The intersection and direction points.
		PointR intersectBegin = new PointR();
		PointR intersectEnd = new PointR();
		PointR directionBegin = new PointR();
		PointR directionEnd = new PointR();
		
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
			
			// Configure line style.
			if(!LineStyle.COHERENT.equals(line)) {
				int order = line.ordinal() - 1/*COHERENT*/;
				float[] dash = DASH_DECORATION[order];
				g2d.setStroke(new BasicStroke(selected? 3.0f : 2.0f, 
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
						1.0f, dash, 0.0f));
			}
			else g2d.setStroke(new BasicStroke(selected? 3.0f : 2.0f));
			
			if(pathObject.controlPoints.size() <= i || 
					pathObject.controlPoints.get(i) == null) {
				// Just draw a direct line.
				g2d.drawLine(xBegin, yBegin, xEnd, yEnd);
				
				// Find intersection.
				if(i == 0) intersectBox(boundBegin, points[1], 
						directionBegin, intersectBegin);
				if(i == numPoints) intersectBox(boundEnd, 
						points[numPoints], directionEnd, intersectEnd);
			}
			else {
				// Retrieve control point and paint.
				PointR control = pathObject.controlPoints.get(i);
				int xCtrl = (int)control.X; int yCtrl = (int)control.Y;
				q2d.setCurve(xBegin, yBegin, xCtrl, yCtrl, xEnd, yEnd);
				g2d.draw(q2d);
				
				// Find intersection.
				if(i == 0) intersectBezier(boundBegin, 
						q2d, directionBegin, intersectBegin);
				if(i == numPoints) {
					// Makes it easier for intersection.
					q2d.setCurve(xEnd, yEnd, xCtrl, yCtrl, xBegin, yBegin);
					intersectBezier(boundEnd, q2d, directionEnd, intersectEnd);
				}
				
				// Render control points when selected.
				g2d.setStroke(new BasicStroke(selected? 3.0f : 2.0f));
				if(selected)
					g2d.fillRect(xCtrl - 3, yCtrl - 3, 6, 6);
			}
		}
		
		// Perform arrow rendering.
		g2d.setStroke(new BasicStroke(3));
		g2d.setColor(selected? Color.GRAY : Color.BLACK);
		renderArrow(g2d, intersectBegin, directionBegin, 
				arrowBegin, selected);
		renderArrow(g2d, intersectEnd, directionEnd, 
				arrowEnd, selected);
	}
	
	private void renderArrow(Graphics2D g2d, PointR origin, 
			PointR direction, ArrowStyle arrow, boolean selected) {
		
		PointR orthoDirection = new PointR();
		orthoDirection.X = direction.Y;
		orthoDirection.Y = -direction.X;
		
		// Basic two points.
		int x0 = (int)origin.X; int y0 = (int)origin.Y;
		int x1 = (int)(origin.X + ARROW_ZONAL * direction.X);
		int y1 = (int)(origin.Y + ARROW_ZONAL * direction.Y);
		
		// Orthogonal extension.
		int x2 = x1 + (int)(ARROW_HORIZONTAL * orthoDirection.X);
		int y2 = y1 + (int)(ARROW_HORIZONTAL * orthoDirection.Y);
		int x3 = x1 - (int)(ARROW_HORIZONTAL * orthoDirection.X);
		int y3 = y1 - (int)(ARROW_HORIZONTAL * orthoDirection.Y);
		
		switch(arrow) {
			// Render the fish bone arrows.
			case FISHBONE:
				g2d.drawLine(x0, y0, x1, y1);
				g2d.drawLine(x0, y0, x2, y2);
				g2d.drawLine(x0, y0, x3, y3);
			break;
			
			// Render the triangle families.
			case TRIANGLE_EMPTY:
			case TRIANGLE_FILLED:
				int[] xsT = new int[] {x0, x2, x3};
				int[] ysT = new int[] {y0, y2, y3};
				
				// Fill with color.
				if(arrow.equals(ArrowStyle.TRIANGLE_EMPTY))
					g2d.setColor(Color.WHITE);
				else g2d.setColor(selected? Color.GRAY : Color.BLACK);
				g2d.fillPolygon(xsT, ysT, 3);
				
				// Draw out border.
				g2d.setColor(selected? Color.GRAY : Color.BLACK);
				g2d.drawPolygon(xsT, ysT, 3);
			break;
			
			// Render the diamond families.
			case DIAMOND_EMPTY:
			case DIAMOND_FILLED:
				int xRD = (int)(ARROW_ZONAL * - 0.5 * direction.X);
				int yRD = (int)(ARROW_ZONAL * - 0.5 * direction.Y);
				int[] xsD = new int[] {x0, x2 + xRD, x1, x3 + xRD };
				int[] ysD = new int[] {y0, y2 + yRD, y1, y3 + yRD };
				
				// Fill with color.
				if(arrow.equals(ArrowStyle.DIAMOND_EMPTY))
					g2d.setColor(Color.WHITE);
				else g2d.setColor(selected? Color.GRAY : Color.BLACK);
				g2d.fillPolygon(xsD, ysD, 4);
				
				// Draw out border.
				g2d.setColor(selected? Color.GRAY : Color.BLACK);
				g2d.drawPolygon(xsD, ysD, 4);
			break;
			
			// Render the circle families.
			case CIRCLE_EMPTY:
			case CIRCLE_FILLED:
				float diameter = Math.min(ARROW_ZONAL, ARROW_HORIZONTAL);
				int xcC = (int)(x0 + 0.5 * diameter * direction.X);
				int ycC = (int)(y0 + 0.5 * diameter * direction.Y);
				int radius = (int)(0.5 * diameter);
				
				// Fill with color.
				if(arrow.equals(ArrowStyle.CIRCLE_EMPTY))
					g2d.setColor(Color.WHITE);
				else g2d.setColor(selected? Color.GRAY : Color.BLACK);
				g2d.fillOval(xcC - radius, ycC - radius, 
						2 * radius, 2 * radius);
				
				// Draw out border.
				g2d.setColor(selected? Color.GRAY : Color.BLACK);
				g2d.drawOval(xcC - radius, ycC - radius, 
						2 * radius, 2 * radius);
			break;
			
			default: break;
		}
	}

}
