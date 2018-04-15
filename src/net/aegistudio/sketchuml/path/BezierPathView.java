package net.aegistudio.sketchuml.path;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.abstraction.LinkView.ArrowStyle;
import net.aegistudio.sketchuml.abstraction.LinkView.LineStyle;
import net.aegistudio.sketchuml.abstraction.SketchRenderHint;

public class BezierPathView<T extends BezierPath> implements PathView<T> {
	public static final float[][] DASH_DECORATION= { 
			/*DASH*/{10.f}, /*DOT*/{2.f}, /*DASHDOT*/{10.f, 4.0f, 4.f} };
	public static final float ARROW_ZONAL = 20;
	public static final float ARROW_HORIZONTAL = 7;
	public static final int BEZIER_RENDER = 20;
	public static final double ARROW_TANGENTBIAS = 0.05;
	
	private double intersectBox(Rectangle2D rect, PointR outPoint,
			PointR resultDirection, PointR resultIntersection) {
		
		PointR rectCenter = PointR.center(rect);
		
		// Difference and normalization.
		resultDirection.combine(1.0, outPoint, -1.0, rectCenter);
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
		resultIntersection.combine(1, rectCenter, 
				vectorLength, resultDirection);
		return vectorLength;
	}
	
	private double intersectBezier(Rectangle2D rect, BezierEvaluator evaluator,
			PointR resultDirection, PointR resultIntersection) {
		
		// The control point coordinates.
		for(double t = 0.0; t <= 1.0; t += 0.01) {
			evaluator.evaluate(t, resultIntersection);
			
			// Check whether there's intersection.
			if(!resultIntersection.inside(rect)) {
				evaluator.tangent(t, resultDirection);
				return t;
			}
		}
		
		return 1.0;
	}

	private void beginLine(Graphics2D g2d, 
			SketchRenderHint hint, boolean selected) {
		g2d.setColor(hint.getLineColor(
				SketchRenderHint.pathColor, selected));
	}
	
	private void beginArrowFill(Graphics2D g2d,
			SketchRenderHint hint, boolean selected) {
		g2d.setColor(hint.getFillColor(
				SketchRenderHint.arrowFillcolor, selected));
	}
	
	private void renderText(Graphics2D g2d, SketchRenderHint hint, 
			PointR point, String text, boolean preview) {
		if(text == null) return;
		
		Rectangle2D bound = g2d.getFontMetrics()
				.getStringBounds(text, g2d);
		int pointX = (int)point.X;
		int pointY = (int)point.Y;
		int boundW = (int)bound.getWidth();
		int boundH = (int)bound.getHeight();
		int boundW2 = (int)bound.getWidth() / 2;
		int boundH2 = (int)bound.getHeight() / 2;
		
		// Draw the text background.
		g2d.setColor(Color.WHITE);
		g2d.fillRect(pointX - boundW2, 
			pointY - boundH2, boundW, boundH);
		
		// Draw the text content.
		g2d.setColor(hint.getLineColor(
				SketchRenderHint.outerLabelColor, preview));
		g2d.drawString(text, pointX - boundW2, pointY + boundH2);
	}
	
	private void renderKnot(Graphics2D g2d, SketchRenderHint hint, 
			int x, int y, boolean preview) {
		beginLine(g2d, hint, preview);
		int knotOffset = (int)hint.lineWidthSelected;
		int knotWidth = (int)hint.lineWidthSelected * 2;
		g2d.fillRect(x - knotOffset, 
				y - knotOffset, knotWidth, knotWidth);
	}
	
