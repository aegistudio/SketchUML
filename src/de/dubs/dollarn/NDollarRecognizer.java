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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import net.aegistudio.sketchmock.io.MultistrokeInputStream;
import net.aegistudio.sketchmock.io.MultistrokeOutputStream;

public class NDollarRecognizer {
	private final static String NAMESPACE = null;
	private final static String VERSION = "1.0";

	private static final double DX = 250.0;
	public static final SizeR ResampleScale = new SizeR(DX, DX);
	public static final double Diagonal = Math.sqrt(DX * DX + DX * DX);
	public static final double HalfDiagonal = 0.5 * Diagonal;
	public static final PointR ResampleOrigin = new PointR(0, 0);
	private static final double Phi = 0.5 * (-1 + Math.sqrt(5)); // Golden Ratio

	private static final double _RotationBound = 45.0; // Lisa 1/2/2008; could
														// also try 15.0;
														// changed from 45.0 so
														// we're no longer fully
														// rotation-invariant
	public static final double _1DThreshold = 0.30; // threshold for the ratio
													// between short-side and
													// long-side of a gesture,
													// Lisa 1/2/2008;
													// empirically determined
	private static final int _MinExamples = 5; // Lisa, 5/20/2008

	// Note that the configurable recognition parameters have now been moved to
	// a singleton
	// class NDollarParameters. Access this by
	// NDollarParameters.getInstance().<parameter>.
	// Lisa 8/16/2009

	// batch testing
	private static final int NumRandomTests = 100;

	private Hashtable<String, Multistroke> _gestures;

	// added for debugging, Lisa 8/9/2009
	public static final boolean _debug = false;

	public NDollarRecognizer() {
		_gestures = new Hashtable<String, Multistroke>(256);
	}

	public NBestList Recognize(Vector<PointR> points, int numStrokes) // candidate
																		// points
	{
		// removed the candidate transformations by creating a Gesture here
		// of the input points
		// this helps keep the transformations done to templates and candidates
		// the same
		// and we won't have to edit code in two places
		// Lisa, 5/12/2008
		Gesture candidate = new Gesture(points);

		NBestList nbest = new NBestList();

		// added to check how much savings we are getting out of the
		// Utils.AngleBetwenVUnitVectors() check
		// Lisa 8/9/2009
		int totalComparisons = 0;
		int actualComparisons = 0;

		// we have to compare the current gesture to all candidates,
		// each subgesture in our set of Multistrokes
		// Lisa 12/22/2007
		for (Multistroke ms : _gestures.values()) {
			// added as of 8/9/2009
			// optional -- only attempt match when number of strokes is same
			if (!NDollarParameters.getInstance().MatchOnlyIfSameNumberOfStrokes
					|| numStrokes == ms.NumStrokes) {

				NBestList thisMSnbest = new NBestList(); // store the best
															// Vector for just
															// this MS
				for (Gesture p : ms.Gestures) {
					totalComparisons++;
					// added as of 8/9/2009
					if (!NDollarParameters.getInstance().DoStartAngleComparison
							|| (NDollarParameters.getInstance().DoStartAngleComparison && Utils
									.AngleBetweenUnitVectors(
											candidate.StartUnitVector,
											p.StartUnitVector) <= NDollarParameters
									.getInstance().StartAngleThreshold)) {
						actualComparisons++;

						double score = -1;
						double[] best = new double[] { -1, -1, -1 };

						if (NDollarParameters.getInstance().SearchMethod == NDollarParameters.PossibleSearchMethods.GSS) {
							best = GoldenSectionSearch(candidate.Points, // to
																			// rotate
									p.Points, // to match
									Utils.Deg2Rad(-_RotationBound), // lbound,
																	// Lisa
																	// 1/2/2008
									Utils.Deg2Rad(+_RotationBound), // ubound,
																	// Lisa
																	// 1/2/2008
									Utils.Deg2Rad(2.0) // threshold
							);

							score = 1d - best[0] / HalfDiagonal;
						} else if (NDollarParameters.getInstance().SearchMethod == NDollarParameters.PossibleSearchMethods.Protractor) {
							best = OptimalCosineDistance(p.VectorVersion,
									candidate.VectorVersion); // candidate.Points,
																// p.Points);
							score = 1 / best[0]; // distance
						}

						// keep track of what subgesture was best match for this
						// multistroke
						// and only add that particular template's score to the
						// nbest Vector
						// Lisa 12/22/2007
						thisMSnbest.AddResult(p.Name, score, best[0], best[1]); // name,
																				// score,
																				// distance,
																				// angle
					}
				}
				thisMSnbest.SortDescending();
				// add the one that was best of those subgestures
				// these properties return the property of the top result
				// Lisa 12/22/2007
				nbest.AddResult(thisMSnbest.getName(), thisMSnbest.getScore(),
						thisMSnbest.getDistance(), thisMSnbest.getAngle()); // name,
																			// score,
																			// distance,
																			// angle
			}
		}
		nbest.SortDescending(); // sort so that nbest[0] is best result
		nbest.setTotalComparisons(totalComparisons);
		nbest.setActualComparisons(actualComparisons);
		return nbest;
	}

