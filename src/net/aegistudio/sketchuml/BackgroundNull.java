package net.aegistudio.sketchuml;

import java.awt.Graphics2D;

public class BackgroundNull implements Background {

	@Override
	public void renderBackground(Graphics2D g2d, 
			SketchRenderHint renderHint, 
			double offsetX, double offsetY,
			double width, double height) {
	}
}
