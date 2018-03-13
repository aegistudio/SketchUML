package net.aegistudio.sketchuml;

import java.util.Stack;

public class DefaultHistory implements History {
	private final Stack<Command> globalHistory = new Stack<>();
	private final Stack<Command> globalRedo = new Stack<>();
	private final Stack<Command> localHistory = new Stack<>();
	private final Stack<Command> localRedo = new Stack<>();
	private Object localObject = null;
	private boolean isHistoryProtected = false;
	
	private Stack<Command> historyStack() {
		return localObject != null? localHistory : globalHistory;
	}
	
	private Stack<Command> redoStack() {
		return localObject != null? localRedo : globalRedo;
	}
	
	@Override
	public void perform(Object id, Command command, boolean perform) {
		if(command == null) return;
		
		// Retrieve which history to use.
		Stack<Command> history = historyStack();
		Stack<Command> redo = redoStack();
		
		// Perform history action.
		history.push(command);
		redo.clear();
		if(perform) command.execute();
	}

	@Override
	public void startLocal(Object id) {
		if(localObject != null) throw new AssertionError(
				"Already in local status!");
		localObject = id;
	}

	@Override
	public void finishLocal(Object id, Command conclusion, boolean perform) {
		if(localObject == null || !localObject.equals(id)) 
			throw new AssertionError("Not in local status!");
		localObject = null;
		localHistory.clear();
		localRedo.clear();
		perform(id, conclusion, perform);
	}

	@Override
	public void undo() {
		if(isHistoryProtected) return;
		
		// Retrieve which history and redo to use.
		Stack<Command> history = historyStack();
		if(history.isEmpty()) return;
		Stack<Command> redo = redoStack();
		
		// Perform undo action work.
		Command undoWork = history.pop();
		undoWork.undo();
		redo.push(undoWork);
	}

	@Override
	public Command lastUndo() {
		Stack<Command> history = historyStack();
		if(history.isEmpty()) return null;
		return history.peek();
	}

	@Override
	public void redo() {
		if(isHistoryProtected) return;
		
		// Retrieve which history and redo to use.
		Stack<Command> redo = redoStack();
		if(redo.isEmpty()) return;
		Stack<Command> history = historyStack();
		
		// Perform redo action work.
		Command redoWork = redo.pop();
		redoWork.execute();
		history.push(redoWork);
	}

	@Override
	public Command lastRedo() {
		Stack<Command> redo = redoStack();
		if(redo.isEmpty()) return null;
		return redo.peek();
	}
	
	public void reset() {
		globalHistory.clear();
		globalRedo.clear();
	}

	@Override
	public void setHistoryProtection(boolean protect) {
		this.isHistoryProtected = protect;
	}
}
