package net.aegistudio.sketchuml.framework;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.Template;
import net.aegistudio.sketchuml.path.PathManager;
import net.aegistudio.sketchuml.path.PathView;
import net.aegistudio.sketchuml.stroke.SketchRecognizer;

public class DefaultSketchModel<Path> implements SketchModel<Path> {
	private final Template template;
	private final SketchRecognizer recognizer;
	private final List<SketchEntityComponent> components;
	private final List<SketchLinkComponent<Path>> links;
	
	private final PathView<Path> pathView;
	private final @SuppressWarnings("unused") PathManager<Path> pathManager;
	
	private int selectedIndexEntity = -1;
	private int selectedIndexLink = -1;
	private SketchEntityComponent selectedEntity;
	
	public DefaultSketchModel(Template template, SketchRecognizer recognizer,
			PathView<Path> pathView, PathManager<Path> pathManager) {
		this.template = template;
		this.recognizer = recognizer;
		this.pathManager = pathManager;
		this.pathView = pathView;
		this.components = new ArrayList<>();
		this.links = new ArrayList<>();
	}
	
	/**
	 * Judge whether the current component is the selected or 
	 * original one of the selected.
	 */
	private boolean isEntitySelected(SketchEntityComponent component) {
		if(selectedIndexEntity < 0) return false;
		if(component == selectedEntity) return true;
		if(component == components.get(selectedIndexEntity))
			return true;
		return false;
	}
	
