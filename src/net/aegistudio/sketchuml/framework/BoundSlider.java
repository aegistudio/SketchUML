package net.aegistudio.sketchuml.framework;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JSlider;
import javax.swing.JTextField;

/**
 * The bound slider a JSlider associated with a
 * JTextField, where the internal should be in
 * double form. 
 * 
 * @author Haoran Luo
 */
public class BoundSlider {
	public final JSlider slider;
	public final JTextField textField;
	private double value;
	private boolean enabled;
	
	private final double min, max;
	private final String placeHolder, format;
	private final int step;
	
	private boolean textEditting;
	private boolean changing;
	
	public BoundSlider(int step, double min, double max, 
			String placeHolder, String format) {
		// Fill-in the final fields.
		this.slider = new JSlider();
		this.textField = new JTextField();
		this.placeHolder = placeHolder;
		this.format = format;
		this.min = min; this.max = max;
		this.step = step;
		slider.setMinimum(0);
		slider.setMaximum(step);
		
		// Initialize the slider reactors.
		slider.addChangeListener(ce -> {
			double ratio = 1.0 * slider.getValue() / step;
			double newValue = min + (max - min) * ratio;
			change(newValue);
		});
		
		// Initialize the text field reactors.
		textField.addActionListener(a -> textChanged());
		textField.addCaretListener(ce -> textChanged());
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent fe) {
				textChanged();
				setValue(value, enabled);
			}
		});
	}
	
	private String lastText;
	public void textChanged() {
		// Eliminates the case that the internal data
		// is being updated.
		if(changing) {
			lastText = textField.getText();
			return;
		}
		
		// Eliminate the case that the text field is 
		// not changed.
		if(lastText.equals(textField.getText())) return;
		lastText = textField.getText();
		
		// Parse the new value.
		double newValue;
		try {
			newValue = Double.parseDouble(lastText);
		}
		catch(NumberFormatException ne) {
			return;
		}
		
		// Clamp to the boundary of value.
		if(Double.isNaN(newValue)) newValue = min;
		if(newValue > max) newValue = max;
		if(newValue < min) newValue = min;
	
		// Broadcast text update.
		textEditting = true;
		this.change(newValue);
		textEditting = false;
	}
	
	public void setValue(double value, boolean enabled) {
		// Initialize internal data changing.
		changing = true;
		this.value = value;
		this.enabled = enabled;
		
		// Update the data of the slider and text field.
		int sliderValue = (int)(
				((value - min) / (max - min)) * step);
		this.slider.setValue(sliderValue);
		if(!textEditting) {
			this.textField.setText(placeHolder);
			this.textField.setPreferredSize(this
					.textField.getPreferredSize());
			this.textField.setText(
					String.format(format, value));
		}
		
		// Set the status of the text fields.
		this.textField.setEnabled(enabled);
		this.slider.setEnabled(enabled);
		
		// Finish internal data changing.
		changing = false;
	}
	
	protected void change(double newValue) {}
}
