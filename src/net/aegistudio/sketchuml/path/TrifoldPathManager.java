package net.aegistudio.sketchuml.path;

import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

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
		TrifoldPath[] trifoldPaths = new TrifoldPath[] {
				trifoldRect, trifoldZigzag, trifoldRoundRect };
		
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

	@Override
	public void save(DataOutputStream output, TrifoldProxyPath pathObject) 
			throws IOException {
		
	}

	@Override
	public TrifoldProxyPath read(DataInputStream input) throws IOException {
		return null;
	}

}
