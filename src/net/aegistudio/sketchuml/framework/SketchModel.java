package net.aegistudio.sketchuml.framework;

import net.aegistudio.sketchuml.stroke.SketchRecognizer;

public interface SketchModel {
	/**
	 * @return the component at specified logic coordinate.
	 */
	public SketchEntityComponent componentAt(int x, int y);
	
	/**
	 * @param component the component to create.
	 */
	public void create(SketchEntityComponent component);
	
	/**
	 * @param component the component to set selected.
	 */
	public void selectComponent(SketchEntityComponent component);
	
	/**
	 * @param component the component to remove.
	 */
	public void destroy(SketchEntityComponent component);
	
	/**
	 * @param c the component to move to the back.
	 */
	public void moveToBack(SketchEntityComponent c);
	
	/**
	 * @param c the component to send to the front.
	 */
	public void moveToFront(SketchEntityComponent c);
	
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
	 * @param changeListener the closure the connect to model.
	 */
	public void connect(Runnable changeListener);

	SketchEntityComponent getSelected();

	SketchEntityComponent getSelectedOriginal();

	void notifySelectedChanged();
}
