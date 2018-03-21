package net.aegistudio.sketchuml.astaxpt;

import java.awt.geom.Rectangle2D;
import java.util.function.Function;

public class AstahSquareFitter implements Function<Rectangle2D, Rectangle2D> {

	@Override
	public Rectangle2D apply(Rectangle2D source2d) {
		double squareSize = Math.min(source2d.getWidth(), source2d.getHeight());
		double halfSize = 0.5 * squareSize;
		
		Rectangle2D rectangle2d = new Rectangle2D.Double();
		rectangle2d.setRect(source2d.getCenterX() - halfSize,
				source2d.getCenterY() - halfSize,
				squareSize, squareSize);
		return rectangle2d;
	}

}
