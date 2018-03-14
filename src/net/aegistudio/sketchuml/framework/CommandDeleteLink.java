package net.aegistudio.sketchuml.framework;

import net.aegistudio.sketchuml.Command;

public class CommandDeleteLink<Path> implements Command {
	private final SketchModel<Path> model;
	private final SketchSelectionModel<Path> selectionModel;
	private final SketchLinkComponent<Path> selectedLink;
	
	public CommandDeleteLink(SketchModel<Path> model,
			SketchSelectionModel<Path> selectionModel,
			SketchLinkComponent<Path> link) {
		this.model = model;
		this.selectedLink = link;
		this.selectionModel = selectionModel;
		if(this.selectedLink == null) throw new AssertionError(
				"The original link should never be null.");
	}
	
	public CommandDeleteLink(SketchModel<Path> model,
			SketchSelectionModel<Path> selectionModel) {
		this(model, selectionModel, selectionModel.selectedLink());
	}
	
	
	@Override
	public void execute() {
		if(selectionModel.selectedLink() == selectedLink)
			selectionModel.requestUnselect();
		model.unlink(selectedLink);
	}

	@Override
	public void undo() {
		model.link(selectedLink);
		selectionModel.requestSelectLink(selectedLink);
	}

	@Override
	public String name() {
		return "Unlink Objects";
	}
}
