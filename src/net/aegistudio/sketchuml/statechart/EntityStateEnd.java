package net.aegistudio.sketchuml.statechart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import net.aegistudio.sketchuml.SimpleRegularEntity;

public class EntityStateEnd extends SimpleRegularEntity {
	@Override
	public void render(Graphics g, int size, boolean preview) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(preview? Color.GRAY : Color.BLACK);
		g2d.drawOval(0, 0, size, size);
		g2d.setStroke(new BasicStroke(1));
		g2d.fillOval((int)(0.2 * size), (int)(0.2 * size), 
				(int)(0.6 * size), (int)(0.6 * size));
	}
}