package net.aegistudio.sketchuml.framework;

import net.aegistudio.sketchuml.Command;

public class CommandDeleteLink<Path> implements Command {
	private final SketchModel<Path> model;
	private final SketchLinkComponent<Path> selectedLink;
	
	public CommandDeleteLink(SketchModel<Path> model, 
			SketchLinkComponent<Path> link) {
		this.model = model;
		this.selectedLink = link;
		if(this.selectedLink == null) throw new AssertionError(
				"The original link should never be null.");
	}
	
	public CommandDeleteLink(SketchModel<Path> model) {
		this(model, model.getSelectedLink());
	}
	
	
	@Override
	public void execute() {
		if(model.getSelectedLink() == selectedLink)
			model.selectLink(null, null);
		model.unlink(null, selectedLink);
	}

	@Override
	public void undo() {
		model.link(null, selectedLink);
		model.selectLink(null, selectedLink);
	}

	@Override
	public String name() {
		return "Unlink Objects";
	}
}