	@Override
	public void render(SketchRenderHint hint,
			Graphics2D g2d, boolean selected,
			T pathObject, LineStyle line, 
			Rectangle2D boundBegin, ArrowStyle arrowBegin,
			Rectangle2D boundEnd, ArrowStyle arrowEnd,
			String startText, String centerText, String endText) {
		
		// Prepare for rendering.
		beginLine(g2d, hint, selected);
		
		// Collect path object information.
		List<PointR> separatePoints = pathObject
				.separatePoints(boundBegin, boundEnd);
		List<PointR> controlPoints = pathObject
				.controlPoints(boundBegin, boundEnd);
		int numPoints = separatePoints.size();
		PointR pointBegin = PointR.center(boundBegin);
		PointR pointEnd = PointR.center(boundEnd);
		PointR[] points = new PointR[numPoints + 2];
		points[0] = pointBegin;
		points[points.length - 1] = pointEnd;
		for(int i = 0; i < numPoints; ++ i) 
			points[i + 1] = separatePoints.get(i);
		
		// The intersection and direction points.
		PointR intersectBegin = new PointR();
		PointR intersectEnd = new PointR();
		PointR directionBegin = new PointR();
		PointR directionEnd = new PointR();
		
		// Calculate the total line length.
		double totalLength = totalLength(pathObject, 
				boundBegin, boundEnd);
		double lengthThreshold = totalLength * 0.5;
		
		// Initialize the line style.
		BasicStroke renderingLineStyle;
		if(!LineStyle.COHERENT.equals(line)) {
			int order = line.ordinal() - 1/*COHERENT*/;
			float[] dash = DASH_DECORATION[order];
			renderingLineStyle = new BasicStroke(selected? 
					hint.lineWidthSelected : hint.outlineWidth, 
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
					1.0f, dash, 0.0f);
		}
		else renderingLineStyle = new BasicStroke(selected? 
				hint.lineWidthSelected: hint.outlineWidth);
		
		// Perform drawings.
		PointR pieceLength = new PointR();
		PointR centerPoint = new PointR();
		for(int i = 0; i < numPoints + 1; ++ i) {
			PointR pBegin = points[i];
			PointR pEnd = points[i + 1];
			int xBegin = pBegin.intX();
			int yBegin = pBegin.intY();
			int xEnd = pEnd.intX();
			int yEnd = pEnd.intY();
			
			// Indicates whether begin line or end line should render.
			boolean renderBegin = true;
			boolean renderEnd = true;
			if(pBegin == pointBegin) renderBegin = 
					pathObject.renderInnerLineBegin();
			if(pEnd == pointEnd) renderEnd = 
					pathObject.renderInnerLineEnd();
			
			// Render knot when selected.
			if(selected) {
				if(renderBegin) renderKnot(g2d, hint, xBegin, yBegin, selected);
				if(renderEnd) renderKnot(g2d, hint, xEnd, yEnd, selected);
			}
			
			// Configure line style.
			g2d.setStroke(renderingLineStyle);
			
			if(controlPoints.size() <= i || 
					controlPoints.get(i) == null) {
				boolean renderLine = renderBegin && renderEnd;
				
				// Just draw a direct line.
				if(renderLine) g2d.drawLine(xBegin, yBegin, xEnd, yEnd);
				
				// Calculate current piece length.
				pieceLength.combine(-1, pBegin, 1, pEnd);
				double lineLength = pieceLength.modulus();
						
				// Find intersection.
				if(pBegin == pointBegin) {
					lineLength -= intersectBox(boundBegin, 
						points[1], directionBegin, intersectBegin);
				}
				if(pEnd == pointEnd) {
					lineLength -= intersectBox(boundEnd, 
						points[points.length - 2], directionEnd, intersectEnd);
				}
				
				// Check whether guard condition is reached.
				// XXX This part may be changed later, or may never.
				if(lineLength > lengthThreshold && lengthThreshold >= 0) {
					centerPoint.interpolate(lengthThreshold / lineLength, 
							pBegin == pointBegin? intersectBegin : pBegin, 
							pEnd == pointEnd? intersectEnd : pEnd);
					renderText(g2d, hint, centerPoint, centerText, selected);
				}
				lengthThreshold -= lineLength;
			}
			else {
				// Retrieve control point and paint.
				PointR control = controlPoints.get(i);
				
				int xCtrl = control.intX(); 
				int yCtrl = control.intY();
				BezierEvaluator evaluator = new BezierEvaluator(
						points[i], control, points[i + 1]);
				double originalLength = evaluator.length(1.0);
				double bezierLength = originalLength;
				
				PointR current = new PointR();
				int[] bx = new int[BEZIER_RENDER + 1];
				int[] by = new int[BEZIER_RENDER + 1];
				for(int j = 0; j <= BEZIER_RENDER; ++ j) {
					evaluator.evaluate(1.0 / BEZIER_RENDER * j, current);
					bx[j] = current.intX();
					by[j] = current.intY();
				}
				g2d.drawPolyline(bx, by, BEZIER_RENDER + 1);
				
				// Find intersection.
				@SuppressWarnings("unused") 
				double offsetLength = 0.0, offsetT = 0.0;
				if(pBegin == pointBegin) offsetLength = evaluator.length(
						offsetT = intersectBezier(boundBegin, evaluator, 
						directionBegin, intersectBegin));
				
				double trailLength = 0.0, trailT = 1.0;
				if(pEnd == pointEnd) {
					// Makes it easier for intersection.
					BezierEvaluator endEvaluator = new BezierEvaluator(
							points[i + 1], control, points[i]);
					trailT = 1.0 - intersectBezier(boundEnd, endEvaluator, 
							directionEnd, intersectEnd);
					trailLength = evaluator.length(trailT);
					trailLength = originalLength - trailLength;
				}
				
				// Render control points when selected.
				if(selected) renderKnot(g2d, hint, xCtrl, yCtrl, selected);
				
				// Check whether guard condition is reached.
				// XXX This part may be changed later, or may never.
				bezierLength -= (trailLength + offsetLength);
				if(bezierLength > lengthThreshold && lengthThreshold >= 0) {
					double requestParameter = (lengthThreshold 
							+ offsetLength) / originalLength;
					
					double solveParameter = evaluator.solveLengthEquation(
							requestParameter, 5, 1e-3);
					evaluator.evaluate(solveParameter, centerPoint);
					renderText(g2d, hint, centerPoint, centerText, selected);
				}
				lengthThreshold -= bezierLength;
			}
		}
		
		// Perform arrow rendering, notice that it will replace 
		// the previous stroke style.
		g2d.setStroke(new BasicStroke(selected? 
				hint.lineWidthSelected : hint.outlineWidth));
		if(selected) g2d.setColor(hint.lineColorSelected);
		else g2d.setColor(hint.lineColorNormal);
		
		if(pathObject.arrowDirectionOnLine()) {
			// Indicates the line is placed on the edge.
			if(!pathObject.renderInnerLineBegin()) {
				// Calculate the starting direction.
				PointR controlPoint = controlPoints.size() > 1?
						controlPoints.get(1) : null;
				if(controlPoint != null) new BezierEvaluator(
						points[1], controlPoint, points[2])
						.tangent(ARROW_TANGENTBIAS, directionBegin);
				else {
					directionBegin.combine(1., points[1], -1., points[2]);
					directionBegin.normalize();
				}
				
				// Render the starting arrow.
				renderArrow(g2d, hint, separatePoints.get(0), 
						directionBegin, arrowBegin, selected);
			}
			else {
				renderArrow(g2d, hint, intersectBegin, directionBegin, 
					arrowBegin, selected);
			}
			
			// Indicates the ending point is placed on the edge.
			if(!pathObject.renderInnerLineEnd()) {
				// Calculate the ending direction.
				PointR controlPoint = controlPoints.size() > 1?
						controlPoints.get(controlPoints.size() - 2) : null;
				if(controlPoint != null) new BezierEvaluator(
						points[points.length - 2], controlPoint, 
						points[points.length - 3])
						.tangent(ARROW_TANGENTBIAS, directionEnd);
				else {
					directionEnd.combine(-1., points[points.length - 2],
							1., points[points.length - 3]);
					directionEnd.normalize();
				}
				
				// Render the ending arrow.
				renderArrow(g2d, hint,
						separatePoints.get(separatePoints.size() - 1), 
						directionEnd, arrowEnd, selected);
			}
			else {
				renderArrow(g2d, hint,
					intersectEnd, directionEnd, 
					arrowEnd, selected);
			}
		}
	}
	
