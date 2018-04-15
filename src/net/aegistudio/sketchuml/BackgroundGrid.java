package net.aegistudio.sketchuml;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import net.aegistudio.sketchuml.abstraction.SketchRenderHint;

/**
 * Render grid with provided grid interval and repetitive period
 * as background to the provided graphics.
 * 
 * @author Haoran Luo
 */
public class BackgroundGrid implements Background {
	private final int period;
	private final double interval;
	
	private final Stroke slimStroke;
	private final Stroke thickStroke;
	private final Color gridColor;
	
	public BackgroundGrid(int period, double interval, 
			Stroke slimStroke, Stroke thickStroke, Color gridColor) {
		this.period = period;
		this.interval = interval;
		this.slimStroke = slimStroke;
		this.thickStroke = thickStroke;
		this.gridColor = gridColor;
	}
	
	@Override
	public void renderBackground(Graphics2D g2d, 
			SketchRenderHint renderHint, 
			double offsetX, double offsetY, 
			double width, double height) {
		g2d.setColor(gridColor);
		
		// Render the vertical lines.
		int numX = (int)Math.ceil(offsetX / interval);
		double currentX;
		for(int i = numX; (currentX = interval * i) <= 
				offsetX + width; ++ i) {
			
			g2d.setStroke((i % period) == 0? 
					thickStroke : slimStroke);
			int x = (int)(currentX - offsetX);
			g2d.drawLine(x, 0, x, (int)height);
		}
		
		// Render the horizontal lines.
		int numY = (int)Math.ceil(offsetY / interval);
		double currentY;
		for(int j = numY; (currentY = interval * j) <=
				offsetY + height; ++ j) {
			
			g2d.setStroke((j % period) == 0? 
					thickStroke : slimStroke);
			int y = (int)(currentY - offsetY);
			g2d.drawLine(0, y, (int)width, y);
		}
	}
}
