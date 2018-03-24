package net.aegistudio.sketchuml.framework;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.SketchRenderHint;

public class RenderUtils {
	public static void drawLines(Graphics g, String[] lines) {
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
	
	public static void drawStroke(Graphics2D g2d, Vector<PointR> points) {
		for (int i = 0; i < (points.size() - 1); ++i) {
			PointR former = points.elementAt(i);
			PointR latter = points.elementAt(i + 1);
			g2d.drawLine(former.intX(), former.intY(), 
					latter.intX(), latter.intY());
		}
	}
	
	static final BasicStroke fillStroke = new BasicStroke(1);
	public static void beginFill(Graphics2D g2d, 
			SketchRenderHint hint, boolean selected) {
		
		g2d.setStroke(fillStroke);
		if(selected) g2d.setColor(hint.fillColorSelected);
		else g2d.setColor(hint.fillColorNormal);
	}
	
	public static void beginOutline(Graphics2D g2d,
			SketchRenderHint hint, boolean selected) {
		
		g2d.setStroke(new BasicStroke(hint.outlineWidth));
		if(selected) g2d.setColor(hint.lineColorSelected);
		else g2d.setColor(hint.lineColorNormal);
	}
}
