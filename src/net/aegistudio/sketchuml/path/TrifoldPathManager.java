package net.aegistudio.sketchuml.path;

import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import JP.co.esm.caddies.golf.geom2D.Pnt2d;
import de.dubs.dollarn.PointR;

public class TrifoldPathManager implements PathManager<TrifoldProxyPath> {

	public static final int STATUS_INSIDEA = 0;
	public static final int STATUS_ACCEPTING = 1;
	public static final int STATUS_INSIDEB = 2;
	public static final int STATUS_REJECTING = 3;
	
	public static final double CENTER_RATIO = 0.4;
	private void judgeCenter(PointR pointBegin, PointR pointEnd,
			Rectangle2D bound, LinePiece.BoxIntersectStatus status) {
		
		LinePiece linePiece = new LinePiece(pointBegin, pointEnd);
		LinePiece.DistanceStatus distance = new LinePiece.DistanceStatus();
		linePiece.distance(distance, PointR.center(bound));

		double minBound = Math.max(1., 0.5 * CENTER_RATIO *
				Math.min(bound.getWidth(), bound.getHeight()));
		if(Math.abs(distance.distance) < minBound)
			status.status = LinePiece.BoxIntersectStatus.BOX_INTERLEAVED;
	}

	/**
	 * Find the stroke that is outside and connects the beginning and
	 * ending objects.
	 * 
	 * @param outerStroke the container to store the reduced stroke.
	 * @param stroke the user input stroke.
	 * @param boundBegin starting bound.
	 * @param boundEnd ending bound.
	 */
	private void outerStrokeBetween(
			Vector<PointR> outerStroke, Vector<PointR> stroke, 
			Rectangle2D boundBegin, Rectangle2D boundEnd) {

		// Find the last piece of lines that connects the beginning 
		// object with the ending object.
		int status = STATUS_INSIDEA; for(PointR p : stroke) {
			// Reject all points when it is still inside starting object.
			if(status == STATUS_INSIDEA) {
				if(!outerStroke.isEmpty()) outerStroke.clear();
				if(!p.inside(boundBegin)) {
					if(p.inside(boundEnd)) status = STATUS_INSIDEB;
					else {
						outerStroke.add(p);
						status = STATUS_ACCEPTING;
					}
				}
			}
			
			// Accept all objects until it reaches the starting or 
			// ending objects.
			else if(status == STATUS_ACCEPTING) {
				if(p.inside(boundBegin)) status = STATUS_INSIDEA;
				else if(p.inside(boundEnd)) status = STATUS_INSIDEB;
				else outerStroke.add(p);
			}
			
			// We automatically reject all objects until it reaches
			// the starting object or get outside.
			else if(status == STATUS_INSIDEB) {
				if(p.inside(boundBegin)) status = STATUS_INSIDEA;
				else if(!p.inside(boundEnd)) status = STATUS_REJECTING;
			}
			
			// Reject all objects until it reaches the bounding of
			// the starting object.
			else if(status == STATUS_REJECTING) {
				if(p.inside(boundBegin)) status = STATUS_INSIDEA;
			}
		}
	}
	
	/**
	 * Find the stroke that is outside and connects a single object.
	 * 
	 * @param outerStroke the container to store the reduced stroke.
	 * @param stroke the user input stroke.
	 * @param bound the object bound itself.
	 */
	private void outerStrokeSelf(Vector<PointR> outerStroke, 
			Vector<PointR> stroke, Rectangle2D bound) {
		boolean outside = false;
		for(PointR p : stroke) {
			// The object is from inside the object or just
			// goes inside the object.
			if(p.inside(bound)) {
				// See whether the object comes from outside.
				if(outside) outside = false;
			}
			
			// The object has been outside the object or just
			// goes outside the object.
			else {
				if(!outside) {
					// Clear previous stroke.
					if(!outerStroke.isEmpty()) outerStroke.clear();
					outside = true;
				}
				
				// Add the stroke to the container.
				outerStroke.add(p);
			}
		}
	}
	
	public static final double LINE_ACCEPTANCE = 10.;
	
