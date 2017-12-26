package net.aegistudio.sketchuml;

import java.awt.Graphics;

/**
 * The view that enable users to see the object from the sketch.
 * 
 * @author Haoran Luo
 */
public interface SketchView {
	/**
	 * @param g the graphic object to render object.
	 * @param entity the entities' data model.
	 * @param preview is object previewing.
	 */
	public void renderEntity(Graphics g, Entity entity, boolean preview);
}