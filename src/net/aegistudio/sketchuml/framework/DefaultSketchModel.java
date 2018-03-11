package net.aegistudio.sketchuml.framework;

import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.Entity;
import net.aegistudio.sketchuml.EntityEntry;
import net.aegistudio.sketchuml.LinkEntry;
import net.aegistudio.sketchuml.Template;
import net.aegistudio.sketchuml.path.PathManager;
import net.aegistudio.sketchuml.path.PathView;

public class DefaultSketchModel<Path> implements SketchModel<Path> {
	private final Template template;
	private final List<SketchEntityComponent> entities;
	private final List<SketchLinkComponent<Path>> links;
	
	private final PathView<Path> pathView;
	private final PathManager<Path> pathManager;
	
	private int selectedIndexEntity = -1;
	private int selectedIndexLink = -1;
	private SketchEntityComponent selectedEntity;
	
	public DefaultSketchModel(Template template, 
			PathView<Path> pathView, PathManager<Path> pathManager) {
		this.template = template;
		this.pathManager = pathManager;
		this.pathView = pathView;
		this.entities = new ArrayList<>();
		this.links = new ArrayList<>();
	}
	
	/**
	 * Judge whether the current component is the selected or 
	 * original one of the selected.
	 */
	private boolean isEntitySelected(SketchEntityComponent component) {
		if(selectedIndexEntity < 0) return false;
		if(component == selectedEntity) return true;
		if(component == entities.get(selectedIndexEntity))
			return true;
		return false;
	}
	
	@Override
	public SketchEntityComponent entityAt(int x, int y) {
		for(int i = 0; i < entities.size(); ++ i) {
			SketchEntityComponent current = entities.get(i);
			int xLocal = x - current.x;
			int yLocal = y - current.y;
			
			if(xLocal > 0 && yLocal > 0 && 
				xLocal < current.w && yLocal < current.h)
				return current;
		}
		return null;
	}
	
	@Override
	public int entityIndexOf(SketchEntityComponent entity) {
		if(entity == null) return -1;
		if(entity == selectedEntity) return selectedIndexEntity;
		return entities.indexOf(entity);
	}

