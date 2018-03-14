package net.aegistudio.sketchuml.framework;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.History;
import net.aegistudio.sketchuml.LinkEntry;

public class LinkComponentPanel<Path> extends JPanel {
	private static final long serialVersionUID = 1L;
	private final SketchModel<Path> model;
	private final PathEditor<Path> pathEditor;
	private final PathEditor.PathChangeListener<Path> pathNotifier;
	private final JButton delete;
	private final JComboBox<LinkEntry> type;
	private final DefaultComboBoxModel<LinkEntry> typeModel;
	private Component property, pathStyle;
	
	public LinkComponentPanel(SketchModel<Path> model, 
			SketchSelectionModel<Path> selectionModel,
			History history, PathEditor<Path> pathEditor, 
			PathEditor.PathChangeListener<Path> pathNotifier) {
		this.model = model; this.pathEditor = pathEditor;
		this.pathNotifier = pathNotifier;
		super.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// Add the editing operation's panel.
		JPanel operationPanel = new JPanel();
		super.add(operationPanel);
		
		// The delete button.
		delete = new JButton();
		operationPanel.add(delete);
		delete.setFont(Configuration.getInstance().EDITING_FONT);
		delete.setText(Configuration.getInstance().EDITING_DELETE);
		delete.addActionListener(a -> {
			if(selectionModel.selectedLink() == null) return;
			history.perform(LinkComponentPanel.this, 
					new CommandDeleteLink<Path>(model, selectionModel), false);
			model.unlink(selectionModel.selectedLink());
		});
		
		// The type combo box.
		type = new JComboBox<>();
		type.setFont(Configuration.getInstance().EDITING_FONT);
		operationPanel.add(type);
		typeModel = new DefaultComboBoxModel<LinkEntry>();
		type.setModel(typeModel);
		type.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, 
					int index, boolean isSelected, boolean cellHasFocus) {
				
				JLabel label = (JLabel) super.getListCellRendererComponent(
						list, value, index, isSelected, cellHasFocus);
				if(value != null && value instanceof LinkEntry) 
					label.setText(((LinkEntry)value).name);
				return label;
			}
		});
		
		type.addItemListener(a -> {
			if(a.getStateChange() != ItemEvent.SELECTED) return;
			
			// Filter un-changed conditions.
			SketchLinkComponent<Path> linkComponent = selectionModel.selectedLink();
			LinkEntry entry = (LinkEntry) typeModel.getSelectedItem();
			if(linkComponent == null || entry == null) return;
			if(linkComponent.entry == entry) return;
			
			// Perform creation and notification.
			linkComponent.entry = entry;
			linkComponent.link = entry.factory.get();
			model.notifyLinkUpdated(linkComponent);
		});
		
		// The reactors.
		model.subscribe(new SketchModel.ObserverAdapter<Path>() {
			@Override
			public void linkUpdated(SketchLinkComponent<Path> link) {
				pathEditor.updatePath(link);
			}
			
			@Override
			public void linkStyleChanged(SketchLinkComponent<Path> link) {
				pathEditor.updatePath(link);
			}
		});
		
		selectionModel.subscribe(new SketchSelectionModel.Observer<Path>() {
			@Override
			public void selectEntity(SketchEntityComponent entity) {
				updateComponent(null);
			}

			@Override
			public void selectLink(SketchLinkComponent<Path> link) {
				updateComponent(link);
			}

			@Override
			public void unselect() {
				updateComponent(null);
			}
		});
		
		updateComponent(null);
	}
	
	private void updateComponent(SketchLinkComponent<Path> link) {
		// Disable link status.
		delete.setEnabled(false);
		typeModel.removeAllElements();
		if(pathStyle != null) remove(pathStyle);
		if(property != null) remove(property);
		property = null;	pathStyle = null;

		// Re-enable link status if there's link.
		if(link == null) return;
		delete.setEnabled(true);

		// The link type candidates.
		type.setPreferredSize(new Dimension(Configuration.getInstance()
				.LINKPANEL_TYPEWIDTH, delete.getPreferredSize().height));
		Arrays.stream(model.getTemplate().links()).filter(le -> le.filter
				.test(link.source.entity, link.destination.entity))
				.forEach(typeModel::addElement);
		typeModel.setSelectedItem(link.entry);
		
		// The link style panel.
		pathStyle = pathEditor.editPath(link, this.pathNotifier);
		if(pathStyle != null) add(pathStyle);
		
		// The editor panel.
		property = link.entry.propertyView.getViewObject(e -> 
					model.notifyLinkUpdated(link));
		if(property != null) {
			add(property);
			link.entry.propertyView.select(link.link);
		}
		
		repaint();
		updateUI();
	}
}
