package net.aegistudio.sketchuml;

import java.awt.Component;
import java.util.function.Consumer;

/**
 * The view that enable users to edit the concrete entity object.
 * 
 * @author Haoran Luo
 */
public interface PropertyView {
	/**
	 * @return the display panel shown on the side bar.
	 */
	public Component getViewObject(Consumer<Entity> notifier);
	
	/**
	 * @param entity set the entity as current editing.
	 */
	public void updateEntity(Entity entity);
}