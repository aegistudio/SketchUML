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

import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PointR {
	public double X, Y;
	public int T;

	public PointR() {
		X = 0;
		Y = 0;
		T = 0;
	}

	public PointR(double x, double y) {
		X = x;
		Y = y;
		T = 0;
	}

	public PointR(double x, double y, int t) {
		X = x;
		Y = y;
		T = t;
	}

	// copy constructor
	public PointR(PointR p) {
		X = p.X;
		Y = p.Y;
		T = p.T;
	}

	public static boolean equals(PointR p1, PointR p2) {
		return (p1.X == p2.X && p1.Y == p2.Y);
	}

	public boolean equals(Object obj) {
		if (obj instanceof PointR) {
			PointR p = (PointR) obj;
			return (X == p.X && Y == p.Y);
		}
		return false;
	}

	// This methods are trimmed to be used with sketch uml.
	
	public void read(DataInputStream inputStream) throws IOException {
		X = inputStream.readDouble();
		Y = inputStream.readDouble();
		T = inputStream.readInt();
	}
	
	public void write(DataOutputStream outputStream) throws IOException {
		outputStream.writeDouble(X);
		outputStream.writeDouble(Y);
		outputStream.writeInt(T);
	}
	
	public double normalize() {
		double modulus = modulus();
		if(modulus == 0.0) X = Y = 0;
		else { X /= modulus; Y /= modulus; }
		return modulus;
	}
	
	public double modulus() {
		return Math.sqrt(X * X + Y * Y);
	}
	
	public double dot(PointR a) {
		return X * a.X + Y * a.Y;
	}
	
	public void interpolate(double ratio, PointR begin, PointR end) {
		combine(1.0 - ratio, begin, ratio, end);
	}
	
	public void combine(double a, PointR x, double b, PointR y) {
		X = a * x.X + b * y.X;
		Y = a * x.Y + b * y.Y; 
	}
	
	public boolean inside(Rectangle2D r2d) {
		return r2d.contains(X, Y);
	}
}