	@Override
	public SketchEntityComponent entityAt(int x, int y) {
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
	public void create(Object key, SketchEntityComponent component) {
		// Discard for the duplicated element.
		if(isEntitySelected(component)) return;
		if(components.contains(component)) return;
		
		// Create a new component here.
		components.add(0, component);
		if(selectedIndexEntity >= 0) selectedIndexEntity ++;
		notifyUpdate(observerEntity, key);
	}
	
	@Override
	public void destroy(Object key, SketchEntityComponent component) {
		// Judge whether the component to remove is selected.
		if(isEntitySelected(component)) {
			// The component to remove is selected.
			// However, as you cannot select link and component simultaneously,
			// there's no need to check whether component is selected.
			SketchEntityComponent original = components.remove(selectedIndexEntity);
			if(original != null) links.removeIf(l -> l.relatedTo(original));
			selectedIndexEntity = -1;
			selectedEntity = null;
			notifyUpdate(observerEntity, key);
		}
		else {
			// Try to get the component index for removing.
			int index = components.indexOf(component);
			if(index < 0) return;
			
			// Remove the component by status.
			if((components.remove(index)) == null) return;
			
			// Remove all components related to this entity.
			// Need to check the selected link index this case.
			if(selectedIndexLink >= 0 && links
					.get(selectedIndexLink)
					.relatedTo(component)) {
				selectedIndexLink = -1;
				notifyUpdate(observerLink, key);
			}
			links.removeIf(l -> l.relatedTo(component));
			
			// Update selection index number.
			if(index < selectedIndexEntity) selectedIndexEntity --;
			notifyUpdate(observerEntity, key);
		}
	}

	@Override
	public void moveToBack(Object key, SketchEntityComponent c) {
		// Judge whether the moving component is selected.
		if(isEntitySelected(c)) {
			// If it is selected, just move it to the end.
			components.add(components
					.remove(selectedIndexEntity));
			selectedIndexEntity = components.size() - 1;
			notifyUpdate(observerEntity, key);
		}
		else {
			// Else judge by the index to update the selected 
			// component's index.
			int index = components.indexOf(c);
			if(index < 0) return;
			
			// Judge by index.
			components.add(components.get(selectedIndexEntity));
			if(index < selectedIndexEntity) selectedIndexEntity --;
			notifyUpdate(observerEntity, key);
		}
	}

	@Override
	public void moveToFront(Object key, SketchEntityComponent c) {
		// Judge whether the moving component is selected.
		if(isEntitySelected(c)) {
			// If it is selected, just move it to the front.
			components.add(0, components
					.remove(selectedIndexEntity));
			selectedIndexEntity = 0;
			notifyUpdate(observerEntity, key);
		}
		else {
			int index = components.indexOf(c);
			if(index < 0) return;
			
			// Judge by index.
			components.add(0, components.get(selectedIndexEntity));
			if(index < selectedIndexEntity) selectedIndexEntity ++;
			notifyUpdate(observerEntity, key);
		}
	}

	@Override
	public SketchRecognizer getRecognizer() {
		return recognizer;
	}

	@Override
	public Template getTemplate() {
		return template;
	}
	
	@Override
	public int numEntities() {
		return components.size();
	}
	
	@Override
	public SketchEntityComponent getEntity(int i) {
		if(i == selectedIndexEntity)
			return selectedEntity;
		return components.get(i);
	}

	Map<Object, Runnable> observerEntity = new HashMap<>();
	Map<Object, Runnable> observerLink = new HashMap<>();
	
	@Override
	public void registerEntityObserver(Object key, Runnable changeListener) {
		observerEntity.put(key, changeListener);
	}
	
	@Override
	public void registerLinkObserver(Object key, Runnable changeListener) {
		observerLink.put(key, changeListener);
	}
	
	private void notifyUpdate(Map<Object, Runnable> registries, Object key) {
		registries.forEach((k, v) -> { if(k != key) v.run(); });
	}

	@Override
	public void selectEntity(Object key, SketchEntityComponent component) {
		int newSelectedIndex = -1;
		if(component != null) 
			newSelectedIndex = components.indexOf(component);
		
		boolean entityUpdated = false;
		if(selectedIndexEntity != newSelectedIndex) {
			if(selectedIndexEntity >= 0) {
				// Replace the selection.
				SketchEntityComponent original 
					= components.get(selectedIndexEntity);
				original.x = selectedEntity.x;
				original.y = selectedEntity.y;
				original.w = selectedEntity.w;
				original.h = selectedEntity.h;
				selectedEntity = null;
				selectedIndexEntity = -1;
				entityUpdated = true;
			}
			
			// Update selected index and sketch component.
			selectedIndexEntity = newSelectedIndex;
			
			if(selectedIndexEntity >= 0) {
				selectedEntity = new SketchEntityComponent(
						component.entry, component.entity);
				selectedEntity.x = component.x;
				selectedEntity.y = component.y;
				selectedEntity.w = component.w;
				selectedEntity.h = component.h;
				
				entityUpdated = true;
			}
		}
		
		if(entityUpdated) notifyUpdate(observerEntity, key);
		else if(selectedIndexLink >= 0) {
			// Un-select current link object, even it does not 
			// change the current selected entity.
			selectedIndexLink = -1;
			notifyUpdate(observerLink, key);
		}
	}
	
	@Override
	public SketchEntityComponent getSelectedEntity() {
		if(selectedIndexEntity < 0) return null;
		return selectedEntity;
	}
	
	@Override
	public SketchEntityComponent getOriginalEntity() {
		if(selectedIndexEntity < 0) return null;
		return components.get(selectedIndexEntity);
	}
	
	@Override
	public void notifyEntityChanged(Object sourceObject) {
		if(selectedIndexEntity < 0) return;
		notifyUpdate(observerEntity, sourceObject);
	}
	
	// Link related operations.

	@Override
	public int numLinks() {
		return links.size();
	}

	@Override
	public void link(Object key, SketchLinkComponent<Path> link) {
		links.add(link);
		notifyUpdate(observerLink, key);
	}

	@Override
	public void unlink(Object key, SketchLinkComponent<Path> link) {
		int linkIndex = links.indexOf(link);
		if(linkIndex < 0) return;
		if(links.remove(linkIndex) != null) {
			if(linkIndex == selectedIndexLink)
				selectedIndexLink = -1;
			else if(linkIndex < selectedIndexLink)
				selectedIndexLink --;
			notifyUpdate(observerLink, key);
		}
	}

	@Override
	public SketchLinkComponent<Path> getLink(int i) {
		return links.get(i);
	}
	
	@Override
	public SketchLinkComponent<Path> getSelectedLink() {
		if(selectedIndexLink < 0) return null;
		return links.get(selectedIndexLink);
	}

	@Override
	public void selectLink(Object key, SketchLinkComponent<Path> link) {
		int linkIndex = -1;
		if(link != null) linkIndex = links.indexOf(link);
		
		// Un-select current entity object.
		if(selectedIndexEntity >= 0)
			this.selectEntity(key, null);
		
		// Update the link if two link index mismatches.
		if(linkIndex != selectedIndexLink) {
			selectedIndexLink = linkIndex;
			notifyUpdate(observerLink, key);
		}
	}

	@Override
	public SketchLinkComponent<Path> linkAt(int x, int y) {
		SketchEntityComponent selected = getOriginalEntity();
		Rectangle2D boundSelected = selected != null? 
				selected.getBoundRectangle() : null;
		PointR position = new PointR(x, y);
		double threshold = Configuration.getInstance().MAX_LINKTHRESHOLD;
		
		// Visit all elements in the links.
		for(SketchLinkComponent<Path> link : links) {
			Rectangle2D boundBegin = link.source == selected?
					boundSelected : link.source.getBoundRectangle();
			Rectangle2D boundEnd = link.destination == selected?
					boundSelected : link.destination.getBoundRectangle();
			
			if(threshold >= pathView.distance(link.pathObject, 
					position, boundBegin, boundEnd)) return link;
		}
		return null;
	}
}
