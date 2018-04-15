package net.aegistudio.sketchuml.path;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.abstraction.LinkView;
import net.aegistudio.sketchuml.abstraction.SketchRenderHint;

public interface PathView<Path> {
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
			Path pathObject, LinkView.LineStyle line,
			Rectangle2D boundBegin, LinkView.ArrowStyle arrowBegin, 
			Rectangle2D boundEnd, LinkView.ArrowStyle arrowEnd,
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
	
	/**
	 * Wrapper method for the invoking the render method of pathView.
	 * 
	 * @param pathView the path view to invoke render method.
	 * @param linkRender the rendering info of the path.
	 * @param hint the hints for rendering entities.
	 * @param g2d the graphics where to render entity.
	 * @param preview is the graphic object under preview mode currently.
	 * @param path the data indicating the path shape.
	 * @param boundBegin the rectangle bound of the beginning entity.
	 * @param boundEnd the rectangle bound of the ending entity.
	 */
	public static <Path> void paint(PathView<Path> pathView,
			LinkView.LinkRender linkRender, SketchRenderHint hint, 
			Graphics2D g2d, boolean preview, Path path, 
			Rectangle2D boundBegin, Rectangle2D boundEnd) {
		
		pathView.render(hint, g2d, preview, path, 
				linkRender.lineStyle, 
				boundBegin, linkRender.beginStyle, 
				boundEnd, linkRender.endStyle,
				linkRender.startText, 
				linkRender.centerText, 
				linkRender.endText);
	}
}
