package net.aegistudio.sketchuml.framework;

import java.awt.Dimension;

import javax.swing.JPanel;

import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.History;

public class ComponentEditPanel<Path> extends JPanel 
	implements SketchSelectionModel.Observer<Path> {
	private static final long serialVersionUID = 1L;
	
	public final EntityComponentPanel<Path> entityEditor;
	public final LinkComponentPanel<Path> linkEditor;
	private Object selectedEditor;
	
	public ComponentEditPanel(SketchModel<Path> model, 
			SketchSelectionModel<Path> selectionModel,
			History history, PathEditor<Path> pathEditor, 
			PathEditor.PathChangeListener<Path> pathNotifier) {
		entityEditor = new EntityComponentPanel<Path>(
				history, model, selectionModel);
		linkEditor = new LinkComponentPanel<Path>(model, 
				selectionModel, history, pathEditor, pathNotifier);
		
		selectionModel.subscribe(this);
		unselect();
	}
	
	private void uiChange() {
		setPreferredSize(new Dimension(Configuration
				.getInstance().EDITPANEL_WIDTH, getHeight()));
		updateUI();
		repaint();
	}

	@Override
	public void selectEntity(SketchEntityComponent entity) {
		if(selectedEditor != entityEditor) {
			removeAll();
			add(entityEditor);
			selectedEditor = entityEditor;
		}
		uiChange();
	}

	@Override
	public void selectLink(SketchLinkComponent<Path> link) {
		if(selectedEditor != linkEditor) {
			removeAll();
			add(linkEditor);
			selectedEditor = linkEditor;
		}
		uiChange();
	}

	@Override
	public void unselect() {
		selectEntity(null);
	}
}