	@Override
	public TrifoldProxyPath quantize(Vector<PointR> stroke, 
			Rectangle2D boundBegin, Rectangle2D boundEnd) {
		TrifoldProxyPath resultPath = new TrifoldProxyPath();
		
		// Retrieve the outer stroke points.
		Vector<PointR> outerStroke = new Vector<PointR>();
		if(boundBegin == boundEnd || boundBegin.equals(boundEnd))
			outerStrokeSelf(outerStroke, stroke, boundBegin);
		else outerStrokeBetween(outerStroke, stroke, boundBegin, boundEnd);
		
		// We can only assume the stroke to be a straight line this case.
		if(outerStroke.size() == 0) return resultPath;
		
		// Find the intersection points.
		PointR outerStart = outerStroke.get(0);
		PointR outerEnd = outerStroke.get(outerStroke.size() - 1);
		PointR innerStart = stroke.get(0);
		PointR innerEnd = stroke.get(stroke.size() - 1);
		for(int i = 0; i < stroke.size() - 1; ++ i) {
			if(stroke.get(i + 1) == outerStart) innerStart = stroke.get(i);
			if(stroke.get(i) == outerEnd) innerEnd = stroke.get(i + 1);
		}
		
		// Create the line pieces.
		LinePiece pieceStart = new LinePiece(innerStart, outerStart);
		LinePiece pieceEnd = new LinePiece(outerEnd,  innerEnd);
		
		// Update the intersection status.
		LinePiece.BoxIntersectStatus intersectStart = resultPath.statusBegin;
		pieceStart.intersectBox(intersectStart, boundBegin);
		LinePiece.BoxIntersectStatus intersectEnd = resultPath.statusEnd;
		pieceEnd.intersectBox(intersectEnd, boundEnd);
	
		// Record the status before aligning them to center.
		int intersectStatusStart = intersectStart.status;
		int intersectStatusEnd = intersectEnd.status;
		
		// See whether the starting and ending point is close
		// enough to the center.
		for(int i = 0; i + 1 < stroke.size() && 
				stroke.get(i).inside(boundBegin); ++ i)
			judgeCenter(stroke.get(i), stroke.get(i + 1), 
					boundBegin, resultPath.statusBegin);
		for(int i = stroke.size() - 2; i >= 0 &&
				stroke.get(i + 1).inside(boundEnd); -- i)
			judgeCenter(stroke.get(i), stroke.get(i + 1), 
					boundEnd, resultPath.statusEnd);
		
		// Add the center point to training if possible.
		Vector<PointR> trainingStroke = outerStroke;
		if(intersectStart.status == LinePiece
				.BoxIntersectStatus.BOX_INTERLEAVED) {
			if(trainingStroke == outerStroke)
				trainingStroke = new Vector<>(outerStroke);
			trainingStroke.add(0, PointR.center(boundBegin));
		}
		if(intersectEnd.status == LinePiece
				.BoxIntersectStatus.BOX_INTERLEAVED) {
			if(trainingStroke == outerStroke)
				trainingStroke = new Vector<>(outerStroke);
			trainingStroke.add(PointR.center(boundEnd));
		}
		
		// Calculate and articulate the line path.
		TrifoldPath trifoldLine = new TrifoldLinePath();
		double varianceLine = trifoldLine
				.articulateAndFitness(outerStroke, trainingStroke,
				intersectStatusStart, intersectStatusEnd);
		double minimumVariance = varianceLine;
		resultPath.setPath(trifoldLine);
		
		// See the variance is no more than the acceptance variance.
		if(varianceLine <= trainingStroke.size() * LINE_ACCEPTANCE)
			return resultPath;
		
		// Initialize available list of paths for fitting.
		TrifoldPath trifoldRect = new TrifoldRectPath();
		TrifoldPath trifoldZigzag = new TrifoldZigzagPath();
		TrifoldPath trifoldRoundRect = new TrifoldRoundRectPath();
		TrifoldPath trifoldLift = new TrifoldLiftPath();
		TrifoldPath trifoldRoundLift = new TrifoldRoundLiftPath();
		TrifoldPath[] trifoldPaths = new TrifoldPath[] {
				trifoldRect, trifoldZigzag, trifoldRoundRect, 
				trifoldLift, trifoldRoundLift };
		
		// Calculate and articulate paths for minimum variance.
		for(TrifoldPath trifoldPath : trifoldPaths) {
			double pathVariance = trifoldPath.articulateAndFitness(
					outerStroke, trainingStroke, 
					intersectStatusStart, intersectStatusEnd);
			if(pathVariance < minimumVariance) {
				minimumVariance = pathVariance;
				resultPath.setPath(trifoldPath);
			}
		}
		
		// Collect and return the result.
		return resultPath;
	}

