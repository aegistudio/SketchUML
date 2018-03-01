package net.aegistudio.sketchuml.path;

import java.util.Vector;

import de.dubs.dollarn.PointR;

/**
 * The augmented version of the line piece, allowing
 * a vector of line parameters to be utilized and solved.
 * 
 * @author Haoran Luo
 */
public class ParametricLinePiece {
	public static interface Parameter {
		public double x(double[] vector);
		
		public double y(double[] vector);
		
		public double dxdai(int i, double[] vector);
		
		public double dydai(int i, double[] vector);
	}
	
	public final Parameter parameterBegin, parameterEnd;
	public ParametricLinePiece(Parameter parameterBegin, 
			Parameter parameterEnd) {
		this.parameterBegin = parameterBegin;
		this.parameterEnd = parameterEnd;
	}
	
	/**
	 * Calculate a line piece when given specific parameters.
	 * 
	 * @param vector the parameter's vector
	 * @return the evaluated line piece object.
	 */
	public LinePiece evaluate(double[] vector) {
		PointR pointBegin = new PointR(
				parameterBegin.x(vector), 
				parameterBegin.y(vector));
		PointR pointEnd = new PointR(
				parameterEnd.x(vector), 
				parameterEnd.y(vector));
		LinePiece piece = new LinePiece(pointBegin, pointEnd);
		return piece;
	}
	
	/**
	 * Calculate the gradient at a given point relative to 
	 * the given gradient vector.
	 */
	public void gradientEvaluated(LinePiece piece, 
			LinePiece.DistanceStatus status, 
			PointR point, double[] gradient, double[] vector) {
		
		double f = status.distance;
		
		// Judge the result by the status word.
		if(f == 0) {
			// You have no where to go this case because f == 0 
			// guarantees for every terms in f, it minimize the f.
			for(int i = 0; i < vector.length; ++ i) gradient[i] = 0;
		}
		else if(status.status != LinePiece.DistanceStatus.STATUS_CENTER) {
			// Either a-side or b-side is used, their form will be analogous.
			Parameter parameterTarget = status.status ==
					LinePiece.DistanceStatus.STATUS_ASIDE?
							parameterBegin : parameterEnd;
			double x = parameterTarget.x(vector);
			double y = parameterTarget.y(vector);
			
			// Calculate the gradient's vector.
			double cos = (x - point.X) / f;
			double sin = (y - point.Y) / f;
			for(int i = 0; i < vector.length; ++ i) {
				// f(a) = sqr((xP - xi(a)) ^ 2 + (yP - yi(a)) ^ 2)
				// dfda = (1/f(a)) * ((xi(a) - xP) * dxida(a) +
				//                    (yi(a) - yP) * dyida(a)).
				double dxdai = parameterTarget.dxdai(i, vector);
				double dydai = parameterTarget.dydai(i, vector);
				gradient[i] = cos * dxdai + sin * dydai;
			}
		}
		else {
			double xA = parameterBegin.x(vector);
			double xB = parameterEnd.x(vector);
			double yA = parameterBegin.y(vector);
			double yB = parameterEnd.y(vector);
			double xP = point.X; double yP = point.Y;
			
			// Calculate the gradient's vector.
			for(int i = 0; i < vector.length; ++ i) {
				double dxAdai = parameterBegin.dxdai(i, vector);
				double dyAdai = parameterBegin.dydai(i, vector);
				double dxBdai = parameterEnd.dxdai(i, vector);
				double dyBdai = parameterEnd.dydai(i, vector);
				
				// f(a) = sqr(|AP.AP| - |AP.AB|^2/|AB.AB|)
				
				// |AP.AP| = (xP - xA(a)) ^ 2 + (yP - yA(a)) ^ 2.				
				// d|AP.AP|da = 2(xA - xP)dxAda + 2(yA - yP)dyAda.
				double dAP2da = 2 * ((xA - xP) * dxAdai + (yA - yP) * dyAdai);
				
				// |AB.AB| = (xB(a) - xA(a)) ^ 2 + (yB(a) - yA(a)) ^ 2.
				// d|AB.AB|da = 2(xB - xA)(dxBda - dxAda) + 
				//              2(yB - yA)(dyBda - dyAda).
				double vAB = piece.pointDistance,  vAB2 = vAB * vAB;
				double dAB2da = 2 * ((xB - xA) * (dxBdai - dxAdai) +
						             (yB - yA) * (dyBdai - dyAdai));
				
				// |AP.AB| = (xP - xA(a))(xB(a) - xA(a)) +
				//           (yP - yA(a))(yB(a) - yA(a)).
				// d|AP.AB|da = 
				//    xP(dxBda - dxAda) - (dxAda.xB + xA.dxBda) + 2xA.dxAda
				//  + yP(dyBda - dyAda) - (dyAda.yB + yA.dyBda) + 2yA.dyAda
				//  = (xP(dxBda - dxAda) + yP(dyBda - dyAda))
				//  - (dxAda.xB + xA.dxBda + dyAda.yB + yA.dyBda)
				//  + 2 * (xA.dxAda + yA.dyAda).
				double vAPAB = (xP - xA) * (xB - xA) + (yP - yA) * (yB - yA);
				double dAPABda = (xP * (dxBdai - dxAdai) + yP * (dyBdai - dyAdai))
					- (dxAdai * xB + xA * dxBdai + dyAdai * yB + yA * dyBdai)
					+ 2 * (xA * dxAdai + yA * dyAdai);
				
				// d|AP.AB|2da = 2 * |AP.AB| * d|AP.AB|da.
				double vAPAB2 = vAPAB * vAPAB;
				double dAPAB2da = 2 * vAPAB * dAPABda;
				
				// dfda = 1/(2 * f(a)) * (d|AP.AP|da - (1.0 / |AB.AB| ^ 2)
				//     * (d|AP.AB|2da * |AB.AB| - |AP.AB|2 * d|AB.AB|da).
				double dfdai = 0.5 / f * (dAP2da - 1.0 / (vAB2 * vAB2) * (
						dAPAB2da * vAB2 - vAPAB2 * dAB2da));
				gradient[i] = dfdai;
			}
		}
	}
	
