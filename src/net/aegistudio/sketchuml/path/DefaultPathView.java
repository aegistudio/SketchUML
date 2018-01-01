package net.aegistudio.sketchuml.path;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.dubs.dollarn.PointR;

public class DefaultPathView implements PathView<DefaultPath> {
	public static final float[][] DASH_DECORATION= { 
			/*DASH*/{10.f}, /*DOT*/{2.f}, /*DASHDOT*/{10.f, 4.0f, 4.f} };
	public static final float ARROW_ZONAL = 20;
	public static final float ARROW_HORIZONTAL = 7;
	public static final int BEZIER_RENDER = 20;
	
	private void intersectBox(Rectangle2D rect, PointR outPoint,
			PointR resultDirection, PointR resultIntersection) {
		
		// Difference and normalization.
		resultDirection.X = outPoint.X - rect.getCenterX();
		resultDirection.Y = outPoint.Y - rect.getCenterY();
		resultDirection.normalize();
		
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
	
	private void intersectBezier(Rectangle2D rect, BezierEvaluator evaluator,
			PointR resultDirection, PointR resultIntersection) {
		
		// The control point coordinates.
		/*double p1X = bezier.getCtrlX();
		double p1Y = bezier.getCtrlY();
		double p0X = bezier.getX1();
		double p0Y = bezier.getY1();
		double p2X = bezier.getX2();
		double p2Y = bezier.getY2();
		
		BezierEvaluator evaluator = new BezierEvaluator(
				p0X, p1X, p2X, p0Y, p1Y,  p2Y);*/
		
		for(double t = 0.0; t <= 1.0; t += 0.01) {
			evaluator.evaluate(t, resultIntersection);
			
			// Check whether there's intersection.
			if(!resultIntersection.inside(rect)) {
				evaluator.tangent(t, resultDirection);
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
				
				int xCtrl = (int)control.X; 
				int yCtrl = (int)control.Y;
				BezierEvaluator evaluator = new BezierEvaluator(
						points[i], control, points[i + 1]);
				PointR current = new PointR();
				int[] bx = new int[BEZIER_RENDER + 1];
				int[] by = new int[BEZIER_RENDER + 1];
				for(int j = 0; j <= BEZIER_RENDER; ++ j) {
					evaluator.evaluate(1.0 / BEZIER_RENDER * j, current);
					bx[j] = (int)current.X;
					by[j] = (int)current.Y;
				}
				g2d.drawPolyline(bx, by, BEZIER_RENDER + 1);
				
				// Find intersection.
				if(i == 0) intersectBezier(boundBegin, 
						evaluator, directionBegin, intersectBegin);
				if(i == numPoints) {
					// Makes it easier for intersection.
					//q2d.setCurve(xEnd, yEnd, xCtrl, yCtrl, xBegin, yBegin);
					evaluator = new BezierEvaluator(points[i + 1], control, points[i]);
					intersectBezier(boundEnd, evaluator, directionEnd, intersectEnd);
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

	private double lineDistance(PointR pBegin, PointR pEnd, PointR position) {
		PointR connect = new PointR();
		PointR evaluate0 = new PointR();
		PointR evaluate1 = new PointR();
		
		// Convert into point difference.
		connect.combine(1, pEnd, -1, pBegin);
		evaluate0.combine(1, position, -1, pBegin);
		evaluate1.combine(1, position, -1, pEnd);
		
		// Notice, the connect is normalized.
		double modulus = connect.normalize();
		
		if(modulus == 0) 
			return evaluate0.normalize();
		else {
			double d1 = connect.dot(evaluate0);
			double d2 = connect.dot(evaluate1);
			
			if(d1 * d2 >= 0) return Math.min(
				evaluate0.modulus(), evaluate1.modulus());
			else {
				double dot = connect.dot(evaluate0);
				double p01 = evaluate0.modulus();
				return Math.sqrt(p01 * p01 - dot * dot);
			}
		}
	}
	
	@Override
	public double distance(DefaultPath pathObject, PointR position, 
			Rectangle2D boundBegin, Rectangle2D boundEnd) {
		double distance = Double.POSITIVE_INFINITY;
		
		// Can never select the internal part of a bounding box.
		if(position.inside(boundBegin)) return distance;
		if(position.inside(boundEnd)) return distance;
		
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

		// Perform calculation.
		for(int i = 0; i < numPoints + 1; ++ i) {
			PointR pBegin = points[i];
			PointR pEnd = points[i + 1];
			
			if(pathObject.controlPoints.size() <= i || 
					pathObject.controlPoints.get(i) == null) {
				// Treat current piece of stroke as line.
				double lineDistance = Double.POSITIVE_INFINITY;
				lineDistance = lineDistance(pBegin, pEnd, position);
				
				// Update distance.
				if(lineDistance < distance)
					distance = lineDistance;
			}
			else {
				// Retrieve control point and calculate.
				PointR control = pathObject.controlPoints.get(i);
				BezierEvaluator evaluator = new BezierEvaluator(
						points[i], control, points[i + 1]);
				
				PointR current = new PointR();
				PointR previous = new PointR();
				evaluator.evaluate(0, previous);
				for(int j = 1; j <= BEZIER_RENDER; ++ j) {
					evaluator.evaluate(1.0 / BEZIER_RENDER * j, current);
					
					// Calculate distance.
					double bezierDistance = lineDistance(
							current, previous, position);
					if(bezierDistance < distance)
						distance = bezierDistance;
					
					// Swap reference between current and previous.
					PointR temp = current;
					current = previous;
					previous = temp;
				}
				
			}
		}
		return distance;
	}
	
}
