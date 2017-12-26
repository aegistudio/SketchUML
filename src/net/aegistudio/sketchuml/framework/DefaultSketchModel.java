package net.aegistudio.sketchuml.framework;

import java.util.ArrayList;
import java.util.List;

import net.aegistudio.sketchuml.stroke.SketchRecognizer;

public class DefaultSketchModel implements SketchModel {
	private final SketchRecognizer recognizer;
	private final List<SketchEntityComponent> components;
	
	public DefaultSketchModel(SketchRecognizer recognizer) {
		this.recognizer = recognizer;
		this.components = new ArrayList<>();
	}
	
	@Override
	public SketchEntityComponent componentAt(int x, int y) {
		for(int i = 0; i < components.size(); ++ i) {
			SketchEntityComponent current = components.get(i);
			int xLocal = x - current.x;
			int yLocal = y - current.y;
			
			if(xLocal > 0 && yLocal > 0 && 
				xLocal < current.w && yLocal < current.h)
				return current;
		}
		return null;
	}

	@Override
	public void create(SketchEntityComponent component) {
		components.add(0, component);
	}

	@Override
	public void moveToBack(SketchEntityComponent c) {
		if(components.remove(c)) components.add(c);
	}

	@Override
	public void moveToFront(SketchEntityComponent c) {
		if(components.remove(c)) components.add(0, c);
	}

	@Override
	public SketchRecognizer getRecognizer() {
		return recognizer;
	}

	@Override
	public int numComponents() {
		return components.size();
	}

	@Override
	public SketchEntityComponent get(int i) {
		return components.get(i);
	}
}
