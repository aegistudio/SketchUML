package net.aegistudio.sketchuml;

/**
 * A reversible closure of action that is capable
 * of being done or undone.
 * 
 * @author Haoran Luo
 */
public interface Command {
	public void execute();
	
	public void undo();
	
	public String name();
}
