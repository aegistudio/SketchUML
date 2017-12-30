package net.aegistudio.sketchuml.statechart;

import java.awt.Color;
import java.awt.Graphics;

import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.EntityAdapter;
import net.aegistudio.sketchuml.framework.RegularRenderer;

public class EntityStateStart extends EntityAdapter implements RegularRenderer.Painter {
	private final RegularRenderer renderer = new RegularRenderer(this);
	
	@Override
	public void render(Graphics g, int size, boolean preview) {
		g.setColor(preview? Color.GRAY : Color.BLACK);
		g.fillOval(0, 0, size, size);
	}
	
	@Override
	public void renderEntity(Graphics g, Entity entity, boolean preview) {
		renderer.renderEntity(g, entity, preview);
	}
}