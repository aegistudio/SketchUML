package net.aegistudio.sketchuml;

import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class SimpleRegularEntity extends SimpleEntity {

	@Override
	public final void renderEntity(Graphics g, Entity entity, boolean preview) {
		Rectangle bound = g.getClipBounds();
		if(bound == null) return;
		
		if(bound.width > bound.height) 
			render(g.create((bound.width - bound.height) / 2, 0, 
					bound.height, bound.height), bound.height, preview);
		else render(g.create(0, (bound.height - bound.width) / 2, 
					bound.width, bound.width), bound.width, preview);
	}

	public abstract void render(Graphics g, int length, boolean preview);
}
