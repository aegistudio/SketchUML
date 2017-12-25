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

public class Gesture implements Comparable<Gesture> {
	public String Name;
	public Vector<PointR> RawPoints; // raw points (for drawing) -- read in from
										// XML
	public Vector<PointR> Points; // resampled points (for matching) -- done
									// when loaded
	public boolean Is1D; // flag indicating if this is a 1D or a 2D gesture,
							// Lisa 1/2/2008; 8/9/2009 boolean now
	public PointR StartUnitVector; // Lisa 8/8/2009
	public Vector<Double> VectorVersion; // Lisa 3/7/2011 -- added to help with
											// Protractor testing

	public Gesture() {
		this.Name = "";
		this.RawPoints = null;
		this.Points = null;
		this.Is1D = true; // Lisa 1/2/2008
	}

	// when a new prototype is made, its raw points are resampled into n
	// equidistantly spaced
	// points, then it is scaled to a preset size and translated to a preset
	// origin. this is
	// the same treatment applied to each candidate stroke, and it allows us to
	// thereafter
	// simply step through each point in each stroke and compare those points'
	// distances.
	// in other words, it removes the challenge of determining corresponding
	// points in each gesture.
	// after resampling, scaling, and translating, we compute the
	// "indicative angle" of the
	// stroke as defined by the angle between its centroid point and first
	// point.
	public Gesture(String name, Vector<PointR> points) {
		this(points);
		this.Name = name;
	}

	public Gesture(Vector<PointR> points) {
		this.Name = "";
		this.RawPoints = new Vector<PointR>(points); // copy (saved for drawing)

		Points = RawPoints;

		// reflects new order of pre-processing steps as of 8/31/2009
		// old order (from 8/8/2009) was
		// 1. check for 1D
		// 2. scale
		// 3. resample
		// 4. translate
		// 5. rotate (if rot-invariant)
		// 6. calculate start angle

		// new order (as of 8/31/2009) is
		// 1. resample
		// 2. rotate (all); save amount
		// 3. check for 1D
		// 4. scale
		// 5. rotate back (if NOT rot-invariant)
		// 6. translate
		// 7. calculate start angle

		// first, resample (influences calculation of centroid)
		Points = Utils.Resample(Points,
				NDollarParameters.getInstance().NumResamplePoints);

		// then, if we are rotation-invariant, rotate to a common reference
		// angle
		// otherwise skip that step
		// Lisa 8/8/2009: this is now set by a flag in the NDollarRecognizer.cs
		// file with all the other flags
		// rotate so that the centroid-to-1st-point is at zero degrees
		double radians = Utils.AngleInRadians(Utils.Centroid(Points),
				(PointR) Points.elementAt(0), false);
		Points = Utils.RotateByRadians(Points, -radians);

		// then, resize to a square
		// check for 1D vs 2D (because we resize differently)
		// Lisa 1/2/2008
		// replace with boolean, Lisa 8/9/2009
		this.Is1D = Utils.Is1DGesture(RawPoints);

		// scale to a common (square) dimension
		// moved determination of scale method to within the scale() method for
		// less branching here
		// Lisa 8/9/2009
		Points = Utils.Scale(Points, NDollarRecognizer._1DThreshold,
				NDollarRecognizer.ResampleScale);

		// next, if NOT rotation-invariant, rotate back
		if (!NDollarParameters.getInstance().RotationInvariant) {
			Points = Utils.RotateByRadians(Points, +radians); // undo angle
		}

		// next, translate to a common origin
		Points = Utils.TranslateCentroidTo(Points,
				NDollarRecognizer.ResampleOrigin);

		// finally, save the start angle
		// Lisa 8/8/2009
		// store the start unit vector after post-processing steps
		this.StartUnitVector = Utils.CalcStartUnitVector(Points,
				NDollarParameters.getInstance().StartAngleIndex);

		// Lisa 3/7/2011
		// make the simple vector-based version for Protractor testing
		this.VectorVersion = Vectorize(this.Points);
	}

	public int getDuration() {
		if (RawPoints.size() >= 2) {
			PointR p0 = (PointR) RawPoints.elementAt(0);
			PointR pn = (PointR) RawPoints.elementAt(RawPoints.size() - 1);
			return pn.T - p0.T;
		} else {
			return 0;
		}
	}

	// From http://yangl.org/protractor/Protractor%20Gesture%20Recognizer.pdf
	// Given a list of PointR's this can translate them into a flat list of X,Y
	// coordinates,
	// a Vector, which is needed by Protractor's OptimalCosineDistance().
	private Vector<Double> Vectorize(Vector<PointR> pts) {
		// skip the resampling, translation because $N already did this in
		// pre-processing
		// re-do the rotation though
		// (note: doing rotation on the pre-processed points is ok because $N
		// rotates it back to the
		// original orientation if !RotationInvariant, e.g., it is rotation
		// sensitive)
		//
		// 04/04/2011 [JOW]: If RotationInvariant, no reason to do this stuff so
		// optimize by skipping.
		//
		double cos = 1.0;
		double sin = 0.0;

		if (!NDollarParameters.getInstance().RotationInvariant) // rotation
																// sensitive
		{
			double iAngle = Math.atan2(pts.elementAt(0).Y, pts.elementAt(0).X);
			double baseOrientation = (Math.PI / 4.0)
					* Math.floor((iAngle + Math.PI / 8.0) / (Math.PI / 4.0));
			cos = Math.cos(baseOrientation - iAngle);
			sin = Math.sin(baseOrientation - iAngle);
		}
		double sum = 0.0;
		Vector<Double> vector = new Vector<Double>();
		for (PointR p : pts) {
			double newX = p.X * cos - p.Y * sin;
			double newY = p.Y * cos + p.X * sin;
			vector.add(newX);
			vector.add(newY);
			sum += newX * newX + newY * newY;
		}
		double magnitude = Math.sqrt(sum);
		for (int i = 0; i < vector.size(); i++) {
			vector.setElementAt(vector.elementAt(i) / magnitude, i);
		}
		return vector;
	}

	@Override
	public int compareTo(Gesture g) {
		return (Name.equals(g.Name)) ? 0 : 1;
	}

	// / <summary>
	// / Pulls the gesture name from the file name, e.g., "circle03" from
	// "C:\gestures\circles\circle03.xml".
	// / </summary>
	// / <param name="s"></param>
	// / <returns></returns>
	public static String ParseName(String filename) {
		int start = filename.lastIndexOf('\\');
		int end = filename.lastIndexOf('_');
		return filename.substring(start + 1, end);
	}

}
