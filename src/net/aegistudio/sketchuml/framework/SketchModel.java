package net.aegistudio.sketchuml.framework;

import net.aegistudio.sketchuml.stroke.SketchRecognizer;

public interface SketchModel {
	public SketchEntityComponent componentAt(int x, int y);
	
	public void create(SketchEntityComponent component);
	
	public void moveToBack(SketchEntityComponent c);
	
	public void moveToFront(SketchEntityComponent c);
	
	public SketchRecognizer getRecognizer();
	
	public int numComponents();
	
	public SketchEntityComponent get(int i);
}
