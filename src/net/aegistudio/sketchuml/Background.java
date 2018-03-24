package net.aegistudio.sketchuml;

import java.awt.Graphics2D;

/**
 * Render the background of the sketch main panel or the exported
 * file, according to provided data.
 *  
 * @author Haoran Luo
 */
public interface Background {
	/**
	 * Perform background rendering at provided graphics.
	 * 
	 * @param g2d the graphics object to render.
	 * @param renderHint the hint while rendering background.
	 * @param offsetX the X-offset of the origin.
	 * @param offsetY the Y-offset of the origin.
	 * @param width the width of the proposed size.
	 * @param height the height of the proposed size.
	 */
	public void renderBackground(Graphics2D g2d, 
			SketchRenderHint renderHint, 
			double offsetX, double offsetY, 
			double width, double height);
}
