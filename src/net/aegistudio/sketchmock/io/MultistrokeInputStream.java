package net.aegistudio.sketchmock.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import de.dubs.dollarn.Gesture;
import de.dubs.dollarn.Multistroke;
import de.dubs.dollarn.PointR;

/**
 * A wrapper input stream that is capable of deserializing the precomputed 
 * head-permutations result from the file, which accelerate the loading speed.
 * 
 * @author Haoran Luo
 *
 */
public class MultistrokeInputStream extends InputStream {
	private final DataInputStream inputStream;
	public MultistrokeInputStream(InputStream inputStream) {
		this.inputStream = new DataInputStream(inputStream);
	}
	
	@Override
	public int read() throws IOException {
		return inputStream.read();
	}

	/**
	 * @return the multistroke object read.
	 * @throws IOException when there's some read error.
	 */
	public Multistroke readStroke() throws IOException {
		Multistroke result = new Multistroke();
		
		// Some header information.
		result.Name = inputStream.readUTF();
		result.User = inputStream.readUTF();
		result.Speed = inputStream.readUTF();
		result.NumStrokes = inputStream.readInt();
		
		// Now read unistrokes.
		int numUniStrokes = inputStream.readInt();
		result.Gestures = new Vector<>(numUniStrokes);
		
		// Read data for every unistroke object.
		for(int i = 0; i < numUniStrokes; ++ i) {
			// Read current block of stroke points.
			int numPointsCurrent = inputStream.readInt();
			
			Vector<PointR> pointsCurrent = new Vector<>(numPointsCurrent);
			for(int j = 0; j < numPointsCurrent; ++ j) {
				PointR point = new PointR();
				readPoint(point);
				pointsCurrent.add(point);
			}

			// Retrieve start vector.
			PointR startUnitVector = new PointR();
			readPoint(startUnitVector);
			
			// Read vector versions.
			int numVectorVersion = inputStream.readInt();
			Vector<Double> vectorVersion = new Vector<>(numVectorVersion);
			for(int j = 0; j < numVectorVersion; ++ j)
				vectorVersion.add(inputStream.readDouble());
			
			// Insert the stroke as gesture into the parent object.
			Gesture gesture = new Gesture();
			gesture.Name = result.Name;
			gesture.RawPoints = pointsCurrent;
			gesture.Points = pointsCurrent;
			gesture.StartUnitVector = startUnitVector;
			gesture.VectorVersion = vectorVersion;
			
			result.Gestures.add(gesture);
		}
		
		return result;
	}
	
	private void readPoint(PointR point) throws IOException {
		point.X = inputStream.readDouble();
		point.Y = inputStream.readDouble();
		point.T = inputStream.readInt();
	}
}