	// From http://yangl.org/protractor/Protractor%20Gesture%20Recognizer.pdf
	private double[] OptimalCosineDistance(Vector<Double> v1, Vector<Double> v2) {
		double a = 0;
		double b = 0;

		for (int i = 0; i < v1.size(); i = i + 2) {
			a = a + v1.elementAt(i) * v2.elementAt(i) + v1.elementAt(i + 1)
					* v2.elementAt(i + 1);
			b = b + v1.elementAt(i) * v2.elementAt(i + 1) - v1.elementAt(i + 1)
					* v2.elementAt(i);
		}

		double angle = Math.atan(b / a);
		return new double[] {
				Math.acos(a * Math.cos(angle) + b * Math.sin(angle)),
				Utils.Rad2Deg(angle), 0d }; // distance, angle, calls to path
											// dist (n/a)
	}

	// From http://www.math.uic.edu/~jan/mcs471/Lec9/gss.pdf
	private double[] GoldenSectionSearch(Vector<PointR> pts1,
			Vector<PointR> pts2, double a, double b, double threshold) {
		double x1 = Phi * a + (1 - Phi) * b;
		Vector<PointR> newPoints = Utils.RotateByRadians(pts1, x1);
		double fx1 = Utils.PathDistance(newPoints, pts2);

		double x2 = (1 - Phi) * a + Phi * b;
		newPoints = Utils.RotateByRadians(pts1, x2);
		double fx2 = Utils.PathDistance(newPoints, pts2);

		double i = 2.0; // calls
		while (Math.abs(b - a) > threshold) {
			if (fx1 < fx2) {
				b = x2;
				x2 = x1;
				fx2 = fx1;
				x1 = Phi * a + (1 - Phi) * b;
				newPoints = Utils.RotateByRadians(pts1, x1);
				fx1 = Utils.PathDistance(newPoints, pts2);
			} else {
				a = x1;
				x1 = x2;
				fx1 = fx2;
				x2 = (1 - Phi) * a + Phi * b;
				newPoints = Utils.RotateByRadians(pts1, x2);
				fx2 = Utils.PathDistance(newPoints, pts2);
			}
			i++;
		}
		return new double[] { Math.min(fx1, fx2), Utils.Rad2Deg((b + a) / 2.0),
				i }; // distance, angle, calls to pathdist
	}

	// continues to rotate 'pts1' by 'step' degrees as long as points become
	// ever-closer
	// in path-distance to pts2. the initial distance is given by D. the best
	// distance
	// is returned in array[0], while the angle at which it was achieved is in
	// array[1].
	// array[3] contains the number of calls to PathDistance.
	private double[] HillClimbSearch(Vector<PointR> pts1, Vector<PointR> pts2,
			double D, double step) {
		double i = 0.0;
		double theta = 0.0;
		double d = D;
		do {
			D = d; // the last angle tried was better still
			theta += step;
			Vector<PointR> newPoints = Utils.RotateByDegrees(pts1, theta);
			d = Utils.PathDistance(newPoints, pts2);
			i++;
		} while (d <= D);
		return new double[] { D, theta - step, i }; // distance, angle, calls to
													// pathdist
	}

