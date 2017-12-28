package net.aegistudio.sketchuml.statechart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import net.aegistudio.sketchuml.SimpleRegularEntity;

public class EntityStateExit extends SimpleRegularEntity {
	@Override
	public void render(Graphics g, int size, boolean preview) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(Color.WHITE);
		g2d.fillOval(1, 1, size - 2, size - 2);
		g2d.setColor(preview? Color.GRAY : Color.BLACK);
		g2d.drawOval(1, 1, size - 2, size - 2);
		
		int sqr2Size = (int)(Math.sqrt(2) * size / 4) - 1;
		int div2Size = size / 2;
		
		g2d.drawLine(div2Size - sqr2Size, div2Size - sqr2Size, 
				div2Size + sqr2Size, div2Size + sqr2Size);
		g2d.drawLine(div2Size + sqr2Size, div2Size - sqr2Size, 
				div2Size - sqr2Size, div2Size + sqr2Size);
	}
}