package net.aegistudio.sketchuml.path;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import de.dubs.dollarn.PointR;

public class TrifoldLinePath implements TrifoldPath {
	@Override
	public void makePath(PointR pointStart, PointR pointEnd, 
			List<PointR> separatePoints, List<PointR> controlPoints) {
		separatePoints.add(pointStart);
		controlPoints.add(null);
		separatePoints.add(pointEnd);
	}

	@Override
	public double articulateAndFitness(Vector<PointR> stroke,
			Vector<PointR> trainingStroke,
			int intersectBegin, int intersectEnd) {
		LinePiece pieceStraight = new LinePiece(stroke.get(0), 
				stroke.get(stroke.size() - 1));
		Vector<LinePiece> pieces = new Vector<>();
		
		pieces.add(pieceStraight);
		return LinePiece.distance(stroke, pieces, 0, 0);
	}
	
	@Override
	public TrifoldLinePath clone() {
		return new TrifoldLinePath();
	}
	
	@Override
	public boolean equals(Object path) {
		return path instanceof TrifoldLinePath;
	}

	@Override
	public void writePath(DataOutputStream outputStream) throws IOException {
		
	}

	@Override
	public void readPath(DataInputStream inputStream) throws IOException {
		
	}
}
