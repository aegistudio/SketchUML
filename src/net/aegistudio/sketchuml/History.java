package net.aegistudio.sketchuml;

/**
 * The history recorder that is used to store various UI events.
 * 
 * Please notice it supports local action taking almost any UI 
 * components will have local history into consideration.
 * 
 * @author Haoran Luo
 */
public interface History {
	/**
	 * Perform the work that should be done globally or locally.
	 * 
	 * If the start local is not called or the id has no related
	 * local stack, the work will be recorded in the global 
	 * stack.
	 * 
	 * Else if the local stack related to the object exists, the
	 * work will be recorded on the related global stack.
	 * 
	 * @param id the id of object that perform this action.
	 * @param command the action that is either global or local.
	 * @param perform will the action be performed.
	 */
	public void perform(Object id, Command command, boolean perform);
	
	/**
	 * Used when the object has gained focus and will perform a 
	 * series of locally reversible actions.
	 * 
	 * @param id the id of object that holds the local history.
	 */
	public void startLocal(Object id);
	
	/**
	 * Conclude the local history with one command that starts
	 * from the initial state to the final state while exit.
	 * 
	 * @param id the id of the local object.
	 * @param conclusion the conclusion action.
	 * @param perform will the conclusion action be executed once.
	 */
	public void finishLocal(Object id, Command conclusion, boolean perform);
	
	/**
	 * Undo the last history event if possible.
	 * If canUndo() returns false, nothing will happen.
	 */
	public void undo();
	
	/**
	 * If the undo history is not empty and it will be
	 * possible to undo the work.
	 * 
	 * @return last command that could be undone.
	 */
	public Command lastUndo();
	
	/**
	 * Redo the last undone history if possible.
	 * If canRedo returns false, nothing will happen.
	 */
	public void redo();
	
	/**
	 * If the redo history is not empty and it will be
	 * possible to redo the work.
	 * 
	 * @return if last command could be redone.
	 */
	public Command lastRedo();
	
	/**
	 * Make the history currently un-reversible.
	 * Could have prevent undesired undo while editing.
	 * 
	 * @param protect whether history is protected.
	 */
	public void setHistoryProtection(boolean protect);
}
