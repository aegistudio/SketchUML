package net.aegistudio.sketchuml.path;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.SketchRenderHint;

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
	 * @param hint the sketch rendering hint.
	 * @param pathObject the path object to reform.
	 * @param boundBegin the starting object's center point.
	 * @param arrowBegin the style of the beginning arrow.
	 * @param boundEnd the ending object's center point.
	 * @param arrowEnd the style of the ending arrow.
	 * @param startText the staring text.
	 * @param centerText the centering text.
	 * @param endText the ending text.
	 */
	public void render(SketchRenderHint hint,
			Graphics2D graphics, boolean selected,
			Path pathObject, LineStyle line,
			Rectangle2D boundBegin, ArrowStyle arrowBegin, 
			Rectangle2D boundEnd, ArrowStyle arrowEnd,
			String startText, String centerText, String endText);
	
	/**
	 * Retrieve the minimal distance between a point and the path.
	 * 
	 * @param pathObject the path object
	 * @param position the point to check
	 * @param boundBegin the begin object's bound
	 * @param boundEnd the end object's bound
	 * @return the minimal distance.
	 */
	public double distance(Path pathObject, PointR position, 
			Rectangle2D boundBegin, Rectangle2D boundEnd);
}
