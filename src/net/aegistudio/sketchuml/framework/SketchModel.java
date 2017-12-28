package net.aegistudio.sketchuml.framework;

import net.aegistudio.sketchuml.stroke.SketchRecognizer;

public interface SketchModel {
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
	public void selectComponent(Object key, SketchEntityComponent component);
	
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
	 * @return the number of components.
	 */
	public int numComponents();
	
	/**
	 * @param i the index of the component.
	 * @return the i-th component.
	 */
	public SketchEntityComponent get(int i);
	
	/**
	 * Used to notify UI update if the underlying model is changed.
	 * 
	 * @param sourceKey the object representing the caller object.
	 * @param changeListener the closure the connect to model.
	 */
	public void connect(Object sourceKey, Runnable changeListener);

	SketchEntityComponent getSelected();

	SketchEntityComponent getSelectedOriginal();

	/**
	 * @param source the source's caller object.
	 */
	void notifySelectedChanged(Object source);
}