	private void saveIntersection(DataOutputStream output, 
			LinePiece.BoxIntersectStatus status) throws IOException {
		output.writeByte(status.status);
		output.writeDouble(status.ratio);
	}
	
	private void readIntersection(DataInputStream input,
			LinePiece.BoxIntersectStatus status) throws IOException {
		status.status = input.readByte();
		status.ratio = input.readDouble();
	}
	
	@Override
	public void save(DataOutputStream output, TrifoldProxyPath pathObject) 
			throws IOException {
		// Persistent the bounding box.
		saveIntersection(output, pathObject.statusBegin);
		saveIntersection(output, pathObject.statusEnd);
		
		// Persistent the path object.
		int pathIndex = TrifoldPath.IMPLEMENTATIONS
				.indexOf(pathObject.path.getClass());
		if(pathIndex == -1) throw new IOException();
		output.writeByte(pathIndex);
		pathObject.path.writePath(output);
	}

	@Override
	public TrifoldProxyPath read(DataInputStream input) throws IOException {
		TrifoldProxyPath result = new TrifoldProxyPath();
		
		// Persistent the bounding box.
		readIntersection(input, result.statusBegin);
		readIntersection(input, result.statusEnd);
		
		// Persistent the path object.
		int pathIndex = input.readByte();
		if(pathIndex < 0 || pathIndex >= TrifoldPath.IMPLEMENTATIONS.size())
			throw new IOException();
		try {
			result.path = TrifoldPath.IMPLEMENTATIONS
				.get(pathIndex).newInstance();
		}
		catch(Exception e) {
			throw new AssertionError("Each trifold path should "+
					"have its no-parameter constructor.");
		}
		result.path.readPath(input);
		
		return result;
	}

	@Override
	public AstahPathHint getAstahPathHint(TrifoldProxyPath path, 
			Rectangle2D pathBegin, Rectangle2D pathEnd) {
		AstahPathHint pathHint = new AstahPathHint();
		
		// Initialize the line position inside the model.
		pathHint.sourceX = objectRatioX(path.statusBegin);
		pathHint.sourceY = objectRatioY(path.statusBegin);
		pathHint.targetX = objectRatioX(path.statusEnd);
		pathHint.targetY = objectRatioY(path.statusEnd);
		
		// Retrieve the beginning and ending point.
		PointR pointBegin = new PointR();
		TrifoldProxyPath.retrieveObjectPoint(pointBegin, 
				pathBegin, path.statusBegin);
		PointR pointEnd = new PointR();
		TrifoldProxyPath.retrieveObjectPoint(pointEnd, 
				pathEnd, path.statusEnd);
		
		// Retrieve the inner points.
		List<PointR> separatePoints = new ArrayList<>();
		List<PointR> controlPoints = new ArrayList<>();
		path.path.makePath(pointBegin, pointEnd, 
				separatePoints, controlPoints);
		if(path.path.isCurve()) {
			pathHint.lineStyle = path.path.isRightAngle()? 
					"curve_right_angle" : "curve";
			List<PointR> evaluatedPoints = new ArrayList<>();
			evaluatedPoints.add(pointBegin);
			controlPoints.forEach(evaluatedPoints::add);
			evaluatedPoints.add(pointEnd);
			pathHint.innerPoints = evaluatedPoints.stream()
				.map(TrifoldPathManager::makePnt2d)
				.toArray(Pnt2d[]::new);
		}
		else {
			pathHint.lineStyle = path.path.isRightAngle()?
					"line_right_angle" : "line";
			pathHint.innerPoints = separatePoints.stream()
				.map(TrifoldPathManager::makePnt2d)
				.toArray(Pnt2d[]::new);
		}
		
		// Convert the control points.
		pathHint.controlPoints = new Pnt2d[Math.max(0, 
				pathHint.innerPoints.length - 2)];
		for(int i = 0; i < pathHint.controlPoints.length; ++ i)
			pathHint.controlPoints[i] = 
				clonePnt2d(pathHint.innerPoints[i]);
		
		// Calculate point intersections.
		PointR intersectionBegin = evaluatePointBegin(pointBegin, pathBegin,
				makePointR(pathHint.innerPoints[0]), 
				makePointR(pathHint.innerPoints[1]), path.statusBegin);
		int numInnerPoints = pathHint.innerPoints.length;
		PointR intersectionEnd = evaluatePointBegin(pointEnd, pathEnd,
				makePointR(pathHint.innerPoints[numInnerPoints - 1]), 
				makePointR(pathHint.innerPoints[numInnerPoints - 2]), 
				path.statusEnd);
		
		// Intersections as outer points now.
		pathHint.outerPoints = new Pnt2d[numInnerPoints];
		for(int i = 1; i < numInnerPoints - 1; ++ i) 
			pathHint.outerPoints[i] = clonePnt2d(pathHint.innerPoints[i]);
		pathHint.outerPoints[0] = makePnt2d(intersectionBegin);
		pathHint.outerPoints[numInnerPoints - 1] = makePnt2d(intersectionEnd);
		
		// Calculate the position of the center text.
		List<PointR> evaluateSeparatePoint = new ArrayList<>();
		separatePoints.forEach(evaluateSeparatePoint::add);
		separatePoints.set(0, intersectionBegin);
		separatePoints.set(separatePoints.size() - 1, intersectionEnd);
		PointR[] separatePointArray = evaluateSeparatePoint.toArray(new PointR[0]);
		PointR[] controlPointArray = controlPoints.toArray(new PointR[0]);
		BezierPathStrider pathStrider = new BezierPathStrider(
				separatePointArray, controlPointArray);
		PointR centerTextLocation = new PointR();
		pathStrider.pointPercentage(centerTextLocation, .5);
		pathHint.pathCenter = makePnt2d(centerTextLocation);
		
		return pathHint;
	}

