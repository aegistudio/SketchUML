package net.aegistudio.sketchuml.framework;

import java.util.ArrayList;
import java.util.List;

import net.aegistudio.sketchuml.stroke.SketchRecognizer;

public class DefaultSketchModel implements SketchModel {
	private final SketchRecognizer recognizer;
	private final List<SketchEntityComponent> components;
	private int selectedIndex = -1;
	private SketchEntityComponent selectedComponent;
	
	public DefaultSketchModel(SketchRecognizer recognizer) {
		this.recognizer = recognizer;
		this.components = new ArrayList<>();
	}
	
	/**
	 * Judge whether the current component is the selected or 
	 * original one of the selected.
	 */
	private boolean isSelected(SketchEntityComponent component) {
		if(selectedIndex < 0) return false;
		if(component == selectedComponent) return true;
		if(component == components.get(selectedIndex))
			return true;
		return false;
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
		// Discard for the duplicated element.
		if(isSelected(component)) return;
		
		// Create a new component here.
		components.add(0, component);
		if(selectedIndex >= 0) selectedIndex ++;
		notifyUpdate();
	}
	
	@Override
	public void destroy(SketchEntityComponent component) {
		// Judge whether the component to remove is selected.
		if(isSelected(component)) {
			components.remove(selectedIndex);
			selectedIndex = -1;
			selectedComponent = null;
			notifyUpdate();
		}
		else {
			// Try to get the component index for removing.
			int index = components.indexOf(component);
			if(index < 0) return;
			
			// Remove the component by status.
			if((components.remove(index)) == null) return;
			if(index < selectedIndex) selectedIndex --;
			notifyUpdate();
		}
	}

	@Override
	public void moveToBack(SketchEntityComponent c) {
		// Judge whether the moving component is selected.
		if(isSelected(c)) {
			// If it is selected, just move it to the end.
			components.add(components
					.remove(selectedIndex));
			selectedIndex = components.size() - 1;
		}
		else {
			// Else judge by the index to update the selected 
			// component's index.
			int index = components.indexOf(c);
			if(index < 0) return;
			
			// Judge by index.
			components.add(components.get(selectedIndex));
			if(index < selectedIndex) selectedIndex --;
			notifyUpdate();
		}
	}

	@Override
	public void moveToFront(SketchEntityComponent c) {
		// Judge whether the moving component is selected.
		if(isSelected(c)) {
			// If it is selected, just move it to the front.
			components.add(0, components
					.remove(selectedIndex));
			selectedIndex = 0;
		}
		else {
			int index = components.indexOf(c);
			if(index < 0) return;
			
			// Judge by index.
			components.add(0, components.get(selectedIndex));
			if(index < selectedIndex) selectedIndex ++;
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
}
