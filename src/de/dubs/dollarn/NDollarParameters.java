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

// This class handles reading in and setting the recognizer parameters
// for recognition and batch testing. The values will be read in from
// a file in the NDollar runtime directory (usually called config.xml --
// that value is set in the constructor).

public class NDollarParameters
{
	static NDollarParameters instance = null; // singleton instance

	// Note: these default values are never used because it uses config.xml always.
	// They are provided as example values only.
	public boolean RotationInvariant = false; // when set to false, recognition is sensitive to rotation
	public boolean ProcessUnistrokes = true; // when set to false, does not reverse order of unistrokes
	public boolean Include1D = true; // when set to false, does not include gestures who pass the 1D test
	public boolean Include2D = true; // when set to false, does not include gestures who fail the 1D test -- and are therefore 2D
	public boolean TestFor1D = true; // when set to false, it's like $1 and ignores 1D vs 2D distinction
	public boolean UseUniformScaling = false; // default should be false; when set to true, does uniform scaling for all shape types
	public boolean MatchOnlyIfSameNumberOfStrokes = false; // when set to true, only allows matches with templates with the same number of strokes
	public boolean DoStartAngleComparison = false; // when set to true, will reduce # of comparisons done based on initial angle of start of gesture
	public int StartAngleIndex = 8; // options: 4, 8
	public double StartAngleThreshold = Utils.Deg2Rad(30.0); // options: 30, 45, 60
	public int NumResamplePoints = 64; // options: 16, 64, 96
	
	public PossibleSearchMethods SearchMethod = PossibleSearchMethods.Protractor;
	public enum PossibleSearchMethods {Protractor, GSS};
	
	// COMMENT Haoran Luo
	// This configuration is used to incorporate with the multistroke streams that stores the pre-computed data.
	public boolean StorePrecompute = true;
	
	// warning: non-thread safe version on first access but this should have low impact
	public static NDollarParameters getInstance()
	{
		if (instance == null){
			instance = new NDollarParameters();
		}
		return instance;
	}

}
