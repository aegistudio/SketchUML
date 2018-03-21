package net.aegistudio.sketchuml.path;

import de.dubs.dollarn.PointR;

/**
 * The strider to calculate the position movement in the 
 * bezier path.
 * 
 * @author Haoran Luo
 */
public class BezierPathStrider {
	public final PointR[] separatePoints;
	public final BezierEvaluator[] bezier;
	public final double[] pieceLength;
	public final double[] accumLength;
	
	public BezierPathStrider(PointR[] separatePoints, 
			PointR[] controlPoints) {
		this.separatePoints = separatePoints;
		
		// Calculate piece-wise length.
		int numPieces = this.separatePoints.length - 1;
		this.accumLength = new double[numPieces + 1];
		this.pieceLength = new double[numPieces];
		this.bezier = new BezierEvaluator[numPieces];
		for(int i = 0; i < numPieces; ++ i) {
			// Is a straight line piece this case.
			if(i >= controlPoints.length || controlPoints[i] == null) {
				pieceLength[i] = new LinePiece(separatePoints[i], 
						separatePoints[i + 1]).pointDistance;
			}
			
			// Is a bezier curve this case.
			else {
				bezier[i] = new BezierEvaluator(separatePoints[i],
						controlPoints[i], separatePoints[i + 1]);
				pieceLength[i] = bezier[i].length(1.);
			}
			
			
			accumLength[i + 1] = pieceLength[i] + accumLength[i];
		}
	}
	
	public void pointPercentage(PointR result, double percentage) {
		if(percentage < 0. || percentage >= 1.) return;
		
		// Find the suitable line piece.
		int numPieces = pieceLength.length;
		double length = percentage * accumLength[numPieces];
		int beginSearch = 0; int endSearch = numPieces;
		while(endSearch - beginSearch > 1) {
			int midSearch = (beginSearch + endSearch) / 2;
			if(accumLength[midSearch] > length) endSearch = midSearch; 
			else beginSearch = midSearch;
		}
		
		// Retrive the actual point now.
		double linePercentage = (length - 
				accumLength[beginSearch]) / pieceLength[beginSearch];
		if(this.bezier[beginSearch] == null) 
			result.interpolate(linePercentage, 
					separatePoints[beginSearch], 
					separatePoints[beginSearch + 1]);
		else bezier[beginSearch].evaluate(bezier[beginSearch]
				.solveLengthEquation(linePercentage, 5, 1e-3), result);
	}
}
