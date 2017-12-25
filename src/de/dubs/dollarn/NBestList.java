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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;

/**
 * The $N Multistroke Recognizer (C# version)
 * 
 * Lisa Anthony, Ph.D. UMBC Information Systems Department 1000 Hilltop Circle
 * Baltimore, MD 21250 lanthony@umbc.edu
 * 
 * Jacob O. Wobbrock, Ph.D. The Information School University of Washington Mary
 * Gates Hall, Box 352840 Seattle, WA 98195-2840 wobbrock@u.washington.edu
 * 
 * The Protractor enhancement was published by Yang Li and programmed here by
 * Lisa Anthony and Jacob O. Wobbrock.
 * 
 * Li, Y. (2010). Protractor: A fast and accurate gesture recognizer.
 * Proceedings of the ACM Conference on Human Factors in Computing Systems (CHI
 * '10). Atlanta, Georgia (April 10-15, 2010). New York: ACM Press, pp.
 * 2169-2172.
 * 
 * This software is distributed under the "New BSD License" agreement:
 * 
 * Copyright (c) 2007-2011, Lisa Anthony and Jacob O. Wobbrock All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the University of Washington nor
 * UMBC, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Jacob O. Wobbrock OR Lisa Anthony BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

public class NBestList {

	class NBestResult implements Comparable<NBestResult> {
		private String _name;
		private double _score;
		private double _distance;
		private double _angle;

		// constructor
		public NBestResult() {
			_name = "";
			_score = -1d;
			_distance = -1d;
			_angle = 0d;
		}

		public NBestResult(String name, double score, double distance,
				double angle) {
			_name = name;
			_score = score;
			_distance = distance;
			_angle = angle;
		}

		public String getName() {
			return _name;
		}

		public double getScore() {
			return _score;
		}

		public double getDistance() {
			return _distance;
		}

		public double getAngle() {
			return _angle;
		}

		public boolean getIsEmpty() {
			return _score == -1d;
		}

		// sorts in descending order of Score
		public int compareTo(NBestResult r) {
			if (_score < r._score)
				return 1;
			else if (_score > r._score)
				return -1;
			return 0;
		}
	}

	public static NBestList Empty = new NBestList();
	private ArrayList<NBestResult> _nBestList;
	private int _totalComparisons = 0;
	private int _actualComparisons = 0;

	public NBestList() {
		_nBestList = new ArrayList<NBestResult>();
	}

	public boolean getIsEmpty() {
		return _nBestList.size() == 0;
	}

	public void AddResult(String name, double score, double distance,
			double angle) {
		NBestResult r = new NBestResult(name, score, distance, angle);
		_nBestList.add(r);
	}

	public void SortDescending() {
		Collections.sort(_nBestList);
	}

	// below methods added by Lisa 8/9/2009
	// to get and set the # of comparisons fields
	public int getTotalComparisons() {
		return _totalComparisons;
	}

	public void setTotalComparisons(int n) {
		_totalComparisons = n;
	}

	public int getActualComparisons() {
		return _actualComparisons;
	}

	public void setActualComparisons(int n) {
		_actualComparisons = n;
	}

	// / <summary>
	// / Gets the gesture name of the top result of the NBestList.
	// / </summary>
	public String getName() {
		if (_nBestList.size() > 0) {
			NBestResult r = (NBestResult) _nBestList.get(0);
			return r.getName();
		}
		return "";
	}

	// / <summary>
	// / Gets the [0..1] matching score of the top result of the NBestList.
	// / </summary>
	public double getScore() {
		if (_nBestList.size() > 0) {
			NBestResult r = (NBestResult) _nBestList.get(0);
			return r.getScore();
		}
		return -1.0;
	}

	// / <summary>
	// / Gets the average pixel distance of the top result of the NBestList.
	// / </summary>
	public double getDistance() {
		if (_nBestList.size() > 0) {
			NBestResult r = (NBestResult) _nBestList.get(0);
			return r.getDistance();
		}
		return -1.0;

	}

	// / <summary>
	// / Gets the average pixel distance of the top result of the NBestList.
	// / </summary>
	public double getAngle() {
		if (_nBestList.size() > 0) {
			NBestResult r = (NBestResult) _nBestList.get(0);
			return r.getAngle();
		}
		return 0.0;
	}

	public NBestResult get(int index) {
		if (0 <= index && index < _nBestList.size()) {
			return (NBestResult) _nBestList.get(index);
		}
		return null;
	}

	public String[] getNames() {
		String[] s = new String[_nBestList.size()];
		if (_nBestList.size() > 0) {
			for (int i = 0; i < s.length; i++) {
				s[i] = ((NBestResult) _nBestList.get(i)).getName();
			}
		}
		return s;
	}

	public String getNamesString() {
		String s = "";
		if (_nBestList.size() > 0) {
			for (NBestResult r : _nBestList) {
				s += MessageFormat.format("{0},", r.getName());
			}
		}
		return s.replaceAll("[,]+$", "");

	}

	public double[] getScores() {
		double[] s = new double[_nBestList.size()];
		if (_nBestList.size() > 0) {
			for (int i = 0; i < s.length; i++) {
				s[i] = ((NBestResult) _nBestList.get(i)).getScore();
			}
		}
		return s;
	}

	public String getScoresString() {
		String s = "";
		if (_nBestList.size() > 0) {
			for (NBestResult r : _nBestList) {
				s += MessageFormat.format("{0,number},",
						Utils.round(r.getScore(), 3));
			}
		}
		return s.replaceAll("[,]+$", "");
	}

}
