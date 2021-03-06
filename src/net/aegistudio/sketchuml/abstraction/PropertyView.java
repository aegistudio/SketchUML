package net.aegistudio.sketchuml.abstraction;

import java.awt.Component;
import java.util.function.Consumer;

/**
 * The view that enable users to edit the concrete entity object.
 * 
 * @author Haoran Luo
 */
public interface PropertyView {
	public interface Factory {
		public PropertyView newPropertyView(Consumer<Entity> notifier);
	}
	
	/**
	 * @return the display panel shown on the side bar.
	 */
	public Component getViewObject();
	
	/**
	 * @param entity set the entity as current editing.
	 */
	public void select(Entity entity);
	
	/**
	 * @param entity update the entity whose view is this property
	 * view. Do notice that the view should not be updated if it
	 * is not currently being edited.
	 */
	public void update(Entity entity);
}
