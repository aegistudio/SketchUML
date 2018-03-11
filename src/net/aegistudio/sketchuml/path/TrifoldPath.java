package net.aegistudio.sketchuml.path;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import de.dubs.dollarn.PointR;

public interface TrifoldPath extends Cloneable {
	/**
	 * The penalty inflicted when the line should penetrate
	 * the object to reach the bound. 
	 */
	public static final double PENALTY_PENETRATE = 10.;
	
	/**
	 * The penalty inflicted when the path has a part 
	 * cutting the line of the bound.
	 */
	public static final double PENALTY_TANGENT = 5.;
	
	/**
	 * Construct the path from the provided data.
	 * 
	 * The starting point is calculated by its container
	 * as it can either start from the center of both 
	 * entities or the boundary of the entity. To reduce
	 * complexity we require the caller to calculate the
	 * starting and ending point before invoking the path. 
	 * 
	 * @see net.aegistudio.sketchuml.path.TrifoldProxyPath
	 * 
	 * @param pointStart the starting point of the path.
	 * @param pointEnd the ending point of the path.
	 * @param separatePoints the points on the path.
	 * @param controlPoints the control points between 
	 * the intra-points.
	 */
	public void makePath(PointR pointStart, PointR pointEnd, 
			List<PointR> separatePoints, List<PointR> controlPoints);
	
	/**
	 * Perform articulation and return the minimum fitness after
	 * articulating. The fitness must be returned by invoking
	 * LinePiece.distance or ParametricLinePiece.gradientDescent.
	 * 
	 * The followed value is the direction of the points on the
	 * bound. See LinePiece.BoxIntersectStatus for enumeration
	 * details.
	 * 
	 * @see net.aegistudio.sketchuml.path.LinePiece#distance
	 * (Vector, Vector, double, double)
	 * @see net.aegistudio.sketchuml.path.ParametricLinePiece
	 * #gradientDescent(double[], Vector, Vector, double, double, 
	 * double, double, double, double, int)
	 * @see net.aegistudio.sketchuml.path.LinePiece.BoxIntersectStatus
	 * 
	 * @param stroke the points in the user input stroke.
	 * @param traningStroke the stroke for training, having the 
	 * evaluated beginning and ending point for training.
	 * @param intersectBegin the intersection of the begin bound.
	 * @param intersectEnd the intersection of the end bound.
	 */
	public double articulateAndFitness(Vector<PointR> stroke,
			Vector<PointR> trainingStroke,
			int intersectBegin, int intersectEnd);
	
	/**
	 * Records the implementations of all trifold paths. While new 
	 * implementation is added, to keep compatibility with previous
	 * version, it should be directly appended to the end of the list.
	 */
	public static List<Class<? extends TrifoldPath>> IMPLEMENTATIONS = 
		Arrays.asList(TrifoldLinePath.class, 
			TrifoldRectPath.class, TrifoldRoundRectPath.class,
			TrifoldLiftPath.class, TrifoldRoundLiftPath.class,
			TrifoldZigzagPath.class);
	
	/**
	 * Persistent of the internal state of this path object.
	 * @param outputStream the data persistent output stream.
	 * @throws IOException while the data block is malformed or there's
	 * some I/O error occurs.
	 */
	public void writePath(DataOutputStream outputStream) throws IOException;
	
	/**
	 * Persistent of the internal state of this path object.
	 * @param inputStream the data persistent input stream.
	 * @throws IOException while there's some I/O error occurs.
	 */
	public void readPath(DataInputStream inputStream) throws IOException;
}