	public void gradient(LinePiece.DistanceStatus status, 
			PointR point, double[] gradient, double[] vector) {
		
		// Calculate the function value.
		LinePiece piece = evaluate(vector);
		piece.distance(status, point);
		
		// Call the internal gradient function.
		gradientEvaluated(piece, status, point, gradient, vector);
	}
	
	private static Parameter contributor(
			BoundingBox.BoundingStatus status,
			Vector<ParametricLinePiece> linePiece) {
		return status.lineStart? linePiece.get(status.index).parameterBegin
				: linePiece.get(status.index).parameterEnd;
	}
	
	/**
	 * Perform gradient descend to find the minimum value of the line pieces
	 * distance.
	 * 
	 * @param parameter the initial value of the parameters.
	 * @param points the points to approximate.
	 * @param linePieces the parametric line pieces.
	 * @param learnRate the initial learning rate.
	 * @param bonusRate the acceleration when at correct direction.
	 * @param penaltyRate the de-acceleration when at wrong direction.
	 * @param terminateBias the bias to end the calculation.
	 * @param terminateIteration the maximum iterating times.
	 * @return the minimum distance of approximation.
	 */
	public static double gradientDescent(double[] parameter, 
			Vector<PointR> points, Vector<ParametricLinePiece> linePieces,
			double learnRate, double bonusRate, double penaltyRate,
			double gammaBoundIn, double gammaBoundOut,
			double terminateBias, int terminateIteration) {
		
		// Initialization of local variables.
		double[] gradient0 = new double[parameter.length];
		double[] gradient1 = new double[parameter.length];
		double[] gradient2 = new double[parameter.length];
		LinePiece.DistanceStatus status = new LinePiece.DistanceStatus();
		double currentBias = Double.MAX_VALUE;
		
		// Profile the point's boundary.
		BoundingBox bound = new BoundingBox(points);
		BoundingBox.BoundingStatus left = new BoundingBox.BoundingStatus();
		BoundingBox.BoundingStatus right = new BoundingBox.BoundingStatus();
		BoundingBox.BoundingStatus top = new BoundingBox.BoundingStatus();
		BoundingBox.BoundingStatus bottom = new BoundingBox.BoundingStatus();
		
		// The gradient descent looping.
		int i; for(i = 0; i < terminateIteration; ++ i) {
			// Evaluate current line piece.
			Vector<LinePiece> evaluatedPieces = new Vector<>();
			for(int j = 0; j < linePieces.size(); ++ j)
				evaluatedPieces.add(linePieces.get(j).evaluate(parameter));

			// Clear the gradient vector in use.
			for(int j = 0; j < parameter.length; ++ j) gradient0[j] = 0.;
			
			// Calculate the distance between all points and the current
			// line piece with given parameters.
			currentBias = 0.;
			for(int j = 0; j < points.size(); ++ j) {
				// Calculate the gradient vector of order one.
				int minimumIndex = LinePiece.distance(status, 
						points.get(j), evaluatedPieces);
				linePieces.get(minimumIndex).gradientEvaluated(
						evaluatedPieces.get(minimumIndex), status, 
						points.get(j), gradient2, parameter);
				
				// Update the new vector with gradient of order two.
				currentBias += status.distance * status.distance;
				for(int k = 0; k < parameter.length; ++ k) {
					gradient0[k] += 2 * status.distance * 
						learnRate * gradient2[k];
				}
			}
			
			// Compare the elastic potential of current line piece.
			double boundPotential = bound.potential(evaluatedPieces, 
					left, right, top, bottom, gammaBoundIn, gammaBoundOut);
			Parameter contributorLeft = contributor(left, linePieces);
			Parameter contributorRight = contributor(right, linePieces);
			Parameter contributorTop = contributor(top, linePieces);
			Parameter contributorBottom = contributor(bottom, linePieces);
			
			// Add essential values to the bias and gradient vectors.
			currentBias += boundPotential;
			for(int k = 0; k < parameter.length; ++ k) {
				gradient0[k] += 2 * learnRate
						* left.gammaTerm(gammaBoundIn, gammaBoundOut)
						* (left.value - bound.pointLeft) 
						* contributorLeft.dxdai(k, parameter);
				gradient0[k] += 2 * learnRate
						* right.gammaTerm(gammaBoundIn, gammaBoundOut)
						* (right.value - bound.pointRight) 
						* contributorRight.dxdai(k, parameter);
				gradient0[k] += 2 * learnRate
						* top.gammaTerm(gammaBoundIn, gammaBoundOut)
						* (top.value - bound.pointTop) 
						* contributorTop.dydai(k, parameter);
				gradient0[k] += 2 * learnRate
						* bottom.gammaTerm(gammaBoundIn, gammaBoundOut)
						* (bottom.value - bound.pointBottom) 
						* contributorBottom.dydai(k, parameter);
			}
			
			// Update the parameter's vector.
			double currentModulus = 0.;
			for(int k = 0; k < parameter.length; ++ k)
				currentModulus += gradient0[k] * gradient0[k];
			
			// Check whether current calculation reaches terminating condition.
			if(Math.abs(currentBias) < terminateBias) break;
			if(Math.abs(currentModulus) < terminateBias * terminateBias) break;
			
			// Update the parameter's vector.
			for(int k = 0; k < parameter.length; ++ k)
				parameter[k] -= gradient0[k];
			
			// Swap the gradient vector buffers.
			double[] gradientTemp = gradient0;
			gradient0 = gradient1; gradient1 = gradientTemp;
			
			// Calculate the gradients dot product.
			double dotProduct = 0.;
			for(int k = 0; k < parameter.length; ++ k)
				dotProduct += gradient0[k] * gradient1[k];
			if(dotProduct < 0) learnRate *= penaltyRate;
			else learnRate *= bonusRate;
		}
		
		return currentBias;
	}
}
