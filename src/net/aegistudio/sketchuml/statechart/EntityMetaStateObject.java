package net.aegistudio.sketchuml.statechart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.function.Consumer;

import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.PropertyView;
import net.aegistudio.sketchuml.SketchView;

public class EntityMetaStateObject implements SketchView, PropertyView {
	public static int STATE_ROUNDSIZE = 50;
	public static int STATE_NAMEHEIGHT = 24;

	@Override
	public Component getViewObject(Consumer<Entity> notifier) {
		return null;
	}

	@Override
	public void updateEntity(Entity entity) {
		
	}

	@Override
	public void renderEntity(Graphics g, Entity entity, boolean preview) {
		Rectangle bound = g.getClipBounds();
		EntityStateObject entityState = (EntityStateObject)entity;
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(2));
		
		g.setColor(preview? Color.GRAY : Color.BLACK);
		
		// Draw outline.
		g.drawRoundRect(1, 1, bound.width - 2, bound.height - 2, 
				STATE_ROUNDSIZE, STATE_ROUNDSIZE);
		
		// Draw internal text.
		if(!entityState.isBrief) {
			g.drawLine(0, STATE_NAMEHEIGHT, 
					bound.width, STATE_NAMEHEIGHT);
		}
	}

}
