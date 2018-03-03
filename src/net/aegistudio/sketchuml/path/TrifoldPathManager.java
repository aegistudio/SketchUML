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
	
		// Calculate and articulate the line path.
		TrifoldPath trifoldLine = new TrifoldLinePath();
		double varianceLine = trifoldLine
				.articulateAndFitness(outerStroke);
		double minimumVariance = varianceLine;
		resultPath.setPath(trifoldLine);
		
		// Calculate and articulate the rect path.
		TrifoldPath trifoldRect = new TrifoldRectPath();
		double varianceRect = trifoldRect
				.articulateAndFitness(outerStroke);
		if(varianceRect < minimumVariance) {
			minimumVariance = varianceRect;
			resultPath.setPath(trifoldRect);
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
