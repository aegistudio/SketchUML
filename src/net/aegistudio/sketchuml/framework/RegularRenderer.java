package net.aegistudio.sketchuml.framework;

import java.awt.Graphics;
import java.awt.Rectangle;

import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.SketchRenderHint;
import net.aegistudio.sketchuml.SketchView;

public class RegularRenderer implements SketchView {
	private final Painter painter;
	public RegularRenderer(Painter painter) {
		this.painter = painter;
	}
	
	public interface Painter {
		public void render(SketchRenderHint hint,
				Graphics g, int length, boolean preview);
	}
	
	@Override
	public void renderEntity(SketchRenderHint hint, 
			Graphics g, Entity entity, boolean preview) {
		Rectangle bound = g.getClipBounds();
		if(bound == null) return;
		
		if(bound.width > bound.height) 
			painter.render(hint, g.create((bound.width - bound.height) / 2, 
				0, bound.height, bound.height), bound.height, preview);
		else painter.render(hint, g.create(0, (bound.height - bound.width) / 2, 
				bound.width, bound.width), bound.width, preview);
	}

	/**
	 * @deprecated The renderer only cares about the changing in the render 
	 * entity interface here, the overlay interface is ignored.
	 */
	@Override
	@Deprecated
	public String overlayEntity(Entity entity, OverlayDirection old) {
		return null;
	}
}
