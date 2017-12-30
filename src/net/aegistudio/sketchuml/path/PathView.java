package net.aegistudio.sketchuml.path;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public interface PathView<Path> {
	public enum ArrowStyle {
		NONE,				// --
		FISHBONE,			// ->
		TRIANGLE_EMPTY,		// -+> (EMPTY)
		TRIANGLE_FILLED,	// -+> (FILLED)
		DIAMOND_EMPTY,		// -<> (EMPTY)
		DIAMOND_FILLED,		// -<> (FILLED)
		CIRCLE_EMPTY,		// -o  (EMPTY)
		CIRCLE_FILLED		// -o  (FILLED)
	}
	
	public enum LineStyle {
		COHERENT,			// -
		DASH,				// --
		DOT,				// ..
		DASHDOT				// -.-.
	}
	
	/**
	 * Render the path object.
	 * 
	 * @param pathObject the path object to reform.
	 * @param boundBegin the starting object's center point.
	 * @param boundEnd the ending object's center point.
	 * @param 
	 */
	public void render(Graphics2D graphics, boolean selected,
			Path pathObject, LineStyle line,
			Rectangle2D boundBegin, ArrowStyle arrowBegin, 
			Rectangle2D boundEnd, ArrowStyle arrowEnd);
}
