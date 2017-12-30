package net.aegistudio.sketchuml.statechart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import net.aegistudio.sketchuml.RegularEntityAdapter;

public class EntityStateEnd extends RegularEntityAdapter {
	@Override
	public void render(Graphics g, int size, boolean preview) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(2));
		
		// Outer circle.
		g2d.setColor(Color.WHITE);
		g2d.fillOval(1, 1, size - 2, size - 2);
		g2d.setColor(preview? Color.GRAY : Color.BLACK);
		g2d.drawOval(0, 0, size, size);
		
		// Inner circle.
		g2d.setStroke(new BasicStroke(1));
		int innerOffset = (int)(0.2 * size);
		int innerSize = size - 2 * innerOffset - 1;
		g2d.fillOval(innerOffset, innerOffset,
				innerSize, innerSize);
	}
}