package net.aegistudio.sketchuml.framework;

/**
 * Indicates the object (entity or link) that is selected 
 * in the workspace. Should take the responsibility of 
 * keep track in the selection changes.
 * 
 * The commands related to the changes in the selection 
 * should not be recorded into the history stacks.
 * 
 * @author Haoran Luo
 */
public interface SketchSelectionModel<Path> {
	/**
	 * The observer that keep tracks in the change in selection.
	 * 
	 * There's multiple sources that could cause the the changes
	 * in selection. So don't expect only by calling the requesting
	 * method will result in the very entity or link to be selected
	 * or un-selected.
	 * 
	 * The observer is designed to take care of the changing in 
	 * selection.
	 */
	public interface Observer<Path> {
		/**
		 * @param entity the entity becomes selected.
		 */
		public void selectEntity(SketchEntityComponent entity);
		
		/**
		 * @param link the link becomes selected.
		 */
		public void selectLink(SketchLinkComponent<Path> link);
		
		/**
		 * No entity or link becomes in selection.
		 */
		public void unselect();
	}
	
	/**
	 * Subscribe the selection changes in workspace.
	 * 
	 * @param subscriber the observer to observe the changes.
	 */
	public void subscribe(Observer<Path> subscriber);
	
	/**
	 * @return the entity that is in selection.
	 */
	public SketchEntityComponent selectedEntity();
	
	/**
	 * @return the link that is in selection.
	 */
	public SketchLinkComponent<Path> selectedLink();
	
	/**
	 * Request the model to select an entity. The selection
	 * may be unchanged so an observer should be registered to
	 * receive the changes.
	 * 
	 * @param entity the component to be selected.
	 */
	public void requestSelectEntity(SketchEntityComponent entity);
	
	/**
	 * Request the model to select a link. The selection may
	 * be unchanged so an observer should be registered to 
	 * receive the changes.
	 * 
	 * @param link the link to be selected
	 */
	public void requestSelectLink(SketchLinkComponent<Path> link);
	
	/**
	 * Request the model to un-select everything if there's 
	 * anything in selection.
	 */
	public void requestUnselect();
}
