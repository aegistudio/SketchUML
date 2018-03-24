package net.aegistudio.sketchuml.framework;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;

import net.aegistudio.sketchuml.Configuration;

public class PropertyPanel<T> extends JPanel {
	public interface PropertyGetter<T, U> {
		public U get(T entity) throws Exception;
	}
	
	public interface PropertySetter<T, U> {
		public void set(T entity, U value) throws Exception;
	}
	
	private static final long serialVersionUID = 1L;
	private final Consumer<? super T> notifier;
	private T entity;
	
	private final List<Runnable> getterRunnables = new ArrayList<>();
	
	public PropertyPanel(Consumer<? super T> notifier) {
		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.notifier = notifier;
	}
	
	private Font getPropertyFont() {
		return Configuration.getInstance().PROPERTY_FONT;
	}
	
	protected <V> void safeRun(PropertySetter<T, V> setter, V value) {
		if(entity == null) return;
		if(notifier == null) return;
		try {
			setter.set(entity, value);
			notifier.accept(entity);
		}
		catch(Exception e) {
			// Just discard the exception.
		}
	}
	
	protected boolean silent = false;
	protected <V> void safeRunSilent(PropertySetter<T, V> setter, V value) {
		silent = true;
		safeRun(setter, value);
		silent = false;
	}
	
	protected <V> void addGetterReactor(
			Consumer<V> consumer, PropertyGetter<T, V> getter) {
		
		getterRunnables.add(() -> {
			try {
				if(entity == null) return;
				V value = getter.get(entity);
				consumer.accept(value);
			}
			catch(Exception e) {
				// Just discard the exception.
			}
		});
	}
	
	public void registerTextField(String tag, 
			PropertyGetter<T, String> getter,
			PropertySetter<T, String> setter) {
		
		// Construct panel block.
		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new BorderLayout());
		JLabel fieldLabel = new JLabel(tag);
		fieldLabel.setFont(getPropertyFont());
		JTextField fieldField = new JTextField();
		fieldField.setFont(getPropertyFont());
		fieldPanel.add(fieldLabel, BorderLayout.WEST);
		fieldPanel.add(fieldField, BorderLayout.CENTER);
		
		// Add action reactors.
		fieldField.addActionListener((a) -> 
			safeRun(setter, fieldField.getText()));
		fieldField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent fe) {
				safeRun(setter, fieldField.getText());
			}
		});
		fieldField.addCaretListener(c ->
			safeRunSilent(setter, fieldField.getText()));
		addGetterReactor(fieldField::setText, getter);
		
		super.add(fieldPanel);
	}
	
	public void registerCheckBox(String tag, 
			PropertyGetter<T, Boolean> getter,
			PropertySetter<T, Boolean> setter) {
		
		// Construct checkbox block.
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new GridLayout(1, 1));
		JCheckBox checkBox = new JCheckBox(tag);
		checkBox.setFont(getPropertyFont());
		checkBoxPanel.add(checkBox);
		
		// Add action reactors.
		checkBox.addActionListener(a -> safeRun(
				setter, checkBox.isSelected()));
		addGetterReactor(checkBox::setSelected, getter);
		
		super.add(checkBoxPanel);
	}
	
	public void registerTextArea(String tag, 
			PropertyGetter<T, String> getter,
			PropertySetter<T, String> setter) {
		
		// Construct area block.
		JPanel areaPanel = new JPanel();
		areaPanel.setLayout(new BoxLayout(
				areaPanel, BoxLayout.Y_AXIS));
		
		JLabel areaLabel = new JLabel(tag);
		areaLabel.setFont(getPropertyFont());
		JPanel areaLabelPanel = new JPanel();
		areaLabelPanel.setLayout(new BorderLayout());
		areaLabelPanel.add(areaLabel);
		areaPanel.add(areaLabelPanel);
		
		JTextArea area = new JTextArea();
		area.setRows(4);
		area.setFont(getPropertyFont());
		areaPanel.add(new JScrollPane(area));
		
		// Add action reactors.
		area.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent fe) {
				safeRun(setter, area.getText());
			}
		});
		area.addCaretListener(c ->
			safeRunSilent(setter, area.getText()));
		addGetterReactor(area::setText, getter);
		
		super.add(areaPanel);
	}
	
	public void registerSlider(String tag,
			PropertyGetter<T, Double> getter,
			PropertySetter<T, Double> setter,
			int step, double minValue, double maxValue, 
			String placeHoder, String format) {
		
		// Construct slider block.
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BorderLayout());
		JLabel sliderLabel = new JLabel(tag);
		sliderLabel.setFont(getPropertyFont());
		sliderPanel.add(sliderLabel, BorderLayout.WEST);
		
		// The slider object.
		BoundSlider sliderObject = new BoundSlider(
			step, minValue, maxValue, placeHoder, format) {
			@Override
			public void change(double newValue) {
				safeRun(setter, newValue);
			}
		};
		addGetterReactor(d -> sliderObject
				.setValue(d, true), getter);
		
		// Add then to the parent object.
		sliderPanel.add(sliderObject.slider, 
				BorderLayout.CENTER);
		sliderPanel.add(sliderObject.textField, 
				BorderLayout.EAST);
		sliderObject.textField.setFont(getPropertyFont());
		sliderObject.textField
			.setHorizontalAlignment(JTextField.RIGHT);
		sliderObject.slider.setPreferredSize(new Dimension(50, 10));
		
		super.add(sliderPanel);
	}
	
	public void registerSpinner(String tag, 
			PropertyGetter<T, Integer> getter,
			PropertySetter<T, Integer> setter,
			SpinnerModel model) {
		
		// Construct spinner block.
		JPanel spinnerPanel = new JPanel();
		spinnerPanel.setLayout(new BorderLayout());
		JLabel spinnerLabel = new JLabel(tag);
		spinnerLabel.setFont(getPropertyFont());
		spinnerPanel.add(spinnerLabel, BorderLayout.WEST);
		
		// Construct spinner.
		JSpinner spinner = new JSpinner();
		spinner.setFont(getPropertyFont());
		if(model != null) spinner.setModel(model);
		spinner.setValue(0);
		spinner.addChangeListener(ce -> safeRun(
				setter, (int)spinner.getValue()));
		spinner.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent fe) {
				safeRun(setter, (int)spinner.getValue());
			}
		});
		addGetterReactor(spinner::setValue, getter);
		spinnerPanel.add(spinner, BorderLayout.CENTER);
		
		super.add(spinnerPanel);
	}
	
	public void selectEntity(T entity) {
		this.entity = entity;
		getterRunnables.forEach(Runnable::run);
	}
	
	public void updateEntity(T updatedEntity) {
		if(silent) return;
		if(this.entity != updatedEntity) return;
		getterRunnables.forEach(Runnable::run);
	}
}
