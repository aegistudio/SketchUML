package net.aegistudio.sketchuml.path;

import java.awt.Component;

public interface PathEditor<Path> {
	public interface PathChangeListener<Path> {
		/**
		 * @param previousPath the path not changed.
		 * @return the backup or direct instance of the path.
		 */
		public Object beforeChange(Path previousPath);
		
		/**
		 * @param beforeChange the path object calling before change.
		 * @param newPath the changed path.
		 */
		public void receiveChange(Object beforeChange, Path newPath);
	}
	
	/**
	 * @param path the path object to perform editing.
	 * @param notifier called when the path state is changed.
	 * @return the component that is to edit the path.
	 */
	public Component editPath(Path path, PathChangeListener<Path> notifier);
}
