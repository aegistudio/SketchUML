package net.aegistudio.sketchmock.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.dubs.dollarn.Gesture;
import de.dubs.dollarn.Multistroke;
import de.dubs.dollarn.PointR;

/**
 * Serialize the output data into the stream. Which contains the
 * precomputed heap-permutation results.
 * 
 * @author Haoran Luo
 */
public class MultistrokeOutputStream extends OutputStream {
	private final DataOutputStream outputStream;
	public MultistrokeOutputStream(OutputStream outputStream) {
		this.outputStream = new DataOutputStream(outputStream);
	}
	
	@Override
	public void write(int arg0) throws IOException {
		this.outputStream.write(arg0);
	}
	
	public void writeStroke(Multistroke stroke) throws IOException {
		// Some header information.
		outputStream.writeUTF(stroke.Name);
		outputStream.writeUTF(stroke.User);
		outputStream.writeUTF(stroke.Speed);
		outputStream.writeInt(stroke.NumStrokes);
		
		// Now write unistrokes.
		outputStream.writeInt(stroke.Gestures.size());
		
		// Write data of every unistroke object.
		for(int i = 0; i < stroke.Gestures.size(); ++ i) {
			Gesture currentStroke = stroke.Gestures.get(i);
			outputStream.writeInt(currentStroke.Points.size());
			
			// Store resampled points.
			for(int j = 0; j < currentStroke.Points.size(); ++ j) {
				PointR point = currentStroke.Points.get(j);
				writePoint(point);
			}
			
			// Store start unit vector.
			writePoint(currentStroke.StartUnitVector);
			
			// Store vector versions.
			outputStream.writeInt(currentStroke.VectorVersion.size());
			for(int j = 0; j < currentStroke.VectorVersion.size(); ++ j)
				outputStream.writeDouble(currentStroke.VectorVersion.get(j));
		}
	}
	
	private void writePoint(PointR point) throws IOException {
		outputStream.writeDouble(point.X);
		outputStream.writeDouble(point.Y);
		outputStream.writeInt(point.T);
	}
}
