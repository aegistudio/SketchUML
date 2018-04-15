package net.aegistudio.sketchuml.framework;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.aegistudio.sketchuml.abstraction.Template;
public interface SketchModel<Path> {
	/**
	 * The observer that listen to the changes in the sketch 
	 * data model, including entity created, entity removed,
	 * entity data updated, entity relocated, link created,
	 * link removed, link data updated, link style updated.
	 */
	public interface Observer<Path> {
		public void entityCreated(SketchEntityComponent entity);
		
		public void entityDestroyed(SketchEntityComponent entity);
		
		public void entityUpdated(SketchEntityComponent entity);
		
		public void entityMoved(SketchEntityComponent entity);
		
		public void entityReordered(SketchEntityComponent entity);
		
		public void linkCreated(SketchLinkComponent<Path> link);
		
		public void linkDestroyed(SketchLinkComponent<Path> link);
		
		public void linkUpdated(SketchLinkComponent<Path> link);
		
		public void linkStyleChanged(SketchLinkComponent<Path> link);
	}
	
	public static class ObserverAdapter<Path> implements Observer<Path> {

		@Override
		public void entityCreated(SketchEntityComponent entity) {}

		@Override
		public void entityDestroyed(SketchEntityComponent entity) {}

		@Override
		public void entityUpdated(SketchEntityComponent entity) {}

		@Override
		public void entityMoved(SketchEntityComponent entity) {}

		@Override
		public void entityReordered(SketchEntityComponent entity) {}

		@Override
		public void linkCreated(SketchLinkComponent<Path> link) {}

		@Override
		public void linkDestroyed(SketchLinkComponent<Path> link) {}

		@Override
		public void linkUpdated(SketchLinkComponent<Path> link) {}

		@Override
		public void linkStyleChanged(SketchLinkComponent<Path> link) {}
	};
	
	public void subscribe(Observer<Path> path);
	
	/**
	 * @return the entity at specified logic coordinate.
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 */
	public SketchEntityComponent entityAt(int x, int y);
	
	/**
	 * @param key the notifier's key.
	 * @param component the component to create.
	 */
	public void create(SketchEntityComponent component);
	
	/**
	 * @param key the notifier's key.
	 * @param component the component to remove.
	 */
	public void destroy(SketchEntityComponent component);
	
	/**
	 * @param key the notifier's key.
	 * @param c the component to move to the back.
	 */
	public void moveToBack(SketchEntityComponent c);
	
	/**
	 * @param key the notifier's key.
	 * @param c the component to send to the front.
	 */
	public void moveToFront(SketchEntityComponent c);
	
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
	 * @param entity the entity to query.
	 * @return the index of the entity.
	 */
	public int entityIndexOf(SketchEntityComponent entity);
	
	/**
	 * @return the number of links.
	 */
	public int numLinks();
	
	/**
	 * @return the entity at specified logic coordinate.
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 */
	public SketchLinkComponent<Path> linkAt(int x, int y);
	
	/**
	 * Create link between two objects.
	 * @param key the notifier's key.
	 * @param link the link to create.
	 */
	public void link(SketchLinkComponent<Path> link);
	
	/**
	 * Remove link between two objects.
	 * @param key the notifier's key.
	 * @param link the link to create.
	 */
	public void unlink(SketchLinkComponent<Path> link);
	
	/**
	 * @return the k-th link.
	 */
	public SketchLinkComponent<Path> getLink(int i);
	
	/**
	 * Persist the state of the sketch model.
	 * 
	 * @param outputStream the output stream.
	 * @throws IOException when the lower level has IO exception, or 
	 * higher level has unexpected problem.
	 */
	public void saveModel(DataOutputStream outputStream) throws IOException;
	
	/**
	 * Persist the state of the sketch model.
	 * 
	 * @param inputStream the input stream.
	 * @throws IOException when the lower level has IO exception, or 
	 * higher level has unexpected problem.
	 */
	public void loadModel(DataInputStream inputStream) throws IOException;
	
	/**
	 * @see Observer#entityUpdated(SketchEntityComponent)
	 * @param entity the updated entity.
	 */
	public void notifyEntityUpdated(SketchEntityComponent entity);
	
	/**
	 * @see Observer#entityMoved(SketchEntityComponent)
	 * @param entity the moved entity.
	 */
	public void notifyEntityMoved(SketchEntityComponent entity);
	
	/**
	 * @see Observer#linkUpdated(SketchLinkComponent)
	 * @param link the updated link.
	 */
	public void notifyLinkUpdated(SketchLinkComponent<Path> link);
	
	/**
	 * @param link the style-updated link.
	 */
	public void notifyLinkStyleChanged(SketchLinkComponent<Path> link);
}
