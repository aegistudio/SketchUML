package net.aegistudio.sketchuml.framework;

import java.awt.Dimension;

import javax.swing.JPanel;

import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.path.PathEditor;

public class ComponentEditPanel<Path> extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public final EntityComponentPanel entityEditor;
	public final LinkComponentPanel<Path> linkEditor;
	private final SketchModel<Path> model;
	private Object selectedEditor;
	
	public ComponentEditPanel(SketchModel<Path> model, 
			PathEditor<Path> pathEditor, 
			PathEditor.PathChangeListener<Path> pathNotifier) {
		this.model = model;
		
		model.registerEntityObserver(this, this::onUpdate);
		model.registerLinkObserver(this, this::onUpdate);
		
		entityEditor = new EntityComponentPanel(model);
		linkEditor = new LinkComponentPanel<Path>(model, 
				pathEditor, pathNotifier);
		
		onUpdate();
	}
	
	private void onUpdate() {
		if(model.getSelectedLink() != null) {
			// Transit to use link editor.
			if(selectedEditor != linkEditor) {
				removeAll();
				add(linkEditor);
				selectedEditor = linkEditor;
			}
		}
		else {
			// Transit to use entity editor.
			if(selectedEditor != entityEditor) {
				removeAll();
				add(entityEditor);
				selectedEditor = entityEditor;
			}
		}
		setPreferredSize(new Dimension(Configuration
				.getInstance().EDITPANEL_WIDTH, getHeight()));
		repaint();
	}
}
