package net.aegistudio.sketchuml.framework;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.aegistudio.sketchuml.Configuration;

public class ComponentPropertyPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final SketchModel model;
	private Component property;
	
	// The editing operations.
	private final JButton delete, moveFront, sendBack;
	
	private JButton createEditingButton(String tag, JPanel operationPanel,
			Consumer<SketchEntityComponent> action) {
		JButton result = new JButton();
		result.setFont(Configuration.getInstance().EDITING_FONT);
		result.setText(tag);
		result.addActionListener(a -> {
			SketchEntityComponent selected = model.getSelected();
			if(selected != null) action.accept(selected);
		});
		operationPanel.add(result);
		return result;
	}
	
	// The location properties.
	private final JTextField x, y, w, h;
	private JTextField createLocationField(String tag, JPanel locationPanel,
			BiConsumer<SketchEntityComponent, Integer> processing) {
		Font propertyFont = Configuration.getInstance().PROPERTY_FONT;
		
		JTextField field = new JTextField();
		field.setFont(propertyFont);
		field.setHorizontalAlignment(JTextField.RIGHT);
		field.addActionListener((a) -> {
			try {
				// Parse and apply input.
				String currentText = field.getText();
				if(currentText.length() == 0) return;
				
				int newValue = Integer.parseInt(currentText);
				SketchEntityComponent selected = model.getSelected();
				if(selected == null) return;
				processing.accept(selected, newValue);
				
				// Notify change but retain the input location.
				int previous = field.getSelectionStart();
				model.notifySelectedChanged();
				field.requestFocusInWindow();
				field.setSelectionStart(previous);
				field.setSelectionEnd(previous);
			}
			catch(Exception e) {	}
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
	
	public ComponentPropertyPanel(SketchModel model) {
		this.model = model;
		super.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// Add the editing operation's panel.
		JPanel operationPanel = new JPanel();
		super.add(operationPanel);
		
		// The delete button.
		delete = this.createEditingButton(Configuration.getInstance()
				.EDITING_DELETE, operationPanel, model::destroy);
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
				locationPanel, (c, v) -> c.w = v);
		this.h = this.createLocationField("H:", 
				locationPanel, (c, v) -> c.h = v);
		
		model.connect(() -> updateComponent(model.getSelected()));
		updateComponent(null);
	}
	
	public void updateComponent(SketchEntityComponent component) {
		// Reset to disabled status.
		delete.setEnabled(false);
		moveFront.setEnabled(false);
		sendBack.setEnabled(false);
		
		this.x.setText(""); this.x.setEnabled(false);
		this.y.setText(""); this.y.setEnabled(false);
		this.w.setText(""); this.w.setEnabled(false);
		this.h.setText(""); this.h.setEnabled(false);
		
		if(property != null) super.remove(property);
		property = null;
		
		// Continue on only if component is specified.
		if(component == null) { repaint(); return; }
		
		// Re-enable control buttons.
		delete.setEnabled(true);
		moveFront.setEnabled(true);
		sendBack.setEnabled(true);
		
		// Fill in fundamental parameters for component.
		this.x.setEnabled(true);
		this.y.setEnabled(true);
		this.w.setEnabled(true);
		this.h.setEnabled(true);
		
		this.x.setText(Integer.toString(component.x));
		this.y.setText(Integer.toString(component.y));
		this.w.setText(Integer.toString(component.w));
		this.h.setText(Integer.toString(component.h));
		
		// Set the property editing panel.
		property = component.entry.propertyView.getViewObject(
				e -> model.notifySelectedChanged());
		if(property != null) {
			component.entry.propertyView.updateEntity(component.entity);
			super.add(property);
		}
		
		updateUI();
	}
}
