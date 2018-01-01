package net.aegistudio.sketchuml.framework;

import net.aegistudio.sketchuml.Template;
import net.aegistudio.sketchuml.stroke.SketchRecognizer;

public interface SketchModel<Path> {
	/**
	 * @return the component at specified logic coordinate.
	 */
	public SketchEntityComponent componentAt(int x, int y);
	
	/**
	 * @param key the notifier's key.
	 * @param component the component to create.
	 */
	public void create(Object key, SketchEntityComponent component);
	
	/**
	 * @param key the notifier's key.
	 * @param component the component to set selected.
	 */
	public void selectEntity(Object key, SketchEntityComponent component);
	
	/**
	 * @param key the notifier's key.
	 * @param component the component to remove.
	 */
	public void destroy(Object key, SketchEntityComponent component);
	
	/**
	 * @param key the notifier's key.
	 * @param c the component to move to the back.
	 */
	public void moveToBack(Object key, SketchEntityComponent c);
	
	/**
	 * @param key the notifier's key.
	 * @param c the component to send to the front.
	 */
	public void moveToFront(Object key, SketchEntityComponent c);
	
	/**
	 * @return the recognizer of a newly coming strokes.
	 */
	public SketchRecognizer getRecognizer();
	
	/**
	 * @return the underlying template object.
	 */
	public Template getTemplate();
	
	/**
	 * @return the number of entities.
	 */
	public int numEntities();
	
	/**
	 * @param i the index of the entity.
	 * @return the i-th entity.
	 */
	public SketchEntityComponent getEntity(int i);
	
	/**
	 * Used to notify when the underlying entity selection is changed.
	 * 
	 * @param sourceKey the object representing the caller object.
	 * @param observer the closure the connect to model.
	 */
	public void registerEntityObserver(Object sourceKey, Runnable observer);

	/**
	 * @return the current selected entity (not link).
	 */
	SketchEntityComponent getSelectedEntity();

	/**
	 * @return the current selected entity's original object (not link).
	 */
	SketchEntityComponent getOriginalEntity();

	/**
	 * @param source the source's caller object.
	 */
	void notifyEntityChanged(Object source);
	
	/**
	 * @return the number of links.
	 */
	public int numLinks();
	
	/**
	 * Create link between two objects.
	 */
	public void link(SketchLinkComponent<Path> link);
	
	/**
	 * Remove link between two objects.
	 */
	public void unlink(SketchLinkComponent<Path> link);
	
	/**
	 * @return the k-th link.
	 */
	public SketchLinkComponent<Path> getLink(int i);
}
