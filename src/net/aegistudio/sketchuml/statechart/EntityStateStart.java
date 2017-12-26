package net.aegistudio.sketchuml.statechart;

import java.awt.Color;
import java.awt.Graphics;
import net.aegistudio.sketchuml.SimpleRegularEntity;

public class EntityStateStart extends SimpleRegularEntity {
	@Override
	public void render(Graphics g, int size, boolean preview) {
		g.setColor(preview? Color.GRAY : Color.BLACK);
		g.fillOval(0, 0, size, size);
	}
}