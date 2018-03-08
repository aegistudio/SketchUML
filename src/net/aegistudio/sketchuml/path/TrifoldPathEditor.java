package net.aegistudio.sketchuml.path;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.framework.BoundSlider;
import net.aegistudio.sketchuml.framework.PropertyPanel;

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
		public static final int SLIDER_STEPS = 2000;
		
		private final JComboBox<String> orientationBox;
		private final BoundSlider positionRatio;
		private double positionValue;
		
		public PointEditingPanel(String label, Runnable react){
			setLayout(new BorderLayout());
			
			Font propertyFont = Configuration
					.getInstance().PROPERTY_FONT;
			JLabel pointEditLabel = new JLabel(label);
			pointEditLabel.setFont(propertyFont);
			add(pointEditLabel, BorderLayout.WEST);
			
			// The combo box to select the point's position.
			orientationBox = new JComboBox<>(new String[] 
					{ "CENTER", "TOP", "RIGHT", "BOTTOM", "LEFT" });
			orientationBox.setFont(propertyFont);
			orientationBox.addActionListener(a -> {
				if(!editting) {
					hasChanged = true;
					react.run();
				}
			});
			add(orientationBox, BorderLayout.CENTER);
			
			// Initialize the ration bound.
			this.positionRatio = new BoundSlider(SLIDER_STEPS, 
					-1.0, 1.0, "+0.0000 ", "%.4f") {
				
				public void change(double newValue) {
					positionValue = newValue;
					hasChanged = true;
					react.run();
				}
			};
			
			// The input box for accepting the point's value.
			positionRatio.textField.setHorizontalAlignment(JTextField.RIGHT);
			positionRatio.textField.setFont(propertyFont);
			add(positionRatio.textField, BorderLayout.EAST);
			
			// The slider for inputting the point's value.
			add(positionRatio.slider, BorderLayout.SOUTH);
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
			orientationBox.setSelectedIndex(data.status);
			positionValue = data.ratio;
			positionRatio.setValue(positionValue, data.status 
					!= LinePiece.BoxIntersectStatus.BOX_INTERLEAVED);
			
			editting = false;
		}
		
		public void fillData(LinePiece.BoxIntersectStatus data) {
			data.status = orientationBox.getSelectedIndex();
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
		
		PropertyPanel<T> propertyPanel;
		
		Function<TrifoldPath, T> cast;
		
		public void updateEntity(TrifoldPath path) {
			if(propertyPanel != null) propertyPanel
				.updateEntity(cast.apply(path));
		}
	}
	
	static class PropertyLiftPath<T extends 
		TrifoldAbstractLiftPath> extends PropertyPanel<T>{
		private static final long serialVersionUID = 1L;

		public PropertyLiftPath() {
			registerSpinner("Offset: ", 
					p -> p.lift, (p, q) -> {
						if(q == TrifoldAbstractLiftPath.BLINDED_PIXEL - 1)
							p.lift = - TrifoldAbstractLiftPath.BLINDED_PIXEL;
						else if(q == -TrifoldAbstractLiftPath.BLINDED_PIXEL + 1)
							p.lift = + TrifoldAbstractLiftPath.BLINDED_PIXEL;
						else p.lift = q > 0? 
							Math.max(TrifoldAbstractLiftPath.BLINDED_PIXEL, q):
							Math.min(-TrifoldAbstractLiftPath.BLINDED_PIXEL, q);
					}, null);
			registerCheckBox("Horizontal", 
					p -> p.horizontal, (p, q) -> p.horizontal = q);
		} 
	}
	
	public Map<Class<? extends TrifoldPath>, 
		StyleObject<? extends TrifoldPath>> pathStyle = new HashMap<>();
	{
		// The straight line style object.
		StyleObject<TrifoldLinePath> styleStraight = new StyleObject<>();
		styleStraight.classObject = TrifoldLinePath.class;
		styleStraight.name = "(/) Straight";
		styleStraight.newInstance = TrifoldLinePath::new;
		styleStraight.cast = p -> p instanceof 
				TrifoldLinePath? (TrifoldLinePath)p : null;
			
		// The rect-angle line style object.
		StyleObject<TrifoldRectPath> styleRectAngle = new StyleObject<>();
		styleRectAngle.classObject = TrifoldRectPath.class;
		styleRectAngle.name = "(L) Rect Angle";
		styleRectAngle.newInstance = TrifoldRectPath::new;
		styleRectAngle.cast = p -> p instanceof
				TrifoldRectPath? (TrifoldRectPath)p : null;
		styleRectAngle.propertyPanel = new PropertyPanel<TrifoldRectPath>();
		styleRectAngle.propertyPanel.registerCheckBox("Horizontal", 
				p -> !p.highSkew, (p, q) -> p.highSkew = !q);
		
		// The zigzag line style object.
		StyleObject<TrifoldZigzagPath> styleZigzag = new StyleObject<>();
		styleZigzag.classObject = TrifoldZigzagPath.class;
		styleZigzag.name = "(Z) Zigzag";
		styleZigzag.newInstance = TrifoldZigzagPath::new;
		styleZigzag.cast = p -> p instanceof
				TrifoldZigzagPath? (TrifoldZigzagPath)p : null;
		styleZigzag.propertyPanel = new PropertyPanel<TrifoldZigzagPath>();
		styleZigzag.propertyPanel.registerSlider("Progress (%)", 
				p -> p.ratio * 1e2, (p, q) -> p.ratio = q * 1e-2, 
				1000, 0., 100., "000.0", "%.1f");
		styleZigzag.propertyPanel.registerCheckBox("Horizontal", 
				p -> p.horizontal, (p, q) -> p.horizontal = q);
		
		// The rect-angle line style object.
		StyleObject<TrifoldRoundRectPath> styleRoundRect = new StyleObject<>();
		styleRoundRect.classObject = TrifoldRoundRectPath.class;
		styleRoundRect.name = "(J) Round Rect Angle";
		styleRoundRect.newInstance = TrifoldRoundRectPath::new;
		styleRoundRect.cast = p -> p instanceof
				TrifoldRoundRectPath? (TrifoldRoundRectPath)p : null;
		styleRoundRect.propertyPanel = new PropertyPanel<TrifoldRoundRectPath>();
		styleRoundRect.propertyPanel.registerCheckBox("Horizontal", 
				p -> !p.highSkew, (p, q) -> p.highSkew = !q);
		
		// The lifted line style object.
		StyleObject<TrifoldLiftPath> styleLift = new StyleObject<>();
		styleLift.classObject = TrifoldLiftPath.class;
		styleLift.name = "([) Rect Lift";
		styleLift.newInstance = TrifoldLiftPath::new;
		styleLift.cast = p -> p instanceof
				TrifoldLiftPath? (TrifoldLiftPath)p : null;
		styleLift.propertyPanel = new PropertyLiftPath<>();
		
		// The rect lifted line style object.
		StyleObject<TrifoldRoundLiftPath> styleRoundLift = new StyleObject<>();
		styleRoundLift.classObject = TrifoldRoundLiftPath.class;
		styleRoundLift.name = "(() Round Lift";
		styleRoundLift.newInstance = TrifoldRoundLiftPath::new;
		styleRoundLift.cast = p -> p instanceof
				TrifoldRoundLiftPath? (TrifoldRoundLiftPath)p : null;
		styleRoundLift.propertyPanel = new PropertyLiftPath<>();
		
		// Add these list objects to the map.
		Arrays.asList(styleStraight, styleRectAngle, styleZigzag, 
				styleRoundRect, styleLift, styleRoundLift)
			.forEach(style -> {
				pathStyle.put(style.classObject, style);
				if(style.propertyPanel != null) style
					.propertyPanel.setNotifier(p -> itemUpdated());
			});
	}
	
	private Class<? extends TrifoldPath> propertyClass;
	
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
		pathStyle.keySet().forEach(styleComboBox::addItem);
		styleComboBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			
			public Component getListCellRendererComponent(JList<?> list, 
					Object value, int index, boolean isSelected, 
					boolean cellHasFocus) {
				
				JLabel label = (JLabel) super.getListCellRendererComponent(list, 
						value, index, isSelected, cellHasFocus);
				if(pathStyle.containsKey(value))
					label.setText(pathStyle.get(value).name);
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

		Class<? extends TrifoldPath> newPropertyClass = 
				edittingPath.path.getClass();
		StyleObject<? extends TrifoldPath> previousStyle
				= pathStyle.get(propertyClass);
		StyleObject<? extends TrifoldPath> style
				= pathStyle.get(newPropertyClass);
		
		// Update the fields.
		styleComboBox.setSelectedItem(newPropertyClass);
		beginPanel.setData(edittingPath.statusBegin);
		endPanel.setData(edittingPath.statusEnd);
		
		// Retrieve the new editing panel.
		if(newPropertyClass != propertyClass) {
			propertyClass = newPropertyClass;
			if(previousStyle != null) {
				if(previousStyle.propertyPanel != null)
					super.remove(previousStyle.propertyPanel);
				previousStyle.updateEntity(null);
			}
			if(style.propertyPanel != null) 
				super.add(style.propertyPanel);
		}
		
		// Update the property data.
		if(style.propertyPanel != null) style
			.updateEntity(edittingPath.path);
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
			this.edittingPath.path = pathStyle.get(this.styleComboBox
					.getSelectedItem()).newInstance.get();
			hasChanged = true;
		}
		else {
			// Caused by editing internal fields.
			hasChanged = true;
		}
		
		if(hasChanged) {
			notifier.receiveChange(backup, edittingPath);
			refreshPath();
		}
	}
}
