package net.aegistudio.sketchuml.framework;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

public class MultilineRenderer {
	public void drawLines(Graphics g, String[] lines) {
		Rectangle bound = g.getClipBounds();
		
		int lineOffsets = 0;
		for(int i = 0; i < lines.length; ++ i) {
			Rectangle2D lineBound = g.getFontMetrics()
					.getStringBounds(lines[i], g);
			
			g.drawString(lines[i], 0, 
					(int)(lineOffsets + lineBound.getHeight()));
			lineOffsets += lineBound.getHeight();
			
			if(lineOffsets > bound.height) break;
		}
	}
}