	private double[] FullSearch(Vector<PointR> pts1, Vector<PointR> pts2,
			OutputStreamWriter writer) {
		double bestA = 0d;
		double bestD = Utils.PathDistance(pts1, pts2);

		try {
			for (int i = -180; i <= +180; i++) {
				Vector<PointR> newPoints = Utils.RotateByDegrees(pts1, i);
				double d = Utils.PathDistance(newPoints, pts2);
				if (writer != null) {
					writer.write(MessageFormat.format("{0}\t{1,number}", i,
							Utils.round(d, 3)));
				}
				if (d < bestD) {
					bestD = d;
					bestA = i;
				}
			}
			writer.write(MessageFormat
					.format("\nFull Search (360 rotations)\n{0,number}{1}\t{2,number} px",
							Utils.round(bestA, 2), (char) 176,
							Utils.round(bestD, 3))); // calls,
														// angle,
														// distance
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new double[] { bestD, bestA, 360.0 }; // distance, angle, calls
														// to pathdist
	}

	public int getNumGestures() {
		return _gestures.size();
	}

	public Vector<Multistroke> getGestures() {
		Vector<Multistroke> vgestures = new Vector<Multistroke>();
		vgestures.addAll(_gestures.values());
		Collections.sort(vgestures);
		return vgestures;
	}

	public void ClearGestures() {
		_gestures.clear();
	}

	// added the numPtsInStroke argument so we can read and write the gestures
	// we draw ourselves for testing
	// Lisa 1/2/2008
	public boolean saveGesture(String filename, Vector<Vector<PointR>> strokes,
			Vector<Integer> numPtsInStroke) {
		// add the new prototype with the name extracted from the filename.
		String name = Gesture.ParseName(filename);

		// Lisa 1/2/2008
		Multistroke newPrototype = new Multistroke(name, "test", "test",
				strokes); // points, numPtsInStroke);

		// jso 09/30/2011
		if (_gestures.containsKey(name)){
			// do not remove but rename the multistroke - we want them all
			// (Recognizer still returns original name as it uses
			// the name of the Multistroke's OriginalGesture)
			//_gestures.remove(newPrototype.name);
			newPrototype.Name = (newPrototype.Name+"-"+(++cnt));
		}

		_gestures.put(newPrototype.Name, newPrototype);

		Vector<PointR> points = newPrototype.OriginalGesture.RawPoints;
		// figure out the duration of the gesture
		PointR p0 = points.elementAt(0);
		PointR pn = points.elementAt(points.size() - 1);

		// do the xml writing (of the raw points)
		boolean success = true;
		boolean indentation = true;
		XmlSerializer writer = null;
		OutputStreamWriter osw = null;
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
					System.getProperty(XmlPullParserFactory.PROPERTY_NAME),
					null);
			writer = factory.newSerializer();
			// save the prototype as an Xml file
			osw = new OutputStreamWriter(new FileOutputStream(filename));
			writer.setOutput(osw);
			writer.startTag(NAMESPACE, "Gesture");
			writer.attribute(NAMESPACE, "Name", name);
			writer.attribute(NAMESPACE, "Subject", "test");
			writer.attribute(NAMESPACE, "Speed", "test");
			writer.attribute(NAMESPACE, "NumPts",
					Integer.toString(points.size()));
			writer.attribute(NAMESPACE, "Milliseconds",
					Integer.toString(pn.T - p0.T));
			writer.attribute(NAMESPACE, "AppName", getClass().getName()
					+ "-java");
			writer.attribute(NAMESPACE, "AppVer", VERSION);
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			writer.attribute(NAMESPACE, "Date", dateFormat
					.format(GregorianCalendar.getInstance().getTime()));
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			writer.attribute(NAMESPACE, "TimeOfDay", timeFormat
					.format(GregorianCalendar.getInstance().getTime()));
			if (indentation)
				writer.text("\n");
			// write out the Stroke tags, Lisa 1/2/2008
			int numStrokesWritten = 0;
			// write out the raw individual points
			// fixed to work with strokes, Lisa 8/8/2009
			for (Vector<PointR> pts : strokes) {
				writer.startTag(NAMESPACE, "Stroke");
				writer.attribute(NAMESPACE, "index",
						Integer.toString(numStrokesWritten + 1));
				if (indentation)
					writer.text("\n");
				numStrokesWritten++;
				for (PointR p : pts) {
					writer.startTag(NAMESPACE, "Point");
					writer.attribute(NAMESPACE, "X", Double.toString(p.X));
					writer.attribute(NAMESPACE, "Y", Double.toString(p.Y));
					writer.attribute(NAMESPACE, "T", Integer.toString(p.T));
					writer.endTag(NAMESPACE, "Point");
					if (indentation)
						writer.text("\n");
				}
				// write the Stroke tags, Lisa 1/2/2008
				writer.endTag(NAMESPACE, "Stroke"); // </Stroke>, I hope
				if (indentation)
					writer.text("\n");
			}
			writer.endTag(NAMESPACE, "Gesture");
			if (indentation)
				writer.text("\n");
			writer.endDocument();
			writer.flush();
			if (osw != null)
				osw.close();
		} catch (IOException xex) {
			xex.printStackTrace();
			success = false;
		} catch (Exception ex) {
			ex.printStackTrace();
			success = false;
		}
		
		// Save precomputed file as well if present.
		File pcxFile = new File(filename + ".pcx");
		savePcxGesture(pcxFile, newPrototype);
		
