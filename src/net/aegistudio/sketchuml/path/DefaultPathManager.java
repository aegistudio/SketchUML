package net.aegistudio.sketchuml.path;

import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import de.dubs.dollarn.PointR;

public class DefaultPathManager implements PathManager<DefaultPath> {
	public static float MERGE_RMSTHRESHOLD = 0.57f;
	public static float MERGE_MINIMAL = 5.f;
	public static float REGULATION_THRESHOLD = 0.15f;
	
	private double minusModulus(PointR result, 
			PointR pointBegin, PointR pointEnd) {
		
		// Calculate difference and modulus.
		result.combine(-1, pointBegin, 1, pointEnd);
		return result.normalize();
	}
	
	private double areaDiff(int begin, int end, 
			PointR axis, PointR[] tangent, double[] modulus) {
		
		double area = 0.0;
		for(int j = begin; j <= end; ++ j) {
			PointR tangentPoint = tangent[j];
			double cosine = tangentPoint.dot(axis);
			area += Math.sqrt(1 - cosine * cosine) * modulus[j];
		}
		return area;
	}
	
	private double midpoint(int begin, int end, double[] modulus) {
		// Calculate modulus sum.
		double sumModulus = 0.0;
		for(int i = begin; i <= end && i < modulus.length; ++ i)
			sumModulus += modulus[i];
		
		// Find midpoint.
		double halfModulus = sumModulus * 0.5;
		sumModulus = 0;
		for(int i = begin; i <= end && i < modulus.length; ++ i) {
			double distance = halfModulus - sumModulus;
			if(modulus[i] > distance) 
				return i + (distance / modulus[i]);
			else sumModulus += modulus[i];
		}
		return (begin + end) * 0.5;
	}
	