	@Override
	public void create(Object key, SketchEntityComponent component) {
		// Discard for the duplicated element.
		if(isEntitySelected(component)) return;
		if(entities.contains(component)) return;
		
		// Create a new component here.
		entities.add(0, component);
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
			SketchEntityComponent original = entities.remove(selectedIndexEntity);
			if(original != null) links.removeIf(l -> l.relatedTo(original));
			selectedIndexEntity = -1;
			selectedEntity = null;
			notifyUpdate(observerEntity, key);
		}
		else {
			// Try to get the component index for removing.
			int index = entities.indexOf(component);
			if(index < 0) return;
			
			// Remove the component by status.
			if((entities.remove(index)) == null) return;
			
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
			entities.add(entities
					.remove(selectedIndexEntity));
			selectedIndexEntity = entities.size() - 1;
			notifyUpdate(observerEntity, key);
		}
		else {
			// Else judge by the index to update the selected 
			// component's index.
			int index = entities.indexOf(c);
			if(index < 0) return;
			
			// Judge by index.
			entities.add(entities.remove(index));
			if(index < selectedIndexEntity) selectedIndexEntity --;
			notifyUpdate(observerEntity, key);
		}
	}

	@Override
	public void moveToFront(Object key, SketchEntityComponent c) {
		// Judge whether the moving component is selected.
		if(isEntitySelected(c)) {
			// If it is selected, just move it to the front.
			entities.add(0, entities
					.remove(selectedIndexEntity));
			selectedIndexEntity = 0;
			notifyUpdate(observerEntity, key);
		}
		else {
			int index = entities.indexOf(c);
			if(index < 0) return;
			
			// Judge by index.
			entities.add(0, entities.remove(index));
			if(index < selectedIndexEntity) selectedIndexEntity ++;
			notifyUpdate(observerEntity, key);
		}
	}

	@Override
	public Template getTemplate() {
		return template;
	}
	
	@Override
	public int numEntities() {
		return entities.size();
	}
	
	@Override
	public SketchEntityComponent getEntity(int i) {
		if(i == selectedIndexEntity)
			return selectedEntity;
		return entities.get(i);
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
			newSelectedIndex = entities.indexOf(component);
		
		boolean entityUpdated = false;
		if(selectedIndexEntity != newSelectedIndex) {
			if(selectedIndexEntity >= 0) {
				// Replace the selection.
				SketchEntityComponent original 
					= entities.get(selectedIndexEntity);
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
		return entities.get(selectedIndexEntity);
	}
	
	@Override
	public void notifyEntityChanged(Object sourceObject) {
		if(selectedIndexEntity < 0) return;
		notifyUpdate(observerEntity, sourceObject);
	}
	
	@Override
	public void notifyLinkChanged(Object sourceObject) {
		if(selectedIndexLink < 0) return;
		notifyUpdate(observerLink, sourceObject);
	}
	
	@Override
	public void notifyLinkStyleChanged(Object sourceObject) {
		notifyLinkChanged(sourceObject);
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

	@Override
	public void saveModel(DataOutputStream outputStream) throws IOException {
		// Model structure version, reversed for further use.
		int modelStructureVersion = 0;
		outputStream.writeInt(modelStructureVersion);
		
		// Initialize list of entity entries.
		List<EntityEntry> entityEntries = 
				Arrays.asList(template.entities());
		
		// Persistence for the entities.
		outputStream.writeInt(entities.size());
		for(int i = 0; i < entities.size(); ++ i) {
			SketchEntityComponent component = entities.get(i);
			
			// Write the boundary parameters.
			outputStream.writeInt(component.x);
			outputStream.writeInt(component.y);
			outputStream.writeInt(component.w);
			outputStream.writeInt(component.h);
			
			// Write the component path.
			int entityIndex = entityEntries.indexOf(component.entry);
			if(entityIndex < 0) throw new IOException();
			outputStream.writeShort(entityIndex);
			component.entity.save(outputStream);
		}
		entityEntries = null;
		
		// Initialize list of link entries.
		List<LinkEntry> linkEntries = Arrays.asList(template.links());
		
		// Persistence for the paths.
		outputStream.writeInt(links.size());
		for(int i = 0; i < links.size(); ++ i) {
			SketchLinkComponent<Path> component = links.get(i);
			
			// Write the both ends of the components.
			int sourceIndex = entities.indexOf(component.source);
			if(sourceIndex < 0) throw new IOException();
			outputStream.writeInt(sourceIndex);
			
			int destinationIndex = entities.indexOf(component.destination);
			if(destinationIndex < 0) throw new IOException();
			outputStream.writeInt(destinationIndex);
			
			// Write the path style of the component.
			pathManager.save(outputStream, component.pathObject);
			
			// Persistent the link entity.
			int linkIndex = linkEntries.indexOf(component.entry);
			if(linkIndex < 0) throw new IOException();
			outputStream.writeShort(linkIndex);
			component.link.save(outputStream);
		}
		linkEntries = null;
	}

	@Override
	public void loadModel(DataInputStream inputStream) throws IOException {
		// Model structure version, reversed for further use.
		@SuppressWarnings("unused")
		int modelStructureVersion = inputStream.readInt();
		
		// Persistence of the entities.
		EntityEntry[] entityEntries = template.entities();
		int numEntities = inputStream.readInt();
		entities.clear();
		for(int i = 0; i < numEntities; ++ i) {
			// Read the boundary parameters.
			int x = inputStream.readInt();
			int y = inputStream.readInt();
			int w = inputStream.readInt();
			int h = inputStream.readInt();
			
			// The index of the entity entry.
			int entityIndex = inputStream.readShort();
			if(entityIndex < 0 || entityIndex >= entityEntries.length)
				throw new IOException();
			EntityEntry entry = entityEntries[entityIndex];
			Entity entity = entry.factory.get();
			entity.load(inputStream);
			
			// The instantiation of the entity component.
			SketchEntityComponent component = 
					new SketchEntityComponent(entry, entity);
			component.x = x; component.y = y;
			component.w = w; component.h = h;
			entities.add(component);
		}
		
		// Persistence of the links.
		LinkEntry[] linkEntries = template.links();
		int numLinks = inputStream.readInt();
		links.clear();
		for(int i = 0; i < numLinks; ++ i) {
			// Read both ends of the components.
			int sourceIndex = inputStream.readInt();
			if(sourceIndex < 0 || sourceIndex >= entities.size()) 
				throw new IOException();
			SketchEntityComponent source = entities.get(sourceIndex);
			
			int destinationIndex = inputStream.readInt();
			if(destinationIndex < 0 || destinationIndex >= entities.size())
				throw new IOException();
			SketchEntityComponent destination = entities.get(destinationIndex);
			
			// Read the path style of the component.
			Path pathObject = pathManager.read(inputStream);
			
			// Persist the link entity.
			SketchLinkComponent<Path> component = new SketchLinkComponent<Path>(
					source, destination, pathObject);
			int linkIndex = inputStream.readShort();
			if(linkIndex < 0 || linkIndex >= linkEntries.length) 
				throw new IOException();
			component.entry = linkEntries[linkIndex];
			component.link = component.entry.factory.get();
			component.link.load(inputStream);
			links.add(component);
		}
	}
}