	private void renderArrow(Graphics2D g2d, SketchRenderHint hint,
			PointR origin, PointR direction, 
			ArrowStyle arrow, boolean selected) {
		
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
				beginLine(g2d, hint, selected);
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
					beginArrowFill(g2d, hint, selected);
				else beginLine(g2d, hint, selected);
				g2d.fillPolygon(xsT, ysT, 3);
				
				// Draw out border.
				beginLine(g2d, hint, selected);
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
					beginArrowFill(g2d, hint, selected);
				else beginLine(g2d, hint, selected);
				g2d.fillPolygon(xsD, ysD, 4);
				
				// Draw out border.
				beginLine(g2d, hint, selected);
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
					beginArrowFill(g2d, hint, selected);
				else beginLine(g2d, hint, selected);
				g2d.fillOval(xcC - radius, ycC - radius, 
						2 * radius, 2 * radius);
				
				// Draw out border.
				beginLine(g2d, hint, selected);
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
	public double distance(BezierPath pathObject, PointR position, 
			Rectangle2D boundBegin, Rectangle2D boundEnd) {
		double distance = Double.POSITIVE_INFINITY;
		
		// Can never select the internal part of a bounding box.
		if(position.inside(boundBegin)) return distance;
		if(position.inside(boundEnd)) return distance;
		
		// Collect path object information.
		List<PointR> separatePoints = pathObject
				.separatePoints(boundBegin, boundEnd);
		List<PointR> controlPoints = pathObject
				.controlPoints(boundBegin, boundEnd);
		int numPoints = separatePoints.size();
		PointR pointBegin = PointR.center(boundBegin);
		PointR pointEnd = PointR.center(boundEnd);
		PointR[] points = new PointR[numPoints + 2];
		points[0] = pointBegin;
		points[points.length - 1] = pointEnd;
		for(int i = 0; i < numPoints; ++ i) 
			points[i + 1] = separatePoints.get(i);

		// Perform calculation.
		for(int i = 0; i < numPoints + 1; ++ i) {
			PointR pBegin = points[i];
			PointR pEnd = points[i + 1];
			
			if(controlPoints.size() <= i || 
					controlPoints.get(i) == null) {
				// Treat current piece of stroke as line.
				double lineDistance = Double.POSITIVE_INFINITY;
				lineDistance = lineDistance(pBegin, pEnd, position);
				
				// Update distance.
				if(lineDistance < distance)
					distance = lineDistance;
			}
			else {
				// Retrieve control point and calculate.
				PointR control = controlPoints.get(i);
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
	
	/**
	 * Notice the part inside the rectangles will not be counted.
	 * @return
	 */
	public double totalLength(BezierPath pathObject, 
			Rectangle2D boundBegin, Rectangle2D boundEnd) {
		
		// Collect path object information.
		List<PointR> separatePoints = pathObject
				.separatePoints(boundBegin, boundEnd);
		List<PointR> controlPoints = pathObject
				.controlPoints(boundBegin, boundEnd);
		double totalLength = 0.0;
		int numPoints = separatePoints.size();
		PointR pointBegin = PointR.center(boundBegin);
		PointR pointEnd = PointR.center(boundEnd);
		PointR[] points = new PointR[numPoints + 2];
		points[0] = pointBegin;
		points[points.length - 1] = pointEnd;
		for(int i = 0; i < numPoints; ++ i) 
			points[i + 1] = separatePoints.get(i);

		// Perform calculation.
		PointR temp = new PointR();
		PointR tempDirection = new PointR();
		PointR tempIntersect = new PointR();
		for(int i = 0; i < numPoints + 1; ++ i) {
			PointR pBegin = points[i];
			PointR pEnd = points[i + 1];
			
			if(controlPoints.size() <= i || 
					controlPoints.get(i) == null) {
				
				// Update distance.
				temp.combine(1.0, pEnd, -1.0, pBegin);
				totalLength += temp.modulus();
				
				// Remove the start length.
				if(pBegin == pointBegin)
					totalLength -= intersectBox(boundBegin, 
						pEnd, tempDirection, tempIntersect);
				
				// Remove the end length.
				if(pEnd == pointEnd)
					totalLength -= intersectBox(boundEnd, 
						pBegin, tempDirection, tempIntersect);
					
			}
			else {
				// Retrieve control point and calculate.
				PointR control = controlPoints.get(i);
				BezierEvaluator evaluator = new BezierEvaluator(
						points[i], control, points[i + 1]);
				totalLength += evaluator.length(1.0);
				
				// Remove the start length.
				if(pBegin == pointBegin)
					totalLength -= evaluator.length(intersectBezier(
							boundBegin, evaluator, 
							tempDirection, tempIntersect));
				
				// Remove the start length.
				if(pEnd == pointEnd) {
					BezierEvaluator endEvaluator = new BezierEvaluator(
							points[i + 1], control, points[i]);
					totalLength -= endEvaluator.length(
							intersectBezier(
							boundEnd, endEvaluator, 
							tempDirection, tempIntersect));
				}
			}
		}
		
		return totalLength;
	}
}