		return success; // Xml file successfully written (or not)
	}

	public boolean loadGesture(String filename) {
		return loadGesture(new File(filename));
	}
	
	private Multistroke loadPcxGesture(File pcx) {
		if(pcx.exists() && NDollarParameters.getInstance().StorePrecompute) 
			try(	GZIPInputStream gzipInput = new GZIPInputStream(
						new FileInputStream(pcx));
					MultistrokeInputStream pcxInput = 
						new MultistrokeInputStream(gzipInput);) {
			return pcxInput.readStroke();
			}
			catch(Exception e) {}
		
		return null;
	}
	
	private void savePcxGesture(File pcx, Multistroke stroke) {
		if(!pcx.exists() && NDollarParameters.getInstance().StorePrecompute) {
			try {
				pcx.createNewFile();
				try (	GZIPOutputStream pcxOutput = new GZIPOutputStream(
						new FileOutputStream(pcx)) 
						{{ this.def.setLevel(Deflater.BEST_COMPRESSION); }};
					MultistrokeOutputStream multiStrokeOutput = 
						new MultistrokeOutputStream(pcxOutput)) {
				
					multiStrokeOutput.writeStroke(stroke);
					pcxOutput.finish();
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	static int cnt =0;
	public boolean loadGesture(File file) {
		boolean success = true;
		MXParser reader = null;
		FileInputStream fis = null;
		try {
			File pcx = new File(file.getParentFile(), file.getName() + ".pcx");
			Multistroke p = loadPcxGesture(pcx);
			
			// Fetch raw data if it has not been found yet.
			if(p == null) {
				reader = new MXParser();
				fis = new FileInputStream(file);
				reader.setInput(fis, "UTF-8");

				p = ReadGesture(reader); // Lisa 1/2/2008
			}

			// Store the precomputed data.
			savePcxGesture(pcx, p);
			
			// remove any with the same name and add the prototype gesture
			if (_gestures.containsKey(p.Name)){
				// jso 09/30/2011
				// do not remove but rename the multistroke - we want them all
				// (Recognizer still returns original name as it uses
				// the name of the Multistroke's OriginalGesture)
				//_gestures.remove(p.Name);		
				p.Name = (p.Name+"-"+(++cnt));
			}
		
			// _gestures now contains Multistrokes, not just Gestures
			// Lisa 12/21/2007
			System.out.println("add "+p.Name);
			synchronized(_gestures) {
				_gestures.put(p.Name, p);
			}
			if (fis != null) {
				fis.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			success = false;
		} finally {

		}
		
		return success;
	}

	// assumes the reader has been just moved to the head of the content.
	// changed this to return a Multistroke so we can change the order of
	// pre-processing.
	// Lisa 1/2/2008
	private Multistroke ReadGesture(XmlPullParser reader) {
		String name = "", user = "", speed = "";
		Vector<PointR> points = new Vector<PointR>();
		Vector<Vector<PointR>> strokes = new Vector<Vector<PointR>>();
		try {
			int next = reader.next();
			while (next != XmlPullParser.END_DOCUMENT) {

				if (next == XmlPullParser.START_TAG
						&& reader.getName().equals("Gesture")) {
					for (int i = 0; i < reader.getAttributeCount(); ++i) {
						if (reader.getAttributeName(i).equals("Name")) {
							name = reader.getAttributeValue(i);
						} else if (reader.getAttributeName(i).equals("Subject")) {
							user = reader.getAttributeValue(i);
						} else if (reader.getAttributeName(i).equals("Speed")) {
							speed = reader.getAttributeValue(i);
						}
					}
				} else if (next == XmlPullParser.START_TAG
						&& reader.getName().equals("Point")) {
					PointR p = new PointR();
					for (int i = 0; i < reader.getAttributeCount(); ++i) {
						if (reader.getAttributeName(i).equals("X")) {
							p.X = Double.parseDouble(reader
									.getAttributeValue(i));
						} else if (reader.getAttributeName(i).equals("Y")) {
							p.Y = Double.parseDouble(reader
									.getAttributeValue(i));
						} else if (reader.getAttributeName(i).equals("T")) {
							p.T = Integer.parseInt(reader.getAttributeValue(i));
						}
					}
					points.add(p);
				} else if (next == XmlPullParser.START_TAG
						&& reader.getName().equals("Stroke")) {
					{
						// set up stroke index for the beginning of this stroke
						strokes.add(new Vector<PointR>(points));
						points = new Vector<PointR>();
					}
				}
				next = reader.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// add last stroke size
		strokes.add(new Vector<PointR>(points));
		return new Multistroke(name, user, speed, strokes); // keep each stroke
															// separate until
															// we're done
															// pre-processing
	}

	// / <summary>
	// / Assemble the gesture filenames into categories that contain
	// / potentially multiple examples of the same gesture.
	// / </summary>
	// / <param name="filenames"></param>
	// / <returns>A Hashtable keyed by user # of category instances,
	// / or <b>null</b> if an error occurs.</returns>
	// / <remarks>
	// / See the comments above MainForm.BatchProcess_Click.
	// / </remarks>
	public SamplesCollection AssembleBatch(File[] files, boolean include1D,
			boolean include2D) {
		// organize these by user: each user's categories are added at the
		// index=user #
		// Lisa 1/5/2008
		SamplesCollection categoriesByUser = new SamplesCollection();

		System.out.println("Assembling batch from files");
		for (int i = 0; i < files.length; i++) {
			FileInputStream fis = null;
			XmlPullParser reader = null;
			try {
				fis = new FileInputStream(files[i]);
				reader = new MXParser();
				reader.setInput(fis, "UTF-8");

				Multistroke p = ReadGesture(reader); // Lisa 1/2/2008
				// only include the kinds of gestures we say to (via threshold),
				// Lisa 5/13/2008
				if (p.OriginalGesture.Is1D && include1D)
					categoriesByUser.AddExample(p);
				else if (!p.OriginalGesture.Is1D && include2D)
					categoriesByUser.AddExample(p);
				System.out.print(".");
				if (fis != null)
					fis.close();
			}

			catch (Exception ex) {
				ex.printStackTrace();
				// categoriesByUser.clear();
				categoriesByUser = null;
			}
		}
		System.out.println();

		// now make sure that each category has the same number of elements in
		// it.
		// actually, we don't need this constraint anymore, so we just return
		// the
		// Hashtable, Lisa 1/5/2008

		Enumeration<String> users = categoriesByUser.keys();
		while (users.hasMoreElements()) {
			String user = (String) users.nextElement();
			if (!categoriesByUser.AreNumExamplesEqualForUser(user))
				System.out
						.println("Warning: in case you were not expecting it, there is a different number of samples across categories for user "
								+ user + ".");
			Vector<String> catsToRemove = new Vector<String>();
			Enumeration<String> cats = categoriesByUser.get(user).keys();
			while (cats.hasMoreElements()) {
				String cat = (String) cats.nextElement();
				// System.out.println("user: " + user + ", category: " +
				// cat + ", num ex = " +
				// categoriesByUser.GetCategoryByUser(user,
				// cat).getNumExamples());
				if (categoriesByUser.GetCategoryByUser(user, cat)
						.getNumExamples() < _MinExamples) {
					// remove any user/symbol pairs with fewer than a certain
					// number of examples
					catsToRemove.add(cat);
				}
			}
			for (String s : catsToRemove) {
				categoriesByUser.RemoveSamples(user, s);
			}
		}
		System.out.println("Done assembling batch.");
		return categoriesByUser; // Vector;
	}

	// / <summary>
	// / Tests an entire batch of files. See comments atop
	// MainForm.TestBatch_Click().
	// /
	// / This was adapted from the original TestBatch() method used by $1 to do
	// multistroke testing.
	// / (Lisa 1/5/2008)
	// / </summary>
	// / <param name="subject">Subject number.</param> ! removed !
	// / <param name="speed">"fast", "medium", or "slow"</param> ! removed !
	// / <param name="categories">A hashtable keyed by user of gesture
	// categories
	// / that each contain Vectors of prototypes (examples) within that gesture
	// category.</param>
	// / <param name="dir">The directory into which to write the output
	// files.</param>
	// / <returns>True if successful; false otherwise.</returns>
	public boolean TestBatch(SamplesCollection categoriesByUser, String dir) {
		System.out.println("Testing batch (one tick per user)");
		boolean success = true;

		OutputStreamWriter mainWriter = null;
		OutputStreamWriter recWriter = null;
		try {
			//
			// set up a main results file and detailed recognition results file
			//
			long start = System.currentTimeMillis();
			String mainFile = MessageFormat.format("{0}\\ndollar_main_{1}.csv",
					dir, start);
			String recFile = MessageFormat.format("{0}\\ndollar_data_{1}.csv",
					dir, start);

			mainWriter = new OutputStreamWriter(new FileOutputStream(new File(
					mainFile)));// , Encoding.UTF8);
			mainWriter.write(MessageFormat.format(
					"Recognizer:,ndollar, StartTime(ms):,{0}\n", start));
			mainWriter
					.write(MessageFormat
							.format("Testing:,within-user,Matching method:,{0},Rotation invariance:,{1},Rotation bound:,{2},1D Threshold:,{3},Do start angle comparison:,{4},Start angle index:,{5},Start angle threshold:,{6},Do match only same number of strokes:,{7},Test for 1D gestures:,{8},UseUniformScaling:,{9}\n",
									(NDollarParameters.getInstance().SearchMethod == NDollarParameters.PossibleSearchMethods.GSS) ? "GSS"
											: "Protractor",
									NDollarParameters.getInstance().RotationInvariant,
									_RotationBound,
									_1DThreshold,
									NDollarParameters.getInstance().DoStartAngleComparison,
									NDollarParameters.getInstance().StartAngleIndex,
									Utils.Rad2Deg(NDollarParameters
											.getInstance().StartAngleThreshold),
									NDollarParameters.getInstance().MatchOnlyIfSameNumberOfStrokes,
									NDollarParameters.getInstance().TestFor1D,
									NDollarParameters.getInstance().UseUniformScaling));
			mainWriter
					.write("Recognizer,Subject,Speed,NumTraining,GestureType,RecognitionRate");

			recWriter = new OutputStreamWriter(new FileOutputStream(new File(
					recFile)));// , Encoding.UTF8);
			recWriter.write(MessageFormat.format(
					"Recognizer:,ndollar, StartTime(ms):,{0}\n", start));
			// recWriter.WriteLine("Testing:,within-user,Rotation invariance:,{0},Rotation bound:,{1},1D Threshold:,{2},Do start angle comparison:,{3},Start angle index:,{4},Start angle threshold:,{5},Do match only same number of strokes:,{6},Test for 1D gestures:,{7},UseUniformScaling:,{8}\n",
			recWriter
					.write(MessageFormat
							.format("Testing:,within-user,Matching method:,{0},Rotation invariance:,{1},Rotation bound:,{2},1D Threshold:,{3},Do start angle comparison:,{4},Start angle index:,{5},Start angle threshold:,{6},Do match only same number of strokes:,{7},Test for 1D gestures:,{8},UseUniformScaling:,{9}\n",
									(NDollarParameters.getInstance().SearchMethod == NDollarParameters.PossibleSearchMethods.GSS) ? "GSS"
											: "Protractor",
									NDollarParameters.getInstance().RotationInvariant,
									_RotationBound,
									_1DThreshold,
									NDollarParameters.getInstance().DoStartAngleComparison,
									NDollarParameters.getInstance().StartAngleIndex,
									Utils.Rad2Deg(NDollarParameters
											.getInstance().StartAngleThreshold),
									NDollarParameters.getInstance().MatchOnlyIfSameNumberOfStrokes,
									NDollarParameters.getInstance().TestFor1D,
									NDollarParameters.getInstance().UseUniformScaling));
			recWriter
					.write("Subject,Speed,Correct?,NumTrain,Tested,Character,ActualComparisons,TotalComparisons,Is1D,1stCorrect,Pts,Ms,NumStrokes,Angle,:,(NBestNames),[NBestScores]");

			// PER-USER-TESTING:
			// for each user
			// for i = 1 to max number of training templates per symbol
			// choose i samples randomly
			// load those templates
			// test on 1 remaining sample per symbol, randomly chosen
			// repeat 100 times

			// new outermost loop: does the whole thing once for each user
			// Lisa, 5/12/2008
			Enumeration<String> users = categoriesByUser.keys();
			while (users.hasMoreElements()) {
				String user = (String) users.nextElement();
				System.out.print(".");
				String speed = "unknown"; // TODO: get this from the new object
											// later

				//
				// determine the maximum number of gesture categories and the
				// minimum number of examples per category for this specific
				// user
				//
				int minNumExamples = categoriesByUser
						.GetMinNumExamplesForUser(user);
				// double totalTests = (minNumExamples - 1) * NumRandomTests;

				//
				// next loop: trains on N=1..9, tests on 10-N (for e.g.,
				// numExamples = 10)
				//
				for (int n = 1; n <= minNumExamples - 1; n++) {
					// storage for the final avg results for each category for
					// this N
					Hashtable<String, Double> results = new Hashtable<String, Double>();

					//
					// run a number of tests at this particular N number of
					// training examples
					//
					for (int r = 0; r < NumRandomTests; r++) {
						_gestures.clear(); // clear any (old) loaded prototypes

						// load (train on) N randomly selected gestures in each
						// category
						// do this for this user only
						Enumeration<String> cats = categoriesByUser.get(user)
								.keys();
						while (cats.hasMoreElements()) {
							String cat = (String) cats.nextElement();
							Category c = (Category) categoriesByUser.get(user)
									.get(cat); // the category to load N
												// examples for
							// choose over the whole range of examples for this
							// user/symbol pair, Lisa 1/5/2008
							int[] chosen = Utils.Random(0,
									c.getNumExamples() - 1, n); // select N
																// unique
																// indices
							for (int j = 0; j < chosen.length; j++) {
								Multistroke p = c.get(chosen[j]); // get the
																	// prototype
																	// from this
																	// category
																	// at
																	// chosen[j],
																	// Lisa
																	// 1/5/2008
								_gestures.put(p.Name, p); // load the randomly
															// selected test
															// gestures into the
															// recognizer
							}
						}

						//
						// testing loop on all unloaded gestures in each
						// category. creates a recognition
						// rate (%) by averaging the binary outcomes (correct,
						// incorrect) for each test.
						//
						// do this for this user only
						Enumeration<String> cats2 = categoriesByUser.get(user)
								.keys();
						while (cats2.hasMoreElements()) {
							// pick a random unloaded gesture in this category
							// for testing
							// instead of dumbly picking, first find out what
							// indices aren't
							// loaded, and then randomly pick from those.
							String cat = (String) cats2.nextElement();
							Category c = (Category) categoriesByUser.get(user)
									.get(cat);
							int[] notLoaded = new int[c.getNumExamples() - n];
							for (int j = 0, k = 0; j < c.getNumExamples(); j++) {
								Multistroke g = c.get(j); // Lisa 1/5/2008
								if (!_gestures.containsKey(g.Name))
									notLoaded[k++] = j; // jth gesture in c is
														// not loaded
							}
							int chosen = Utils.Random(0, notLoaded.length - 1); // index
							Multistroke ms = c.get(notLoaded[chosen]); // gesture
																		// to
																		// test
							Gesture p = ms.OriginalGesture; // we only test on
															// the original
															// Gesture in the
															// Multistroke, Lisa
															// 1/5/2008
							// Debug.Assert(!_gestures.contains(p.Name));

							// do the recognition!
							NBestList result = this.Recognize(p.RawPoints,
									ms.NumStrokes);
							String category = Category.ParseName(result
									.getName());
							int correct = (c.getName().equals(category)) ? 1
									: 0;

							recWriter
									.write(MessageFormat
											.format("\n{0},{1},{2},{3},{4},{5},{6},{7},{8},{9},{10},{11},{12},{13,number}{14},:,({15}),[{16}]",
													ms.User, // 0 Subject
													ms.Speed, // 1 Speed
													correct, // 2 Correct?
													n, // 3 NumTrain
													p.Name, // 4 Tested
													Category.ParseName(p.Name), // 5
																				// Character
													result.getActualComparisons(), // 6
																					// ActualComparisons
													result.getTotalComparisons(), // 7
																					// TotalComparisons
													p.Is1D, // 8 Is1D
													FirstCorrect(p.Name,
															result.getNames()), // 9
																				// 1stCorrect
													p.RawPoints.size(), // 10
																		// Pts
													p.getDuration(), // 11 Ms
													ms.NumStrokes, // 12 number
																	// of
																	// strokes
													Utils.round(
															result.getAngle(),
															1), (char) 176, // 13/14
																			// Angle
																			// tweaking
																			// :
													result.getNamesString(), // 15
																				// (NBestNames)
													result.getScoresString())); // 16
																				// [NBestScores]

							// c is a Category object, unique to user/category
							// pair
							// use the category NAME to store the results
							// Lisa 1/6/2008
							if (results.containsKey(cat)) {
								double temp = (double) results.get(cat)
										+ correct;
								results.put(cat, temp);
							} else {
								results.put(cat, (double) correct);
							}
						}

						// provide feedback as to how many tests have been
						// performed thus far.
						// double testsSoFar = ((n - 1) * NumRandomTests) + r;
						// System.out.println("performed " + testsSoFar
						// + " out of " + totalTests + " total tests"); //
						// callback
					}

					//
					// now create the final results for this user and this N and
					// write them to a file
					//
					Enumeration<String> cats3 = categoriesByUser.get(user)
							.keys();
					while (cats3.hasMoreElements()) {
						String cat = (String) cats3.nextElement();
						double temp = (double) results.get(cat)
								/ ((double) NumRandomTests * 1); // normalize by
																	// the
																	// number of
																	// tests at
																	// this N,
																	// Lisa
																	// 1/5/2008
						results.put(cat, temp);
						// Subject Recognizer Speed NumTraining GestureType
						// RecognitionRate
						mainWriter.write(MessageFormat.format(
								"ndollar,{0},{1},{2},{3},{4,number}", user,
								speed, n, cat,
								Utils.round((double) results.get(cat), 3)));
					}
				}
			}
			// time-stamp the end of the processing when it's allll done
			long end = System.currentTimeMillis();
			mainWriter.write(MessageFormat.format(
					"\nEndTime(ms):,{0}, Minutes:,{1,number,integer}", end,
					Utils.round((end - start) / 60000.0, 2)));
			recWriter.write(MessageFormat.format(
					"\nEndTime(ms):,{0}, Minutes:,{1,number,integer}", end,
					Utils.round((end - start) / 60000.0, 2)));
			System.out.println();
			System.out.println("Done testing batch.");
			if (mainWriter != null)
				mainWriter.close();
			if (recWriter != null)
				recWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			success = false;
		}
		return success;
	}

	private int FirstCorrect(String name, String[] names) {
		String category = Category.ParseName(name);
		for (int i = 0; i < names.length; i++) {
			String c = Category.ParseName(names[i]);
			if (category.equals(c)) {
				return i + 1;
			}
		}
		return -1;
	}

	public boolean CreateRotationGraph(String file1, String file2, String dir,
			boolean similar) {
		boolean success = true;
		OutputStreamWriter writer = null;
		FileInputStream fis = null;
		XmlPullParser reader = null;
		try {
			// read gesture file #1
			fis = new FileInputStream(new File(file1));
			reader = new MXParser();
			reader.setInput(fis, "UTF-8");
			Multistroke g1 = ReadGesture(reader); // Lisa 1/2/2008
			fis.close();

			// read gesture file #2
			fis = new FileInputStream(new File(file2));
			reader = new MXParser();
			reader.setInput(fis, "UTF-8");
			Multistroke g2 = ReadGesture(reader); // Lisa 1/2/2008
			fis.close();
			fis = null;

			// create output file for results
			String outfile = MessageFormat.format("{0}\\{1}({2}, {3})_{4}.txt",
					dir, similar ? "o" : "x", g1.Name, g2.Name,
					System.currentTimeMillis());
			FileOutputStream fos = new FileOutputStream(new File(outfile));
			writer = new OutputStreamWriter(fos);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			writer.write(MessageFormat.format(
					"Rotated: {0} --> {1}. {2}, {3}\n", g1.Name, g2.Name,
					dateFormat
							.format(GregorianCalendar.getInstance().getTime()),
					timeFormat
							.format(GregorianCalendar.getInstance().getTime())));

			// do the full 360 degree rotations
			double[] full = FullSearch(g1.OriginalGesture.Points,
					g2.OriginalGesture.Points, writer); // Lisa 1/2/2008

			// use bidirectional hill climbing to do it again
			double init = Utils.PathDistance(g1.OriginalGesture.Points,
					g2.OriginalGesture.Points); // initial distance // Lisa
												// 1/2/2008
			double[] pos = HillClimbSearch(g1.OriginalGesture.Points,
					g2.OriginalGesture.Points, init, 1d); // Lisa 1/2/2008
			double[] neg = HillClimbSearch(g1.OriginalGesture.Points,
					g2.OriginalGesture.Points, init, -1d); // Lisa 1/2/2008
			double[] best = new double[3];
			best = (neg[0] < pos[0]) ? neg : pos; // min distance
			writer.write(MessageFormat
					.format("\nHill Climb Search ({0} rotations)\n{1,number}{2}\t{3,number} px",
							pos[2] + neg[2] + 1, Utils.round(best[1], 2),
							(char) 176, Utils.round(best[0], 3))); // calls,
																	// angle,
																	// distance

			// use golden section search to do it yet again
			double[] gold = GoldenSectionSearch(g1.OriginalGesture.Points, // to
																			// rotate
																			// //
																			// Lisa
																			// 1/2/2008
					g2.OriginalGesture.Points, // to match // Lisa 1/2/2008
					Utils.Deg2Rad(-_RotationBound), // lbound // Lisa 1/2/2008
					Utils.Deg2Rad(+_RotationBound), // ubound // Lisa 1/2/2008
					Utils.Deg2Rad(2.0)); // threshold
			writer.write(MessageFormat
					.format("\nGolden Section Search ({0} rotations)\n{1,number}{2}\t{3,number} px",
							gold[2], Utils.round(gold[1], 2), (char) 176,
							Utils.round(gold[0], 3))); // calls, angle, distance

			// for pasting into Excel
			writer.write(MessageFormat
					.format("\n{0} {1} {2,number} {3,number} {4,number} {5,number} {6} {7,number} {8,number} {9,number} {10} {11,number} {12,number} {13,number} {14}",
							g1.Name, // rotated
							g2.Name, // into
							Math.abs(Utils.round(full[1], 2)), // |angle|
							Utils.round(full[1], 2), // Full Search angle
							Utils.round(full[0], 3), // Full Search distance
							Utils.round(init, 3), // Initial distance w/o any
													// search
							full[2], // Full Search iterations
							Math.abs(Utils.round(best[1], 2)), // |angle|
							Utils.round(best[1], 2), // Bidirectional Hill Climb
														// Search angle
							Utils.round(best[0], 3), // Bidirectional Hill Climb
														// Search distance
							pos[2] + neg[2] + 1, // Bidirectional Hill Climb
													// Search iterations
							Math.abs(Utils.round(gold[1], 2)), // |angle|
							Utils.round(gold[1], 2), // Golden Section Search
														// angle
							Utils.round(gold[0], 3), // Golden Section Search
														// distance
							gold[2])); // Golden Section Search iterations
			if (fos != null)
				fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			success = false;
		}
		return success;
	}

}
