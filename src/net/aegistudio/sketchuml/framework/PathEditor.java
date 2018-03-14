package net.aegistudio.sketchuml.framework;

import java.awt.Component;

public interface PathEditor<Path> {
	public interface PathChangeListener<Path> {
		/**
		 * @param previousPath the path not changed.
		 * @return the backup or direct instance of the path.
		 */
		public Object beforeChange(SketchLinkComponent<Path> previousPath);
		
		/**
		 * @param beforeChange the path object calling before change.
		 * @param newPath the changed path.
		 */
		public void receiveChange(Object beforeChange, 
				SketchLinkComponent<Path> newPath);
	}
	
	/**
	 * @param path the path object to perform editing.
	 * @param notifier called when the path state is changed.
	 * @return the component that is to edit the path.
	 */
	public Component editPath(SketchLinkComponent<Path> path, 
			PathChangeListener<Path> notifier);
	
	/**
	 * @param path the path object to be updated. Should only
	 * update if the editor is currently editing the object.
	 */
	public void updatePath(SketchLinkComponent<Path> path);
}