	@Override
	public DefaultPath quantize(Vector<PointR> stroke, 
			Rectangle2D boundBegin, Rectangle2D boundEnd) {
		DefaultPath resultPath = new DefaultPath();
		stroke.removeIf(point -> point.inside(boundBegin));
		stroke.removeIf(point -> point.inside(boundEnd));
		
		// Calculate tangent line for every two points, 
		// including the end and begin.
		PointR[] tangent = new PointR[stroke.size() + 1];
		double[] modulus = new double[stroke.size() + 1];
		
		PointR pointBegin = new PointR(boundBegin.getCenterX(), 
				boundBegin.getCenterY());
		PointR pointEnd = new PointR(boundEnd.getCenterX(), 
				boundEnd.getCenterY());
		
		// Add points for calculating tangents.
		Vector<PointR> points = new Vector<>(stroke.size() + 2);
		points.add(pointBegin); 
		points.addAll(stroke); 
		points.add(pointEnd);
		for(int i = 0; i < points.size() - 1; ++ i) {
			PointR before = points.get(i);
			PointR after = points.get(i + 1);
					
			// Calculate difference and modulus.
			tangent[i] = new PointR();
			modulus[i] = minusModulus(tangent[i], before, after);
		}
		
		// Perform line merging calculation.
		List<Integer> representIndex = new ArrayList<>();
		List<PointR> representTangent = new ArrayList<>();
		
		int beginIndex = 0;
		for(int i = 1; i < tangent.length; ++ i) {
			PointR before = points.get(beginIndex);
			PointR after = points.get(i);
			PointR average = new PointR();
			
			double distance = minusModulus(average, before, after);
			double realModulus = areaDiff(beginIndex, 
					i, average, tangent, modulus);
			
			if(realModulus > distance * MERGE_RMSTHRESHOLD || i == tangent.length - 1) {
				double[] diffs = new double[i - beginIndex];
				double minDiff = Double.POSITIVE_INFINITY;
				PointR candidateTangent = tangent[beginIndex];
				for(int j = 0; j < i - beginIndex; ++ j) {
					PointR currentTangent = tangent[j + beginIndex];
					diffs[j] = areaDiff(beginIndex, i - 1, 
							currentTangent, tangent, modulus);
					if(diffs[j] < minDiff) {
						minDiff = diffs[j];
						candidateTangent = currentTangent;
					}
				}
				
				// Find a representative point among current strip.
				List<Integer> hitDiffs = new ArrayList<>();
				for(int j = 0; j < diffs.length; ++ j)
					if(diffs[j] - minDiff <= MERGE_MINIMAL)
						hitDiffs.add(beginIndex + j);
				
				// Add the point and tangent there.
				if(hitDiffs.size() > 0) {
					representIndex.add(hitDiffs.get(hitDiffs.size() / 2));
					representTangent.add(candidateTangent);
				}
				
				// Update index.
				beginIndex = i;
			}
		}
		
		// Add last point.
		/*PointR lastTangent = new PointR();
		for(int j = points.size() - 2; j >= 0; -- j) {
			PointR current = points.get(j);
			minusModulus(lastTangent, 
					pointEnd, current);
			if(!current.inside(boundEnd)) break;
		}
		representIndex.add(points.size() - 1);
		representTangent.add(lastTangent);*/
		
		// Calculate intersections between representative tangent points.
		Set<Integer> acquaintedIndices = new TreeSet<>();
		acquaintedIndices.add(0); 
		acquaintedIndices.add(representIndex.size() - 1);
		PointR ignoreRepresent = new PointR();
		for(int i = 0; i < representIndex.size() - 1; ++ i) {
			int startIndex = representIndex.get(i);
			int endIndex = representIndex.get(i + 1);
			PointR before = points.get(startIndex);
			PointR after = points.get(endIndex);
			
			PointR beforeTangent = representTangent.get(i);
			PointR afterTangent = representTangent.get(i + 1);
			
			// Calculate determinant.
			double delta = beforeTangent.Y * afterTangent.X 
					- beforeTangent.X * afterTangent.Y;
			if(delta == 0) continue;
			
			// Calculate intersection.
			double dx = after.X - before.X;
			double dy = after.Y - before.Y;
			double t = - (afterTangent.Y * dx 
					- afterTangent.X * dy) / delta;
			PointR intersect = new PointR();
			intersect.combine(1, before, t, beforeTangent);
			
			// Calculate midpoint index for current strip.
			double midPointIndex = midpoint(startIndex, endIndex, modulus);
			int lowerMidPoint = (int)Math.floor(midPointIndex);
			double difference = midPointIndex - lowerMidPoint;
			int higherMidPoint = Math.min(lowerMidPoint + 1, points.size() - 1);
			
			// Calculate midpoint for current strip.
			PointR midPoint = new PointR();
			PointR pointLower = points.get(lowerMidPoint);
			PointR pointHigher = points.get(higherMidPoint);
			midPoint.interpolate(difference, pointLower, pointHigher);
			
			// Calculate distance between intersection point.
			PointR pointFitting = new PointR(); 
			double distanceStart = minusModulus(ignoreRepresent, intersect, before);
			double distanceEnd = minusModulus(ignoreRepresent, intersect, after);
			double distanceMiddle = 0.5 * (distanceStart + distanceEnd);
			if(distanceStart >= distanceEnd) pointFitting.interpolate(
					distanceMiddle / distanceStart, before, intersect);
			else pointFitting.interpolate((distanceMiddle - distanceStart) 
					/ distanceEnd, intersect, after);
			double distanceIntersect = minusModulus(
					ignoreRepresent, midPoint, pointFitting);
			
			// Calculate distance between bezier subdivide point.
			BezierEvaluator bezier = new BezierEvaluator(
					before, intersect, after);
			bezier.evaluate(0.5, pointFitting);
			double distanceBezier = minusModulus(
					ignoreRepresent, midPoint, pointFitting);
			
			// Append the calculated point.
			if(acquaintedIndices.add(i)) 
				resultPath.separatePoints.add(new PointR(before));
			
			if(distanceIntersect < distanceBezier)
				resultPath.separatePoints.add(intersect);
			
			if(acquaintedIndices.add(i + 1)) 
				resultPath.separatePoints.add(new PointR(after));
			
			if(distanceIntersect < distanceBezier)
				resultPath.controlPoints.add(null);
			else resultPath.controlPoints.add(intersect);
		}
		
		// Perform point regulations.
		PointR beforeRegulate, regulateVector = new PointR();
		
		beforeRegulate = pointBegin;
		for(int i = 0; i < resultPath.separatePoints.size(); ++ i) {
			// Forward regulate.
			PointR afterRegulate = resultPath.separatePoints.get(i);
			regulate(regulateVector, beforeRegulate, afterRegulate);
			beforeRegulate = afterRegulate;
		}

		beforeRegulate = pointEnd;
		for(int i = resultPath.separatePoints.size() - 1; i >= 0; -- i) {
			// Backward regulate.
			PointR after = resultPath.separatePoints.get(i);
			regulate(regulateVector, beforeRegulate, after);
			beforeRegulate = after;
		}
		
		// Remove abnormal paths.
		PointR controlVector0 = new PointR();
		PointR controlVector1 = new PointR();
		for(int i = 0; i < resultPath.controlPoints.size(); ++ i) {
			PointR control = resultPath.controlPoints.get(i);
			if(control == null) continue;
			
			minusModulus(controlVector0, control, 
					i == 0? pointBegin : resultPath
							.separatePoints.get(i - 1));
			controlVector0.normalize();
			minusModulus(controlVector1, control, 
					i < resultPath.separatePoints.size()?
							resultPath.separatePoints.get(i) : pointEnd);
			controlVector1.normalize();
			
			if(Math.abs(controlVector0.dot(controlVector1)) >= 1.0)
				resultPath.controlPoints.set(i, null);
		}
		
		return resultPath;
	}
	
	private boolean regulate(PointR regulateVector, 
			PointR before, PointR after) {
		
		boolean regulated = false;
		minusModulus(regulateVector, before, after);
		
		// Horizontal aligned.
		if(Math.abs(regulateVector.Y) < REGULATION_THRESHOLD) {
			after.Y = before.Y;
			regulated = true;
		}
		
		// Vertical aligned
		if(Math.abs(regulateVector.X) < REGULATION_THRESHOLD) {
			after.X = before.X;
			regulated = true;
		}
		
		return regulated;
	}
	
	@Override
	public void save(DataOutputStream output, 
			DefaultPath pathObject) throws IOException {
		output.writeInt(pathObject.controlPoints.size());
		for(PointR point : pathObject.controlPoints)
			point.write(output);
		
		output.writeInt(pathObject.separatePoints.size());
		for(PointR point : pathObject.separatePoints)
			point.write(output);
	}

	@Override
	public DefaultPath read(DataInputStream input) throws IOException {
		DefaultPath pathObject = new DefaultPath();
		
		int numControlPoints = input.readInt();
		for(int i = 0; i < numControlPoints; ++ i) {
			PointR point = new PointR();
			point.read(input);
			pathObject.controlPoints.add(point);
		}
		
		int numSeparatePoints = input.readInt();
		for(int i = 0; i < numSeparatePoints; ++ i) {
			PointR point = new PointR();
			point.read(input);
			pathObject.separatePoints.add(point);
		}
		
		return pathObject;
	}
	
}
