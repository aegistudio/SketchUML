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

import java.text.DecimalFormat;

public class RectangleR {
	private static final int Digits = 4;
	private double _x;
	private double _y;
	private double _width;
	private double _height;
	public static final RectangleR Empty = new RectangleR(0, 0, 0, 0);
	DecimalFormat df = new DecimalFormat("#.###");

	public RectangleR(double x, double y, double width, double height) {
		_x = x;
		_y = y;
		_width = width;
		_height = height;
	}

	// copy constructor
	public RectangleR(RectangleR r) {
		_x = r.getX();
		_y = r.getY();
		_width = r.getWidth();
		_height = r.getHeight();
	}

	public double getX() {
		return Utils.round(_x, Digits);
	}

	public void setX(double value) {
		_x = value;
	}

	public double getY() {

		return Utils.round(_y, Digits);
	}

	public void setY(double value) {
		_y = value;

	}

	public double getWidth() {

		return Utils.round(_width, Digits);
	}

	public void setWidth(double value) {
		_width = value;
	}

	public double getHeight() {

		return Utils.round(_height, Digits);
	}

	public void setHeight(double value) {
		_height = value;

	}

	public PointR getTopLeft() {

		return new PointR(getX(), getY());
	}

	public PointR getBottomRight() {

		return new PointR(getX() + getWidth(), getY() + getHeight());
	}

	public PointR getCenter() {

		return new PointR(getX() + getWidth() / 2d, getY() + getHeight() / 2d);
	}

	public double getMaxSide() {

		return Math.max(_width, _height);
	}

	public double getMinSide() {
		return Math.min(_width, _height);
	}

	public double getDiagonal() {
		return Utils.Distance(getTopLeft(), getBottomRight());
	}

	public boolean equals(Object obj) {
		if (obj instanceof RectangleR) {
			RectangleR r = (RectangleR) obj;
			return (getX() == r.getX() && getY() == r.getY()
					&& getWidth() == r.getWidth() && getHeight() == r
						.getHeight());
		}
		return false;
	}

}
