package net.aegistudio.sketchuml.stroke;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import de.dubs.dollarn.Multistroke;
import de.dubs.dollarn.PointR;

public class SketchInstance {
	public String name = "", user = "", speed = "";
	public Vector<Vector<PointR>> strokes = new Vector<>();
	
	public static SketchInstance createAndLoad(File file) {
		SketchInstance instance = new SketchInstance();
		if(instance.load(file)) return instance;
		return null;
	}
	
	public boolean load(File file) {
		MXParser reader = new MXParser();
		try {
			reader.setInput(new FileInputStream(file), "UTF-8");
			return load(reader);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean load(XmlPullParser reader) {
		Vector<PointR> points = new Vector<PointR>();
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
			return false;
		}
		// add last stroke size
		strokes.add(new Vector<PointR>(points));
		return true;
	}
	
	public boolean save(File file) {
		String NAMESPACE = null;
		String VERSION = "1.0";
		boolean success = true;
		boolean indentation = true;
		XmlSerializer writer = null;
		OutputStreamWriter osw = null;
		
		// figure out the duration of the gesture
		Vector<PointR> points = new Vector<PointR>();
		for(Vector<PointR> stroke : strokes) 
			points.addAll(stroke);
		PointR p0 = points.elementAt(0);
		PointR pn = points.elementAt(points.size() - 1);
		
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
					System.getProperty(XmlPullParserFactory.PROPERTY_NAME),
					null);
			writer = factory.newSerializer();
			// save the prototype as an Xml file
			osw = new OutputStreamWriter(new FileOutputStream(file));
			writer.setOutput(osw);
			writer.startTag(NAMESPACE, "Gesture");
			writer.attribute(NAMESPACE, "Name", name);
			writer.attribute(NAMESPACE, "Subject", user);
			writer.attribute(NAMESPACE, "Speed", speed);
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
		
		return success;
	}
	
	public Multistroke toMultistroke() {
		return new Multistroke(name, user, speed, strokes);
	}
}
