package net.aegistudio.sketchuml.abstraction;

import java.awt.Graphics;
import java.awt.Rectangle;

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
			Graphics g, Entity entity, boolean preview,
			int entityWidth, int entityHeight) {
		Rectangle bound = g.getClipBounds();
		if(bound == null) return;
		
		if(entityWidth > entityHeight) 
			painter.render(hint, g.create((entityWidth - entityHeight) / 2, 
				0, entityHeight, entityHeight), entityHeight, preview);
		else painter.render(hint, g.create(0, (entityHeight - entityWidth) / 2, 
				entityWidth, entityWidth), entityWidth, preview);
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