	private static PointR evaluatePointBegin(
				PointR point, Rectangle2D bound, 
				PointR lineStart, PointR lineEnd,
				LinePiece.BoxIntersectStatus status) {
		
		PointR intersection = new PointR();
		boolean useBeginPoint = true;
		if(TrifoldProxyPath.isCenter(status)) {
			LinePiece linePieceBegin = new LinePiece(
					lineStart, lineEnd);
			LinePiece.BoxIntersectStatus evalStatus 
				= new LinePiece.BoxIntersectStatus(); 
			linePieceBegin.intersectBox(evalStatus, bound);
			if(evalStatus.status != LinePiece
					.BoxIntersectStatus.BOX_INTERLEAVED) {
				useBeginPoint = false;
				evalStatus.retrievePoint(
						intersection, bound);
			}
		}
		
		if(useBeginPoint) {
			intersection.X = point.X;
			intersection.Y = point.Y;
		}
		return intersection;
	}
	
	private static Pnt2d makePnt2d(PointR pointR) {
		Pnt2d pnt2d = new Pnt2d();
		pnt2d.x = pointR.X;
		pnt2d.y = pointR.Y;
		return pnt2d;
	}
	
	public static Pnt2d clonePnt2d(Pnt2d pnt2d) {
		Pnt2d cloned2d = new Pnt2d();
		cloned2d.x = pnt2d.x;
		cloned2d.y = pnt2d.y;
		return cloned2d;
	}
	
	public static PointR makePointR(Pnt2d pnt2d) {
		PointR pointR = new PointR();
		pointR.X = pnt2d.x;
		pointR.Y = pnt2d.y;
		return pointR;
	}
	
	private double objectRatioX(LinePiece.BoxIntersectStatus info) {
		switch(info.status) {
			case LinePiece.BoxIntersectStatus.BOX_TOP:
			case LinePiece.BoxIntersectStatus.BOX_BOTTOM:
				return 0.5 * info.ratio + 0.5;
			case LinePiece.BoxIntersectStatus.BOX_LEFT:
				return 1e-6;
			case LinePiece.BoxIntersectStatus.BOX_RIGHT:
				return 1.0 - 1e-6;
			default: return 0.5;
		}
	}
	
	private double objectRatioY(LinePiece.BoxIntersectStatus info) {
		switch(info.status) {
			case LinePiece.BoxIntersectStatus.BOX_TOP:
				return 1e-6;
			case LinePiece.BoxIntersectStatus.BOX_BOTTOM:
				return 1.0 - 1e-6;
			case LinePiece.BoxIntersectStatus.BOX_LEFT:
			case LinePiece.BoxIntersectStatus.BOX_RIGHT:
				return 0.5 * info.ratio + 0.5;
			default: return 0.5;
		}
	}
}
