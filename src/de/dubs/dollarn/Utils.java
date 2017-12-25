package de.dubs.dollarn;
/*
 *  The $N Multistroke Recognizer (Java version)
 *
 *      Jan Sonnenberg, Ph.D.
 *      TU Braunschweig
 *      Institut fuer Nachrichtentechnik
 *      Schleinitzstr. 22
 *      38106 Braunschweig
 *      sonnenberg@ifn.ing.tu-bs.de
 *      
 * Based on the $N Multistroke Recognizer (C# version)
 *
 *	    Lisa Anthony, Ph.D.
 *		UMBC
 *		Information Systems Department
 * 		1000 Hilltop Circle
 *		Baltimore, MD 21250
 * 		lanthony@umbc.edu
 * 
 *      Jacob O. Wobbrock, Ph.D.
 * 		The Information School
 *		University of Washington
 *		Mary Gates Hall, Box 352840
 *		Seattle, WA 98195-2840
 *		wobbrock@u.washington.edu
 *
 * The Protractor enhancement was published by Yang Li and programmed
 * here by Lisa Anthony and Jacob O. Wobbrock.
 *
 *	Li, Y. (2010). Protractor: A fast and accurate gesture 
 *	  recognizer. Proceedings of the ACM Conference on Human 
 *	  Factors in Computing Systems (CHI '10). Atlanta, Georgia
 *	  (April 10-15, 2010). New York: ACM Press, pp. 2169-2172.
 *
 * This software is distributed under the "New BSD License" agreement:
 * 
 * Copyright (c) 2007-2011, Lisa Anthony and Jacob O. Wobbrock
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the University of Washington nor UMBC,
 *      nor the names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Jacob O. Wobbrock OR Lisa Anthony 
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

import java.util.Vector;

public class Utils {

	public static RectangleR FindBox(Vector<PointR> points) {
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;

		for (PointR p : points) {
			if (p.X < minX)
				minX = p.X;
			if (p.X > maxX)
				maxX = p.X;

			if (p.Y < minY)
				minY = p.Y;
			if (p.Y > maxY)
				maxY = p.Y;
		}

		return new RectangleR(minX, minY, maxX - minX, maxY - minY);
	}

	public static double round(double x, int digits) {
		return (double) Math.round(x * 10 * (digits)) / (10 * digits);
	}

	public static double Distance(PointR p1, PointR p2) {
		double dx = p2.X - p1.X;
		double dy = p2.Y - p1.Y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	// compute the centroid of the points given
	public static PointR Centroid(Vector<PointR> points) {
		double xsum = 0.0;
		double ysum = 0.0;

		for (PointR p : points) {
			xsum += p.X;
			ysum += p.Y;
		}
		return new PointR(xsum / points.size(), ysum / points.size());
	}

	public static double PathLength(Vector<PointR> points) {
		double length = 0;
		for (int i = 1; i < points.size(); i++) {
			length += Distance((PointR) points.elementAt(i - 1),
					(PointR) points.elementAt(i));
		}
		return length;
	}

	// determines the angle, in degrees, between two points. the angle is
	// defined
	// by the circle centered on the start point with a radius to the end point,
	// where 0 degrees is straight right from start (+x-axis) and 90 degrees is
	// straight down (+y-axis).
	public static double AngleInDegrees(PointR start, PointR end,
			boolean positiveOnly) {
		double radians = AngleInRadians(start, end, positiveOnly);
		return Rad2Deg(radians);
	}

	// determines the angle, in radians, between two points. the angle is
	// defined
	// by the circle centered on the start point with a radius to the end point,
	// where 0 radians is straight right from start (+x-axis) and PI/2 radians
	// is
	// straight down (+y-axis).
	public static double AngleInRadians(PointR start, PointR end,
			boolean positiveOnly) {
		double radians = 0.0;
		if (start.X != end.X) {
			radians = Math.atan2(end.Y - start.Y, end.X - start.X);
		} else // pure vertical movement
		{
			if (end.Y < start.Y)
				radians = -Math.PI / 2.0; // -90 degrees is straight up
			else if (end.Y > start.Y)
				radians = Math.PI / 2.0; // 90 degrees is straight down
		}
		if (positiveOnly && radians < 0.0) {
			radians += Math.PI * 2.0;
		}
		return radians;
	}

	public static double Rad2Deg(double rad) {
		return (rad * 180d / Math.PI);
	}

	public static double Deg2Rad(double deg) {
		return (deg * Math.PI / 180d);
	}

	// rotate the points by the given degrees about their centroid
	public static Vector<PointR> RotateByDegrees(Vector<PointR> points,
			double degrees) {
		double radians = Deg2Rad(degrees);
		return RotateByRadians(points, radians);
	}

	// rotate the points by the given radians about their centroid
	public static Vector<PointR> RotateByRadians(Vector<PointR> points,
			double radians) {
		Vector<PointR> newPoints = new Vector<PointR>(points.size());
		PointR c = Centroid(points);

		double cos = Math.cos(radians);
		double sin = Math.sin(radians);

		double cx = c.X;
		double cy = c.Y;

		for (int i = 0; i < points.size(); i++) {
			PointR p = (PointR) points.elementAt(i);

			double dx = p.X - cx;
			double dy = p.Y - cy;

			PointR q = new PointR();
			q.X = dx * cos - dy * sin + cx;
			q.Y = dx * sin + dy * cos + cy;

			newPoints.add(q);
		}
		return newPoints;
	}

	// Rotate a point 'p' around a point 'c' by the given radians.
	// Rotation (around the origin) amounts to a 2x2 matrix of the form:
	//
	// [ cos A -sin A ] [ p.x ]
	// [ sin A cos A ] [ p.y ]
	//
	// Note that the C# Math coordinate system has +x-axis stright right and
	// +y-axis straight down. Rotation is clockwise such that from +x-axis to
	// +y-axis is +90 degrees, from +x-axis to -x-axis is +180 degrees, and
	// from +x-axis to -y-axis is -90 degrees.
	public static PointR RotatePoint(PointR p, PointR c, double radians) {
		PointR q = new PointR();
		q.X = (p.X - c.X) * Math.cos(radians) - (p.Y - c.Y) * Math.sin(radians)
				+ c.X;
		q.Y = (p.X - c.X) * Math.sin(radians) + (p.Y - c.Y) * Math.cos(radians)
				+ c.Y;
		return q;
	}

	// Calculate the angle from the initial point of the array of Points
	// (points.elementAt(0))
	// to points[index].
	//
	// Returns this angle represented by a unit vector (stored in a Point).
	//
	// **This is used in Gesture.cs:Gesture() to compute the start angle in
	// support
	// of the optimization to not compare candidates to templates whose start
	// angles
	// are widely different. Lisa 8/8/2009
	public static PointR CalcStartUnitVector(Vector<PointR> points, int index) {
		// v is the vector from points.elementAt(0) to points[index]
		PointR v = new PointR(((PointR) points.elementAt(index)).X
				- ((PointR) points.elementAt(0)).X,
				((PointR) points.elementAt(index)).Y
						- ((PointR) points.elementAt(0)).Y);
		// len is the length of vector v
		double len = Math.sqrt(v.X * v.X + v.Y * v.Y);
		// the unit vector representing the angle between points.elementAt(0)
		// and points[index]
		// is the vector v divided by its length len
		// TODO: does there need to be a divide by zero check?
		return new PointR(v.X / len, v.Y / len);
	}

	// will return result in radians
	public static double AngleBetweenUnitVectors(PointR v1, PointR v2) // gives
																		// acute
																		// angle
																		// between
																		// unit
																		// vectors
																		// from
																		// (0,0)
																		// to
																		// v1,
																		// and
																		// (0,0)
																		// to v2
	{
		// changed this method on 9/28/2009, Lisa
		double test = v1.X * v2.X + v1.Y * v2.Y; // arc cosine of the vector dot
													// product
		// sometimes these two cases can happen because of rounding error in the
		// dot product calculation
		if (test < -1.0)
			test = -1.0; // truncate rounding errors
		if (test > 1.0)
			test = 1.0; // truncate rounding errors
		return Math.acos(test);
	}

	// translates the points so that the upper-left corner of their bounding box
	// lies at 'toPt'
	public static Vector<PointR> TranslateBBoxTo(Vector<PointR> points,
			PointR toPt) {
		Vector<PointR> newPoints = new Vector<PointR>(points.size());
		RectangleR r = Utils.FindBox(points);
		for (int i = 0; i < points.size(); i++) {
			PointR p = (PointR) points.elementAt(i);
			p.X += (toPt.X - r.getX());
			p.Y += (toPt.Y - r.getY());
			newPoints.add(p);
		}
		return newPoints;
	}

	// translates the points so that their centroid lies at 'toPt'
	public static Vector<PointR> TranslateCentroidTo(Vector<PointR> points,
			PointR toPt) {
		Vector<PointR> newPoints = new Vector<PointR>(points.size());
		PointR centroid = Centroid(points);
		for (int i = 0; i < points.size(); i++) {
			PointR p = (PointR) points.elementAt(i);
			p.X += (toPt.X - centroid.X);
			p.Y += (toPt.Y - centroid.Y);
			newPoints.add(p);
		}
		return newPoints;
	}

	// translates the points by the given delta amounts
	public static Vector<PointR> TranslateBy(Vector<PointR> points, SizeR sz) {
		Vector<PointR> newPoints = new Vector<PointR>(points.size());
		for (int i = 0; i < points.size(); i++) {
			PointR p = (PointR) points.elementAt(i);
			p.X += sz.getWidth();
			p.Y += sz.getHeight();
			newPoints.add(p);
		}
		return newPoints;
	}

	// new scaling methods rewritten by Lisa as of 8/9/2009 from input from Jake
	public static Vector<PointR> Scale(Vector<PointR> pts, double oneDRatio,
			SizeR size) // scales the oriented bbox based on 1D or 2D
	{
		if (NDollarParameters.getInstance().UseUniformScaling) // scale to a
																// uniform
																// circle
		{
			// do new thing
			PointR centroid = Utils.Centroid(pts);
			double radiusSquared = 1.0d;
			for (PointR point : pts) {
				double distanceSquared = Math.pow((centroid.X - point.X), 2.0)
						+ Math.pow((centroid.Y - point.Y), 2.0);
				if (distanceSquared > radiusSquared)
					radiusSquared = distanceSquared;
			}

			double factor = size.getWidth() / Math.sqrt(radiusSquared);// Assume
																		// that
																		// size
																		// is a
																		// square
																		// and
																		// arbitrarily
																		// select
																		// width
			// this could also be replaced with a constant value (250?)

			Vector<PointR> scaledPts = new Vector<PointR>();
			for (int i = 0; i < pts.size(); i++) {
				PointR p = new PointR((PointR) pts.elementAt(i));
				p.X *= factor;
				p.Y *= factor;
				scaledPts.add(p);
			}
			return scaledPts;
		} else // do old thing
		{
			return Utils.ScaleByDimension(pts, oneDRatio, size);
		}
	}

	public static Vector<PointR> ScaleByDimension(Vector<PointR> points,
			double oneDRatio, SizeR size) // scales properly based on 1D or 2D
	{
		RectangleR B = FindBox(points);
		boolean uniformly = false; // Lisa 8/16/2009; if we're not testing for
									// 1D (i.e., when emulating $1), never scale
									// uniformly
		if (NDollarParameters.getInstance().TestFor1D)
			uniformly = (Math.min(B.getWidth() / B.getHeight(), B.getHeight()
					/ B.getWidth()) <= oneDRatio); // 1D or 2D gesture test
		Vector<PointR> newpoints = new Vector<PointR>(points.size());
		for (int i = 0; i < points.size(); i++) {
			double qx = uniformly ? ((PointR) points.elementAt(i)).X
					* (size.getWidth() / Math.max(B.getWidth(), B.getHeight()))
					: ((PointR) points.elementAt(i)).X
							* (size.getWidth() / B.getWidth());
			double qy = uniformly ? ((PointR) points.elementAt(i)).Y
					* (size.getHeight() / Math.max(B.getWidth(), B.getHeight()))
					: ((PointR) points.elementAt(i)).Y
							* (size.getHeight() / B.getHeight());
			newpoints.add(new PointR(qx, qy));
		}
		return newpoints;
	}

	// scales the points so that they form the size given. does not restore the
	// origin of the box.
	public static Vector<PointR> ScaleTo(Vector<PointR> points, SizeR sz) {
		Vector<PointR> newPoints = new Vector<PointR>(points.size());
		RectangleR r = FindBox(points);
		for (int i = 0; i < points.size(); i++) {
			PointR p = (PointR) points.elementAt(i);
			if (r.getWidth() != 0d)
				p.X *= (sz.getWidth() / r.getWidth());

			if (r.getHeight() != 0d)
				p.Y *= (sz.getHeight() / r.getHeight());
			newPoints.add(p);
		}
		return newPoints;
	}

	// scales the points so that they form the size given. does not restore the
	// origin of the box.
	// adapted from ScaleTo() to smartly handle 1D scaling -- ie, if a gesture
	// is 1D,
	// scale to the longer dimension only, or we warp our gestures too much
	// Lisa 12/28/2007
	public static Vector<PointR> ScaleTo1D(Vector<PointR> points, SizeR sz) {
		Vector<PointR> newPoints = new Vector<PointR>(points.size());
		RectangleR r = FindBox(points);

		// scale both by same factor
		double scaleFactor = 0.0;
		if (r.getWidth() > r.getHeight())
			scaleFactor = r.getWidth();
		else
			scaleFactor = r.getHeight();

		for (int i = 0; i < points.size(); i++) {
			PointR p = (PointR) points.elementAt(i);
			if (r.getWidth() != 0d)
				p.X *= (sz.getWidth() / scaleFactor);

			if (r.getHeight() != 0d)
				p.Y *= (sz.getHeight() / scaleFactor);
			newPoints.add(p);
		}
		return newPoints;
	}

	// scales by the percentages contained in the 'sz' parameter. values of 1.0
	// would result in the
	// identity scale (that is, no change).
	public static Vector<PointR> ScaleBy(Vector<PointR> points, SizeR sz) {
		Vector<PointR> newPoints = new Vector<PointR>(points.size());
		for (int i = 0; i < points.size(); i++) {
			PointR p = (PointR) points.elementAt(i);
			p.X *= sz.getWidth();
			p.Y *= sz.getHeight();
			newPoints.add(p);
		}
		return newPoints;
	}

	// scales the points so that the length of their longer side
	// matches the length of the longer side of the given box.
	// thus, both dimensions are warped proportionally, rather than
	// independently, like in the function ScaleTo.
	public static Vector<PointR> ScaleToMax(Vector<PointR> points,
			RectangleR box) {
		Vector<PointR> newPoints = new Vector<PointR>(points.size());
		RectangleR r = FindBox(points);
		for (int i = 0; i < points.size(); i++) {
			PointR p = (PointR) points.elementAt(i);
			p.X *= (box.getMaxSide() / r.getMaxSide());
			p.Y *= (box.getMaxSide() / r.getMaxSide());
			newPoints.add(p);
		}
		return newPoints;
	}

	// scales the points so that the length of their shorter side
	// matches the length of the shorter side of the given box.
	// thus, both dimensions are warped proportionally, rather than
	// independently, like in the function ScaleTo.
	public static Vector<PointR> ScaleToMin(Vector<PointR> points,
			RectangleR box) {
		Vector<PointR> newPoints = new Vector<PointR>(points.size());
		RectangleR r = FindBox(points);
		for (int i = 0; i < points.size(); i++) {
			PointR p = (PointR) points.elementAt(i);
			p.X *= (box.getMinSide() / r.getMinSide());
			p.Y *= (box.getMinSide() / r.getMinSide());
			newPoints.add(p);
		}
		return newPoints;
	}

	public static Vector<PointR> Resample(Vector<PointR> points, int n) {
		double I = PathLength(points) / (n - 1); // interval length
		double D = 0.0;
		Vector<PointR> srcPts = new Vector<PointR>(points);
		Vector<PointR> dstPts = new Vector<PointR>(n);
		dstPts.add(srcPts.elementAt(0));
		for (int i = 1; i < srcPts.size(); i++) {
			PointR pt1 = (PointR) srcPts.elementAt(i - 1);
			PointR pt2 = (PointR) srcPts.elementAt(i);

			double d = Distance(pt1, pt2);
			if ((D + d) >= I) {
				double qx = pt1.X + ((I - D) / d) * (pt2.X - pt1.X);
				double qy = pt1.Y + ((I - D) / d) * (pt2.Y - pt1.Y);
				PointR q = new PointR(qx, qy);
				dstPts.add(q); // append new point 'q'
				srcPts.insertElementAt(q, i); // insert 'q' at position i in
												// points s.t.
				// 'q' will be the next i
				D = 0.0;
			} else {
				D += d;
			}
		}
		// somtimes we fall a rounding-error short of adding the last point, so
		// add it if so
		if (dstPts.size() == n - 1) {
			dstPts.add(srcPts.elementAt(srcPts.size() - 1));
		}
		return dstPts;
	}

	// computes the 'distance' between two point paths by summing their
	// corresponding point distances.
	// assumes that each path has been resampled to the same number of points at
	// the same distance apart.
	public static double PathDistance(Vector<PointR> path1, Vector<PointR> path2) {
		double distance = 0;
		for (int i = 0; i < path1.size(); i++) {
			distance += Distance((PointR) path1.elementAt(i),
					(PointR) path2.elementAt(i));
		}
		return distance / path1.size();
	}

	// / <summary>
	// / Gets a random number between low and high, inclusive.
	// / </summary>
	// / <param name="low"></param>
	// / <param name="high"></param>
	// / <returns></returns>
	public static int Random(int low, int high) {
		return low + (int) (((double) high - (double) low) * Math.random());
	}

	// / <summary>
	// / Gets multiple random numbers between low and high, inclusive. The
	// / numbers are guaranteed to be distinct.
	// / </summary>
	// / <param name="low"></param>
	// / <param name="high"></param>
	// / <param name="num"></param>
	// / <returns></returns>
	public static int[] Random(int low, int high, int num) {
		int[] array = new int[num];
		for (int i = 0; i < num; i++) {
			array[i] = Random(low, high + 1);
			for (int j = 0; j < i; j++) {
				if (array[i] == array[j]) {
					i--; // redo i
					break;
				}
			}
		}
		return array;
	}

	// determine if this gesture is 1D or 2D based on ratio of "oriented"
	// (rotated to 0)
	// bounding box compared to a threshold (determined empirically)
	// Lisa 1/2/2008
	public static boolean Is1DGesture(Vector<PointR> rawPts) // , String name)
	{
		// make a copy of the pts
		Vector<PointR> pts = new Vector<PointR>(rawPts);

		// rotate points to 0 (temporarily!)
		double radians = Utils.AngleInRadians(Utils.Centroid(pts),
				(PointR) pts.elementAt(0), false);
		pts = Utils.RotateByRadians(pts, -radians); // undo angle

		// determine ratio of height to width to see which side is shorter
		RectangleR r = Utils.FindBox(pts);

		// check for divide by zero
		if ((r.getWidth() == 0) || (r.getHeight() == 0))
			return true;
		else if ((r.getWidth() / r.getHeight()) < (r.getHeight() / r.getWidth())) // width
																					// is
																					// shorter
																					// side
		{
			if ((r.getWidth() / r.getHeight()) < NDollarRecognizer._1DThreshold)
				return true;
			else
				return false;
		} else // else height is shorter side
		{
			if ((r.getHeight() / r.getWidth()) < NDollarRecognizer._1DThreshold)
				return true;
			else
				return false;
		}
	}

}
