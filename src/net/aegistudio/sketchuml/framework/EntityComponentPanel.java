package net.aegistudio.sketchuml.framework;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.EntityEntry;
import net.aegistudio.sketchuml.History;
import net.aegistudio.sketchuml.PropertyView;

public class EntityComponentPanel<Path> extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private final SketchModel<Path> model;
	private final SketchSelectionModel<Path> selectionModel;
	
	private Component property;
	private final Map<EntityEntry, PropertyView> propertyViews = new HashMap<>();
	private PropertyView propertyView;
	
	// The editing operations.
	private final JButton delete, moveFront, sendBack;
	
	private JButton createEditingButton(String tag, JPanel operationPanel,
			Consumer<SketchEntityComponent> action) {
		JButton result = new JButton();
		result.setFont(Configuration.getInstance().EDITING_FONT);
		result.setText(tag);
		result.addActionListener(a -> {
			SketchEntityComponent selected = 
					selectionModel.selectedEntity();
			if(selected != null) action.accept(selected);
		});
		operationPanel.add(result);
		return result;
	}
	
	// The location properties.
	private final JTextField x, y, w, h;
	private boolean dispatchLocation = false;
	private JTextField createLocationField(String tag, JPanel locationPanel,
			BiConsumer<SketchEntityComponent, Integer> processing) {
		Font propertyFont = Configuration.getInstance().PROPERTY_FONT;
		
		JTextField field = new JTextField();
		field.setFont(propertyFont);
		field.setHorizontalAlignment(JTextField.RIGHT);
		Runnable actionRunnable = new Runnable() {
			@Override
			public void run() {
				if(dispatchLocation) return;
				try {
					dispatchLocation = true;
					
					// Parse and apply input.
					String currentText = field.getText();
					if(currentText.length() == 0) return;
					
					int newValue = Integer.parseInt(currentText);
					SketchEntityComponent selected = selectionModel.selectedEntity();
					if(selected == null) return;
					processing.accept(selected, newValue);
					
					// Notify change but retain the input location.
					model.notifyEntityMoved(selected);
				}
				catch(Exception e) {	}
				finally { dispatchLocation = false; }
			}
		};
		field.addActionListener((a) -> actionRunnable.run());
		field.addCaretListener((a) -> actionRunnable.run());
		field.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent fe) {
				actionRunnable.run();
			}
		});
		
		JLabel label = new JLabel(tag);
		label.setFont(propertyFont);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.WEST);
		panel.add(field, BorderLayout.CENTER);
		
		locationPanel.add(panel);
		return field;
	}
	
	public EntityComponentPanel(History history, SketchModel<Path> model, 
			SketchSelectionModel<Path> selectionModel) {
		
		this.model = model;
		this.selectionModel = selectionModel;
		super.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// Add the editing operation's panel.
		JPanel operationPanel = new JPanel();
		super.add(operationPanel);
		
		// The delete button.
		delete = this.createEditingButton(Configuration.getInstance()
				.EDITING_DELETE, operationPanel, (selectedEntity) -> {
					history.perform(EntityComponentPanel.this, 
							new CommandDeleteEntity<>(model, selectionModel), false);
					model.destroy(selectedEntity);
				});
		moveFront = this.createEditingButton(Configuration.getInstance()
				.EDITING_MOVEFRONT, operationPanel, model::moveToFront);
		sendBack = this.createEditingButton(Configuration.getInstance()
				.EDITING_SENDBACK, operationPanel, model::moveToBack);
		
		// Add the location property panel.
		JPanel locationPanel = new JPanel();
		locationPanel.setLayout(new GridLayout(2, 2));
		super.add(locationPanel);
		
		// The editing panel.
		this.x = this.createLocationField("X:", 
				locationPanel, (c, v) -> c.x = v);
		this.y = this.createLocationField("Y:", 
				locationPanel, (c, v) -> c.y = v);
		this.w = this.createLocationField("W:", 
				locationPanel, (c, v) -> c.w = Math.max(v,
						Configuration.getInstance().MIN_ENTITYWIDTH));
		this.h = this.createLocationField("H:", 
				locationPanel, (c, v) -> c.h = Math.max(v,
						Configuration.getInstance().MIN_ENTITYHEIGHT));
		
		model.subscribe(new SketchModel.ObserverAdapter<Path>() {
			@Override
			public void entityMoved(SketchEntityComponent component) {
				updateComponent(component);
			}
			
			@Override
			public void entityUpdated(SketchEntityComponent component) {
				if(component != null && propertyView != null) 
					propertyView.update(component.entity);
			}
		});
		
		selectionModel.subscribe(new SketchSelectionModel.Observer<Path>() {

			@Override
			public void selectEntity(SketchEntityComponent entity) {
				updateComponent(entity);
			}

			@Override
			public void selectLink(SketchLinkComponent<Path> link) {
				updateComponent(null);
			}

			@Override
			public void unselect() {
				updateComponent(null);
			}
		});
		updateComponent(null);
	}
	
	public void updateComponent(SketchEntityComponent component) {
		// Reset to disabled status.
		delete.setEnabled(false);
		moveFront.setEnabled(false);
		sendBack.setEnabled(false);
		
		if(!dispatchLocation) {
			this.x.setText(""); this.x.setEnabled(false);
			this.y.setText(""); this.y.setEnabled(false);
			this.w.setText(""); this.w.setEnabled(false);
			this.h.setText(""); this.h.setEnabled(false);
		}
		
		if(property != null) super.remove(property);
		property = null;
		
		// Continue on only if component is specified.
		if(component == null) { repaint(); return; }
		
		// Re-enable control buttons.
		delete.setEnabled(true);
		moveFront.setEnabled(true);
		sendBack.setEnabled(true);
		
		// Fill in fundamental parameters for component.
		if(!dispatchLocation) {
			this.x.setEnabled(true);
			this.y.setEnabled(true);
			this.w.setEnabled(true);
			this.h.setEnabled(true);
			
			this.x.setText(Integer.toString(component.x));
			this.y.setText(Integer.toString(component.y));
			this.w.setText(Integer.toString(component.w));
			this.h.setText(Integer.toString(component.h));
		}
		
		// Set the property editing panel.
		if(!propertyViews.containsKey(component.entry)) {
			propertyViews.put(component.entry, propertyView =
				component.entry.propertyFactory.newPropertyView(
					e -> model.notifyEntityUpdated(component)));
		}
		else propertyView = propertyViews.get(component.entry);
		
		// Update the property view.
		if(propertyView != null) {
			property = propertyView.getViewObject();
			if(property != null) {
				super.add(property);
				propertyView.select(component.entity);
			}
		}
		
		updateUI();
	}
}
