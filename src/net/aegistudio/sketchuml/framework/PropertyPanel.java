package net.aegistudio.sketchuml.framework;

import java.awt.BorderLayout;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.Entity;

public class PropertyPanel<T extends Entity> extends JPanel {
	public interface PropertyGetter<T, U> {
		public U get(T entity) throws Exception;
	}
	
	public interface PropertySetter<T, U> {
		public void set(T entity, U value) throws Exception;
	}
	
	private static final long serialVersionUID = 1L;
	private Consumer<Entity> notifier;
	private T entity;
	
	private final List<Runnable> getterRunnables = new ArrayList<>();
	
	public PropertyPanel() {
		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	
	private Font getPropertyFont() {
		return Configuration.getInstance().PROPERTY_FONT;
	}
	
	private <V> void safeRun(PropertySetter<T, V> setter, V value) {
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
	
	private <V> void addGetterReactor(
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
			safeRun(setter, fieldField.getText()));
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
			safeRun(setter, area.getText()));
		addGetterReactor(area::setText, getter);
		
		super.add(areaPanel);
	}
	
	public void setNotifier(Consumer<Entity> notifier) {
		this.notifier = notifier;
	}
	
	public void updateEntity(T entity) {
		this.entity = entity;
		getterRunnables.forEach(Runnable::run);
	}
}
