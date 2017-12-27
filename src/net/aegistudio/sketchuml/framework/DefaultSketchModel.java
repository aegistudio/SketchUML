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
		selectComponent(null);
		components.add(0, component);
		notifyUpdate();
	}
	
	@Override
	public void destroy(SketchEntityComponent component) {
		selectComponent(null);
		if(components.remove(component)) 
			notifyUpdate();
	}

	@Override
	public void moveToBack(SketchEntityComponent c) {
		if(components.remove(c)) {
			components.add(c);
			notifyUpdate();
		}
	}

	@Override
	public void moveToFront(SketchEntityComponent c) {
		if(components.remove(c)) {
			components.add(0, c);
			notifyUpdate();
		}
	}

	@Override
	public SketchRecognizer getRecognizer() {
		return recognizer;
	}

	@Override
	public int numComponents() {
		return components.size();
	}

	private int selectedIndex = -1;
	private SketchEntityComponent selectedComponent;
	
	@Override
	public SketchEntityComponent get(int i) {
		if(i == selectedIndex)
			return selectedComponent;
		return components.get(i);
	}

	List<Runnable> connections = new ArrayList<>();
	@Override
	public void connect(Runnable changeListener) {
		connections.add(changeListener);
	}
	
	private void notifyUpdate() {
		connections.forEach(Runnable::run);
	}

	@Override
	public void selectComponent(SketchEntityComponent component) {
		int newSelectedIndex = -1;
		if(component != null) 
			newSelectedIndex = components.indexOf(component);
		
		boolean isUpdated = false;
		if(selectedIndex != newSelectedIndex) {
			if(selectedIndex >= 0) {
				// Replace the selection.
				components.set(selectedIndex, selectedComponent);
				selectedComponent = null;
				selectedIndex = -1;
				isUpdated = true;
			}
			
			// Update selected index and sketch component.
			selectedIndex = newSelectedIndex;
			
			if(selectedIndex >= 0) {
				selectedComponent = new SketchEntityComponent(
						component.entry, component.entity);
				selectedComponent.x = component.x;
				selectedComponent.y = component.y;
				selectedComponent.w = component.w;
				selectedComponent.h = component.h;
				
				isUpdated = true;
			}
		}
		
		if(isUpdated) notifyUpdate();
	}
	
	@Override
	public SketchEntityComponent getSelected() {
		if(selectedIndex < 0) return null;
		return selectedComponent;
	}
	
	@Override
	public SketchEntityComponent getSelectedOriginal() {
		if(selectedIndex < 0) return null;
		return components.get(selectedIndex);
	}
	
	@Override
	public void notifySelectedChanged() {
		if(selectedIndex < 0) return;
		notifyUpdate();
	}

	@Override
	public void destroySelected() {
		if(selectedIndex < 0) return;
		components.remove(selectedIndex);
		selectedComponent = null;
		selectedIndex = -1;
		notifyUpdate();
	}
}
