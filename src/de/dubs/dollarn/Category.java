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

public class Category {
	private String _name;
	private Vector<Multistroke> _prototypes;

	public Category(String name) {
		_name = name;
		_prototypes = null;
	}

	// changed to store Multistrokes instead of Gestures, Lisa 1/5/2008
	public Category(String name, Multistroke firstExample) // Gesture
															// firstExample)
	{
		_name = name;
		_prototypes = new Vector<Multistroke>();
		AddExample(firstExample);
	}

	public Category(String name, Vector<Multistroke> examples) {
		_name = name;
		_prototypes = new Vector<Multistroke>(examples.size());
		for (int i = 0; i < examples.size(); i++) {
			Multistroke p = (Multistroke) examples.elementAt(i);
			AddExample(p);
		}
	}

	public String getName() {
		return _name;
	}

	public int getNumExamples() {
		return _prototypes.size();
	}

	// / <summary>
	// / Indexer that returns the prototype at the given index within
	// / this gesture category, or null if the gesture does not exist.
	// / </summary>
	// / <param name="i"></param>
	// / <returns></returns>
	public Multistroke get(int i) // changed to Multistroke, Lisa 1/5/2008
	{
		if (0 <= i && i < _prototypes.size()) {
			return _prototypes.get(i); // Lisa 1/5/2008
		} else {
			return null;
		}
	}

	public void AddExample(Multistroke p) // changed to Multistroke, Lisa
											// 1/5/2008
	{
		boolean success = true;
		try {
			// first, ensure that p's name is right
			String name = ParseName(p.Name);
			if (!name.equals(_name))
				throw new Exception("Prototype \"" + name
						+ "\" does not equal the category \"" + _name
						+ "\" to which it was added.");

			// second, ensure that it doesn't already exist
			for (int i = 0; i < _prototypes.size(); i++) {
				Multistroke p0 = _prototypes.get(i); // Lisa 15/2/008
				if (p0.Name.equals(p.Name))
					throw new Exception("Prototype \"" + p0.Name
							+ "\" was added more than once to its category.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			success = false;
		}
		if (success) {
			_prototypes.add(p);
		}
	}

	// / <summary>
	// / Pulls the category name from the gesture name, e.g., "circle" from
	// "circle03".
	// /
	// / This has been updated to also be able to parse the category name from
	// the new
	// / dataset's format, e.g., "minus" from "minus_42_13_0".
	// / (Lisa 1/5/2008)
	// / </summary>
	// / <param name="s"></param>
	// / <returns></returns>
	public static String ParseName(String s) {
		String category = "";

		// check for which type it is
		// the original $1 gesture dataset doesn't have any ~ in the names
		// Lisa 1/5/2008
		if (!s.contains("~")) {
			for (int i = s.length() - 1; i >= 0; i--) {
				if (!Character.isDigit(s.charAt(i))) {
					category = s.substring(0, i + 1);
					break;
				}
			}
		} else // it's a new dataset, including the $1 gesture dataset when all
				// grouped together with unique names
		{
			category = s.substring(0, s.indexOf("~")); // start at beginning,
														// count=index of first
														// char not in the name
		}

		return category;
	}

}
