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

import java.util.Collections;
import java.util.Vector;

// This class was added to extend $1 to $N. It allows a Multistroke
// object to encapsulate several Gestures (all possible orderings
// and directions of a particular multistroke gesture).

public class Multistroke implements Comparable<Multistroke> {
	public String Name;
	public String User;
	public String Speed;
	public int NumStrokes = -1; // how many strokes this multistroke has
	public Vector<Gesture> Gestures; // all possible orderings/directions of
										// this multistroke gesture
	public Gesture OriginalGesture; // the original gesture used to instantiate
									// this Multistroke

	public Multistroke() {
		this.Name = "";
		this.User = "";
		this.Speed = "";
		this.Gestures = null;
		this.OriginalGesture = null;
	}
	
	// when a new Multistroke is made, it handles pre-processing the points
	// given
	// so that all possible orderings and directions of the points are handled.
	// this allows $N to receive 1 template for a multistroke gesture such as
	// "="
	// without limiting future recognition to users writing that template with
	// the
	// strokes in the same order and the same direction.
	public Multistroke(String name, String user, String spd,
			Vector<Vector<PointR>> strokes) {
		this.Name = name;
		this.User = user;
		this.Speed = spd;

		// Lisa 8/8/2009
		// combine the strokes into one unistroke gesture to save the original
		// gesture
		Vector<PointR> points = new Vector<PointR>();
		for (Vector<PointR> pts : strokes) {
			points.addAll(pts);
		}
		this.OriginalGesture = new Gesture(name, points);

		this.NumStrokes = strokes.size();

		// if it's a unistroke and we are trying to emulate $1, don't process;
		// Lisa 8/16/2009
		if (!NDollarParameters.getInstance().ProcessUnistrokes
				&& this.NumStrokes == 1) {
			this.Gestures = new Vector<Gesture>(1);
			this.Gestures.add(this.OriginalGesture);
		} else {
			// Computes all possible stroke orderings/stroke direction
			// combinations of the
			// given Gesture. This is done in two steps:
			// 1. Use the algorithm HeapPermute(n) to find all possible
			// orderings (permutations)
			// 2. For each ordering,
			// Use the binary enumeration technique to enumerate all possible
			// combinations of stroke directions.
			Vector<Integer> defaultOrder = new Vector<Integer>(strokes.size()); // array
																				// of
																				// integer
																				// indices
			for (int i = 0; i < strokes.size(); i++) {
				defaultOrder.add(i); // initialize
			}

			Vector<Vector<Integer>> allOrderings = new Vector<Vector<Integer>>();
			// HeapPermute operates on the indices
			HeapPermute(this.NumStrokes, defaultOrder, allOrderings);
			// now allOrderings should contain all possible permutations of the
			// stroke indices

			// now enumerate each ordering with all possible stroke directions
			// (forward/backward)
			// operates directly on the strokes
			Vector<Vector<PointR>> unistrokes = MakeUnistrokes(strokes,
					allOrderings);

			this.Gestures = new Vector<Gesture>(unistrokes.size());
			for (Vector<PointR> entry : unistrokes) {
				Gesture newG = new Gesture(this.Name, entry);
				this.Gestures.add(newG);
			}
		}
	}

	// this algorithm is given by B. Heap
	// A. Levitin, Introduction to The Design & Analysis of Algorithms, Addison
	// Wesley, 2003
	// http://www.cut-the-knot.org/do_you_know/AllPerm.shtml
	//
	// NOTE: this will side effect into allOrders, Lisa 8/8/2009
	public void HeapPermute(int n, Vector<Integer> currentOrder,
			Vector<Vector<Integer>> allOrders) {
		if (n < 1)
			return;
		if (n == 1) // base case
		{
			// build return value to be an ArrayVector containing 1 ArrayVector
			// (strokes) of ArrayVectors (points)
			allOrders.add(new Vector<Integer>(currentOrder)); // copy
		} else {
			for (int i = 0; i < n; i++) {
				// recurse here, building up set of Vectors
				HeapPermute(n - 1, currentOrder, allOrders);
				if ((n % 2) == 1) // odd n
				{
					SwapStrokes(0, n - 1, currentOrder);
				} else // even n
				{
					SwapStrokes(i, n - 1, currentOrder);
				}
			}
		}
	}

	// swap the strokes given by the indices "first" and "second" in the
	// "order" argument; this DOES change the ArrayVector sent as an argument.
	private void SwapStrokes(int first, int second, Vector<Integer> order) {
		Collections.swap(order, first, second);
	}

	// now swap stroke directions within all possible permutations
	// this can be done by treating the strokes as binary variables (F=0, B=1)
	// therefore, for each ordering, iterate 2^(num strokes) and extract bits of
	// that # to determine which stroke is forward and which is backward
	// allOrderings has indices in it
	public Vector<Vector<PointR>> MakeUnistrokes(
			Vector<Vector<PointR>> originalStrokes,
			Vector<Vector<Integer>> allOrderings) {
		Vector<Vector<PointR>> allUnistrokes = new Vector<Vector<PointR>>(); // will
																				// contain
																				// all
																				// possible
																				// orderings/direction
																				// enumerations
																				// of
																				// this
																				// gesture
		for (Vector<Integer> ordering : allOrderings) {
			for (int b = 0; b < Math.pow(2d, ordering.size()); b++) // decimal
																	// value b
			{
				Vector<PointR> unistroke = new Vector<PointR>(); // we're
																	// building
																	// a
																	// unistroke
																	// instead
																	// of
																	// multistroke
																	// now for
																	// ease of
																	// processing
				for (int i = 0; i < ordering.size(); i++) // examine b's bits
				{
					// copy the correct unistroke
					Vector<PointR> stroke = new Vector<PointR>(
							originalStrokes.elementAt((int) ordering
									.elementAt(i)));
					if (((b >> i) & 1) == 1) // if (BitAt(b, i) == 1), i.e., is
												// b's bit at index i on?
					{
						Collections.reverse(stroke); // reverse the strokes
					}
					unistroke.addAll(stroke); // add stroke to current
												// strokePermute
				}
				// add completed strokePermute to set of strokePermutes (aka
				// Multistrokes)
				allUnistrokes.add(unistroke);
			}
		}
		return allUnistrokes;
	}

	@Override
	public int compareTo(Multistroke ms) {
		return (Name.equals(ms.Name)) ? 0 : 1;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Multistring{name=" + Name + ", ");
		buf.append("user=" + User + ", ");
		buf.append("speed=" + Speed + ", ");
		buf.append("gestures=[");
		for (int i = 0; i < Gestures.size(); ++i) {
			buf.append(Gestures.elementAt(i).Name + " "); // +"[ ");
			// Vector<PointR> points = Gestures.elementAt(i).Points;
			// for(int j=0; j<points.size(); ++j){
			// buf.append(points.elementAt(i).X+",");
			// buf.append(points.elementAt(i).Y+",");
			// buf.append(points.elementAt(i).T+" ");
			// }
			// buf.append("] ");
		}
		buf.append("]}");
		return buf.toString();
	}

}
