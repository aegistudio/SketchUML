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
	public static float MERGE_RMSTHRESHOLD = 0.47f;
	public static float MERGE_MINIMAL = 5.f;
	
	private double minusModulus(PointR result, 
			PointR pointBegin, PointR pointEnd) {
		double modulus = 0.0;
		
		// Calculate difference and modulus.
		result.X = pointEnd.X - pointBegin.X;
		result.Y = pointEnd.Y - pointBegin.Y;
		
		modulus = Math.sqrt(result.X * result.X 
				+ result.Y * result.Y);
		if(modulus == 0) { 
			result.X = result.Y = 0.0; 
		}
		else { 
			result.X /= modulus; 
			result.Y /= modulus;
		}
		return modulus;
	}
	
	private double areaDiff(int begin, int end, 
			PointR axis, PointR[] tangent, double[] modulus) {
		
		double area = 0.0;
		for(int j = begin; j <= end; ++ j) {
			PointR tangentPoint = tangent[j];
			double cosine = tangentPoint.X * axis.X 
					+ tangentPoint.Y * axis.Y;
			area += Math.sqrt(1 - cosine * cosine) * modulus[j];
		}
		return area;
	}
	
	@Override
	public DefaultPath quantize(Vector<PointR> stroke, 
			Rectangle2D boundBegin, Rectangle2D boundEnd) {
		DefaultPath resultPath = new DefaultPath();
		
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
		
		// Calculate intersections between representative tangent points.
		Set<Integer> acquaintedIndices = new TreeSet<>();
		acquaintedIndices.add(0); 
		acquaintedIndices.add(representIndex.size() - 1);
		for(int i = 0; i < representIndex.size() - 1; ++ i) {
			PointR before = points.get(representIndex.get(i));
			PointR after = points.get(representIndex.get(i + 1));
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
			double xi = before.X + beforeTangent.X * t;
			double yi = before.Y + beforeTangent.Y * t;
			
			// Append the calculated point.
			if(acquaintedIndices.add(i)) 
				resultPath.separatePoints.add(new PointR(before));
			if(acquaintedIndices.add(i + 1)) 
				resultPath.separatePoints.add(new PointR(after));
			resultPath.controlPoints.add(new PointR(xi, yi));
		}
		
		return resultPath;
	}

	@Override
	public void save(DataOutputStream output, 
			DefaultPath pathObject) throws IOException {
		
	}

	@Override
	public DefaultPath read(DataInputStream input) throws IOException {
		return null;
	}
	
}
