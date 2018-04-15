package net.aegistudio.sketchuml.framework;

import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.abstraction.Entity;
import net.aegistudio.sketchuml.abstraction.EntityEntry;
import net.aegistudio.sketchuml.abstraction.LinkEntry;
import net.aegistudio.sketchuml.abstraction.Template;
import net.aegistudio.sketchuml.path.PathManager;
import net.aegistudio.sketchuml.path.PathView;

public class DefaultSketchModel<Path> implements 
		SketchModel<Path>, SketchSelectionModel<Path> {
	
	private final Template template;
	private final List<SketchEntityComponent> entities;
	private final List<SketchLinkComponent<Path>> links;
	
	private final PathView<Path> pathView;
	private final PathManager<Path> pathManager;

	private int selectedIndexEntity = -1;
	private int selectedIndexLink = -1;
	
	private final List<SketchSelectionModel.Observer<Path>> 
		selectionModelObserver = new ArrayList<>();
	private final List<SketchModel.Observer<Path>>
		sketchModelObserver = new ArrayList<>();
	
	public DefaultSketchModel(Template template, 
			PathView<Path> pathView, PathManager<Path> pathManager) {
		this.template = template;
		this.pathManager = pathManager;
		this.pathView = pathView;
		this.entities = new ArrayList<>();
		this.links = new ArrayList<>();
	}
	
	@Override
	public void subscribe(SketchModel.Observer<Path> path) {
		sketchModelObserver.add(path);
	}
	
	@Override
	public void subscribe(SketchSelectionModel.Observer<Path> subscriber) {
		selectionModelObserver.add(subscriber);
	}
	
	/**
	 * Judge whether the current component is the selected or 
	 * original one of the selected.
	 */
	private boolean isEntitySelected(SketchEntityComponent component) {
		if(selectedIndexEntity < 0) return false;
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
		return entities.indexOf(entity);
	}

	@Override
	public void create(SketchEntityComponent component) {
		// Discard for the duplicated element.
		if(isEntitySelected(component)) return;
		if(entities.contains(component)) return;
		
		// Create a new component here.
		entities.add(0, component);
		if(selectedIndexEntity >= 0) selectedIndexEntity ++;
		sketchModelObserver.forEach(a -> a.entityCreated(component));
	}
	
	// Remove and notify links that are related to specified entity.
	private void removeRelatedLink(SketchEntityComponent entity) {
		Iterator<SketchLinkComponent<Path>> iterator = links.iterator();
		while(iterator.hasNext()) {
			SketchLinkComponent<Path> link = iterator.next();
			if(link.relatedTo(entity)) {
				iterator.remove();
				sketchModelObserver.forEach(
						o -> o.linkDestroyed(link));
			}
		}
	}
	
	@Override
	public void destroy(SketchEntityComponent component) {
		// Judge whether the component to remove is selected.
		if(isEntitySelected(component)) {
			// The component to remove is selected.
			// However, as you cannot select link and component simultaneously,
			// there's no need to check whether component is selected.
			SketchEntityComponent original = entities.remove(selectedIndexEntity);
			if(original != null) removeRelatedLink(original);
			selectedIndexEntity = -1;
			selectionModelObserver.forEach(
					SketchSelectionModel.Observer::unselect);
			sketchModelObserver.forEach(o -> 
					o.entityDestroyed(original));
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
				selectionModelObserver.forEach(
						SketchSelectionModel.Observer::unselect);
			}
			removeRelatedLink(component);
			
			// Update selection index number and notify entity destroyed.
			if(index < selectedIndexEntity) 
				selectedIndexEntity --;
			sketchModelObserver.forEach(o ->
					o.entityDestroyed(component));
		}
	}

	@Override
	public void moveToBack(SketchEntityComponent c) {
		// Judge whether the moving component is selected.
		if(isEntitySelected(c)) {
			// If it is selected, just move it to the end.
			entities.add(entities
					.remove(selectedIndexEntity));
			selectedIndexEntity = entities.size() - 1;
			sketchModelObserver.forEach(a -> a.entityReordered(c));
		}
		else {
			// Else judge by the index to update the selected 
			// component's index.
			int index = entities.indexOf(c);
			if(index < 0) return;
			
			// Judge by index.
			entities.add(entities.remove(index));
			if(index < selectedIndexEntity) selectedIndexEntity --;
			sketchModelObserver.forEach(a -> a.entityReordered(c));
		}
	}

	@Override
	public void moveToFront(SketchEntityComponent c) {
		// Judge whether the moving component is selected.
		if(isEntitySelected(c)) {
			// If it is selected, just move it to the front.
			entities.add(0, entities
					.remove(selectedIndexEntity));
			selectedIndexEntity = 0;
			sketchModelObserver.forEach(a -> a.entityReordered(c));
		}
		else {
			int index = entities.indexOf(c);
			if(index < 0) return;
			
			// Judge by index.
			entities.add(0, entities.remove(index));
			if(index < selectedIndexEntity) selectedIndexEntity ++;
			sketchModelObserver.forEach(a -> a.entityReordered(c));
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
		if(i < 0 || i >= entities.size()) return null;
		return entities.get(i);
	}
	
	@Override
	public void notifyEntityUpdated(SketchEntityComponent entity) {
		if(entityIndexOf(entity) < 0) return;
		sketchModelObserver.forEach(a -> a.entityUpdated(entity));
	}
	
	@Override
	public void notifyEntityMoved(SketchEntityComponent entity) {
		if(entityIndexOf(entity) < 0) return;
		sketchModelObserver.forEach(a -> a.entityMoved(entity));
	}
	
	@Override
	public void notifyLinkUpdated(SketchLinkComponent<Path> link) {
		if(links.indexOf(link) < 0) return;
		sketchModelObserver.forEach(a -> a.linkUpdated(link));
	}
	
	@Override
	public void notifyLinkStyleChanged(SketchLinkComponent<Path> link) {
		if(links.indexOf(link) < 0) return;
		sketchModelObserver.forEach(a -> a.linkUpdated(link));
	}
	
	// Link related operations.

	@Override
	public int numLinks() {
		return links.size();
	}

	@Override
	public void link(SketchLinkComponent<Path> link) {
		if(entityIndexOf(link.source) < 0) return;
		if(entityIndexOf(link.destination) < 0) return;
		links.add(link);
		sketchModelObserver.forEach(a -> a.linkCreated(link));
	}

	@Override
	public void unlink(SketchLinkComponent<Path> link) {
		int linkIndex = links.indexOf(link);
		if(linkIndex < 0) return;
		if(links.remove(linkIndex) != null) {
			if(linkIndex == selectedIndexLink) {
				selectedIndexLink = -1;
				selectionModelObserver.forEach(
					SketchSelectionModel.Observer::unselect);
			}
			else if(linkIndex < selectedIndexLink)
				selectedIndexLink --;
			sketchModelObserver.forEach(a -> a.linkDestroyed(link));
		}
	}

	@Override
	public SketchLinkComponent<Path> getLink(int i) {
		return links.get(i);
	}

	@Override
	public SketchLinkComponent<Path> linkAt(int x, int y) {
		PointR position = new PointR(x, y);
		double threshold = Configuration.getInstance().MAX_LINKTHRESHOLD;
		
		// Visit all elements in the links.
		for(SketchLinkComponent<Path> link : links) {
			Rectangle2D boundBegin = link.source.getBoundRectangle();
			Rectangle2D boundEnd = link.destination.getBoundRectangle();
			
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

	// The domain related to the changes in selection.
	@Override
	public SketchEntityComponent selectedEntity() {
		if(this.selectedIndexEntity < 0) return null;
		return this.entities.get(this.selectedIndexEntity);
	}

	@Override
	public SketchLinkComponent<Path> selectedLink() {
		if(this.selectedIndexLink < 0) return null;
		return this.links.get(this.selectedIndexLink);
	}

	@Override
	public void requestSelectEntity(SketchEntityComponent entity) {
		int entityIndex = -1;
		if(entity != null) entityIndex = entities.indexOf(entity);
		
		// Update the entity index if two entities mismatches.
		if(entityIndex !=  selectedIndexEntity) {
			selectedIndexLink = -1;
			selectedIndexEntity = entityIndex;
			if(selectedIndexEntity < 0)
				selectionModelObserver.forEach(
						SketchSelectionModel.Observer::unselect);
			else selectionModelObserver.forEach(
						a -> a.selectEntity(entity));
		}
	}

	@Override
	public void requestSelectLink(SketchLinkComponent<Path> link) {
		int linkIndex = -1;
		if(link != null) linkIndex = links.indexOf(link);
		
		// Update the link if two link index mismatches.
		if(linkIndex != selectedIndexLink) {
			selectedIndexLink = linkIndex;
			selectedIndexEntity = -1;
			if(selectedIndexLink < 0) 
				selectionModelObserver.forEach(
					SketchSelectionModel.Observer::unselect);
			else selectionModelObserver
					.forEach(a -> a.selectLink(link));
		}
	}

	@Override
	public void requestUnselect() {
		// There's nothing in selection, no notification is
		// required.
		if(selectedIndexEntity == -1 && selectedIndexLink == -1)
			return;
		
		// Perform notification of entity changes.
		selectedIndexEntity = selectedIndexLink = -1;
		selectionModelObserver.forEach(
				SketchSelectionModel.Observer::unselect);
	}
}
