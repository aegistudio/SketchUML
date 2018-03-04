package net.aegistudio.sketchuml.path;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import net.aegistudio.sketchuml.Configuration;

public class TrifoldPathEditor extends JPanel 
	implements PathEditor<TrifoldProxyPath> {
	
	private static final long serialVersionUID = 1L;
	private PathEditor.PathChangeListener<TrifoldProxyPath> notifier = null;
	private TrifoldProxyPath edittingPath;
	
	// The point related objects.
	static class PointEditingPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private boolean editting = false;
		private boolean hasChanged = false;
		private boolean textNotifying = false;
		public static final int SLIDER_STEPS = 1000;
		
		private final JComboBox<String> positionBox;
		private final JSlider positionSlider;
		private final JTextField positionText;
		private double positionValue;
		
		private final Runnable react;
		
		public PointEditingPanel(String label, Runnable react){
			this.react = react;
			setLayout(new BorderLayout());
			
			Font propertyFont = Configuration
					.getInstance().PROPERTY_FONT;
			
			JLabel pointEditLabel = new JLabel(label);
			pointEditLabel.setFont(propertyFont);
			add(pointEditLabel, BorderLayout.WEST);
			
			// The combo box to select the point's position.
			positionBox = new JComboBox<>(new String[] 
					{ "CENTER", "TOP", "RIGHT", "BOTTOM", "LEFT" });
			positionBox.setFont(propertyFont);
			positionBox.addActionListener(a -> {
				if(!editting) {
					hasChanged = true;
					react.run();
				}
			});
			add(positionBox, BorderLayout.CENTER);
			
			// The input box for accepting the point's value.
			positionText = new JTextField();
			positionText.setHorizontalAlignment(JTextField.RIGHT);
			positionText.setFont(propertyFont);
			positionText.addActionListener(a -> textChanged());
			positionText.addCaretListener(ce -> textChanged());
			positionText.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent fe) {
					textChanged();
					hasChanged = true;
					react.run();
				}
			});
			add(positionText, BorderLayout.EAST);
			
			// The slider for inputting the point's value.
			positionSlider = new JSlider();
			positionSlider.setMinimum(- SLIDER_STEPS);
			positionSlider.setMaximum(+ SLIDER_STEPS);
			positionSlider.addChangeListener(a -> {
				if(!editting) {
					positionValue = 1. * positionSlider
							.getValue() / SLIDER_STEPS;
					hasChanged = true;
					react.run();
				}
			});
			add(positionSlider, BorderLayout.SOUTH);
		}
		
		String previousText;
		private void textChanged() {
			// Remove the case while editting.
			if(editting) {
				previousText = positionText.getText(); 
				return;
			}
			
			// Remove the case while removing.
			if(previousText.equals(positionText.getText())) return;
			previousText = positionText.getText();
			
			// Parse the text first.
			double value; try {
				value = Double.parseDouble(previousText);
			}
			catch(NumberFormatException ne) {
				return;
			}
			
			// Perform updating.
			if(value >= 1.0) value = 1.0;
			else if(value <= -1.0) value = -1.0;
			
			// Update the position value this case.
			positionValue = value;
			hasChanged = true;
			textNotifying = true;
			react.run();
			textNotifying = false;
		}
		
		public boolean interruptResponse() {
			if(hasChanged) { 
				hasChanged = false;
				return true;
			}
			else return false;
		}
		
		public void setData(LinePiece.BoxIntersectStatus data) {
			editting = true;
			
			// Set the data of the panel.
			positionBox.setSelectedIndex(data.status);
			this.positionValue = data.ratio;
			positionSlider.setValue((int)(positionValue * SLIDER_STEPS));
			if(!textNotifying) {
				String displayText = String.format("%.4f", data.ratio);
				positionText.setText("+0.0000 ");
				positionText.setPreferredSize(
						positionText.getPreferredSize());
				positionText.setText(displayText);
			}
			
			// Set the status of input box and slider if the center 
			// is selected.
			boolean notCenterSelected = data.status 
					!= LinePiece.BoxIntersectStatus.BOX_INTERLEAVED;
			positionSlider.setEnabled(notCenterSelected);
			positionText.setEnabled(notCenterSelected);
			
			editting = false;
		}
		
		public void fillData(LinePiece.BoxIntersectStatus data) {
			data.status = positionBox.getSelectedIndex();
			data.ratio = positionValue;
		}
	}
	
	PointEditingPanel beginPanel, endPanel;
	
	// The style related objects.
	JComboBox<Class<? extends TrifoldPath>> styleComboBox;
	static class StyleObject<T extends TrifoldPath> {
		Class<T> classObject;
		
		String name;
		
		Supplier<T> newInstance;
	}
	
	public static final Map<Class<? extends TrifoldPath>, 
		StyleObject<? extends TrifoldPath>> PATHSTYLE = new HashMap<>();
	
	static {
		// The straight line style object.
		StyleObject<TrifoldLinePath> styleStraight = new StyleObject<>();
		styleStraight.classObject = TrifoldLinePath.class;
		styleStraight.name = "(/) Straight";
		styleStraight.newInstance = TrifoldLinePath::new;
			
		// The rect-angle line style object.
		StyleObject<TrifoldRectPath> styleRectAngle = new StyleObject<>();
		styleRectAngle.classObject = TrifoldRectPath.class;
		styleRectAngle.name = "(L) Rect-angle";
		styleRectAngle.newInstance = TrifoldRectPath::new;
		
		// The zigzag line style object.
		StyleObject<TrifoldZigzagPath> styleZigzag = new StyleObject<>();
		styleZigzag.classObject = TrifoldZigzagPath.class;
		styleZigzag.name = "(Z) Zigzag";
		styleZigzag.newInstance = TrifoldZigzagPath::new;
		
		// Add these list objects to the map.
		Arrays.asList(styleStraight, styleRectAngle, styleZigzag)
			.forEach(style -> PATHSTYLE.put(style.classObject, style));
	}
	
	public TrifoldPathEditor() {
		super();
		super.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// Add the line style panel.
		JPanel stylePanel = new JPanel();
		stylePanel.setLayout(new BorderLayout());
		
		JLabel styleLabel = new JLabel("Style: ");
		styleLabel.setFont(Configuration.getInstance().PROPERTY_FONT);
		stylePanel.add(styleLabel, BorderLayout.WEST);
		
		styleComboBox = new JComboBox<>();
		styleComboBox.setFont(Configuration.getInstance().PROPERTY_FONT);
		PATHSTYLE.keySet().forEach(styleComboBox::addItem);
		styleComboBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			
			public Component getListCellRendererComponent(JList<?> list, 
					Object value, int index, boolean isSelected, 
					boolean cellHasFocus) {
				
				JLabel label = (JLabel) super.getListCellRendererComponent(list, 
						value, index, isSelected, cellHasFocus);
				if(PATHSTYLE.containsKey(value))
					label.setText(PATHSTYLE.get(value).name);
				return label;
			}
		});
		styleComboBox.addActionListener(a -> this.itemUpdated());
		stylePanel.add(styleComboBox, BorderLayout.CENTER);
		super.add(stylePanel);
		
		// The beginning and ending point panel.
		super.add(beginPanel = new PointEditingPanel(
				"Begin: ", this::itemUpdated));
		super.add(endPanel = new PointEditingPanel(
				"End:   ", this::itemUpdated));
	}
	
	@Override
	public synchronized Component editPath(TrifoldProxyPath path, 
			PathEditor.PathChangeListener<TrifoldProxyPath> notifier) {
		
		// Initialize changing.
		this.edittingPath = path;
		this.notifier = notifier;
		
		// Update the path data.
		refreshPath();
		
		// Finish changing and return the result.
		return this;
	}
	
	private synchronized void refreshPath() {
		this.changing = true;
		
		// Update the fields.
		styleComboBox.setSelectedItem(edittingPath.path.getClass());
		beginPanel.setData(edittingPath.statusBegin);
		endPanel.setData(edittingPath.statusEnd);
		
		this.changing = false;
	}
	
	private boolean changing = false;
	
	private void itemUpdated() {
		if(changing) return;
		if(notifier == null) return;
		if(edittingPath == null) return;
		
		// Backup essential data.
		Object backup = notifier.beforeChange(edittingPath);
		boolean hasChanged = false;
		
		// Respond to the beginning point's edit.
		if(this.beginPanel.interruptResponse()) {
			this.beginPanel.fillData(this.edittingPath.statusBegin);
			hasChanged = true;
		}
		
		// Respond to the ending point's edit.
		if(this.endPanel.interruptResponse()) {
			this.endPanel.fillData(this.edittingPath.statusEnd);
			hasChanged = true;
		}
		
		// Check whether the user has changed the line style.
		if(this.edittingPath.path.getClass() != 
				this.styleComboBox.getSelectedItem()) {
			// Respond to the change on the line style.
			this.edittingPath.path = PATHSTYLE.get(this.styleComboBox
					.getSelectedItem()).newInstance.get();
			hasChanged = true;
		}
		else {
			
		}
		
		if(hasChanged) {
			notifier.receiveChange(backup, edittingPath);
			refreshPath();
		}
	}
}
